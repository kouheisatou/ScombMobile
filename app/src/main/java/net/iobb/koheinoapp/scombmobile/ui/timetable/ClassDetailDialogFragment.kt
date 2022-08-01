package net.iobb.koheinoapp.scombmobile.ui.timetable

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ArrayRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import eltos.simpledialogfragment.SimpleDialog
import eltos.simpledialogfragment.color.SimpleColorDialog
import kotlinx.android.synthetic.main.class_cell.view.*
import kotlinx.android.synthetic.main.fragment_class_detail_dialog.*
import kotlinx.android.synthetic.main.fragment_class_detail_dialog.view.*
import net.iobb.koheinoapp.scombmobile.R
import net.iobb.koheinoapp.scombmobile.common.CLASS_PAGE_URL
import net.iobb.koheinoapp.scombmobile.ui.task.timeToString

class ClassDetailDialogFragment() : DialogFragment(), SimpleDialog.OnDialogResultListener {

    private val viewModel: TimetableViewModel by activityViewModels()
    lateinit var classCell: ClassCell
    private val classTimeMap = mapOf<Int, String>(
        0 to "9:00 ~ 10:40",
        1 to "10:50 ~ 12:30",
        2 to "13:20 ~ 15:00",
        3 to "15:10 ~ 16:50",
        4 to "17:00 ~ 18:40",
        5 to "18:50 ~ 20:30",
        6 to "21:40 ~ 23:10",
    )
    private val dayOfWeekMap = mapOf(
        0 to "月曜",
        1 to "火曜",
        2 to "水曜",
        3 to "木曜",
        4 to "金曜",
        5 to "土曜",
    )

    companion object {
        fun create(row: Int, col: Int): ClassDetailDialogFragment{
            return ClassDetailDialogFragment().apply {
                val bundle = Bundle()
                bundle.putInt("row", row)
                bundle.putInt("col", col)
                arguments = bundle
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        arguments?.let {
            val row = it.getInt("row")
            val col = it.getInt("col")
            classCell = viewModel.timeTable.value!![row][col]!!
        }
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_class_detail_dialog, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        view.title.text = classCell.name
        view.classIdTextView.text = classCell.classId
        view.teachersTextView.text = classCell.teachers
        view.classRoomTextView.text = classCell.room
        view.classTimeTextView.text = "${dayOfWeekMap[classCell.dayOfWeek]}${classCell.period+1}限  ${classTimeMap[classCell.period]}"
        if (classCell.customColorInt != null) {
            view.customColor.background.setTint(classCell.customColorInt!!)
        }
        view.lastUpdateTimeTextView.text = timeToString(classCell.createdDate)

        view.negative_button.setOnClickListener {
            dialog?.cancel()
        }
        view.positive_button.setOnClickListener {
            if (selectedColor != null) {
                classCell.customColorInt = selectedColor
                classCell.setCustomColor(selectedColor)
            }
            dialog?.cancel()
        }
        view.webLink.setOnClickListener {
            dialog?.cancel()
            val action = TimetableFragmentDirections.actionNavHomeToNavSingleWebPageFragment("$CLASS_PAGE_URL${classCell.classId}")
            parentFragment?.findNavController()?.navigate(action)
        }
        view.customColor.setOnClickListener {
            openColorSettingDialog()
        }
        return view
    }

    private fun openColorSettingDialog(){
        @ArrayRes
        val colors = R.array.material_pallet_light
        SimpleColorDialog.build()
            .title("色を選択")
            .colors(requireContext(), colors)
            .allowCustom(false)
            .show(this, "color_dialog")
    }

    private var selectedColor: Int? = null
    override fun onResult(dialogTag: String, which: Int, extras: Bundle): Boolean {
        val color = extras.getIntArray(SimpleColorDialog.COLORS)?.getOrNull(0) ?: return false

        selectedColor = color
        customColor.background.setTint(color)

        return false
    }
}