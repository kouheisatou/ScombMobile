package net.iobb.koheinoapp.scombmobile.ui.classdetail

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.iobb.koheinoapp.scombmobile.AppViewModel
import net.iobb.koheinoapp.scombmobile.CLASS_OVERVIEW_CSS_CLASS_NM
import net.iobb.koheinoapp.scombmobile.CLASS_PAGE_URL
import net.iobb.koheinoapp.scombmobile.scraping.*
import org.jsoup.Jsoup

class ClassDetailViewModel(var classId: String) : ViewModel() {

    class Factory constructor(
        private val classId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ClassDetailViewModel(classId) as T
        }
    }

    val page = Page(CLASS_PAGE_URL + classId)
    lateinit var appViewModel: AppViewModel

    var className = ""
    var classOverview = ""
    var teacher = ""
    var teachersMail = ""

    fun fetch(){
        viewModelScope.launch(Dispatchers.IO) {
            page.fetch(appViewModel.sessionId)

            val overviewElement = page.document.getElementById(CLASS_OVERVIEW_CSS_CLASS_NM)
            classOverview = overviewElement?.text() ?: "jjj"
            Log.d("class_overview", page.document.html())
            Log.d("class_overview", page.url)

        }
    }
}