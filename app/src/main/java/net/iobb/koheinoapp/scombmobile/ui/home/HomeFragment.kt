package net.iobb.koheinoapp.scombmobile.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.webView
import kotlinx.android.synthetic.main.fragment_login.*
import net.iobb.koheinoapp.scombmobile.*
import net.iobb.koheinoapp.scombmobile.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val appViewModel: AppViewModel by activityViewModels()

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

    override fun onStart() {
        if(appViewModel.sessionId == null){
            view?.findNavController()?.navigate(R.id.action_nav_home_to_loginFragment)
        }else{
            webView.webViewClient = BasicAuthWebViewClient(
                "",
                "" ,
                webView,
                onPageFetched = {
                    // on logged out
                    if(webView.url == SCOMB_LOGGED_OUT_PAGE_URL){
                        webView.clearCache(true)
                        appViewModel.userId.value = ""
                        appViewModel.sessionId = null
                        view?.findNavController()?.navigate(R.id.action_nav_home_to_loginFragment)
                    }
                },
                onHtmlSrcFetched = {},
                onCookieFetchError = {})
            webView.settings.javaScriptEnabled = true
            webView.loadUrl(SCOMB_HOME_URL)
        }
        super.onStart()
    }
}