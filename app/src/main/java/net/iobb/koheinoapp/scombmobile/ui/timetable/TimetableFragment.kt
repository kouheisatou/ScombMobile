package net.iobb.koheinoapp.scombmobile.ui.timetable

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.Toast
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import eltos.simpledialogfragment.SimpleDialog
import eltos.simpledialogfragment.color.SimpleColorDialog
import kotlinx.android.synthetic.main.class_cell.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import net.iobb.koheinoapp.scombmobile.*
import net.iobb.koheinoapp.scombmobile.common.AppViewModel
import net.iobb.koheinoapp.scombmobile.common.CLASS_PAGE_URL
import net.iobb.koheinoapp.scombmobile.common.NetworkState
import net.iobb.koheinoapp.scombmobile.databinding.FragmentHomeBinding


class TimetableFragment : Fragment(), SimpleDialog.OnDialogResultListener {

    private var _binding: FragmentHomeBinding? = null
    private val appViewModel: AppViewModel by activityViewModels()
    private lateinit var viewModel: TimetableViewModel

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(this)[TimetableViewModel::class.java]
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
                    findNavController().navigate(R.id.loginFragment)
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
                    val menu = (activity as MainActivity).binding.appBarMain.toolbar.menu
                    menu.findItem(R.id.colorSettings)?.isVisible = true
                    menu.findItem(R.id.disableColorSettingMode)?.isVisible = false
                    menu.findItem(R.id.palette)?.isVisible = false

                    applyToAllCell { classCell, _, _ ->
                        classCell ?: return@applyToAllCell

                        classCell.view.classNameBtn.setOnLongClickListener { v ->
                            Snackbar.make(v, "教室 : ${classCell.room}", Snackbar.LENGTH_LONG).show()
                            true
                        }

                        classCell.view.classNameBtn.setOnClickListener {
                            val action = TimetableFragmentDirections.actionNavHomeToClassDetailFragment("$CLASS_PAGE_URL${classCell.classId}")
                            it.findNavController().navigate(action)
                        }
                    }
                }
                ListenerState.ColorSelect -> {
                    val menu = (activity as MainActivity).binding.appBarMain.toolbar.menu
                    menu.findItem(R.id.colorSettings)?.isVisible = false
                    menu.findItem(R.id.disableColorSettingMode)?.isVisible = true
                    menu.findItem(R.id.palette)?.isVisible = true

                    Toast.makeText(requireContext(), "タップで色を設定", Toast.LENGTH_SHORT).show()

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
                    }
                }
                ListenerState.AttendanceCount -> {
                }
            }
        }



        if(appViewModel.sessionId == null){
            findNavController().navigate(R.id.loginFragment)
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
        val isInColorSetting = viewModel.timetableListenerState.value == ListenerState.ColorSelect

        menu.findItem(R.id.colorSettings)?.isVisible = !isInColorSetting
        menu.findItem(R.id.disableColorSettingMode)?.isVisible = isInColorSetting
        menu.findItem(R.id.palette)?.isVisible = isInColorSetting

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.colorSettings -> {
                viewModel.timetableListenerState.value = ListenerState.ColorSelect
            }
            R.id.disableColorSettingMode -> {
                viewModel.timetableListenerState.value = ListenerState.Normal
            }
            R.id.palette -> {
                openColorSettingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
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

    // when color dialog closed
    override fun onResult(dialogTag: String, which: Int, extras: Bundle): Boolean {
        if(which == Dialog.BUTTON_POSITIVE){
            viewModel.selectedColor = extras.getIntArray(SimpleColorDialog.COLORS)?.getOrNull(0) ?: return false

            val menuItem = (activity as MainActivity).binding.appBarMain.toolbar.menu.findItem(R.id.palette)
            val drawable = menuItem.icon
            drawable.setTint(viewModel.selectedColor!!)
        }
        return false
    }
}