package net.iobb.koheinoapp.scombmobile.ui.classdetail

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_class_detail.*
import kotlinx.android.synthetic.main.fragment_class_detail.progressBar
import net.iobb.koheinoapp.scombmobile.R
import net.iobb.koheinoapp.scombmobile.common.*

class ClassDetailFragment : Fragment() {

    private lateinit var viewModel: ClassDetailViewModel
    private val appViewModel: AppViewModel by activityViewModels()
    private val args: ClassDetailFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_class_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, ClassDetailViewModel.Factory(args.classId)).get(ClassDetailViewModel::class.java)
        viewModel.appViewModel = appViewModel
    }


    @SuppressLint("SetJavaScriptEnabled")
    override fun onStart() {

        if(appViewModel.sessionId == null){
            view?.findNavController()?.navigate(R.id.loginFragment)
            return
        }else{
            webView.loadUrl(
                "$CLASS_PAGE_URL${args.classId}",
                null,
                "document.getElementById('$HEADER_ELEMENT_ID').remove();",
                "document.getElementById('$FOOTER_ELEMENT_ID').remove();"
            )
        }

        webView.networkState.observe(viewLifecycleOwner){
            when(it){
                NetworkState.Finished -> {
                    progressBar.isVisible = false
                    webView.isVisible = true
                }
                NetworkState.NotPermitted -> {
                    view?.findNavController()?.navigate(R.id.loginFragment)
                }
                else -> {
                    progressBar.isVisible = true
                    webView.isVisible = false
                }
            }
        }

        super.onStart()

    }

}