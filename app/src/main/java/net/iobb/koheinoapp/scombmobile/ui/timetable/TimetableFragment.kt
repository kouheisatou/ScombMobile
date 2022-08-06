package net.iobb.koheinoapp.scombmobile.ui.timetable

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.TableRow
import androidx.core.view.children
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import eltos.simpledialogfragment.SimpleDialog
import eltos.simpledialogfragment.color.SimpleColorDialog
import kotlinx.android.synthetic.main.class_cell.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import net.iobb.koheinoapp.scombmobile.*
import net.iobb.koheinoapp.scombmobile.common.AppViewModel
import net.iobb.koheinoapp.scombmobile.common.NetworkState
import net.iobb.koheinoapp.scombmobile.databinding.FragmentHomeBinding
import net.iobb.koheinoapp.scombmobile.ui.timetable.TimetableViewModel.Companion.refreshRequired


class TimetableFragment : Fragment(), SimpleDialog.OnDialogResultListener {

    private var _binding: FragmentHomeBinding? = null
    private val appViewModel: AppViewModel by activityViewModels()
    private val viewModel: TimetableViewModel by activityViewModels()

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        viewModel.appViewModel = appViewModel

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.swipeLayout.setOnRefreshListener {
            refresh()
        }

        viewModel.page.networkState.observe(viewLifecycleOwner){
            Log.d("network_state", viewModel.page.networkState.value.toString())
            when(it){
                NetworkState.Initialized -> {
                    binding.swipeLayout.isRefreshing = false
                    binding.timeTable.isVisible = true
                    viewModel.fetch(requireContext(), refreshRequired)
                    viewModel.checkSessionIdValidity()
                }
                NetworkState.Loading -> {
                    binding.swipeLayout.isRefreshing = true
                    binding.timeTable.isVisible = false
                }
                NetworkState.Finished -> {
                    binding.swipeLayout.isRefreshing = false
                    binding.timeTable.isVisible = true
                }
                NetworkState.NotPermitted -> {
                    if(appViewModel.sessionId == null){
                        findNavController().navigate(R.id.nav_loginFragment)
                    }
                    // when backed from login fragment
                    else{
                        viewModel.page.networkState.value = NetworkState.Initialized
                    }
                }
            }
        }
        viewModel.timeTable.observe(viewLifecycleOwner){
            // remove cell
            for(row in timeTable.children.withIndex()){
                if(row.index == 0) continue
                for(cell in (row.value as TableRow).children.withIndex()){
                    if(cell.index == 0) continue
                    (cell.value as LinearLayout).removeAllViews()
                }
            }

            // construct cell
            applyToAllCell { classCell, row, col ->
                val tableRow = binding.timeTable[row + 1] as TableRow
                val cellView = tableRow[col + 1] as LinearLayout
                classCell?.genView(requireContext(), cellView)

                classCell?.timetable = viewModel.timeTable.value
            }
            viewModel.timetableListenerState.value = ListenerState.Normal
        }
        viewModel.timetableListenerState.observe(viewLifecycleOwner){ state ->
            Log.d("listener_state", state.toString())
            when(state){
                ListenerState.Initialize -> {}
                ListenerState.Normal -> {
                    applyToAllCell { classCell, row, col ->
                        classCell ?: return@applyToAllCell

                        classCell.view.classNameBtn.setOnClickListener { v ->
                            val dialog = ClassDetailDialogFragment.create(row, col)
                            dialog.show(childFragmentManager, "class_detail_dialog")
                        }

                        classCell.view.classNameBtn.setOnLongClickListener { v ->
                            Snackbar.make(v, "教室 : ${classCell.room}", Snackbar.LENGTH_LONG).show()
                            true
                        }
                    }
                }
                ListenerState.ColorSelect -> {
                    applyToAllCell { classCell, _, _ ->
                        classCell ?: return@applyToAllCell
                        classCell.view.classNameBtn.setOnClickListener {
                            if(classCell.customColorInt == null){
                                classCell.setCustomColor(viewModel.selectedColor ?: return@setOnClickListener, true)
                            }else{
                                if(viewModel.selectedColor == null || viewModel.selectedColor == classCell.customColorInt){
                                    classCell.setCustomColor(null, true)
                                }else{
                                    classCell.setCustomColor(viewModel.selectedColor, true)
                                }
                            }
                        }
                        classCell.view.classNameBtn.setOnLongClickListener(null)
                    }
                }
                ListenerState.AttendanceCount -> {
                }
            }
        }

        return root
    }

    enum class ListenerState{
        Normal, ColorSelect, AttendanceCount, Initialize
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.timetable_color_selector, menu)

        val modeSwitch = menu.findItem(R.id.editModeSwitch)

        if(viewModel.timetableListenerState.value == ListenerState.ColorSelect){
            modeSwitch.icon.setTint(viewModel.selectedColor!!)
        }else{
            modeSwitch.icon.setTint(Color.parseColor("#FFFFFF"))
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.editModeSwitch -> {
                val modeSwitch = (activity as MainActivity).binding.appBarMain.toolbar.menu.findItem(R.id.editModeSwitch)

                // edit_mode : on -> off
                if(viewModel.timetableListenerState.value == ListenerState.ColorSelect){
                    viewModel.timetableListenerState.value = ListenerState.Normal
                    modeSwitch.icon.setTint(Color.parseColor("#FFFFFF"))
                    Snackbar.make(timeTable, "色設定モード : OFF", Snackbar.LENGTH_SHORT).show()
                }
                // edit_mode : off -> on
                else{
                    openColorSettingDialog()
                }

            }
        }

        return super.onOptionsItemSelected(item)
    }

    // when color dialog closed
    override fun onResult(dialogTag: String, which: Int, extras: Bundle): Boolean {
        viewModel.selectedColor = extras.getIntArray(SimpleColorDialog.COLORS)?.getOrNull(0) ?: return false
        Log.d("selected_color", viewModel.selectedColor?.toString() ?: "null")

        val modeSwitch = (activity as MainActivity).binding.appBarMain.toolbar.menu.findItem(R.id.editModeSwitch)

        // edit_mode : off -> on
        if(viewModel.timetableListenerState.value != ListenerState.ColorSelect){
            modeSwitch.icon.setTint(Color.parseColor("#000000"))
            Snackbar.make(timeTable, "色設定モード : ON", Snackbar.LENGTH_SHORT).show()
            viewModel.timetableListenerState.value = ListenerState.ColorSelect
        }
        return false
    }

    private fun openColorSettingDialog(){
        SimpleColorDialog.build()
            .title("色を選択")
            .colors(requireContext(), SimpleColorDialog.MATERIAL_COLOR_PALLET_LIGHT)
            .allowCustom(false)
            .show(this, "color_dialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun applyToAllCell(applyProcess: (classCell: ClassCell?, row: Int, col: Int) -> Unit){
        for(row in (viewModel.timeTable.value ?: arrayOf()).withIndex()){
            val rowNum = row.index
            for(cell in row.value.withIndex()){
                val colNum = cell.index
                applyProcess(cell.value, rowNum, colNum)
            }
        }
    }

    fun refresh(){
        refreshRequired = true
        viewModel.page.networkState.value = NetworkState.Initialized
    }
}