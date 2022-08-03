package net.iobb.koheinoapp.scombmobile.ui.task.list

import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import net.iobb.koheinoapp.scombmobile.R
import net.iobb.koheinoapp.scombmobile.common.SCOMBZ_DOMAIN
import net.iobb.koheinoapp.scombmobile.common.TaskType
import net.iobb.koheinoapp.scombmobile.common.timeToString
import net.iobb.koheinoapp.scombmobile.databinding.FragmentItemBinding
import net.iobb.koheinoapp.scombmobile.ui.task.Task
import net.iobb.koheinoapp.scombmobile.ui.task.calendar.TaskCalendarFragmentDirections
import java.util.*

class TaskRecyclerViewAdapter(
    private val tasks: List<Task>
) : RecyclerView.Adapter<TaskRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = FragmentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(view)

        view.linearLayout.setOnClickListener {
            val task = tasks[holder.layoutPosition]
            try {
                val action =
                    TaskListFragmentDirections.actionTaskListFragmentToNavSinglePageWebScombFragment("$SCOMBZ_DOMAIN${task.url}")
                view.linearLayout.findNavController().navigate(action)
            }catch (e: Exception){
                try {
                    val action = TaskCalendarFragmentDirections.actionTaskCalendarFragmentToNavSinglePageWebScombFragment("$SCOMBZ_DOMAIN${task.url}")
                    view.linearLayout.findNavController().navigate(action)
                }catch (e: Exception){

                }
            }
        }

        return holder

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = tasks[position]
        holder.titleTextView.text = item.title
        holder.classNameTextView.text = item.className
        if (item.customColor != null) {
            holder.classNameTextView.setTextColor(item.customColor!!)
        }

        val iconResource: Int
        val taskTitleString: String
        when(item.taskType){
            TaskType.Task -> {
                iconResource = R.drawable.ic_baseline_assignment_24
                taskTitleString = "課題"
            }
            TaskType.Exam -> {
                iconResource = R.drawable.ic_baseline_checklist_24
                taskTitleString = "テスト"
            }
            TaskType.Questionnaire -> {
                iconResource = R.drawable.question_24
                taskTitleString = "アンケート"
            }
            else -> {
                iconResource = R.drawable.ic_baseline_insert_drive_file_24
                taskTitleString = "その他"
            }
        }
        holder.icon.setImageResource(iconResource)
        holder.taskTypeTextView.text = taskTitleString
        // if tasks deadline in 24h
        if(item.deadLineTime - Date().time < 86400000){
            holder.deadlineTextView.setTextColor(Color.parseColor("#EE0000"))
        }
        holder.deadlineTextView.text = "締切 : ${timeToString(item.deadLineTime)}"
    }

    override fun getItemCount(): Int = tasks.size

    inner class ViewHolder(binding: FragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val deadlineTextView: TextView = binding.deadlineTextView
        val classNameTextView: TextView = binding.classNameTextView
        val titleTextView: TextView = binding.titleTextView
        val icon: ImageView = binding.iconView
        val taskTypeTextView = binding.taskTypeTextView
    }

}