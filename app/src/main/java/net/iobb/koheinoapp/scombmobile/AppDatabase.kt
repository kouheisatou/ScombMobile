package net.iobb.koheinoapp.scombmobile

import androidx.room.Database
import androidx.room.RoomDatabase
import net.iobb.koheinoapp.scombmobile.ui.login.User
import net.iobb.koheinoapp.scombmobile.ui.login.UserDao

@Database(entities = [User::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase(){
    abstract fun userDao(): UserDao
}