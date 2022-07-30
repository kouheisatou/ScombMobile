package net.iobb.koheinoapp.scombmobile.ui.home

import android.content.Context
import android.util.Log
import androidx.annotation.ColorInt
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.iobb.koheinoapp.scombmobile.common.*
import java.lang.StringBuilder
import java.util.*

class HomeViewModel : ViewModel() {

    val page = Page(SCOMB_TIMETABLE_URL)
    lateinit var appViewModel: AppViewModel
    // 時間割二次元配列 (7(1-7限) * 6(月-土の6日))
    val timeTable = MutableLiveData<Array<Array<ClassCell?>>>(
        Array(7){
            Array(6){
                null
            }
        }
    )
    val timetableListenerState = MutableLiveData(HomeFragment.ListenerState.Initialize)
    @ColorInt
    var selectedColor: Int? = null

    suspend fun fetchFromServer(context: Context): Array<ClassCell>{

        // get from web
        page.fetch(appViewModel.sessionId)

        val tableElement = page.document.getElementsByClass(TIMETABLE_ROW_CSS_CLASS_NM)
        val classes = mutableListOf<ClassCell>()

        // extract html
        for(row in tableElement.withIndex()){
            for(cell in row.value.getElementsByClass(TIMETABLE_CELL_CSS_CLASS_NM).withIndex()){
                if(cell.value.allElements.isNotEmpty()){

                    val cellHeader = cell.value.getElementsByClass(
                        TIMETABLE_CELL_HEADER_CSS_CLASS_NM
                    ) ?: continue
                    val id = cellHeader.getOrNull(0)?.attr("id") ?: continue
                    val name = cellHeader.getOrNull(0)?.text() ?: continue

                    val cellDetail = cell.value.getElementsByClass(
                        TIMETABLE_CELL_DETAIL_CSS_CLASS_NM
                    ).getOrNull(0)?.child(0) ?: continue
                    val room = cellDetail.attr(TIMETABLE_ROOM_ATTR_KEY) ?: continue

                    val teachers = StringBuilder()
                    for(teacher in cellDetail.children()){
                        if(teacher.text() == "【教室】") continue
                        teachers.append(teacher.text().replace(", ", ","))
                    }

                    val newCell = ClassCell(id, name, teachers.toString(), room, cell.index, row.index)
                    newCell.context = context

                    classes.add(newCell)
                }
            }
        }
        return classes.toTypedArray()
    }

    fun updateDB(classes: Array<ClassCell>, context: Context){
        classes.forEach {
            val db = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "ScombMobileDB"
            ).allowMainThreadQueries().build()
            db.classCellDao().insertClassCell(it)
        }
    }

    // get classes fetched in 24h
    suspend fun fetchFromDB(context: Context): Array<ClassCell>{
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ScombMobileDB"
        ).allowMainThreadQueries().build()

        return db.classCellDao().getAllClassCell()
    }

    fun fetch(context: Context){
        viewModelScope.launch(Dispatchers.IO) {

            var classes = fetchFromDB(context)


            val yesterday = Calendar.getInstance()
            yesterday.add(Calendar.HOUR, TIMETABLE_EFFECTIVE_TIME)

            // get classes fetched in 24h
            val in24h = mutableListOf<ClassCell>()
            classes.forEach {
                if(it.createdDate > yesterday.timeInMillis){
                    in24h.add(it)
                }
            }

            // if classes info in db is too old, fetch new from server
            if(in24h.isEmpty()){
                val newClasses = fetchFromServer(context)

                // update classes and take over custom color
                for (newClass in newClasses) {
                    for (c in classes) {
                        if(newClass.id == c.id){
                            newClass.customColorInt = c.customColorInt
                        }
                    }
                }
                classes = newClasses

                updateDB(newClasses, context)
            }

            constructTimetable(classes)

            var s = ""
            classes.forEach {
                s += "{${it.name}, ${it.customColorInt}}, "
            }
            Log.d("timetable", s)
        }

    }

    fun constructTimetable(classes: Array<ClassCell>){
        val newTimetable: Array<Array<ClassCell?>> = Array(7){ Array(6){ null } }
        classes.forEach {
            newTimetable[it.period][it.dayOfWeek] = it
        }
        timeTable.postValue(newTimetable)
    }
}
