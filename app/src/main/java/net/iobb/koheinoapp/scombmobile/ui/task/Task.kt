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
) {
    enum class TaskType {
        Task, Test, Questionnaire
    }

    override fun toString(): String {
        return "Task { title=$title, className=$className, taskType=$taskType, deadlineTime=$deadLineTime, url=$url, done=$done }"
    }
}

fun getTaskTypeFromString(taskType: String): Task.TaskType? {
    return when(taskType){
        Task.TaskType.Task.toString() -> {
            Task.TaskType.Task
        }
        Task.TaskType.Test.toString() -> {
            Task.TaskType.Test
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
    val date = Date(timeMillis)
    val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
    return formatter.format(date)
}