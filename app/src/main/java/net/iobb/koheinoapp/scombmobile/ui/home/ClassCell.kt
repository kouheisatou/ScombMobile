package net.iobb.koheinoapp.scombmobile.ui.home

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
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

    lateinit var view: View
    lateinit var defaultBackground: Drawable

    fun genView(context: Context, attachTo: ViewGroup): View{
        view = View.inflate(context, R.layout.class_cell, attachTo)
        defaultBackground = view.classNameBtn.background

        view.classNameBtn.text = name

        return view
    }
}