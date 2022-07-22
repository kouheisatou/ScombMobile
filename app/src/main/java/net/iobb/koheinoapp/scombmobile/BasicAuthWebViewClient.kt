package net.iobb.koheinoapp.scombmobile

import android.webkit.CookieManager
import android.webkit.HttpAuthHandler
import android.webkit.WebView
import android.webkit.WebViewClient


class BasicAuthWebViewClient(private val user: String, private val pass: String, private val onCookieFetched: (cookies: List<String>) -> Unit) : WebViewClient() {

    private lateinit var cookies: List<String>

    override fun onReceivedHttpAuthRequest(
        view: WebView?,
        handler: HttpAuthHandler, host: String?, realm: String?
    ) {
        handler.proceed(user, pass)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        cookies = CookieManager.getInstance().getCookie(url).split(";")
        onCookieFetched(cookies)
    }
}