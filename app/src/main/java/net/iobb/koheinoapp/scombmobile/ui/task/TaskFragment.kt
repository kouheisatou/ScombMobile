package net.iobb.koheinoapp.scombmobile.ui.task

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import kotlinx.android.synthetic.main.fragment_item_list.*
import kotlinx.android.synthetic.main.fragment_item_list.view.*
import net.iobb.koheinoapp.scombmobile.R
import net.iobb.koheinoapp.scombmobile.common.AppViewModel
import net.iobb.koheinoapp.scombmobile.common.NetworkState

class TaskFragment : Fragment() {

    private val appViewModel: AppViewModel by activityViewModels()
    private lateinit var viewModel: TaskViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        viewModel.appViewModel = appViewModel
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)

        viewModel.page.networkState.observe(viewLifecycleOwner) {
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

                    viewModel.constructTasks()

                    // construct list view
                    if (view.list is RecyclerView) {
                        val dividerItemDecoration = DividerItemDecoration(requireContext(), LinearLayoutManager(requireContext()).orientation)
                        view.list.addItemDecoration(dividerItemDecoration)
                        with(view.list) {
                            layoutManager = LinearLayoutManager(context)
                            adapter = MyTaskRecyclerViewAdapter(viewModel.tasks)
                        }
                    }
                }
                NetworkState.NotPermitted -> {
                    findNavController().navigate(R.id.loginFragment)
                }
            }
        }
        return view
    }
}