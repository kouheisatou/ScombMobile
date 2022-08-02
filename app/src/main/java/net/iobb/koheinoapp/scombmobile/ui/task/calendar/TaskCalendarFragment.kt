package net.iobb.koheinoapp.scombmobile.ui.task.calendar

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.CompactCalendarView.CompactCalendarViewListener
import com.github.sundeepk.compactcalendarview.domain.Event
import kotlinx.android.synthetic.main.fragment_calendar.*
import kotlinx.android.synthetic.main.fragment_calendar.view.*
import net.iobb.koheinoapp.scombmobile.R
import net.iobb.koheinoapp.scombmobile.common.AppViewModel
import net.iobb.koheinoapp.scombmobile.common.NetworkState
import net.iobb.koheinoapp.scombmobile.ui.task.TaskViewModel
import net.iobb.koheinoapp.scombmobile.ui.task.list.TaskRecyclerViewAdapter
import java.util.*

class TaskCalendarFragment : Fragment() {

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
                val event = Event(Color.parseColor("#FF0000"), it.deadLineTime)
                root.calendarView.addEvent(event)
            }
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
                Log.d("selected_date", dateClicked.toString())
            }
            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                val currentCalMonth = Calendar.getInstance().apply { timeInMillis = firstDayOfNewMonth.time }
                root.yearAndMonthTextView.text = "${currentCalMonth.get(Calendar.YEAR)}年 ${currentCalMonth.get(Calendar.MONTH)+1}月"
                taskViewModel.getTasksOf(firstDayOfNewMonth.time)
                Log.d("selected_date", firstDayOfNewMonth.toString())
            }
        })
        val today = Calendar.getInstance()
        root.yearAndMonthTextView.text = "${today.get(Calendar.YEAR)}年 ${today.get(Calendar.MONTH+1)}月"
        root.nextMonthBtn.setOnClickListener {
            taskViewModel.tasksOfTheDate.value = mutableListOf()
            root.calendarView.scrollRight()
        }
        root.prevMonthBtn.setOnClickListener {
            taskViewModel.tasksOfTheDate.value = mutableListOf()
            root.calendarView.scrollLeft()
        }


        return root
    }


}