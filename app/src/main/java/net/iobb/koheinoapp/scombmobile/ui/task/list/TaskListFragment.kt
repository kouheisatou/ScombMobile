package net.iobb.koheinoapp.scombmobile.ui.task.list

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import kotlinx.android.synthetic.main.fragment_item_list.*
import kotlinx.android.synthetic.main.fragment_item_list.view.*
import net.iobb.koheinoapp.scombmobile.R
import net.iobb.koheinoapp.scombmobile.common.AppViewModel
import net.iobb.koheinoapp.scombmobile.common.NetworkState
import net.iobb.koheinoapp.scombmobile.ui.task.TaskViewModel

class TaskListFragment : Fragment() {

    private val appViewModel: AppViewModel by activityViewModels()
    private val taskViewModel: TaskViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        taskViewModel.appViewModel = appViewModel
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)

        view.swipeLayout.setOnRefreshListener {
            taskViewModel.tasks.value = mutableListOf()
            taskViewModel.page.reset()
        }
        val dividerItemDecoration = DividerItemDecoration(requireContext(), LinearLayoutManager(requireContext()).orientation)
        view.list.addItemDecoration(dividerItemDecoration)

        taskViewModel.page.networkState.observe(viewLifecycleOwner) {
            when(it) {
                NetworkState.Initialized -> {
                    progressBar.isVisible = true
                    list.isVisible = false
                    taskViewModel.fetchTasks(requireContext())
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
                    taskViewModel.page.reset()
                    findNavController().navigate(R.id.loginFragment)
                }
            }
        }
        taskViewModel.tasks.observe(viewLifecycleOwner){
            if(it.isNotEmpty()){

                // construct list view
                if (list is RecyclerView) {
                    with(list) {
                        layoutManager = LinearLayoutManager(context)
                        adapter = TaskRecyclerViewAdapter(taskViewModel.tasks.value?.toList() ?: return@with)
                    }
                }
            }
        }
        return view
    }
}