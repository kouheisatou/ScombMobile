package net.iobb.koheinoapp.scombmobile.ui.task

import android.content.Context

interface TaskFragment {
    fun requireContext(): Context
    fun refresh()
    fun addTask(newTask: Task)
    fun removeTask(removeTarget: Task)
}