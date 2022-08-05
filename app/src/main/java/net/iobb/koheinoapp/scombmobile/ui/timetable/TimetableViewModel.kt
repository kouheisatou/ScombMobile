package net.iobb.koheinoapp.scombmobile.ui.timetable

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.iobb.koheinoapp.scombmobile.common.*
import net.iobb.koheinoapp.scombmobile.ui.settings.Setting
import java.lang.StringBuilder
import java.util.*

class TimetableViewModel : ViewModel() {

    val page = Page()
    lateinit var appViewModel: AppViewModel
    // 時間割二次元配列 (7(1-7限) * 6(月-土の6日))
    val timeTable = MutableLiveData<Array<Array<ClassCell?>>>(
        Array(7){
            Array(6){
                null
            }
        }
    )
    val timetableListenerState = MutableLiveData(TimetableFragment.ListenerState.Initialize)
    var selectedColor: Int? = null

    // timetable setting
    var refreshInterval: Long = 86400000L * 7
    var timetableYear: Int? = null
    var timetablePeriod: Int? = null    // 前期 -> 10, 後期 -> 20

    companion object {
        var refreshRequired = false
    }

    suspend fun fetchFromServer(context: Context): Array<ClassCell>?{
        loadTimetableSettings(context)
        val url = "$SCOMB_TIMETABLE_URL?risyunen=${timetableYear ?: ""}&kikanCd=${timetablePeriod ?: ""}"

        // get from web
        val doc = page.fetch(url, appViewModel.sessionId)

        val tableElement = doc?.getElementsByClass(TIMETABLE_ROW_CSS_CLASS_NM) ?: return null
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
                        teachers.append(teacher.text())
                    }

                    val newCell = ClassCell(id, name, teachers.toString(), room, cell.index, row.index)
                    newCell.context = context

                    classes.add(newCell)
                }
            }
        }
        return classes.toTypedArray()
    }

    fun loadTimetableSettings(context: Context) {
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ScombMobileDB"
        ).allowMainThreadQueries().build().settingDao()


        val interval = db.getSetting("refresh_interval")?.settingValue
        val year = db.getSetting("timetable_year")?.settingValue
        val period = db.getSetting("timetable_period")?.settingValue

        Log.d("load_timetable_setting", "interval=$interval, year=$year, period=$period")

        refreshInterval = when(interval){
            "0" -> -1L
            "1" -> 0L
            "2" -> 86400000L
            "3" -> 86400000L * 2
            "4" -> 86400000L * 7
            "5" -> 86400000L * 14
            else -> 86400000L * 7
        }

        val today = Calendar.getInstance()
        // year as 4 digits
        if(year?.matches(Regex("\\d{4}")) == true){
            timetableYear = year.toInt()
            timetablePeriod = when(period){
                "0" -> 10
                "1" -> 20
                else -> 10
            }
        }
        // latest
        else{
            timetableYear = today.get(Calendar.YEAR)
            timetablePeriod = if(today.get(Calendar.MONTH) < 9){
                10
            }else{
                20
            }
        }
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

    /**
     * @param requiredRefresh force fetch from server
     */
    fun fetch(context: Context, requiredRefresh: Boolean = false){
        viewModelScope.launch(Dispatchers.IO) {

            var classesFromDB = fetchFromDB(context)

            val now = Calendar.getInstance()

            // get classes fetched in 24h
            val in24h = mutableListOf<ClassCell>()
            classesFromDB.forEach {
                if(it.createdDate > now.timeInMillis - refreshInterval){
                    in24h.add(it)
                }
            }

            // if classes info in db is too old, fetch new from server
            if((in24h.isEmpty() || refreshInterval != -1L) || requiredRefresh){
                val newClasses = fetchFromServer(context)

                if(newClasses != null){
                    // update classes and marge old
                    for (newClass in newClasses) {
                        for (oldClass in classesFromDB) {
                            if(newClass.id == oldClass.id){
                                newClass.customColorInt = oldClass.customColorInt
                            }
                        }
                    }
                    classesFromDB = newClasses
                    updateDB(newClasses, context)
                    refreshRequired = false
                }else{
                    return@launch
                }
            }

            constructTimetable(classesFromDB)

            var s = "["
            classesFromDB.forEach {
                s += "${it}, "
            }
            s += "]"
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
