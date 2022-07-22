package net.iobb.koheinoapp.scombmobile.ui.login

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import kotlinx.android.synthetic.main.fragment_login.*
import net.iobb.koheinoapp.scombmobile.BasicAuthWebViewClient
import net.iobb.koheinoapp.scombmobile.R
import net.iobb.koheinoapp.scombmobile.Values

class LoginFragment : Fragment() {
    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
    }



    enum class LoginState{
        loggedIn, loggedOut, inAuth
    }

    private val loginState = MutableLiveData(LoginState.loggedOut)


    override fun onStart() {
        loginButton.setOnClickListener {
            if(idTextView.text.toString() != "" || passwordTextView.text.toString() != ""){
                login(idTextView.text.toString(), passwordTextView.text.toString())
            }
        }
        logoutButton.setOnClickListener {
            logout()
        }
        loginState.observe(viewLifecycleOwner){
            when(loginState.value){
                LoginState.loggedOut -> {
                    loginLL.isVisible = true
                    logoutLL.isVisible = false
                    webView.isVisible = false
                }
                LoginState.loggedIn -> {
                    loginLL.isVisible = false
                    logoutLL.isVisible = true
                    webView.isVisible = false
                }
                LoginState.inAuth -> {
                    webView.isVisible = true
                    loginLL.isVisible = true
                    loginButton.isEnabled = false
                    logoutLL.isVisible = false
                }
                else -> {}
            }
        }
        super.onStart()
    }

    fun logout(){
        Values.sessionId = null
        (webView.webViewClient as BasicAuthWebViewClient).removeAllCookies()
        loginState.value = LoginState.loggedOut
        webView.clearCache(true)
    }

    fun login(user: String, pass: String){
        loginState.value = LoginState.inAuth
        webView.webViewClient = BasicAuthWebViewClient(user, pass){
            if(it.getOrNull(1)?.matches(Regex(".*SESSION=.*")) == true){
                Values.sessionId = it[1].split(Regex(".*SESSION="))[1]
            }
            Log.d("cookie", Values.sessionId ?: "null")

            if(Values.sessionId != null){
                loginState.value = LoginState.loggedIn
            }
        }
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(Values.SCOMB_LOGIN_PAGE_URL)

    }

}