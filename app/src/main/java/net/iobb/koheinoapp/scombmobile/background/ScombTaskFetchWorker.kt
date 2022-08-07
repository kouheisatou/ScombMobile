package net.iobb.koheinoapp.scombmobile.background

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.work.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.iobb.koheinoapp.scombmobile.common.Page.Companion.HEADER_ACCEPT
import net.iobb.koheinoapp.scombmobile.common.Page.Companion.HEADER_ACCEPT_ENCODING
import net.iobb.koheinoapp.scombmobile.common.Page.Companion.HEADER_ACCEPT_LANG
import net.iobb.koheinoapp.scombmobile.common.Page.Companion.HEADER_REFERER
import net.iobb.koheinoapp.scombmobile.common.Page.Companion.USER_AGENT
import net.iobb.koheinoapp.scombmobile.common.SCOMB_LOGGED_OUT_PAGE_URL
import net.iobb.koheinoapp.scombmobile.common.TASK_LIST_PAGE_URL
import net.iobb.koheinoapp.scombmobile.ui.task.TaskViewModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.concurrent.TimeUnit

const val WORKER_TAG = "scomb_task_fetch_worker"
const val SESSION_ID_KEY = "session_id"

class ScombTaskFetchWorker(
    val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    companion object {
        fun resumeBackgroundFetch(context: Context, sessionId: String){

            val data = Data.Builder()
                .putString(SESSION_ID_KEY, sessionId)
                .build()
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = PeriodicWorkRequest.Builder(ScombTaskFetchWorker::class.java, 1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .setInputData(data)
                .addTag(WORKER_TAG)
                .build()

            val workerManager = WorkManager.getInstance(context)
            workerManager.enqueue(request)
        }
    }

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {

            val sessionId = inputData.getString("session_id") ?: return@withContext Result.failure()

            val document = fetchTasksFromScomb(sessionId)

            val fetchedTasks = TaskViewModel.generateTaskFromHtml(
                context,
                document ?: return@withContext Result.failure()
            )
            Log.d("background_fetched_tasks", fetchedTasks.toString())

            fetchedTasks.forEach {
                ScombMobileNotification.setTaskAlarm(context, it)
            }

            return@withContext Result.success()
        }
    }

    private suspend fun fetchTasksFromScomb(sessionId: String?): Document?{
        if (sessionId == null) {
            return null
        }

        var document: Document?

        try {
            document = Jsoup.connect(TASK_LIST_PAGE_URL)
                .userAgent(USER_AGENT)
                .header("Accept", HEADER_ACCEPT)
                .header("Accept-Language", HEADER_ACCEPT_LANG)
                .header("Accept-Encoding", HEADER_ACCEPT_ENCODING)
                .header("Referer", HEADER_REFERER)
                .timeout(10 * 1000)
                .cookie("SESSION", sessionId)
                .get()

            if(document.baseUri() == SCOMB_LOGGED_OUT_PAGE_URL){
                document = null
            }
        }catch (e: Exception){
            e.printStackTrace()
            document = null
        }
        return document
    }
}