package net.iobb.koheinoapp.scombmobile.ui.task

import android.content.Context
import android.util.Log
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.Room
import net.iobb.koheinoapp.scombmobile.common.AppDatabase
import org.apache.http.client.utils.URLEncodedUtils
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

@Entity
class Task(
    val title: String,
    val className: String,
    val taskType: TaskType?,
    val deadLineTime: Long,
    val url: String,
    val done: Boolean,
    context: Context
) {

    @PrimaryKey
    lateinit var reportId: String
    lateinit var classId: String
    var customColor: Int? = null
    init {
        val params = URLEncodedUtils.parse(url, StandardCharsets.UTF_8)
        params.forEach {
            val paramKey = it.name.replace(Regex("^/.*\\?"), "")
            val paramValue = it.value

            when(paramKey) {
                "idnumber" -> classId = paramValue
                "reportId" -> reportId = paramValue
            }
        }
        customColor = getClassCustomColor(classId, context)
    }

    enum class TaskType {
        Task, Exam, Questionnaire
    }

    override fun toString(): String {
        return "Task { id=$reportId, classId=$classId, title=$title, className=$className, taskType=$taskType, deadlineTime=$deadLineTime, url=$url, customColor=$customColor, done=$done }"
    }

    fun getClassCustomColor(classId: String, context: Context): Int? {
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ScombMobileDB"
        ).allowMainThreadQueries().build()
        val classes = db.classCellDao().getClassCell(classId)
        return classes?.customColorInt
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