package net.iobb.koheinoapp.scombmobile.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_home.*
import net.iobb.koheinoapp.scombmobile.databinding.FragmentHomeBinding
import net.iobb.koheinoapp.scombmobile.BasicAuthWebViewClient
import net.iobb.koheinoapp.scombmobile.Values.SCOMB_LOGIN_PAGE_URL
import net.iobb.koheinoapp.scombmobile.Values.sessionId
import java.net.CookieManager

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val loginState = MutableLiveData(false)

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }


    override fun onStart() {
        loginButton.setOnClickListener {
            if(idTextView.text.toString() != "" || passwordTextView.text.toString() != ""){
                login(idTextView.text.toString(), passwordTextView.text.toString())
            }else{
                Snackbar.make(it, "empty password or username", Snackbar.LENGTH_SHORT)
            }
        }
        logoutButton.setOnClickListener {
            logout()
        }
        loginState.observe(viewLifecycleOwner){
            if(it){
                loginLL.isVisible = false
                logoutLL.isVisible = true
            }else{
                loginLL.isVisible = true
                logoutLL.isVisible = false
            }
        }
        super.onStart()
    }

    fun logout(){
        sessionId = null
        (webView.webViewClient as BasicAuthWebViewClient).removeAllCookies()
        loginState.value = false
    }

    fun login(user: String, pass: String){
        webView.webViewClient = BasicAuthWebViewClient(user, pass){
            if(it.getOrNull(1)?.matches(Regex(".*SESSION=.*")) == true){
                sessionId = it[1].split(Regex(".*SESSION="))[1]
            }
            Log.d("cookie", sessionId ?: "null")

            if(sessionId != null){
                loginState.value = true
            }
        }
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(SCOMB_LOGIN_PAGE_URL)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}