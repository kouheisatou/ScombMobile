package net.iobb.koheinoapp.scombmobile.ui.task.list

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
