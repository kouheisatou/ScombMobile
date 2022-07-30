package net.iobb.koheinoapp.scombmobile.common

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebView
import android.webkit.WebViewClient

class BackgroundWebView(
    context: Context,
    url: String,
    val onHtmlSrcFetched: (html: String) -> Unit
) : WebView(context) {

    private val loadUrl = url

    @SuppressLint("SetJavaScriptEnabled")
    override fun onAttachedToWindow() {
        settings.javaScriptEnabled = true
        webViewClient = BackgroundWebViewClient(
            this,
            onHtmlSrcFetched = onHtmlSrcFetched
        )
        loadUrl(loadUrl)

        super.onAttachedToWindow()
    }

    fun exportHtml(onReturnedHtml: (String) -> Unit) {
        evaluateJavascript(
            "(function(){return document.getElementById('pageMain').innerHTML})();",
            ValueCallback {
                onReturnedHtml(it)
            }
        )
    }

    class BackgroundWebViewClient(
        val webView: WebView,
        onHtmlSrcFetched: (html: String) -> Unit,
        private val onPageFetched: () -> Unit = {},
    ) : WebViewClient() {

        init {
            // add javascript that get html
            webView.addJavascriptInterface(JavaScriptInterface, "HTMLOUT")
            JavaScriptInterface.onHtmlSrcFetched = onHtmlSrcFetched
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            view?.loadUrl("javascript:window.HTMLOUT.viewSource(document.documentElement.outerHTML);")
            try{
                onPageFetched()
            }catch (e: Exception){
                e.printStackTrace()
            }
        }

        // javascript for fetch html source
        object JavaScriptInterface {
            lateinit var onHtmlSrcFetched: (html: String) -> Unit
            @JavascriptInterface
            fun viewSource(src: String?) {
                onHtmlSrcFetched(src ?: "")
            }
        }
    }
}