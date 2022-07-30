package net.iobb.koheinoapp.scombmobile.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.AttributeSet
import android.webkit.*
import androidx.lifecycle.MutableLiveData


class StatefulWebView : WebView {

    var networkState = MutableLiveData(NetworkState.Initialized)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onAttachedToWindow() {
        settings.javaScriptEnabled = true
        webViewClient = StatefulWebViewClient()
        super.onAttachedToWindow()
    }

    fun loadUrl(url: String, onScriptCallback: ((String) -> Unit)?, vararg scripts: String) {
        (webViewClient as StatefulWebViewClient).scripts.addAll(scripts)
        (webViewClient as StatefulWebViewClient).onScriptCallback = onScriptCallback ?: {}
        super.loadUrl(url)
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    class StatefulWebViewClient : WebViewClient() {
        val scripts = mutableListOf<String>()
        var onScriptCallback: (String) -> Unit = {}

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            view as StatefulWebView
            view.networkState.value = NetworkState.Loading
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            view as StatefulWebView
            scripts.forEach { script ->
                view.evaluateJavascript(script, ValueCallback { onScriptCallback(it) })
            }
            view.networkState.value = NetworkState.Finished
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            view as StatefulWebView
            view.networkState.value = NetworkState.NotPermitted
            super.onReceivedError(view, request, error)
        }

        // open url on default browser
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            view?.context?.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(view.url))
            )
            return true
        }
    }
}