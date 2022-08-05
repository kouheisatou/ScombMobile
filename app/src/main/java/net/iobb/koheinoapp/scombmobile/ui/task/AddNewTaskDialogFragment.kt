package net.iobb.koheinoapp.scombmobile.ui.task

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    var selectedClass: ClassCell? = null

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

        val allClasses = mutableListOf<ClassCell>()
        taskViewModel.getAllClasses(requireContext()).toMutableList().apply {
            sortWith(compareBy<ClassCell> { it.year }.thenBy { it.term }.thenBy { it.dayOfWeek })
            reverse()
        }.forEach {
            var contains = false
            for (c in allClasses) {
                if(c.classId == it.classId){
                    contains = true
                    break
                }
            }
            if (!contains) {
                allClasses.add(it)
            }
        }
        val classesNameString = mutableListOf("指定なし")
        allClasses.forEach {
            if(!classesNameString.contains(it.name)){
                classesNameString.add(it.name)
            }
        }
        setRightGravityAdapterToSpinner(requireContext(), classesNameString, root.classNameSpinner){ position, _ ->
            selectedClass = if(position == 0){
                null
            }else{
                allClasses[position-1]
            }
        }

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

            val url = if(selectedClass != null){ "${CLASS_PAGE_URL}${selectedClass!!.classId}" }else{ "" }
            val newTask = Task(
                root.taskTitle.text.toString(),
                selectedClass?.name ?: "",
                TaskType.values()[root.taskTypeSpinner.selectedItemPosition],
                selectedDate!!.timeInMillis,
                url,
                true
            )
            (parentFragment as TaskFragment).addTask(newTask)

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