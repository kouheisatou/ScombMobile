package net.iobb.koheinoapp.scombmobile.ui.classdetail

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gargoylesoftware.htmlunit.BrowserVersion
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.util.Cookie
import kotlinx.android.synthetic.main.fragment_class_detail.*
import kotlinx.android.synthetic.main.fragment_class_detail.progressBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.iobb.koheinoapp.scombmobile.AppViewModel
import net.iobb.koheinoapp.scombmobile.CLASS_PAGE_URL
import net.iobb.koheinoapp.scombmobile.R
import net.iobb.koheinoapp.scombmobile.SESSION_COOKIE_ID
import net.iobb.koheinoapp.scombmobile.scraping.BackgroundWebView
import net.iobb.koheinoapp.scombmobile.scraping.Page
import org.jsoup.Jsoup

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
        teacherTextView.setOnClickListener {
            // todo copy to clipboard email address
        }

        viewModel.page.networkState.observe(viewLifecycleOwner){
            when(it){
                // after fetched page
                Page.NetworkState.Finished -> {
                    progressBar.isVisible = false
                    linearLayout.isVisible = true
                    constructViews()
                }
                // initialized
                Page.NetworkState.Initialized -> {
                    progressBar.isVisible = false
                    linearLayout.isVisible = true
                }
                // fetching
                Page.NetworkState.Loading -> {
                    progressBar.isVisible = true
                    linearLayout.isVisible = false
                }
                // permittion error
                Page.NetworkState.NotPermitted -> {
                    this.findNavController().navigate(R.id.loginFragment)
                }
            }
        }

        if(appViewModel.sessionId == null){
            view?.findNavController()?.navigate(R.id.loginFragment)
        }else{
//            viewModel.fetch()
            viewModel.fetchDynamicPage()

//            val backgroundWebView = BackgroundWebView(requireContext(), CLASS_PAGE_URL + args.classId)
//            teacherTextView.setOnClickListener {
//                backgroundWebView.exportHtml {
//                    val s = it.replace("\n", "").replace("\t", "")
//                    val doc = Jsoup.parse(s)
//                    Log.d("exported_html", doc.html())
//                }
//            }
//            linearLayout.addView(backgroundWebView)
        }
        super.onStart()
    }

    fun constructViews(){
        classTitleTextView.text = viewModel.className
        classOverviewTextView.text = viewModel.classOverview
        teacherTextView.text = viewModel.teacher
    }
}