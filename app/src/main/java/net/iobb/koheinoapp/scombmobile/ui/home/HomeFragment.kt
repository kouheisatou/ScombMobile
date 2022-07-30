package net.iobb.koheinoapp.scombmobile.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TableRow
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_home.*
import net.iobb.koheinoapp.scombmobile.*
import net.iobb.koheinoapp.scombmobile.common.AppViewModel
import net.iobb.koheinoapp.scombmobile.common.NetworkState
import net.iobb.koheinoapp.scombmobile.databinding.FragmentHomeBinding
import net.iobb.koheinoapp.scombmobile.common.Page


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
        viewModel.page.networkState.observe(viewLifecycleOwner){
            when(it){
                NetworkState.Finished -> {
                    progressBar.isVisible = false
                    timeTable.isVisible = true
                }
                NetworkState.Initialized -> {
                    progressBar.isVisible = false
                    timeTable.isVisible = true
                }
                NetworkState.Loading -> {
                    progressBar.isVisible = true
                    timeTable.isVisible = false
                }
                NetworkState.NotPermitted -> {
                    this.findNavController().navigate(R.id.loginFragment)
                }
            }
        }
        viewModel.timeTable.observe(viewLifecycleOwner){
            for(row in it.withIndex()){
                val tableRow = timeTable[row.index + 1] as TableRow
                for(cell in row.value.withIndex()){
                    val cellView = (tableRow[cell.index + 1] as LinearLayout)
                    cell.value?.genView(requireContext(), cellView)
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
}