package net.iobb.koheinoapp.scombmobile.ui.task.calendar

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.sundeepk.compactcalendarview.CompactCalendarView.CompactCalendarViewListener
import com.github.sundeepk.compactcalendarview.domain.Event
import kotlinx.android.synthetic.main.fragment_calendar.*
import kotlinx.android.synthetic.main.fragment_calendar.view.*
import net.iobb.koheinoapp.scombmobile.R
import net.iobb.koheinoapp.scombmobile.common.AppViewModel
import net.iobb.koheinoapp.scombmobile.common.NetworkState
import net.iobb.koheinoapp.scombmobile.ui.task.AddNewTaskDialogFragment
import net.iobb.koheinoapp.scombmobile.ui.task.Task
import net.iobb.koheinoapp.scombmobile.ui.task.TaskFragment
import net.iobb.koheinoapp.scombmobile.ui.task.TaskViewModel
import net.iobb.koheinoapp.scombmobile.ui.task.list.TaskRecyclerViewAdapter
import java.util.*

class TaskCalendarFragment : Fragment(), TaskFragment {

    companion object {
        fun newInstance() = TaskCalendarFragment()
    }

    private val taskViewModel: TaskViewModel by activityViewModels()
    private val appViewModel: AppViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        taskViewModel.appViewModel = appViewModel
        val root = inflater.inflate(R.layout.fragment_calendar, container, false)
        val dividerItemDecoration = DividerItemDecoration(requireContext(), LinearLayoutManager(requireContext()).orientation)
        root.taskList.addItemDecoration(dividerItemDecoration)
        setHasOptionsMenu(true)

        val today = Calendar.getInstance()
        taskViewModel.page.networkState.observe(viewLifecycleOwner) {
            Log.d("network_status", taskViewModel.page.networkState.value.toString())
            when(it) {
                NetworkState.Initialized -> {
                    root.calendarContainer.isVisible = false
                    root.progressBar.isVisible = false
                    taskViewModel.fetchTasks(requireContext())
                }
                NetworkState.Loading -> {
                    root.calendarContainer.isVisible = false
                    root.progressBar.isVisible = true
                }
                NetworkState.Finished -> {
                    root.calendarContainer.isVisible = true
                    root.progressBar.isVisible = false
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
            taskViewModel.tasks.value?.forEach {
                val event = if(it.customColor != null){
                    Event(it.customColor!!, it.deadLineTime)
                }else{
                    Event(R.color.black, it.deadLineTime)
                }
                root.calendarView.addEvent(event)
            }
            taskViewModel.getTasksOf(today.timeInMillis)
            root.calendarView.setCurrentDate(today.time)
        }
        taskViewModel.tasksOfTheDate.observe(viewLifecycleOwner){
            // construct list view
            if (root.taskList is RecyclerView) {
                with(root.taskList) {
                    layoutManager = LinearLayoutManager(context)
                    adapter = TaskRecyclerViewAdapter(taskViewModel.tasksOfTheDate.value?.toList() ?: return@with)
                }
            }
        }

        root.calendarView.setListener(object : CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {
                taskViewModel.getTasksOf(dateClicked.time)
            }
            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                val currentCalMonth = Calendar.getInstance().apply { timeInMillis = firstDayOfNewMonth.time }
                root.yearAndMonthTextView.text = "${currentCalMonth.get(Calendar.YEAR)}年 ${currentCalMonth.get(Calendar.MONTH)+1}月"
                taskViewModel.getTasksOf(firstDayOfNewMonth.time)
            }
        })
        root.yearAndMonthTextView.text = "${today.get(Calendar.YEAR)}年 ${today.get(Calendar.MONTH)+1}月"
        root.nextMonthBtn.setOnClickListener {
            taskViewModel.tasksOfTheDate.value = mutableListOf()
            root.calendarView.scrollRight()
        }
        root.prevMonthBtn.setOnClickListener {
            taskViewModel.tasksOfTheDate.value = mutableListOf()
            root.calendarView.scrollLeft()
        }
        root.addTaskBtn.setOnClickListener {
            AddNewTaskDialogFragment.create().show(childFragmentManager, "add_new_task_dialog")
        }

        return root
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.calendar_menu, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.reloadBtn -> {
                if(taskViewModel.page.networkState.value == NetworkState.Finished){
                    refresh()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun refresh() {
        taskViewModel.page.networkState.value = NetworkState.Initialized
        calendarView.removeAllEvents()
    }

    override fun addTask(newTask: Task) {
        taskViewModel.addMyTask(requireContext(), newTask)
    }
}