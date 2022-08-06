package net.iobb.koheinoapp.scombmobile.background

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.iobb.koheinoapp.scombmobile.common.SCOMB_LOGGED_OUT_PAGE_URL
import net.iobb.koheinoapp.scombmobile.common.TASK_LIST_PAGE_URL
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

private const val USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36"
private const val HEADER_ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
private const val HEADER_ACCEPT_LANG = "ja,en-US;q=0.7,en;q=0.3"
private const val HEADER_ACCEPT_ENCODING = "gzip, deflate, br"
private const val HEADER_REFERER = "https://www.xxxxx/yyyy"

class TasksFetchDemon : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(baseContext, "${this::class.simpleName}#onStartCommand()", Toast.LENGTH_SHORT).show()

        val sessionId = intent?.getStringExtra("session_id")
        Toast.makeText(baseContext, "session_id=${sessionId}", Toast.LENGTH_SHORT).show()

        fetchTasksFromScomb(sessionId){ document ->
            Log.d("fetched_tasks", document?.text() ?: "null")
        }

        return START_NOT_STICKY
    }

    private fun fetchTasksFromScomb(sessionId: String?, onFinished: (document: Document?) -> Unit){
        if (sessionId == null) {
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            var document: Document? = null
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
                Toast.makeText(baseContext, e.stackTraceToString(), Toast.LENGTH_SHORT).show()
            }

            onFinished(document)
        }
    }

}