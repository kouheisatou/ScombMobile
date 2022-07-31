package net.iobb.koheinoapp.scombmobile.ui.task

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.iobb.koheinoapp.scombmobile.common.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class TaskViewModel : ViewModel() {
    lateinit var appViewModel: AppViewModel
    val page = Page(TASK_LIST_PAGE_URL)
    val tasks = mutableListOf<Task>()

    fun fetchTasks(context: Context){
        viewModelScope.launch(Dispatchers.IO) {
            page.fetch(appViewModel.sessionId)
        }
    }

    fun constructTasks(){
        val fetchedTasks = page.document.getElementsByClass(TASK_LIST_CSS_CLASS_NM)
        for(row in fetchedTasks) {
            val className = row.getElementsByClass(TASK_LIST_CLASS_CULUMN_CSS_NM).text()
            val taskType = when(row.getElementsByClass(TASK_LIST_TYPE_CULUMN_CSS_NM).getOrNull(0)?.text()) {
                "課題" -> Task.TaskType.Task
                "テスト" -> Task.TaskType.Test
                "アンケート" -> Task.TaskType.Questionnaire
                else -> null
            }
            val taskTitle = row.getElementsByClass(TASK_LIST_TITLE_CULUMN_CSS_NM).getOrNull(0)?.text() ?: "null"
            val url = row.getElementsByAttribute("href").attr("href")
            val deadlineString = row.getElementsByClass(TASK_LIST_DEADLINE_CULUMN_CSS_NM).text().replace("期限： ", "")
            val deadline = Calendar.getInstance()
            try{
                deadline.time = SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(deadlineString)
            }catch (e: Exception){
                e.printStackTrace()
            }

            val newTask = Task(taskTitle, className, taskType, deadline.timeInMillis, url, false)
            tasks.add(newTask)
        }
        Log.d("fetched_tasks", tasks.toString())

    }
}