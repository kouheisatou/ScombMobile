package net.iobb.koheinoapp.scombmobile.common

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.MutableLiveData

class StatefulWebView : WebView {

    var networkState = MutableLiveData(NetworkState.Initialized)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onAttachedToWindow() {
        settings.javaScriptEnabled = true
        webViewClient = ClassDetailWebViewClient()
        super.onAttachedToWindow()
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    class ClassDetailWebViewClient : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            view as StatefulWebView
            view.networkState.value = NetworkState.Loading
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            view as StatefulWebView
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
    }
}