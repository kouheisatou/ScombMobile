package net.iobb.koheinoapp.scombmobile.ui.task

import java.text.SimpleDateFormat
import java.util.*

class Task(
    val title: String,
    val className: String,
    val taskType: TaskType?,
    val deadLineTime: Long,
    val url: String,
    val done: Boolean,
    var customColor: Int?,
) {

    enum class TaskType {
        Task, Exam, Questionnaire
    }

    override fun toString(): String {
        return "Task { title=$title, className=$className, taskType=$taskType, deadlineTime=$deadLineTime, url=$url, customColor=$customColor, done=$done }"
    }
}

fun getTaskTypeFromString(taskType: String): Task.TaskType? {
    return when(taskType){
        Task.TaskType.Task.toString() -> {
            Task.TaskType.Task
        }
        Task.TaskType.Exam.toString() -> {
            Task.TaskType.Exam
        }
        Task.TaskType.Questionnaire.toString() -> {
            Task.TaskType.Questionnaire
        }
        else -> {
            null
        }
    }
}

fun timeToString(timeMillis: Long): String {
    val date = Calendar.getInstance().apply { this.timeInMillis = timeMillis }
    val today = Calendar.getInstance()
    return if(date.get(Calendar.DATE) == today.get(Calendar.DATE)){
        val formatter = SimpleDateFormat("HH:mm")
        "今日 ${formatter.format(date.time)}"
    }else if(date.get(Calendar.DATE) == today.get(Calendar.DATE)+1){
        val formatter = SimpleDateFormat("HH:mm")
        "明日 ${formatter.format(date.time)}"
    }else if(date.get(Calendar.DATE) == today.get(Calendar.DATE)-1){
        val formatter = SimpleDateFormat("HH:mm")
        "昨日 ${formatter.format(date.time)}"
    }else{
        val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm")
        formatter.format(date.time)
    }
}