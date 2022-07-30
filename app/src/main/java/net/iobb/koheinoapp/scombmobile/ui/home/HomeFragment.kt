package net.iobb.koheinoapp.scombmobile.ui.home

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
import net.iobb.koheinoapp.scombmobile.common.NetworkState
import net.iobb.koheinoapp.scombmobile.databinding.FragmentHomeBinding


class HomeFragment : Fragment(), SimpleDialog.OnDialogResultListener {

    private var _binding: FragmentHomeBinding? = null
    private val appViewModel: AppViewModel by activityViewModels()
    private lateinit var viewModel: HomeViewModel

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        viewModel.appViewModel = appViewModel

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    enum class ListenerState{
        Normal, ColorSelect, AttendanceCount, Initialize
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.timetable_color_selector, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.colorSettings -> {
                openColorSettingDialog()
            }
            R.id.showColorPalette -> {
                openColorSettingDialog()
            }
            R.id.disableColorPickerMode -> {
                viewModel.timetableListenerState.value = ListenerState.Normal
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

    override fun onPause() {
        viewModel.timetableListenerState.value = ListenerState.Normal
        super.onPause()
    }

    override fun onStart() {
        super.onStart()

        viewModel.page.networkState.observe(viewLifecycleOwner){
            when(it){
                NetworkState.Loading -> {
                    progressBar.isVisible = true
                    timeTable.isVisible = false
                }
                NetworkState.NotPermitted -> {
                    this.findNavController().navigate(R.id.loginFragment)
                }
                else -> {
                    progressBar.isVisible = false
                    timeTable.isVisible = true
                }
            }
        }
        viewModel.timeTable.observe(viewLifecycleOwner){
            applyToAllCell { classCell, row, col ->
                val tableRow = timeTable[row + 1] as TableRow
                val cellView = tableRow[col + 1] as LinearLayout
                classCell?.genView(requireContext(), cellView)
            }
            viewModel.timetableListenerState.value = ListenerState.Normal
        }
        viewModel.timetableListenerState.observe(viewLifecycleOwner){ state ->
            if(state != ListenerState.ColorSelect) {
                viewModel.selectedColor = null
            }
            Log.d("listener_state", state.toString())
            when(state){
                ListenerState.Initialize -> {}
                ListenerState.Normal -> {
                    val menu = (activity as MainActivity).binding.appBarMain.toolbar.menu
                    menu.findItem(R.id.disableColorPickerMode)?.isVisible = false
                    menu.findItem(R.id.showColorPalette)?.title = "色設定モード"

                    applyToAllCell { classCell, _, _ ->
                        classCell ?: return@applyToAllCell

                        classCell.view.classNameBtn.setOnLongClickListener { v ->
                            Snackbar.make(v, "教室 : ${classCell.room}", Snackbar.LENGTH_LONG).show()
                            true
                        }

                        classCell.view.classNameBtn.setOnClickListener {
                            val action = HomeFragmentDirections.actionNavHomeToClassDetailFragment(classCell.id)
                            it.findNavController().navigate(action)
                        }
                    }
                }
                ListenerState.ColorSelect -> {
                    val menu = (activity as MainActivity).binding.appBarMain.toolbar.menu
                    menu.findItem(R.id.disableColorPickerMode)?.isVisible = true
                    menu.findItem(R.id.showColorPalette)?.title = "パレットを表示"

                    Toast.makeText(requireContext(), "タップで色を設定\n長押しでデフォルト色に戻す", Toast.LENGTH_SHORT).show()

                    applyToAllCell { classCell, _, _ ->
                        classCell ?: return@applyToAllCell
                        classCell.view.classNameBtn.setOnClickListener {
                            Log.d("selected_color", viewModel.selectedColor?.toString() ?: "null")
                            classCell.setCustomColor(viewModel.selectedColor ?: return@setOnClickListener)
                        }

                        classCell.view.classNameBtn.setOnLongClickListener{
                            classCell.resetCustomColor()
                            true
                        }
                    }
                }
                ListenerState.AttendanceCount -> {
                    viewModel.selectedColor = null
                }
            }
        }



        if(appViewModel.sessionId == null){
            view?.findNavController()?.navigate(R.id.loginFragment)
            return
        }

        viewModel.fetch(requireContext())

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
            viewModel.selectedColor = extras.getIntArray(SimpleColorDialog.COLORS)!![0]
            viewModel.timetableListenerState.value = ListenerState.ColorSelect
        }
        return false
    }
}