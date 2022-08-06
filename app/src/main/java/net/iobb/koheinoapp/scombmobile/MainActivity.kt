package net.iobb.koheinoapp.scombmobile

import android.app.AlarmManager
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.room.Room
import com.google.android.material.navigation.NavigationView
import net.iobb.koheinoapp.scombmobile.common.AppViewModel
import net.iobb.koheinoapp.scombmobile.background.ScombMobileNotification
import net.iobb.koheinoapp.scombmobile.background.TasksFetchReceiver
import net.iobb.koheinoapp.scombmobile.common.AppDatabase
import net.iobb.koheinoapp.scombmobile.databinding.ActivityMainBinding
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var binding: ActivityMainBinding
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_ScombMobile_NoActionBar);
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_settingFragment,
                R.id.nav_loginFragment,
                R.id.nav_webScombFragment,
                R.id.nav_taskListFragment,
                R.id.nav_taskCalendarFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        ScombMobileNotification.createNotificationChannel(this)

        val db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "ScombMobileDB"
        ).allowMainThreadQueries().build()
        val sessionId = db.settingDao().getSetting("session_id")?.settingValue
        TasksFetchReceiver.resumeBackgroundTask(this, sessionId, Calendar.getInstance().timeInMillis + 5000, AlarmManager.INTERVAL_DAY)

    }

    override fun onStart() {
        appViewModel.userId.observe(this){
            binding.navView.getHeaderView(0).findViewById<TextView>(R.id.userNameTextView).text = it ?: "ログインしていません"
        }
        super.onStart()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun restart(){
        val intent = Intent(baseContext, MainActivity::class.java)
        finish()
        startActivity(intent)
    }
}