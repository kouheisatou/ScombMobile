package net.iobb.koheinoapp.scombmobile.ui.task.list

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.fragment_item_list.*
import kotlinx.android.synthetic.main.fragment_item_list.view.*
import net.iobb.koheinoapp.scombmobile.R
import net.iobb.koheinoapp.scombmobile.common.AppViewModel
import net.iobb.koheinoapp.scombmobile.common.NetworkState

class TaskListFragment : Fragment() {

    private val appViewModel: AppViewModel by activityViewModels()
    private lateinit var viewModel: TaskListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[TaskListViewModel::class.java]
        viewModel.appViewModel = appViewModel
        super.onCreate(savedInstanceState)
    }

    override fun onPause() {
        viewModel.page.reset()
        super.onPause()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)

        viewModel.page.reset()

        view.swipeLayout.setOnRefreshListener {
            viewModel.tasks.value = mutableListOf()
            viewModel.page.reset()
        }
        val dividerItemDecoration = DividerItemDecoration(requireContext(), LinearLayoutManager(requireContext()).orientation)
        view.list.addItemDecoration(dividerItemDecoration)

        viewModel.page.networkState.observe(viewLifecycleOwner) {
            Log.d("network_status", viewModel.page.networkState.value.toString())
            when(it) {
                NetworkState.Initialized -> {
                    progressBar.isVisible = true
                    list.isVisible = false
                    viewModel.fetchTasks(requireContext())
                }
                NetworkState.Loading -> {
                    progressBar.isVisible = true
                    list.isVisible = false
                }
                NetworkState.Finished -> {
                    progressBar.isVisible = false
                    list.isVisible = true
                    swipeLayout.isRefreshing = false
                }
                NetworkState.NotPermitted -> {
                    findNavController().navigate(R.id.loginFragment)
                }
            }
        }
        viewModel.tasks.observe(viewLifecycleOwner){
            if(it.isNotEmpty()){

                // construct list view
                if (list is RecyclerView) {
                    with(list) {
                        layoutManager = LinearLayoutManager(context)
                        adapter = TaskRecyclerViewAdapter(viewModel.tasks.value?.toList() ?: return@with)
                    }
                }
            }
        }
        return view
    }
}