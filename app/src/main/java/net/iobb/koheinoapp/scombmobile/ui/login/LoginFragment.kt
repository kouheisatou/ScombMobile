package net.iobb.koheinoapp.scombmobile.ui.login

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.room.Room
import kotlinx.android.synthetic.main.fragment_login.*
import net.iobb.koheinoapp.scombmobile.*
import java.io.*

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
        loggedIn, loggedOut, inAuth
    }

    private val loginState = MutableLiveData(LoginState.loggedOut)


    override fun onStart() {

        val db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java,
            "ScombMobileDB"
        ).allowMainThreadQueries().build()

        // recover saved id and pass from db
        idTextView.setText(db.userDao().loadAllUser().getOrNull(0)?.username ?: "")
        passwordTextView.setText(db.userDao().loadAllUser().getOrNull(0)?.password ?: "")
        if(idTextView.text.toString() != "" && passwordTextView.text.toString() != ""){
            savePassCheckBox.isChecked = true
        }

        loginButton.setOnClickListener {
            if(idTextView.text.toString() != "" && passwordTextView.text.toString() != ""){
                login(idTextView.text.toString(), passwordTextView.text.toString())
            }
        }
        logoutButton.setOnClickListener {
            logout()
        }
        savePassCheckBox.setOnClickListener {
            // if checked, add user to db
            if((it as CheckBox).isChecked){
                db.userDao().insertUser(User(idTextView.text.toString(), passwordTextView.text.toString()))
                for(item in db.userDao().loadAllUser()){
                    Log.d("UserDB", item.toString())
                }
            }else{
                db.userDao().removeAllUser()
            }
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
        appViewModel.sessionId = null
        (webView.webViewClient as BasicAuthWebViewClient).removeAllCookies()
        loginState.value = LoginState.loggedOut
        webView.clearCache(true)
    }

    fun login(user: String, pass: String){
        loginState.value = LoginState.inAuth
        webView.webViewClient = BasicAuthWebViewClient(user, pass){
            if(it.getOrNull(1)?.matches(Regex(".*SESSION=.*")) == true){
                appViewModel.sessionId = it[1].split(Regex(".*SESSION="))[1]
            }
            Log.d("cookie", appViewModel.sessionId ?: "null")

            // login successful
            if(appViewModel.sessionId != null){
                loginState.value = LoginState.loggedIn

                view?.findNavController()?.navigate(R.id.action_loginFragment_to_nav_home)
            }
        }
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(SCOMB_LOGIN_PAGE_URL)

    }

}