package net.iobb.koheinoapp.scombmobile

import android.util.Log
import android.webkit.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import net.iobb.koheinoapp.scombmobile.ui.login.LoginFragment

class BasicAuthWebViewClient(
    private val user: String,
    private val pass: String,
    webView: WebView,
    private val onPageFetched: (cookies: List<String>) -> Unit,
    onHtmlSrcFetched: (html: String) -> Unit = {},
    private val onCookieFetchError: () -> Unit
) : WebViewClient() {

    private lateinit var cookies: List<String>

    init {
        // add javascript that get html
        webView.addJavascriptInterface(JavaScriptInterface, "HTMLOUT")
        JavaScriptInterface.onHtmlSrcFetched = onHtmlSrcFetched
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

            onPageFetched(cookies)
        }catch (e: Exception){
            e.printStackTrace()
            onCookieFetchError()
        }
    }

    fun removeAllCookies() {
        CookieManager.getInstance().removeAllCookies(null)
    }

    // javascript for fetch html source
    object JavaScriptInterface {
        lateinit var onHtmlSrcFetched: (html: String) -> Unit
        @JavascriptInterface
        fun viewSource(src: String?) {
//            Log.d("WebViewHtmlSrc", src ?: "null")
            onHtmlSrcFetched(src ?: "")
        }
    }
}