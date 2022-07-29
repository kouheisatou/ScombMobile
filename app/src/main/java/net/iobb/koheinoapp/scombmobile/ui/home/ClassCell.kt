package net.iobb.koheinoapp.scombmobile.ui.home

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.class_cell.view.*
import net.iobb.koheinoapp.scombmobile.R

class ClassCell(
    val id: String,
    val name: String,
    val teachers: List<String>,
    val room: String
) {
    override fun toString(): String {
        return "id=$id, name=$name, teachers=$teachers, room=$room"
    }

    fun genView(context: Context, attachTo: ViewGroup): View{
        val view = View.inflate(context, R.layout.class_cell, attachTo)

        view.titleTextView.text = name

        view.titleTextView.setOnLongClickListener { v ->
            Snackbar.make(v, "教室 : $room", Snackbar.LENGTH_LONG).show()
            true
        }

        return view
    }
}