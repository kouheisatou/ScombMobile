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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import kotlinx.android.synthetic.main.fragment_item_list.*
import kotlinx.android.synthetic.main.fragment_item_list.view.*
import net.iobb.koheinoapp.scombmobile.R
import net.iobb.koheinoapp.scombmobile.common.AppViewModel
import net.iobb.koheinoapp.scombmobile.common.NetworkState
import net.iobb.koheinoapp.scombmobile.common.ScombMobileNotification
import net.iobb.koheinoapp.scombmobile.ui.task.AddNewTaskDialogFragment
import net.iobb.koheinoapp.scombmobile.ui.task.Task
import net.iobb.koheinoapp.scombmobile.ui.task.TaskFragment
import net.iobb.koheinoapp.scombmobile.ui.task.TaskViewModel

class TaskListFragment : Fragment(), TaskFragment {

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
        val dividerItemDecoration = DividerItemDecoration(requireContext(), LinearLayoutManager(requireContext()).orientation)
        view.list.addItemDecoration(dividerItemDecoration)

        view.swipeLayout.setOnRefreshListener {
            refresh()
        }
        taskViewModel.page.networkState.observe(viewLifecycleOwner) {
            Log.d("network_status", taskViewModel.page.networkState.value.toString())
            when(it) {
                NetworkState.Initialized -> {
                    swipeLayout.isRefreshing = false
                    list.isVisible = false
                    taskViewModel.fetchTasks(requireContext())
                }
                NetworkState.Loading -> {
                    swipeLayout.isRefreshing = true
                    list.isVisible = false
                }
                NetworkState.Finished -> {
                    list.isVisible = true
                    swipeLayout.isRefreshing = false
                }
                NetworkState.NotPermitted -> {
                    if(appViewModel.sessionId == null){
                        findNavController().navigate(R.id.nav_loginFragment)
                    }
                    // when backed from login fragment
                    else{
                        taskViewModel.page.networkState.value = NetworkState.Initialized
                    }
                }
            }
        }
        taskViewModel.tasks.observe(viewLifecycleOwner){
            // construct list view
            if (list is RecyclerView) {
                with(list) {
                    layoutManager = LinearLayoutManager(context)
                    adapter = TaskRecyclerViewAdapter(taskViewModel.tasks.value?.toMutableList() ?: return@with, this@TaskListFragment)
                }
            }
        }
        view.addTaskBtn.setOnClickListener {
            AddNewTaskDialogFragment.create().show(childFragmentManager, "add_new_task_dialog")
        }
        return view
    }

    override fun refresh() {
        taskViewModel.page.networkState.value = NetworkState.Initialized
        swipeLayout.isRefreshing = true
    }

    override fun addTask(newTask: Task) {
        taskViewModel.addMyTask(requireContext(), newTask)
        refresh()
    }

    override fun removeTask(removeTarget: Task) {
        taskViewModel.removeMyTask(requireContext(), removeTarget)
        ScombMobileNotification.cancelTaskAlerm(requireContext(), removeTarget)
        refresh()
    }
}