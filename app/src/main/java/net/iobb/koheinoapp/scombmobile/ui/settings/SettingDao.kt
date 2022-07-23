package net.iobb.koheinoapp.scombmobile.ui.settings

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SettingDao {

    @Query("SELECT * FROM setting WHERE settingKey=:key LIMIT 1;")
    fun getSetting(key: String): Setting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSetting(setting: Setting)
}