package net.iobb.koheinoapp.scombmobile.ui.home

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.iobb.koheinoapp.scombmobile.AppViewModel
import net.iobb.koheinoapp.scombmobile.SCOMB_HOME_URL
import net.iobb.koheinoapp.scombmobile.scraping.Page

class HomeViewModel : ViewModel() {


    val page = Page(SCOMB_HOME_URL)
    lateinit var appViewModel: AppViewModel
    val text = MutableLiveData("")

    fun fetch(){
        viewModelScope.launch(Dispatchers.IO) {
            page.fetch(appViewModel.sessionId)
            text.postValue(page.document?.text() ?: "")
        }
    }
}