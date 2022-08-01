package net.iobb.koheinoapp.scombmobile.ui.timetable

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TableRow
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

        viewModel.page.networkState.observe(viewLifecycleOwner){
            when(it){
                NetworkState.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.timeTable.isVisible = false
                }
                NetworkState.NotPermitted -> {
                    viewModel.page.reset()
                    findNavController().navigate(R.id.nav_loginFragment)
                }
                else -> {
                    binding.progressBar.isVisible = false
                    binding.timeTable.isVisible = true
                }
            }
        }
        viewModel.timeTable.observe(viewLifecycleOwner){
            applyToAllCell { classCell, row, col ->
                val tableRow = binding.timeTable[row + 1] as TableRow
                val cellView = tableRow[col + 1] as LinearLayout
                classCell?.genView(requireContext(), cellView)
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
                            true
                        }
                    }
                }
                ListenerState.ColorSelect -> {
                    applyToAllCell { classCell, _, _ ->
                        classCell ?: return@applyToAllCell
                        classCell.view.classNameBtn.setOnClickListener {
                            if(classCell.customColorInt == null){
                                classCell.setCustomColor(viewModel.selectedColor ?: return@setOnClickListener)
                            }else{
                                if(viewModel.selectedColor == null || viewModel.selectedColor == classCell.customColorInt){
                                    classCell.setCustomColor(null)
                                }else{
                                    classCell.setCustomColor(viewModel.selectedColor)
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



        if(appViewModel.sessionId == null){
            findNavController().navigate(R.id.nav_loginFragment)
            return root
        }

        viewModel.fetch(requireContext())

        return root
    }

    enum class ListenerState{
        Normal, ColorSelect, AttendanceCount, Initialize
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.timetable_color_selector, menu)

        val modeSwitch = menu.findItem(R.id.editModeSwitch)
        val colorPalette = menu.findItem(R.id.colorPalette)

        if(viewModel.timetableListenerState.value == ListenerState.ColorSelect){
            colorPalette.isVisible = true
            modeSwitch.icon.alpha = 1000
            colorPalette.icon.setTint(viewModel.selectedColor!!)
        }else{
            colorPalette.isVisible = false
            modeSwitch.icon.alpha = 100
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.editModeSwitch -> {
                val modeSwitch = (activity as MainActivity).binding.appBarMain.toolbar.menu.findItem(R.id.editModeSwitch)
                val colorPalette = (activity as MainActivity).binding.appBarMain.toolbar.menu.findItem(R.id.colorPalette)

                // edit_mode : on -> off
                if(viewModel.timetableListenerState.value == ListenerState.ColorSelect){
                    viewModel.timetableListenerState.value = ListenerState.Normal
                    modeSwitch.icon.alpha = 100
                    colorPalette.isVisible = false
                    Snackbar.make(timeTable, "色設定モード : OFF", Snackbar.LENGTH_SHORT).show()
                }
                // edit_mode : off -> on
                else{
                    openColorSettingDialog()
                }

            }
            R.id.colorPalette -> {
                openColorSettingDialog()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    // when color dialog closed
    override fun onResult(dialogTag: String, which: Int, extras: Bundle): Boolean {
        if(which == Dialog.BUTTON_POSITIVE){
            viewModel.selectedColor = extras.getIntArray(SimpleColorDialog.COLORS)?.getOrNull(0) ?: return false

            val modeSwitch = (activity as MainActivity).binding.appBarMain.toolbar.menu.findItem(R.id.editModeSwitch)
            val colorPalette = (activity as MainActivity).binding.appBarMain.toolbar.menu.findItem(R.id.colorPalette)

            // set palette color
            colorPalette.icon.setTint(viewModel.selectedColor!!)
            Log.d("selected_color", viewModel.selectedColor?.toString() ?: "null")

            // edit_mode : off -> on
            if(viewModel.timetableListenerState.value != ListenerState.ColorSelect){
                modeSwitch.icon.alpha = 1000
                Snackbar.make(timeTable, "色設定モード : ON\nマスをタップして色を登録", Snackbar.LENGTH_SHORT).show()
                viewModel.timetableListenerState.value = ListenerState.ColorSelect
            }
        }
        return false
    }

    fun openColorSettingDialog(){
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
}