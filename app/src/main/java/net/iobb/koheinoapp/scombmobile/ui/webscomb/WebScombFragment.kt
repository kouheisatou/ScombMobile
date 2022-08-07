package net.iobb.koheinoapp.scombmobile.ui.webscomb

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_web_scomb.*
import kotlinx.android.synthetic.main.fragment_web_scomb.view.*
import net.iobb.koheinoapp.scombmobile.*
import net.iobb.koheinoapp.scombmobile.common.AppViewModel
import net.iobb.koheinoapp.scombmobile.common.SCOMB_HOME_URL
import net.iobb.koheinoapp.scombmobile.common.SESSION_COOKIE_ID

class WebScombFragment : Fragment() {

    companion object {
        fun newInstance() = WebScombFragment()
    }
    val TAG = "WebScombFragment"

    private lateinit var viewModel: WebScombViewModel
    private val appViewModel: AppViewModel by activityViewModels()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_web_scomb, container, false)

        if(appViewModel.sessionId == null){
            this.findNavController().navigate(R.id.nav_loginFragment)
        }else{
            root.webView.webViewClient = WebViewClient()
            root.webView.settings.javaScriptEnabled = true

            val cookieString = "$SESSION_COOKIE_ID=${appViewModel.sessionId ?: ""}; path=/"
            CookieManager.getInstance().setCookie(SCOMB_HOME_URL, cookieString)
            Log.d(TAG, "cookie=$cookieString")

            root.webView.loadUrl(SCOMB_HOME_URL)
        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(WebScombViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(
            true
        ) {
            override fun handleOnBackPressed() {
                webView.goBack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            callback
        )
    }

}