package net.iobb.koheinoapp.scombmobile.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import kotlinx.android.synthetic.main.fragment_setting.view.*
import net.iobb.koheinoapp.scombmobile.R
import net.iobb.koheinoapp.scombmobile.common.AppDatabase
import net.iobb.koheinoapp.scombmobile.common.setRightGravityAdapterToSpinner
import net.iobb.koheinoapp.scombmobile.ui.login.User
import java.util.*


class SettingFragment : Fragment() {

    private lateinit var viewModel: SettingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_setting, container, false)

        val db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java,
            "ScombMobileDB"
        ).allowMainThreadQueries().build()

        val intervalSelection = mutableListOf("常に更新", "1日", "2日", "1週間", "2週間")
        setRightGravityAdapterToSpinner(requireContext(), intervalSelection, root.refreshIntervalSpinner){ selectedIndex, _ ->
            db.settingDao().insertSetting(Setting("refresh_interval", selectedIndex.toString()))
        }

        val yearSelection = mutableListOf("最新")
        val yearOfToday = Calendar.getInstance().get(Calendar.YEAR)
        for(i in 0 until 10){
            yearSelection.add((yearOfToday - i).toString())
        }
        setRightGravityAdapterToSpinner(requireContext(), yearSelection, root.yearSpinner){ selectedIndex, selectedValue ->
            db.settingDao().insertSetting(Setting("timetable_year", selectedValue))
            Log.d("setting_inserted", selectedValue)
            // "最新" selected
            if(selectedIndex == 0){
                root.termSpinner.visibility = View.INVISIBLE
            }else{
                root.termSpinner.visibility = View.VISIBLE
            }
        }

        val termSelection = mutableListOf("前期", "後期")
        setRightGravityAdapterToSpinner(requireContext(), termSelection, root.termSpinner) { selectedIndex, _ ->
            db.settingDao().insertSetting(Setting("timetable_term", selectedIndex.toString()))
        }

        root.taskNotificationCheckbox.setOnCheckedChangeListener { _, isChecked ->
            db.settingDao().insertSetting(Setting("task_notification", isChecked.toString()))
            root.taskNotifyTimeSpinner.isVisible = isChecked
        }

        val timeSelection = mutableListOf("10分前", "30分前", "1時間前", "2時間前", "3時間前", "24時間前")
        setRightGravityAdapterToSpinner(requireContext(), timeSelection, root.taskNotifyTimeSpinner){ selectedItemIndex, _ ->
            db.settingDao().insertSetting(Setting("task_notify_time", selectedItemIndex.toString()))
        }

        root.timetableNotificationCheckbox.setOnCheckedChangeListener { _, isChecked ->
            db.settingDao().insertSetting(Setting("timetable_notification", isChecked.toString()))
            root.timetableNotifyTimeSpinner.isVisible = isChecked
        }

        setRightGravityAdapterToSpinner(requireContext(), timeSelection, root.timetableNotifyTimeSpinner) { selectedItemIndex, _ ->
            db.settingDao().insertSetting(Setting("timetable_notify_time", selectedItemIndex.toString()))
        }

        recoverSettings(db, root, yearSelection)

        // auto login checkbox
        root.autoLoginCheckBox.setOnClickListener {
            db.settingDao().insertSetting(Setting("enabled_auto_login", root.autoLoginCheckBox.isChecked.toString()))
        }

        // saved user and pass
        root.userEditText.addTextChangedListener {
            db.userDao().removeAllUser()
            db.userDao().insertUser(User(root.userEditText.text.toString(), root.passEditText.text.toString()))
        }
        root.passEditText.addTextChangedListener {
            db.userDao().removeAllUser()
            db.userDao().insertUser(User(root.userEditText.text.toString(), root.passEditText.text.toString()))
        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SettingViewModel::class.java)
    }


    fun recoverSettings(db: AppDatabase, root: View, yearSelection: List<String>){
        root.autoLoginCheckBox.isChecked = (db.settingDao().getSetting("enabled_auto_login")?.settingValue ?: "true") == "true"

        root.userEditText.setText(db.userDao().getUser()?.username ?: "")
        root.passEditText.setText(db.userDao().getUser()?.password ?: "")

        root.refreshIntervalSpinner.setSelection(db.settingDao().getSetting("refresh_interval")?.settingValue?.toInt() ?: 4)
        val yearIndex = yearSelection.indexOf(db.settingDao().getSetting("timetable_year")?.settingValue ?: "最新")
        root.yearSpinner.setSelection(yearIndex)
        root.termSpinner.setSelection(db.settingDao().getSetting("timetable_term")?.settingValue?.toInt() ?: 0)

        root.taskNotificationCheckbox.isChecked = db.settingDao().getSetting("task_notification")?.settingValue == "true"
        root.taskNotifyTimeSpinner.setSelection(db.settingDao().getSetting("task_notify_time")?.settingValue?.toInt() ?: 0)
        root.timetableNotificationCheckbox.isChecked = db.settingDao().getSetting("timetable_notification")?.settingValue == "true"
        root.timetableNotifyTimeSpinner.setSelection(db.settingDao().getSetting("timetable_notify_time")?.settingValue?.toInt() ?: 0)
    }

}