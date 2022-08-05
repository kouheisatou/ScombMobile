package net.iobb.koheinoapp.scombmobile.common

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_setting.view.*
import net.iobb.koheinoapp.scombmobile.ui.settings.Setting
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

fun <T>rightGravityArrayAdapter(context: Context, spinner: Spinner): ArrayAdapter<T> {
    val arrayAdapter: ArrayAdapter<T> = object : ArrayAdapter<T>(context, android.R.layout.simple_spinner_item) {
        // pos in item
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val v = super.getView(position, convertView, parent)
            (v as TextView).gravity = Gravity.RIGHT
            return v
        }
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val v = super.getDropDownView(position, convertView, parent)
            (v as TextView).gravity = Gravity.CENTER
            return v
        }
    }
    spinner.gravity = Gravity.RIGHT
    arrayAdapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
    return arrayAdapter
}

fun setRightGravityAdapterToSpinner(context: Context, selection: List<String>, spinner: Spinner, onSpinnerItemSelected: (selectedItemIndex: Int, selectedItemValue: String) -> Unit){

    val adapter = rightGravityArrayAdapter<String>(context, spinner)
    adapter.addAll(selection)
    spinner.adapter = adapter
    spinner.onItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                onSpinnerItemSelected(position, selection[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
}