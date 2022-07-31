package net.iobb.koheinoapp.scombmobile.ui.task

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import net.iobb.koheinoapp.scombmobile.common.SCOMBZ_DOMAIN
import net.iobb.koheinoapp.scombmobile.databinding.FragmentItemBinding
import java.util.*

class MyTaskRecyclerViewAdapter(
    private val tasks: List<Task>
) : RecyclerView.Adapter<MyTaskRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = FragmentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(view)

        view.linearLayout.setOnClickListener {
            val task = tasks[holder.layoutPosition]
            val action = TaskFragmentDirections.actionTaskFragmentToClassDetailFragment("$SCOMBZ_DOMAIN${task.url}")
            view.linearLayout.findNavController().navigate(action)
        }

        return holder

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = tasks[position]
        println(item)
        holder.titleTextView.text = item.title
        holder.deadlineTextView.text = Date(item.deadLineTime).toString()
        holder.classNameTextView.text = item.className
    }

    override fun getItemCount(): Int = tasks.size

    inner class ViewHolder(binding: FragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val deadlineTextView: TextView = binding.deadlineTextView
        val classNameTextView: TextView = binding.classNameTextView
        val titleTextView: TextView = binding.titleTextView
    }

}