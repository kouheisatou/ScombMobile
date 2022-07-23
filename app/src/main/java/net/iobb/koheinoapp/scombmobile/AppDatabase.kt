package net.iobb.koheinoapp.scombmobile

import androidx.room.Database
import androidx.room.RoomDatabase
import net.iobb.koheinoapp.scombmobile.ui.login.User
import net.iobb.koheinoapp.scombmobile.ui.login.UserDao
import net.iobb.koheinoapp.scombmobile.ui.settings.Setting
import net.iobb.koheinoapp.scombmobile.ui.settings.SettingDao

@Database(entities = [User::class, Setting::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase(){
    abstract fun userDao(): UserDao
    abstract fun settingDao(): SettingDao
}
