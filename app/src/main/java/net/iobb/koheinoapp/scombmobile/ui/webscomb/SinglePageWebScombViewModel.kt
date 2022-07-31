package net.iobb.koheinoapp.scombmobile.ui.webscomb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import net.iobb.koheinoapp.scombmobile.common.AppViewModel

class SinglePageWebScombViewModel(val url: String) : ViewModel() {

    class Factory constructor(
        private val classId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SinglePageWebScombViewModel(classId) as T
        }
    }

    lateinit var appViewModel: AppViewModel
}