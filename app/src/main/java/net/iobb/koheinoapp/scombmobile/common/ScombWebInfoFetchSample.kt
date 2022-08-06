package net.iobb.koheinoapp.scombmobile.common

import androidx.lifecycle.MutableLiveData
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

private const val USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36"
private const val HEADER_ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
private const val HEADER_ACCEPT_LANG = "ja,en-US;q=0.7,en;q=0.3"
private const val HEADER_ACCEPT_ENCODING = "gzip, deflate, br"
private const val HEADER_REFERER = "https://www.xxxxx/yyyy"

fun main() {
    val doc = fetch(SCOMB_HOME_URL, "MjYzZWJiNjktODBiNS00NTc5LTlhYWEtNWUxZjE4ZjU0Mjcz")
    println(doc?.text())

}

fun fetch(url: String, cookieId: String): Document?{
    var document: Document? = null

    try {
        document = Jsoup.connect(url)
            .userAgent(USER_AGENT)
            .header("Accept", HEADER_ACCEPT)
            .header("Accept-Language", HEADER_ACCEPT_LANG)
            .header("Accept-Encoding", HEADER_ACCEPT_ENCODING)
            .header("Referer", HEADER_REFERER)
            .timeout(10 * 1000)
            .cookie("SESSION", cookieId)
            .get()

        if(document.baseUri() == SCOMB_LOGGED_OUT_PAGE_URL){
            return null
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
    return document
}