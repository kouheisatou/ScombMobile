package net.iobb.koheinoapp.scombmobile

import android.util.Log
import android.webkit.*

class BasicAuthWebViewClient(
    private val user: String,
    private val pass: String,
    webView: WebView,
    private val onPageFetched: (cookies: List<String>, html: String) -> Unit
) : WebViewClient() {

    private lateinit var cookies: List<String>
    private var html = ""

    init {
        // add javascript that get html
        webView.addJavascriptInterface(JavaScriptInterface, "HTMLOUT")
    }

    override fun onReceivedHttpAuthRequest(
        view: WebView?,
        handler: HttpAuthHandler, host: String?, realm: String?
    ) {
        handler.proceed(user, pass)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        view?.loadUrl("javascript:window.HTMLOUT.viewSource(document.documentElement.outerHTML);")
        try{
            cookies = CookieManager.getInstance().getCookie(url).split(";")
            html = JavaScriptInterface.html
            onPageFetched(cookies, html)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun removeAllCookies() {
        CookieManager.getInstance().removeAllCookies(null)
    }

    object JavaScriptInterface {
        var html = ""
        @JavascriptInterface
        fun viewSource(src: String?) {
            html = src ?: ""
            Log.d("WebViewSrc", src ?: "null")
        }
    }
}