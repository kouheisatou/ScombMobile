package net.iobb.koheinoapp.scombmobile.ui.classdetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import net.iobb.koheinoapp.scombmobile.common.AppViewModel
import net.iobb.koheinoapp.scombmobile.common.CLASS_PAGE_URL
import net.iobb.koheinoapp.scombmobile.common.*

class ClassDetailViewModel(val url: String) : ViewModel() {

    class Factory constructor(
        private val classId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ClassDetailViewModel(classId) as T
        }
    }

    lateinit var appViewModel: AppViewModel
}