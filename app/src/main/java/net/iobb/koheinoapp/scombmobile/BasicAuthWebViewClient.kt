package net.iobb.koheinoapp.scombmobile

import android.webkit.HttpAuthHandler
import android.webkit.WebView
import android.webkit.WebViewClient


class BasicAuthWebViewClient(private val user: String, private val pass: String) : WebViewClient() {

    override fun onReceivedHttpAuthRequest(
        view: WebView?,
        handler: HttpAuthHandler, host: String?, realm: String?
    ) {
        handler.proceed(user, pass)
    }
}