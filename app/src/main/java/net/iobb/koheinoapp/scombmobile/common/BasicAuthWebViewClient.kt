package net.iobb.koheinoapp.scombmobile.common

import android.webkit.*

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
}