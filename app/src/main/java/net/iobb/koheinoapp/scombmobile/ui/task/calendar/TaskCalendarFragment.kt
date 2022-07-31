package net.iobb.koheinoapp.scombmobile.ui.task.calendar

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import net.iobb.koheinoapp.scombmobile.R
import net.iobb.koheinoapp.scombmobile.common.AppViewModel
import net.iobb.koheinoapp.scombmobile.ui.task.TaskViewModel

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
        Toast.makeText(requireContext(), taskViewModel.tasks.value.toString(), Toast.LENGTH_SHORT).show()
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }


}