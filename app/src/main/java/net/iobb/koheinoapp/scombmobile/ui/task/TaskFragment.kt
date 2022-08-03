package net.iobb.koheinoapp.scombmobile.ui.task

interface TaskFragment {
    fun refresh()
    fun addTask(newTask: Task)
}