package net.iobb.koheinoapp.scombmobile.ui.login

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class User(
    @PrimaryKey
    val username: String,
    val password: String) {

    override fun toString(): String {
        return "User(user=$username, pass=$password)"
    }
}