package com.adrcotfas.wod.common

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.adrcotfas.wod.R
import com.adrcotfas.wod.data.model.SessionMinimal
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun EditText.onDone(callback: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            callback.invoke()
        }
        false
    }
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}

fun View.showKeyboard(activity: Activity) {
    val inputManager: InputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

fun View.hideKeyboard(activity: Activity) {
    val view = activity.findViewById<View>(android.R.id.content)
    val inputManager: InputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun calculateRowHeight(layoutInflater : LayoutInflater, large: Boolean = true): Float {
    val textView = layoutInflater
        .inflate(if (large) R.layout.row_number_picker_large else R.layout.row_number_picker, null) as TextView
    val fm = textView.paint.fontMetrics
    return fm.descent - fm.ascent
}

val FragmentManager.currentNavigationFragment: Fragment?
    get() = primaryNavigationFragment?.childFragmentManager?.fragments?.first()

fun sessionsToString(vararg sessions: SessionMinimal) : String {
    val gson = Gson()
    return gson.toJson(listOf(*sessions))
}

fun stringToSessions(string : String) : ArrayList<SessionMinimal> {
    val gson = Gson()
    val typeToken = object : TypeToken<ArrayList<SessionMinimal>>() {}
    return gson.fromJson(string, typeToken.type)
}