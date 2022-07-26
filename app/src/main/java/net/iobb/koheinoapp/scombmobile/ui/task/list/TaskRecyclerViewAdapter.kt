package net.iobb.koheinoapp.scombmobile.ui.task.list

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.findNavController
import net.iobb.koheinoapp.scombmobile.R
import net.iobb.koheinoapp.scombmobile.common.SCOMBZ_DOMAIN
import net.iobb.koheinoapp.scombmobile.common.TaskType
import net.iobb.koheinoapp.scombmobile.common.timeToString
import net.iobb.koheinoapp.scombmobile.databinding.FragmentItemBinding
import net.iobb.koheinoapp.scombmobile.ui.task.Task
import net.iobb.koheinoapp.scombmobile.ui.task.TaskFragment
import net.iobb.koheinoapp.scombmobile.ui.task.calendar.TaskCalendarFragmentDirections
import java.util.*

class TaskRecyclerViewAdapter(
    private val tasks: MutableList<Task>,
    private val taskFragment: TaskFragment
) : RecyclerView.Adapter<TaskRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = FragmentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = tasks[position]
        holder.titleTextView.text = item.title
        holder.classNameTextView.text = item.className
        if (item.customColor != null) {
            holder.classNameTextView.setTextColor(item.customColor!!)
        }else{
            holder.classNameTextView.setTextColor(holder.classNameTextView.textColors.defaultColor)
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
        }else{
            holder.deadlineTextView.setTextColor(holder.deadlineTextView.textColors.defaultColor)
        }
        holder.deadlineTextView.text = "締切 : ${timeToString(item.deadLineTime)}"


        holder.linearLayout.setOnClickListener {
            val task = tasks[holder.layoutPosition]
            if(task.url == "") return@setOnClickListener
            try {
                val action =
                    TaskListFragmentDirections.actionTaskListFragmentToNavSinglePageWebScombFragment(task.url)
                holder.linearLayout.findNavController().navigate(action)
            }catch (e: Exception){
                try {
                    val action = TaskCalendarFragmentDirections.actionTaskCalendarFragmentToNavSinglePageWebScombFragment(task.url)
                    holder.linearLayout.findNavController().navigate(action)
                }catch (e: Exception){

                }
            }
        }

        holder.deleteBtn.visibility = if(item.addManually){
            View.VISIBLE
        }else{
            View.GONE
        }
        holder.deleteBtn.setOnClickListener {
            AlertDialog.Builder(taskFragment.requireContext())
                .setTitle("タスクの削除")
                .setMessage("\"${item.title}\"\nを削除しますか？")
                .setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                    if(item.addManually){
                        taskFragment.removeTask(item)
                        tasks.remove(item)
                    }else{
                        Toast.makeText(taskFragment.requireContext(), "Scombのタスクは削除できません", Toast.LENGTH_SHORT).show()
                    }
                })
                .setNegativeButton("CANCEL", null)
                .show()
        }
    }

    override fun getItemCount(): Int = tasks.size

    inner class ViewHolder(binding: FragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val deadlineTextView: TextView = binding.deadlineTextView
        val classNameTextView: TextView = binding.classNameTextView
        val titleTextView: TextView = binding.titleTextView
        val icon: ImageView = binding.iconView
        val taskTypeTextView = binding.taskTypeTextView
        val linearLayout = binding.linearLayout
        val deleteBtn = binding.deleteBtn
    }

}