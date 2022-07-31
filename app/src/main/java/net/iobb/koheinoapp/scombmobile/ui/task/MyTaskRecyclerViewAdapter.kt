package net.iobb.koheinoapp.scombmobile.ui.task

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import net.iobb.koheinoapp.scombmobile.R
import net.iobb.koheinoapp.scombmobile.databinding.FragmentItemBinding
import java.text.SimpleDateFormat
import java.util.*

class MyTaskRecyclerViewAdapter(
    private val values: List<Task>
) : RecyclerView.Adapter<MyTaskRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        println(item)
        holder.titleTextView.text = item.title
        holder.deadlineTextView.text = Date(item.deadLineTime).toString()
        holder.classNameTextView.text = item.className
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val deadlineTextView: TextView = binding.deadlineTextView
        val classNameTextView: TextView = binding.classNameTextView
        val titleTextView: TextView = binding.titleTextView
    }

}