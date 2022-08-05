package net.iobb.koheinoapp.scombmobile.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import kotlinx.android.synthetic.main.fragment_setting.*
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

        val intervalSelection = mutableListOf("自動更新なし", "常に更新", "1日", "2日", "1週間", "2週間")
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
                root.periodSpinner.visibility = View.INVISIBLE
            }else{
                root.periodSpinner.visibility = View.VISIBLE
            }
        }

        val periodSelection = mutableListOf("前期", "後期")
        setRightGravityAdapterToSpinner(requireContext(), periodSelection, root.periodSpinner) { selectedIndex, _ ->
            db.settingDao().insertSetting(Setting("timetable_period", selectedIndex.toString()))
        }

        recoverSettings(db, root, yearSelection)

        // auto login checkbox
        root.autoLoginCheckBox.setOnClickListener {
            db.settingDao().insertSetting(Setting("enabled_auto_login", root.autoLoginCheckBox.isChecked.toString()))
        }

        // saved user and pass
        root.userNameSaveBtn.setOnClickListener {
            db.userDao().removeAllUser()
            db.userDao().insertUser(User(db.userDao().getUser()?.username ?: root.userEditText.text.toString(), root.passEditText.text.toString()))
            Toast.makeText(requireContext(), "学籍番号を保存しました", Toast.LENGTH_SHORT).show()
        }
        root.passwordSaveBtn.setOnClickListener {
            db.userDao().removeAllUser()
            db.userDao().insertUser(User(root.userEditText.text.toString(), db.userDao().getUser()?.password ?: root.passEditText.text.toString()))
            Toast.makeText(requireContext(), "パスワードを保存しました", Toast.LENGTH_SHORT).show()
        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SettingViewModel::class.java)
    }


    fun recoverSettings(db: AppDatabase, root: View, yearSelection: List<String>){
        root.autoLoginCheckBox.isChecked = db.settingDao().getSetting("enabled_auto_login")?.settingValue == "true"

        root.userEditText.setText(db.userDao().getUser()?.username ?: "")
        root.passEditText.setText(db.userDao().getUser()?.password ?: "")

        root.refreshIntervalSpinner.setSelection(db.settingDao().getSetting("refresh_interval")?.settingValue?.toInt() ?: 4)
        val yearIndex = yearSelection.indexOf(db.settingDao().getSetting("timetable_year")?.settingValue ?: "最新")
        root.yearSpinner.setSelection(yearIndex)
        root.periodSpinner.setSelection(db.settingDao().getSetting("timetable_period")?.settingValue?.toInt() ?: 0)
    }

}