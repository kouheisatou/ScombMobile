package net.iobb.koheinoapp.scombmobile.ui.task

import android.content.Context
import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import net.iobb.koheinoapp.scombmobile.common.AppDatabase
import net.iobb.koheinoapp.scombmobile.common.SCOMBZ_DOMAIN
import net.iobb.koheinoapp.scombmobile.common.TaskType
import org.apache.http.client.utils.URLEncodedUtils
import java.nio.charset.StandardCharsets

@Entity
class Task(
    val title: String,
    val className: String,
    val taskType: TaskType?,
    val deadLineTime: Long,
    val url: String,
    val addManually: Boolean
) {

    @PrimaryKey(autoGenerate = true)
    var taskId: Int = 0
    var reportId: String? = null
    var classId: String? = null
    var customColor: Int? = null
    init {
        val params = URLEncodedUtils.parse(url, StandardCharsets.UTF_8)
        params.forEach {
            val paramKey = it.name.replace(Regex("^$SCOMBZ_DOMAIN.*\\?"), "")
            val paramValue = it.value

            when(paramKey) {
                "idnumber" -> classId = paramValue
                "reportId" -> reportId = paramValue
                "examinationId" -> reportId = paramValue
            }
        }
    }

    override fun toString(): String {
        return "Task { id=$reportId, classId=$classId, title=$title, className=$className, taskType=$taskType, deadlineTime=$deadLineTime, url=$url, customColor=$customColor, addManually=$addManually }"
    }

    fun applyClassCustomColor(context: Context) {
        val classId = classId ?: return
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ScombMobileDB"
        ).allowMainThreadQueries().build()
        customColor = db.classCellDao().getClassCell(classId)?.customColorInt
    }
}