package net.iobb.koheinoapp.scombmobile

import android.os.Build
import android.webkit.CookieManager
import android.webkit.HttpAuthHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import java.lang.Exception


class BasicAuthWebViewClient(private val user: String, private val pass: String, private val onCookieFetched: (cookies: List<String>) -> Unit) : WebViewClient() {

    private lateinit var cookies: List<String>

    override fun onReceivedHttpAuthRequest(
        view: WebView?,
        handler: HttpAuthHandler, host: String?, realm: String?
    ) {
        handler.proceed(user, pass)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        try{
            cookies = CookieManager.getInstance().getCookie(url).split(";")
            onCookieFetched(cookies)
        }catch (e: Exception){

        }
    }

    fun removeAllCookies() {
        CookieManager.getInstance().removeAllCookies(null)
    }
}