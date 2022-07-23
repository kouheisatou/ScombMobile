package net.iobb.koheinoapp.scombmobile.ui.settings

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.room.Room
import kotlinx.android.synthetic.main.fragment_setting.*
import net.iobb.koheinoapp.scombmobile.AppDatabase
import net.iobb.koheinoapp.scombmobile.R

class SettingFragment : Fragment() {

    companion object {
        fun newInstance() = SettingFragment()
    }

    private lateinit var viewModel: SettingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SettingViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onStart() {

        val db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java,
            "ScombMobileDB"
        ).allowMainThreadQueries().build()


        autoLoginCheckBox.isChecked = db.settingDao().getSetting("enabled_auto_login")?.settingValue == "true"
        autoLoginCheckBox.setOnClickListener {
            db.settingDao().insertSetting(Setting("enabled_auto_login", autoLoginCheckBox.isChecked.toString()))
            Log.d("SettingDao : enabled_auto_login", db.settingDao().getSetting("enabled_auto_login")?.settingValue ?: "null")
        }

        super.onStart()
    }

}