package net.iobb.koheinoapp.scombmobile.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import kotlinx.android.synthetic.main.fragment_setting.*
import net.iobb.koheinoapp.scombmobile.R
import net.iobb.koheinoapp.scombmobile.common.AppDatabase
import net.iobb.koheinoapp.scombmobile.ui.login.User


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
    }

    override fun onStart() {

        val db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java,
            "ScombMobileDB"
        ).allowMainThreadQueries().build()

        // auto login checkbox
        autoLoginCheckBox.isChecked = db.settingDao().getSetting("enabled_auto_login")?.settingValue == "true"
        autoLoginCheckBox.setOnClickListener {
            db.settingDao().insertSetting(Setting("enabled_auto_login", autoLoginCheckBox.isChecked.toString()))
            Log.d("SettingDao : enabled_auto_login", db.settingDao().getSetting("enabled_auto_login")?.settingValue ?: "null")
        }

        // saved user and pass
        idTextView.setText(db.userDao().getUser()?.username ?: "")
        passwordTextView.setText(db.userDao().getUser()?.password ?: "")
        idTextView.addTextChangedListener {
            db.userDao().removeAllUser()
            db.userDao().insertUser(User(idTextView.text.toString(), passwordTextView.text.toString()))
        }
        passwordTextView.addTextChangedListener {
            db.userDao().removeAllUser()
            db.userDao().insertUser(User(idTextView.text.toString(), passwordTextView.text.toString()))
        }


        refreshIntervalSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                // アイテムが選択された時の動作
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                }

                // 何も選択されなかった時の動作
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        super.onStart()
    }

}