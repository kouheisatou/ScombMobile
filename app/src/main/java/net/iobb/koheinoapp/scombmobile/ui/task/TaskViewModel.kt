package net.iobb.koheinoapp.scombmobile.ui.task

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.iobb.koheinoapp.scombmobile.common.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class TaskViewModel : ViewModel() {
    lateinit var appViewModel: AppViewModel
    val page = Page(TASK_LIST_PAGE_URL)
    val tasks = MutableLiveData(mutableListOf<Task>())
    val tasksOfTheDate = MutableLiveData(mutableListOf<Task>())

    fun fetchTasks(context: Context){
        viewModelScope.launch(Dispatchers.IO) {
            val document = page.fetch(appViewModel.sessionId)

            val newTasks = mutableListOf<Task>()
            val fetchedTasks = document?.getElementsByClass(TASK_LIST_CSS_CLASS_NM) ?: return@launch
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
                val customColor = getClassCustomColor(className, context)

                val newTask = Task(taskTitle, className, taskType, deadline.timeInMillis, url, false, customColor)
                newTasks.add(newTask)
            }
            tasks.postValue(newTasks)
        }
    }

    fun getTasksOf(timeMillis: Long){
        val calendarDate = Calendar.getInstance().apply {
            timeInMillis = timeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dayOfTasks = mutableListOf<Task>()
        tasks.value?.forEach {
            val deadlineDate = Calendar.getInstance().apply {
                timeInMillis = it.deadLineTime
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if(calendarDate.equals(deadlineDate)){
                dayOfTasks.add(it)
            }
        }
        tasksOfTheDate.value = dayOfTasks
    }

    fun getClassCustomColor(className: String, context: Context): Int? {
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ScombMobileDB"
        ).allowMainThreadQueries().build()
        val classes = db.classCellDao().findClassesByName(className)
        return classes.getOrNull(0)?.customColorInt
    }
}