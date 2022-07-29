package net.iobb.koheinoapp.scombmobile.ui.classdetail

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_class_detail.*
import kotlinx.android.synthetic.main.fragment_class_detail.progressBar
import net.iobb.koheinoapp.scombmobile.AppViewModel
import net.iobb.koheinoapp.scombmobile.R
import net.iobb.koheinoapp.scombmobile.scraping.Page

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
            viewModel.fetch()
        }
        super.onStart()
    }

    fun constructViews(){
        classTitleTextView.text = viewModel.className
        classOverviewTextView.text = viewModel.classOverview
        teacherTextView.text = viewModel.teacher
    }
}