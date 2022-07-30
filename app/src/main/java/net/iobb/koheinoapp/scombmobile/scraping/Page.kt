package net.iobb.koheinoapp.scombmobile.scraping

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.gargoylesoftware.htmlunit.BrowserVersion
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.util.Cookie
import net.iobb.koheinoapp.scombmobile.SCOMB_LOGGED_OUT_PAGE_URL
import net.iobb.koheinoapp.scombmobile.SESSION_COOKIE_ID
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document


private const val USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36"
private const val HEADER_ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
private const val HEADER_ACCEPT_LANG = "ja,en-US;q=0.7,en;q=0.3"
private const val HEADER_ACCEPT_ENCODING = "gzip, deflate, br"
private const val HEADER_REFERER = "https://www.xxxxx/yyyy"

class Page(val url: String) {

    enum class NetworkState {
        Finished, Loading, NotPermitted, Initialized
    }

    lateinit var document: Document
    var networkState = MutableLiveData(NetworkState.Initialized)

    fun fetch(cookieId: String?){
        networkState.postValue(NetworkState.Loading)
        try {
            document = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .header("Accept", HEADER_ACCEPT)
                .header("Accept-Language", HEADER_ACCEPT_LANG)
                .header("Accept-Encoding", HEADER_ACCEPT_ENCODING)
                .header("Referer", HEADER_REFERER)
                .timeout(10 * 1000)
                .cookie("SESSION", cookieId ?: "")
                .get()
            if(document.baseUri() == SCOMB_LOGGED_OUT_PAGE_URL){
                networkState.postValue(NetworkState.NotPermitted)
            }
        } catch (e: HttpStatusException) {
            e.printStackTrace()
        }finally {
            networkState.postValue(NetworkState.Finished)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun fetchDynamicPage(cookieId: String?){
        networkState.postValue(NetworkState.Loading)
        try {

            val webClient = WebClient(BrowserVersion.CHROME)
            webClient.options.isJavaScriptEnabled = true
            webClient.options.isThrowExceptionOnScriptError = false
            webClient.cookieManager.addCookie(Cookie("scombz.shibaura-it.ac.jp", SESSION_COOKIE_ID, cookieId))
            webClient.ajaxController = NicelyResynchronizingAjaxController()
            webClient.waitForBackgroundJavaScript(3000)

            val htmlPage: HtmlPage = webClient.getPage(url)

            document = Jsoup.parse(htmlPage.asXml())
            Log.d("class_overview", htmlPage.textContent ?: "")

            if(document.baseUri() == SCOMB_LOGGED_OUT_PAGE_URL){
                networkState.postValue(NetworkState.NotPermitted)
            }
        } catch (e: HttpStatusException) {
            e.printStackTrace()
        }finally {
            networkState.postValue(NetworkState.Finished)
        }
    }
}