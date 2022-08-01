package net.iobb.koheinoapp.scombmobile.ui.task

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import kotlinx.android.synthetic.main.fragment_task.*
import kotlinx.android.synthetic.main.fragment_task.view.*
import net.iobb.koheinoapp.scombmobile.R
import net.iobb.koheinoapp.scombmobile.common.AppViewModel
import net.iobb.koheinoapp.scombmobile.ui.task.calendar.TaskCalendarFragment
import net.iobb.koheinoapp.scombmobile.ui.task.list.TaskListFragment

class TaskFragment : Fragment() {

    private val appViewModel: AppViewModel by activityViewModels()
    private val taskViewModel: TaskViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_task, container, false)

        view.bottomNavigationView.setOnNavigationItemSelectedListener {
            val newFragment = when(it.itemId){
                R.id.nav_taskList -> TaskListFragment()
                R.id.nav_taskCalendar -> TaskCalendarFragment()
                else -> return@setOnNavigationItemSelectedListener true
            }
            childFragmentManager.beginTransaction().replace(R.id.fragmentContainer, newFragment).commit()
            true
        }

//        view.bottomNavigationView.setItemOnTouchListener(R.id.nav_taskCalendar, View.OnTouchListener { _, _ ->
//            childFragmentManager.beginTransaction().replace(R.id.fragmentContainer, TaskCalendarFragment()).commit()
//            false
//        })
//        view.bottomNavigationView.setItemOnTouchListener(R.id.nav_taskList, View.OnTouchListener { _, _ ->
//            childFragmentManager.beginTransaction().replace(R.id.fragmentContainer, TaskListFragment()).commit()
//            false
//        })

        return view
    }

}