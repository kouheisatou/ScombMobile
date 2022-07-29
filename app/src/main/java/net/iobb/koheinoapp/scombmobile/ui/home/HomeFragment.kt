package net.iobb.koheinoapp.scombmobile.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.launch
import net.iobb.koheinoapp.scombmobile.*
import net.iobb.koheinoapp.scombmobile.databinding.FragmentHomeBinding
import net.iobb.koheinoapp.scombmobile.scraping.Page


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val appViewModel: AppViewModel by activityViewModels()
    private lateinit var viewModel: HomeViewModel

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        viewModel.appViewModel = appViewModel

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        Log.d("session_id", appViewModel.sessionId?: "null")
        if(appViewModel.sessionId == null){
            view?.findNavController()?.navigate(R.id.loginFragment)
        }else{
            viewModel.fetch()
        }

        viewModel.text.observe(viewLifecycleOwner){
            textView.text = it
        }
        viewModel.page.networkState.observe(viewLifecycleOwner){
            when(it){
                Page.NetworkState.Finished -> {
                    progressBar.isVisible = false
                    textView.isVisible = true
                }
                Page.NetworkState.Loading -> {
                    progressBar.isVisible = true
                    textView.isVisible = false
                }
            }
        }

        super.onStart()
    }
}