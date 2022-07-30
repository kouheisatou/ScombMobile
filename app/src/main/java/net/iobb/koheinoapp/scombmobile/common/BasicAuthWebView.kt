package net.iobb.koheinoapp.scombmobile.common

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.webkit.*
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import kotlinx.android.synthetic.main.fragment_login.view.*

class BasicAuthWebView : WebView {

    var loginState = MutableLiveData(LoginState.LoggedOut)
    var sessionId: String? = null
    var loginUser: String? = null
    private val scripts = mutableListOf<String>()

    // web view setting
    @SuppressLint("SetJavaScriptEnabled")
    override fun onAttachedToWindow() {
        settings.javaScriptEnabled = true
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        super.onAttachedToWindow()
    }

    private fun reset(){
        sessionId = null
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        clearCache(true)
    }

    fun logout(){
        reset()
        loginState.value = LoginState.LoggedOut
    }

    fun login(url: String, user: String, pass: String) {
        reset()

        loginState.value = LoginState.InAuth

        webViewClient = BasicAuthWebViewClient(
            user,
            pass,
            onPageFetched = { cookies ->
                //save session id
                sessionId = getSessionIdFromCookie(cookies)
                loginUser = user
                Log.d("session_id", sessionId ?: "null")

                // run javascript
                for(script in scripts){
                    evaluateJavascript(script){}
                }

                // login successful
                if(sessionId != null){
                    loginState.value = LoginState.LoggedIn
                }
            },
            onCookieFetchError = {
                try {
                    Toast.makeText(context, "ログイン失敗", Toast.LENGTH_SHORT).show()
                }catch (e: Exception){
                    e.printStackTrace()
                }finally {
                    loginState.value = LoginState.LoggedOut
                }
            }
        )
        loadUrl(url)
    }

    // these scripts invoked when finished page loading
    fun addJavascript(script: String){
        scripts.add(script)
    }

    private fun getSessionIdFromCookie(cookies: List<String>): String?{
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
            Log.d("login_user", user)
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
    }
}