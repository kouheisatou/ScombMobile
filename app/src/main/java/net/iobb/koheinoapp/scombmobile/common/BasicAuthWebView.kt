package net.iobb.koheinoapp.scombmobile.common

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.util.Log
import android.webkit.*
import androidx.lifecycle.MutableLiveData
import kotlinx.android.synthetic.main.fragment_login.*

class BasicAuthWebView : WebView {

    var loginState = MutableLiveData(LoginState.LoggedOut)
    var sessionId: String? = null
    private val scripts = mutableListOf<String>()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onAttachedToWindow() {
        settings.javaScriptEnabled = true
        super.onAttachedToWindow()
    }

    fun loadUrl(url: String, user: String, pass: String) {
        webViewClient = BasicAuthWebViewClient(
            user,
            pass,
            onPageFetched = { cookies ->
                sessionId = getSessionId(cookies)
                for(script in scripts){
                    evaluateJavascript(script){}
                }

                if(sessionId != null){

                }
            },
            onCookieFetchError = {
                loginState.value = LoginState.LoggedOut
            }
        )
        loadUrl(url)
    }

    // this script invoked when finished page loading
    fun addJavascript(script: String){
        scripts.add(script)
    }

    fun getSessionId(cookies: List<String>): String?{
        var sessionId: String? = null
        for(cookie in cookies){
            if(cookie.matches(Regex(".*$SESSION_COOKIE_ID=.*"))){
                sessionId = cookie.split(Regex(".*$SESSION_COOKIE_ID="))[1]
            }
        }
        return sessionId
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    class BasicAuthWebViewClient(
        private val user: String,
        private val pass: String,
        private val onPageFetched: (cookies: List<String>) -> Unit,
        private val onCookieFetchError: () -> Unit
    ) : WebViewClient() {

        private lateinit var cookies: List<String>

        override fun onReceivedHttpAuthRequest(
            view: WebView?,
            handler: HttpAuthHandler, host: String?, realm: String?
        ) {
            handler.proceed(user, pass)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            try {
                cookies = CookieManager.getInstance().getCookie(url).split(";")
                onPageFetched(cookies)
            } catch (e: Exception) {
                e.printStackTrace()
                onCookieFetchError()
            }
        }

        fun removeAllCookies() {
            CookieManager.getInstance().removeAllCookies(null)
        }
    }
}