package net.iobb.koheinoapp.scombmobile.ui.home

import androidx.annotation.ColorInt
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.iobb.koheinoapp.scombmobile.common.*

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
    var isInitalized = false
    val timetableListenerState = MutableLiveData(HomeFragment.ListenerState.Initialize)
    @ColorInt
    var selectedColor: Int? = null

    fun fetch(){
        if(isInitalized) return

        viewModelScope.launch(Dispatchers.IO) {
            val newTimetable: Array<Array<ClassCell?>> = Array(7){ Array(6){ null } }

            // get from web
            page.fetch(appViewModel.sessionId)

            val tableElement = page.document.getElementsByClass(TIMETABLE_ROW_CSS_CLASS_NM)

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

                        val teachers = mutableListOf<String>()
                        for(teacher in cellDetail.children()){
                            if(teacher.text() == "【教室】") continue
                            teachers.add(teacher.text().replace(", ", ""))
                        }

                        newTimetable[row.index][cell.index] = ClassCell(id, name, teachers, room)
                    }
                }
            }

            timeTable.postValue(newTimetable)
            isInitalized = true
        }
    }
}
