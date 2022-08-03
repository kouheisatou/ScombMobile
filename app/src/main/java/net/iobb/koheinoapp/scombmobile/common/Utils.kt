package net.iobb.koheinoapp.scombmobile.common

import java.text.SimpleDateFormat
import java.util.*


fun timeToString(timeMillis: Long): String {
    val date = Calendar.getInstance().apply { this.timeInMillis = timeMillis }
    val today = Calendar.getInstance()
    return if(date.get(Calendar.DATE) == today.get(Calendar.DATE)){
        val formatter = SimpleDateFormat("HH:mm")
        "今日 ${formatter.format(date.time)}"
    }else if(date.get(Calendar.DATE) == today.get(Calendar.DATE)+1){
        val formatter = SimpleDateFormat("HH:mm")
        "明日 ${formatter.format(date.time)}"
    }else if(date.get(Calendar.DATE) == today.get(Calendar.DATE)-1){
        val formatter = SimpleDateFormat("HH:mm")
        "昨日 ${formatter.format(date.time)}"
    }else{
        val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm")
        formatter.format(date.time)
    }
}