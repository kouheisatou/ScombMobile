package net.iobb.koheinoapp.scombmobile.common

enum class TaskType {
    Task, Exam, Questionnaire, Others
}

fun getTaskTypeFromString(taskType: String): TaskType? {
    return when(taskType){
        TaskType.Task.toString() -> {
            TaskType.Task
        }
        TaskType.Exam.toString() -> {
            TaskType.Exam
        }
        TaskType.Questionnaire.toString() -> {
            TaskType.Questionnaire
        }
        TaskType.Others.toString() -> {
            TaskType.Others
        }
        else -> {
            null
        }
    }
}

fun japaneseTaskType() = listOf("課題", "テスト", "アンケート", "その他")

val japaneseTaskTypeMap = mapOf(
    "課題" to TaskType.Task,
    "テスト" to TaskType.Exam,
    "アンケート" to TaskType.Questionnaire,
    "その他" to TaskType.Others
)