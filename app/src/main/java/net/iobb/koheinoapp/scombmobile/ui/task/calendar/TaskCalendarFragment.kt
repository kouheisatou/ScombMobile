package net.iobb.koheinoapp.scombmobile.ui.task.calendar

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.iobb.koheinoapp.scombmobile.R

class TaskCalendarFragment : Fragment() {

    companion object {
        fun newInstance() = TaskCalendarFragment()
    }

    private lateinit var viewModel: TaskCalendarViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TaskCalendarViewModel::class.java)
        // TODO: Use the ViewModel
    }

}