package net.iobb.koheinoapp.scombmobile.ui.login

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class User(val username: String, val password: String) {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    override fun toString(): String {
        return "User(user=$username, pass=$password)"
    }
}