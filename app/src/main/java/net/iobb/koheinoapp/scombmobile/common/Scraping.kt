package net.iobb.koheinoapp.scombmobile.common

import org.apache.commons.net.util.Base64
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.lang.Exception

private const val USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36"
private const val HEADER_ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
private const val HEADER_ACCEPT_LANG = "ja,en-US;q=0.7,en;q=0.3"
private const val HEADER_ACCEPT_ENCODING = "gzip, deflate, br"
private const val HEADER_REFERER = "https://www.xxxxx/yyyy"

fun main() {
//    basicAuthLogin(getScombSamlRequestUrl() ?: return, "user", "password")
//    getScombSamlRequestUrl()
//    cookieLogin(SCOMB_HOME_URL, "NTgxZjQ5MWUtYzAxMS00M2FkLWJhMzktMDhiNzYwOTRjNGU2")
    cookieLogin(CLASS_PAGE_URL + "202201SU0086501001", "OGFhOTZhZmEtNTlmNS00MGIxLTllZDItODgyNzcwZDM2ZjIy")
}

/**
 * ScombのSAMLRequestURLをログインページから取得する
 */
fun getScombSamlRequestUrl(): String?{

    var loginUrl: String? = null

    try {
        Jsoup.connect(SCOMB_LOGIN_PAGE_URL)
            .userAgent(USER_AGENT)
            .header("Accept", HEADER_ACCEPT)
            .header("Accept-Language", HEADER_ACCEPT_LANG)
            .header("Accept-Encoding", HEADER_ACCEPT_ENCODING)
            .header("Referer", HEADER_REFERER)
            .timeout(10 * 1000)
            .get();
    }catch (e: HttpStatusException){
        loginUrl = e.url
        println(e.url)
    }

    return loginUrl
}

/**
 * Scombのホームページにブラウザでログインしたときに得られるCookieを使ってログイン
 */
fun cookieLogin(url: String, cookie: String){

    var doc: Document? = null
    try {
        doc = Jsoup.connect(url)
            .userAgent(USER_AGENT)
            .header("Accept", HEADER_ACCEPT)
            .header("Accept-Language", HEADER_ACCEPT_LANG)
            .header("Accept-Encoding", HEADER_ACCEPT_ENCODING)
            .header("Referer", HEADER_REFERER)
            .timeout(10 * 1000)
            .cookie("SESSION", cookie)
            .get()
    } catch (e: HttpStatusException) {
        e.printStackTrace()
    }

    println(doc?.baseUri())
    println(doc?.text())
}

/**
 * BASIC認証のサイトをスクレイピングするサンプル
 */
fun basicAuthLogin(url: String, user: String, pass: String){
    val authString = "$user:$pass"
    val encodedString = String(Base64.encodeBase64(authString.toByteArray()))

    val doc = try {
        Jsoup.connect(url)
            .header("Authorization", "Basic $encodedString")
            .get()
        println("Logged in using basic authentication")
    } catch (e: Exception) {
        e.printStackTrace()
    }

}