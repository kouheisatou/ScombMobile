package net.iobb.koheinoapp.scombmobile.ui.login

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {

    @Query("SELECT * FROM user;")
    fun loadAllUser(): Array<User>

    @Query("DELETE FROM user;")
    fun removeAllUser()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertUser(user: User)
}