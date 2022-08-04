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
import net.iobb.koheinoapp.scombmobile.ui.timetable.ClassCell
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

            // tasks from scomb
            val newTasks = mutableListOf<Task>()
            val fetchedTasks = document?.getElementsByClass(TASK_LIST_CSS_CLASS_NM) ?: return@launch
            for(row in fetchedTasks) {
                val className = row.getElementsByClass(TASK_LIST_CLASS_CULUMN_CSS_NM).text()
                val taskType = when(row.getElementsByClass(TASK_LIST_TYPE_CULUMN_CSS_NM).getOrNull(0)?.text()) {
                    "課題" -> TaskType.Task
                    "テスト" -> TaskType.Exam
                    "アンケート" -> TaskType.Questionnaire
                    else -> null
                }
                val taskTitle = row.getElementsByClass(TASK_LIST_TITLE_CULUMN_CSS_NM).getOrNull(0)?.text() ?: "null"
                val relativeLocation = row.getElementsByAttribute("href").attr("href")
                val url = "$SCOMBZ_DOMAIN$relativeLocation"
                val deadlineString = row.getElementsByClass(TASK_LIST_DEADLINE_CULUMN_CSS_NM).text().replace("期限： ", "")
                val deadline = Calendar.getInstance()
                try{
                    deadline.time = SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(deadlineString)
                }catch (e: Exception){
                    e.printStackTrace()
                }

                val newTask = Task(taskTitle, className, taskType, deadline.timeInMillis, url, false)
                newTask.applyClassCustomColor(context)
                newTasks.add(newTask)
            }

            // tasks added manually
            val myTasks = fetchMyTask(context)
            for(task in myTasks){
                task.applyClassCustomColor(context)
            }
            newTasks.addAll(myTasks)

            // sort by deadline
            newTasks.sortBy { it.deadLineTime }

            Log.d("fetched_task", newTasks.toString())

            tasks.postValue(newTasks)
        }
    }

    // tasks of the day
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

    fun addMyTask(context: Context, newTask: Task){
        tasks.value?.add(newTask)
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ScombMobileDB"
        ).allowMainThreadQueries().build()
        db.taskDao().insertTask(newTask)
    }

    fun fetchMyTask(context: Context) = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ScombMobileDB"
        ).allowMainThreadQueries().build().taskDao().getAllTask()

    fun getAllClasses(context: Context): List<ClassCell> {
        val allClasses = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ScombMobileDB"
        ).allowMainThreadQueries().build().classCellDao().getAllClassCell()
        val result = mutableListOf<ClassCell>()
        allClasses.forEach {
            if(!result.contains(it)){
                result.add(it)
            }
        }
        return result
    }
}