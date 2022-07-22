package net.iobb.koheinoapp.scombmobile.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_home.*
import net.iobb.koheinoapp.scombmobile.databinding.FragmentHomeBinding
import net.iobb.koheinoapp.scombmobile.BasicAuthWebViewClient
import net.iobb.koheinoapp.scombmobile.Values.SCOMB_LOGIN_PAGE_URL
import net.iobb.koheinoapp.scombmobile.Values.sessionId

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

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
        webView.webViewClient = BasicAuthWebViewClient("af20023", "#L4oR3xRhEa7"){
            if(it.getOrNull(1)?.matches(Regex(".*SESSION=.*")) == true){
                sessionId = it[1].split(Regex(".*SESSION="))[1]
            }
            Log.d("cookie", sessionId ?: "null")

            if(sessionId != null){
                webView.isVisible = false
            }
        }
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(SCOMB_LOGIN_PAGE_URL)

        super.onStart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}