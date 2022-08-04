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

    private var selectedDate = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val now = Calendar.getInstance()

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
                    adapter = TaskRecyclerViewAdapter(taskViewModel.tasksOfTheDate.value?.toMutableList() ?: return@with, this@TaskCalendarFragment)
                }
            }
        }

        root.calendarView.setListener(object : CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {
                selectedDate.timeInMillis = dateClicked.time
                taskViewModel.getTasksOf(dateClicked.time)
            }
            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                selectedDate.timeInMillis = firstDayOfNewMonth.time
                var currentCalMonth = Calendar.getInstance().apply { timeInMillis = firstDayOfNewMonth.time }
                if(now.get(Calendar.MONTH) == currentCalMonth.get(Calendar.MONTH) && now.get(Calendar.YEAR) == currentCalMonth.get(Calendar.YEAR)){
                    currentCalMonth = now
                    root.calendarView.setCurrentDate(now.time)
                }
                root.yearAndMonthTextView.text = "${currentCalMonth.get(Calendar.YEAR)}年 ${currentCalMonth.get(Calendar.MONTH)+1}月"
                taskViewModel.getTasksOf(currentCalMonth.timeInMillis)
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
            AddNewTaskDialogFragment.create(selectedDate.timeInMillis).show(childFragmentManager, "add_new_task_dialog")
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

    override fun removeTask(removeTarget: Task) {
        taskViewModel.removeMyTask(requireContext(), removeTarget)
    }
}