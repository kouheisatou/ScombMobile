package net.iobb.koheinoapp.scombmobile.ui.webscomb

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_class_detail.*
import kotlinx.android.synthetic.main.fragment_class_detail.progressBar
import net.iobb.koheinoapp.scombmobile.MainActivity
import net.iobb.koheinoapp.scombmobile.R
import net.iobb.koheinoapp.scombmobile.common.*

class SinglePageWebScombFragment : Fragment() {

    private lateinit var viewModel: SinglePageWebScombViewModel
    private val appViewModel: AppViewModel by activityViewModels()
    private val args: SinglePageWebScombFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_class_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true);
        viewModel = ViewModelProvider(this, SinglePageWebScombViewModel.Factory(args.url)).get(
            SinglePageWebScombViewModel::class.java)
        viewModel.appViewModel = appViewModel
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.class_detail_option_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.openDefaultBrowser -> {
                val uri = Uri.parse(viewModel.url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onStart() {

        if(appViewModel.sessionId == null){
            view?.findNavController()?.navigate(R.id.loginFragment)
            return
        }else{
            webView.loadUrl(
                args.url,
                onScriptCallback = {
                    ((activity ?: return@loadUrl) as MainActivity).binding.appBarMain.toolbar.title = it.replace("\"", "")
                },
                "document.getElementById('$HEADER_ELEMENT_ID').remove();",
                "document.getElementById('$FOOTER_ELEMENT_ID').remove();",
                "document.title;"
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