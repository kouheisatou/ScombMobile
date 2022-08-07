package net.iobb.koheinoapp.scombmobile.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.android.synthetic.main.fragment_setting.view.*
import net.iobb.koheinoapp.scombmobile.R
import net.iobb.koheinoapp.scombmobile.common.AppDatabase
import net.iobb.koheinoapp.scombmobile.common.getIndexOfValuesInSpinner
import net.iobb.koheinoapp.scombmobile.common.setRightGravityAdapterToSpinner
import net.iobb.koheinoapp.scombmobile.ui.login.User
import net.iobb.koheinoapp.scombmobile.ui.settings.SettingFragment.SettingValues.intervalSelection
import net.iobb.koheinoapp.scombmobile.ui.settings.SettingFragment.SettingValues.termSelection
import net.iobb.koheinoapp.scombmobile.ui.settings.SettingFragment.SettingValues.notifyShiftTimeSelection
import net.iobb.koheinoapp.scombmobile.ui.settings.SettingFragment.SettingValues.yearSelection
import java.util.*

class SettingFragment : Fragment() {

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

        val db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java,
            "ScombMobileDB"
        ).allowMainThreadQueries().build()

        setRightGravityAdapterToSpinner(requireContext(), intervalSelection, refreshIntervalSpinner){ _, selectedValue ->
            db.settingDao().insertSetting(Setting(SettingKeys.REFRESH_INTERVAL, selectedValue))
        }

        setRightGravityAdapterToSpinner(requireContext(), yearSelection, yearSpinner){ _, selectedValue ->
            db.settingDao().insertSetting(Setting(SettingKeys.TIMETABLE_YEAR, selectedValue))
            // "最新" selected
            termSpinner.visibility = if(selectedValue == "最新"){ View.INVISIBLE }else{ View.VISIBLE }
        }

        setRightGravityAdapterToSpinner(requireContext(), termSelection, termSpinner) { _, selectedValue ->
            db.settingDao().insertSetting(Setting(SettingKeys.TIMETABLE_TERM, selectedValue.toString()))
        }

        // enabled task and test notification
        taskNotificationCheckbox.setOnCheckedChangeListener { _, isChecked ->
            db.settingDao().insertSetting(Setting(SettingKeys.ENABLED_TASK_NOTIFICATION, isChecked.toString()))
            taskNotifyTimeSpinner.isVisible = isChecked
        }

        // timing of task and test notification
        setRightGravityAdapterToSpinner(requireContext(), notifyShiftTimeSelection, taskNotifyTimeSpinner){ _, selectedValue ->
            db.settingDao().insertSetting(Setting(SettingKeys.TASK_NOTIFY_TIME_SHIFT, selectedValue))
        }

        recoverSettings(db)

        // auto login checkbox
        autoLoginCheckBox.setOnClickListener {
            db.settingDao().insertSetting(Setting(SettingKeys.ENABLED_AUTO_LOGIN, autoLoginCheckBox.isChecked.toString()))
        }

        // saved user and pass
        userEditText.addTextChangedListener {
            db.userDao().removeAllUser()
            db.userDao().insertUser(User(userEditText.text.toString(), passEditText.text.toString()))
        }
        passEditText.addTextChangedListener {
            db.userDao().removeAllUser()
            db.userDao().insertUser(User(userEditText.text.toString(), passEditText.text.toString()))
        }

        // github link
        githubLinkTextView.setOnClickListener {
            val uri = Uri.parse(githubLinkTextView.text.toString())
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
        // privacy policy link
        privacyPolicyLinkTextView.setOnClickListener {
            val uri = Uri.parse(privacyPolicyLinkTextView.text.toString())
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }


    fun recoverSettings(db: AppDatabase){
        autoLoginCheckBox.isChecked = (db.settingDao().getSetting(SettingKeys.ENABLED_AUTO_LOGIN)?.settingValue ?: "true") == "true"

        userEditText.setText(db.userDao().getUser()?.username ?: "")
        passEditText.setText(db.userDao().getUser()?.password ?: "")

        val refreshIntervalSettingValue = db.settingDao().getSetting(SettingKeys.REFRESH_INTERVAL)?.settingValue
        val refreshIntervalSelectionIndex = getIndexOfValuesInSpinner(
            refreshIntervalSpinner,
            refreshIntervalSettingValue ?: "1週間"
        ) ?: 0
        refreshIntervalSpinner.setSelection(refreshIntervalSelectionIndex)

        val yearSettingValue = db.settingDao().getSetting(SettingKeys.TIMETABLE_YEAR)?.settingValue
        termSpinner.visibility = if(yearSettingValue == "最新"){ View.INVISIBLE }else{ View.VISIBLE }
        val yearIndex = getIndexOfValuesInSpinner(
            yearSpinner,
            yearSettingValue ?: "最新"
        ) ?: 0
        yearSpinner.setSelection(yearIndex)

        val termSettingValue = db.settingDao().getSetting(SettingKeys.TIMETABLE_TERM)?.settingValue
        val termIndex = getIndexOfValuesInSpinner(termSpinner, termSettingValue ?: "前期") ?: 0
        termSpinner.setSelection(termIndex)

        taskNotificationCheckbox.isChecked = (db.settingDao().getSetting(SettingKeys.ENABLED_TASK_NOTIFICATION)?.settingValue ?: "true") == "true"
        taskNotifyTimeSpinner.isVisible = taskNotificationCheckbox.isChecked
        val taskNotifySpinnerIndex = getIndexOfValuesInSpinner(
            taskNotifyTimeSpinner,
            db.settingDao().getSetting(SettingKeys.TASK_NOTIFY_TIME_SHIFT)?.settingValue ?: "30分前"
        ) ?: 0
        taskNotifyTimeSpinner.setSelection(taskNotifySpinnerIndex)
    }

    object SettingKeys {
        const val ENABLED_TASK_NOTIFICATION = "enabled_task_notification"
        const val TASK_NOTIFY_TIME_SHIFT = "task_notify_time_shift"
        const val ENABLED_AUTO_LOGIN = "enabled_auto_login"
        const val REFRESH_INTERVAL = "refresh_interval"
        const val TIMETABLE_YEAR = "timetable_year"
        const val TIMETABLE_TERM = "timetable_term"
        const val SESSION_ID = "session_id"
    }

    object SettingValues {
        val notifyShiftTimeSelection = mapOf(
            "10分前" to 60000 * 10,
            "30分前" to 60000 * 30,
            "1時間前" to 60000 * 60,
            "2時間前" to 60000 * 60 * 2,
            "3時間前" to 60000 * 60 * 3,
            "24時間前" to 60000 * 60 * 4
        )
        val termSelection = mapOf(
            "前期" to 10,
            "後期" to 20
        )
        val intervalSelection = mapOf(
            "常に更新" to 0,
            "1日" to 86400000L * 1,
            "2日" to 86400000L * 2,
            "1週間" to 86400000L * 7,
            "2週間" to 86400000L * 14
        )
        val yearSelection = mutableMapOf<String, Int>()

        init {
            val today = Calendar.getInstance()
            val yearOfToday = today.get(Calendar.YEAR)
            yearSelection["最新"] = yearOfToday
            // {key=2022, value=2022}, {key=2021, value=2021}, ...
            for(i in 0 until 10){
                yearSelection[(yearOfToday - i).toString()] = yearOfToday - i
            }
        }
    }

}