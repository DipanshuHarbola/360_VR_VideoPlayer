package com.deepanshu.vrplayer

import android.app.Activity
import android.util.SparseArray
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

class SpinnerHelper(private val activity: Activity) {
    private var data: SparseArray<String>? = null
    private var clickHandler: ClickHandler? = null
    private var defaultKey = 0

    interface ClickHandler {
        fun onSpinnerClicked(index: Int, key: Int, value: String?)
    }

    fun setDefault(key: Int): SpinnerHelper {
        defaultKey = key
        return this
    }

    fun setData(data: SparseArray<String>?): SpinnerHelper {
        this.data = data
        return this
    }

    fun setClickHandler(clickHandler: ClickHandler?): SpinnerHelper {
        this.clickHandler = clickHandler
        return this
    }

    fun init(id: Int) {
        if (data == null) {
            return
        }
        val spinner = activity.findViewById<View>(id) as Spinner
        val adapter = ArrayAdapter<String>(activity, R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        for (i in 0 until data!!.size()) {
            val value = data!!.valueAt(i)
            adapter.add(value)
        }
        spinner.adapter = adapter
        var index = data!!.indexOfKey(defaultKey)
        index = if (index == -1) 0 else index
        spinner.setSelection(index)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View, position: Int, id: Long
            ) {
                val key = data!!.keyAt(position)
                val value = data!!.valueAt(position)
                if (clickHandler != null) {
                    clickHandler!!.onSpinnerClicked(position, key, value)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    companion object {
        fun with(activity: Activity): SpinnerHelper {
            return SpinnerHelper(activity)
        }
    }
}