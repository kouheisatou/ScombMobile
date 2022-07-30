package net.iobb.koheinoapp.scombmobile.ui.home

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.DrawableCompat
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.Room
import kotlinx.android.synthetic.main.class_cell.view.*
import net.iobb.koheinoapp.scombmobile.R
import net.iobb.koheinoapp.scombmobile.common.AppDatabase
import java.util.*

@Entity
class ClassCell(
    @PrimaryKey
    val id: String,
    val name: String,
    val teachers: String,
    val room: String,
    val dayOfWeek: Int,
    val period: Int,
) {

    var customColorInt: Int? = null
    var createdDate: Long

    init {
        createdDate = Calendar.getInstance().timeInMillis
    }

    @Ignore
    lateinit var view: View
    @Ignore
    private lateinit var defaultBackground: Drawable
    @Ignore
    lateinit var context: Context

    fun genView(context: Context, attachTo: ViewGroup): View{
        this.context = attachTo.context
        view = View.inflate(context, R.layout.class_cell, attachTo)
        defaultBackground = view.classNameBtn.background
        if (customColorInt != null) {
            setCustomColor(customColorInt!!)
        }

        view.classNameBtn.text = name

        return view
    }

    fun setCustomColor(color: Int){
        val button = view.classNameBtn
        val drawable = button.background.constantState?.newDrawable() ?: return
        DrawableCompat.setTint(drawable, color)
        customColorInt = color
        button.background = drawable

        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ScombMobileDB"
        ).allowMainThreadQueries().build()
        db.classCellDao().insertClassCell(this)
    }

    fun resetCustomColor(){
        customColorInt = null
        view.classNameBtn.background = defaultBackground

        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ScombMobileDB"
        ).allowMainThreadQueries().build()
        db.classCellDao().insertClassCell(this)
    }

    override fun toString(): String {
        return "id=$id, name=$name, teachers=$teachers, room=$room"
    }
}