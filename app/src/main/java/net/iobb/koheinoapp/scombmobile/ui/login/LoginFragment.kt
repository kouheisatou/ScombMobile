package net.iobb.koheinoapp.scombmobile.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.room.Room
import kotlinx.android.synthetic.main.fragment_login.*
import net.iobb.koheinoapp.scombmobile.*
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.lang.Exception

class LoginFragment : Fragment() {
    private lateinit var viewModel: LoginViewModel
    private val appViewModel: AppViewModel by activityViewModels()


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
        LoggedIn, LoggedOut, InAuth
    }

    private val loginState = MutableLiveData(LoginState.LoggedOut)

    override fun onStart() {

        loginState.value = if(appViewModel.sessionId != null){LoginState.LoggedIn }else{LoginState.LoggedOut }

        val db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java,
            "ScombMobileDB"
        ).allowMainThreadQueries().build()

        // recover saved id and pass from db
        idTextView.setText(db.userDao().getUser()?.username ?: "")
        passwordTextView.setText(db.userDao().getUser()?.password ?: "")

        // auto login
        if(db.settingDao().getSetting("enabled_auto_login")?.settingValue == "true" && loginState.value == LoginState.LoggedOut){
            login(idTextView.text.toString(), passwordTextView.text.toString())
        }

        loginButton.setOnClickListener {
            if(idTextView.text.toString() != "" && passwordTextView.text.toString() != ""){
                login(idTextView.text.toString(), passwordTextView.text.toString())
            }
        }
        logoutButton.setOnClickListener {
            logout()
        }
        idTextView.addTextChangedListener {
            db.userDao().removeAllUser()
            db.userDao().insertUser(User(idTextView.text.toString(), passwordTextView.text.toString()))
        }
        passwordTextView.addTextChangedListener {
            db.userDao().removeAllUser()
            db.userDao().insertUser(User(idTextView.text.toString(), passwordTextView.text.toString()))
        }

        // display WebView in auth
        val dispWebView = false

        // control views
        loginState.observe(viewLifecycleOwner){
            when(loginState.value){
                LoginState.LoggedOut -> {
                    loginLL.isVisible = true
                    logoutLL.isVisible = false
                    progressBar.isVisible = false
                    loginButton.isEnabled = true
                    if(dispWebView){ webView.isVisible = false }
                }
                LoginState.LoggedIn -> {
                    loginLL.isVisible = false
                    logoutLL.isVisible = true
                    progressBar.isVisible = false
                    if(dispWebView){ webView.isVisible = false }
                }
                LoginState.InAuth -> {
                    progressBar.isVisible = true
                    loginLL.isVisible = true
                    loginButton.isEnabled = false
                    logoutLL.isVisible = false
                    if(dispWebView){ webView.isVisible = true }
                }
                else -> {}
            }
        }

        super.onStart()
    }

    fun logout(){
        appViewModel.sessionId = null
        appViewModel.userId.value = ""
        try{
            (webView.webViewClient as BasicAuthWebViewClient).removeAllCookies()
        }catch (e: Exception){
            e.printStackTrace()
        }
        webView.clearCache(true)
        loginState.value = LoginState.LoggedOut
    }

    fun login(user: String, pass: String){
        loginState.value = LoginState.InAuth

        // javascript for auto login
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = BasicAuthWebViewClient(
            user,
            pass,
            webView,
            onPageFetched = { cookie ->
                // getSessionID
                if(cookie.getOrNull(1)?.matches(Regex(".*SESSION=.*")) == true){
                    appViewModel.sessionId = cookie[1].split(Regex(".*SESSION="))[1]
                }
                Log.d("session_id", appViewModel.sessionId ?: "null")

                // skip 2-step verification confirmation script
                webView.evaluateJavascript("javascript:document.getElementById('$TWO_STEP_VERIFICATION_LOGIN_BUTTON_ID').click();"){}

                // login successful
                if(appViewModel.sessionId != null){
                    appViewModel.userId.value = user

                    view?.findNavController()?.navigate(R.id.action_loginFragment_to_nav_home)

                    loginState.value = LoginState.LoggedIn
                }
            },
            onCookieFetchError = {
                try {
                    Toast.makeText(requireContext(), "ログイン失敗", Toast.LENGTH_SHORT).show()
                }catch (e: Exception){
                    e.printStackTrace()
                }finally {
                    loginState.value = LoginState.LoggedOut
                }
            }
        )

        // reset sessions
        (webView.webViewClient as BasicAuthWebViewClient).removeAllCookies()

        // access to login page
        webView.loadUrl(SCOMB_LOGIN_PAGE_URL)
    }
}