package net.iobb.koheinoapp.scombmobile.ui.settings

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Setting(
    @PrimaryKey
    val settingKey: String,
    val settingValue: String) {

    override fun toString(): String {
        return "Setting(settingKey=$settingKey, settingValue=$settingValue)"
    }
}