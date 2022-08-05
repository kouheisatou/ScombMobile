package net.iobb.koheinoapp.scombmobile.ui.login

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import kotlinx.android.synthetic.main.fragment_login.*
import net.iobb.koheinoapp.scombmobile.*
import net.iobb.koheinoapp.scombmobile.common.*
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


    override fun onStart() {

        val db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java,
            "ScombMobileDB"
        ).allowMainThreadQueries().build()

        // recover saved id and pass from db
        idTextView.setText(db.userDao().getUser()?.username ?: "")
        passwordTextView.setText(db.userDao().getUser()?.password ?: "")

        // auto login
        if((db.settingDao().getSetting("enabled_auto_login")?.settingValue ?: "true") == "true"){
            login(idTextView.text.toString(), passwordTextView.text.toString())
        }

        loginButton.setOnClickListener {
            if(idTextView.text.toString() != "" && passwordTextView.text.toString() != ""){
                login(idTextView.text.toString(), passwordTextView.text.toString())
            }
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
        webView.loginState.observe(viewLifecycleOwner){
            when(it){
                LoginState.LoggedOut -> {
                    progressBar.isVisible = false
                    loginButton.isEnabled = true
                    if(dispWebView){ webView.isVisible = false }
                }
                LoginState.LoggedIn -> {
                    progressBar.isVisible = false
                    loginButton.isEnabled = false
                    if(dispWebView){ webView.isVisible = false }

                    appViewModel.sessionId = webView.sessionId ?: return@observe
                    appViewModel.userId.value = webView.loginUser ?: return@observe

                    findNavController().popBackStack()
                }
                LoginState.InAuth -> {
                    progressBar.isVisible = true
                    loginButton.isEnabled = false
                    if(dispWebView){ webView.isVisible = true }
                }
                else -> {}
            }
        }

        super.onStart()
    }

    private fun logout(){
        appViewModel.sessionId = null
        appViewModel.userId.value = ""
        webView.logout()
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun login(user: String, pass: String){
        webView.addJavascript("javascript:document.getElementById('$TWO_STEP_VERIFICATION_LOGIN_BUTTON_ID').click();")
        webView.login(SCOMB_LOGIN_PAGE_URL, user, pass)
    }
}