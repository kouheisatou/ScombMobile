package net.iobb.koheinoapp.scombmobile.ui.task

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import kotlinx.android.synthetic.main.fragment_add_new_task.*
import kotlinx.android.synthetic.main.fragment_add_new_task.view.*
import net.iobb.koheinoapp.scombmobile.R
import net.iobb.koheinoapp.scombmobile.common.*
import net.iobb.koheinoapp.scombmobile.ui.timetable.ClassCell
import java.util.*


class AddNewTaskDialogFragment : DialogFragment() {

    val taskViewModel: TaskViewModel by activityViewModels()
    var selectedDate: Calendar? = null

    companion object {
        fun create(selectedDate: Long): AddNewTaskDialogFragment {
            return AddNewTaskDialogFragment().apply {
                val bundle = Bundle()
                bundle.putLong("selected_date", selectedDate)
                arguments = bundle
            }
        }
        fun create(): AddNewTaskDialogFragment {
            return AddNewTaskDialogFragment()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        arguments?.let {
            selectedDate = Calendar.getInstance().apply { timeInMillis = it.getLong("selected_date") }
        }
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_add_new_task, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        if(selectedDate != null){
            root.selectedTimeDialog.text = timeToString(selectedDate!!.timeInMillis)
        }else{
            root.selectedTimeDialog.text = ""
        }

        root.deadlineCalendarBtn.setOnClickListener {
            showDatePickerDialog()
        }

        val taskTypeAdapter = rightGravityArrayAdapter<String>(requireContext(), root.taskTypeSpinner)
        taskTypeAdapter.addAll(japaneseTaskType())
        root.taskTypeSpinner.adapter = taskTypeAdapter

        val allClasses = taskViewModel.getAllClasses(requireContext()).toMutableList()
        allClasses.sortWith(compareBy<ClassCell> { it.year }.thenBy { it.term }.thenBy { it.dayOfWeek })
        allClasses.reverse()
        val classNameAdapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item) {
            // pos in item
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                (v as TextView).gravity = Gravity.RIGHT
                return v
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getDropDownView(position, convertView, parent)
                (v as TextView).gravity = Gravity.CENTER
                val classInfo = allClasses.getOrNull(position) ?: return v
                if(classInfo.customColorInt != null) {
                    v.setTextColor(classInfo.customColorInt!!)
                }else{
                    v.setTextColor(Color.parseColor("#000000"))
                }
                return v
            }
        }
        classNameAdapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
        val classesNameString = mutableListOf<String>()
        allClasses.forEach {
            if(!classesNameString.contains(it.name)){
                classesNameString.add(it.name)
            }
        }
        classNameAdapter.add("指定なし")
        classNameAdapter.addAll(classesNameString)
        root.classNameSpinner.adapter = classNameAdapter

        root.positive_button.setOnClickListener {
            if(root.taskTitle.text.toString() == ""){
                Toast.makeText(requireContext(), "タスク名を入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(selectedDate == null){
                Toast.makeText(requireContext(), "締切時刻を指定してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if(selectedDate!!.timeInMillis <= Calendar.getInstance().timeInMillis){
                Toast.makeText(requireContext(), "今よりも先の時刻を選択してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedClass = if(root.classNameSpinner.selectedItemPosition == 0){
                null
            }else{
                allClasses.getOrNull(root.classNameSpinner.selectedItemPosition-1)
            }
            val url = if(selectedClass != null){ "${CLASS_PAGE_URL}${selectedClass.classId}" }else{ "" }
            val newTask = Task(
                root.taskTitle.text.toString(),
                selectedClass?.name ?: "",
                TaskType.values()[root.taskTypeSpinner.selectedItemPosition],
                selectedDate!!.timeInMillis,
                url,
                true
            )
            (parentFragment as TaskFragment).addTask(newTask)
            (parentFragment as TaskFragment).refresh()

            Log.d("added_new_task", newTask.toString())
            dialog?.cancel()
        }

        root.negative_button.setOnClickListener {
            dialog?.cancel()
        }

        return root
    }

    fun showDatePickerDialog(){
        val selectedDate = selectedDate ?: Calendar.getInstance()
        this.selectedDate = selectedDate
        DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _, year, month, date ->
                selectedDate.set(year, month, date)
                TimePickerDialog(
                    requireContext(),
                    TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                        selectedDate.set(Calendar.HOUR_OF_DAY, hour)
                        selectedDate.set(Calendar.MINUTE, minute)
                        selectedTimeDialog.text = timeToString(selectedDate.timeInMillis)
                    },
                    selectedDate.get(Calendar.HOUR_OF_DAY),
                    selectedDate.get(Calendar.MINUTE),
                    true
                ).apply {
                    setCancelable(false)
                }.show()
                selectedTimeDialog.text = timeToString(selectedDate.timeInMillis)
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DATE)
        ).apply {
            setCancelable(false)
        }.show()
    }
}