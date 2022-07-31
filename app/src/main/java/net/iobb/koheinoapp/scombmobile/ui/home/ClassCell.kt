package net.iobb.koheinoapp.scombmobile.ui.home

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.view.ViewGroup
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
    val classId: String,
    val name: String,
    val teachers: String,
    val room: String,
    val dayOfWeek: Int,
    val period: Int,
) {

    var customColorInt: Int? = null
    var createdDate: Long
    @PrimaryKey
    var id: String

    @Ignore
    lateinit var view: View
    @Ignore
    lateinit var context: Context

    init {
        id = "$dayOfWeek,$period"
        createdDate = Calendar.getInstance().timeInMillis
    }


    fun genView(context: Context, attachTo: ViewGroup): View{
        this.context = attachTo.context
        view = View.inflate(context, R.layout.class_cell, attachTo)
        view.classNameBtn.text = name

        if (customColorInt != null) {
            setCustomColor(customColorInt!!)
        }

        return view
    }

    fun setCustomColor(color: Int?){

        customColorInt = color

        val drawable = view.classNameBtn.background
        if(color == null){
            drawable.clearColorFilter()
        }else{
            drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
        }
        view.classNameBtn.background = drawable

        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ScombMobileDB"
        ).allowMainThreadQueries().build()
        db.classCellDao().insertClassCell(this)
        Log.d("set_color_to_cell", "$name, $customColorInt")
    }

    override fun toString(): String {
        return "id=$classId, name=$name, teachers=$teachers, room=$room"
    }
}