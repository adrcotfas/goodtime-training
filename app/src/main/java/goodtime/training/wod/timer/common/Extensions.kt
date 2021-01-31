package goodtime.training.wod.timer.common

import android.annotation.SuppressLint
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
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.number_picker.NumberPicker.Companion.PickerSize

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
        override fun beforeTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

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

fun calculateRowHeight(layoutInflater: LayoutInflater, size: PickerSize = PickerSize.LARGE): Float {
    val textView = layoutInflater
            .inflate(
                    when (size) {
                        PickerSize.LARGE -> R.layout.row_number_picker_large
                        PickerSize.MEDIUM -> R.layout.row_number_picker_medium
                    }, null
            ) as TextView
    val fm = textView.paint.fontMetrics
    return fm.descent - fm.ascent
}

val FragmentManager.currentNavigationFragment: Fragment?
    get() = primaryNavigationFragment?.childFragmentManager?.fragments?.first()

fun RecyclerView.smoothSnapToPosition(position: Int) {
    val smoothScroller = object : LinearSmoothScroller(this.context) {
        override fun getVerticalSnapPreference(): Int = SNAP_TO_START
    }
    smoothScroller.targetPosition = position
    this.post { layoutManager?.startSmoothScroll(smoothScroller) }
}

@SuppressLint("SetTextI18n")
fun TextInputEditText.setTextWithZeroPrefix(text: String) {
    when {
        text.isEmpty() -> setText("00")
        text.length == 1 -> setText("0${text}")
        else -> setText(text)
    }
}

@SuppressLint("SetTextI18n")
fun TextInputEditText.setupZeroPrefixBehaviourOnFocus() {
    setOnFocusChangeListener { _, hasFocus ->
        if (!hasFocus) {
            if (editableText.isNullOrEmpty()) setText("00")
            // prefix single digits with a zero
            if (editableText?.length == 1) {
                text?.insert(0, "0")
            }
        }
    }
}

@SuppressLint("SetTextI18n")
fun TextInputEditText.trimTo(limit: Int) {
    if(toInt(editableText.toString()) > limit) {
        setText(if (limit < 10) "0$limit" else limit.toString())
    }
}

fun toInt(string: String): Int {
    if (string.isEmpty()) {
        return 0
    }
    return Integer.parseInt(string)
}

fun hideKeyboardFrom(context: Context, view: View) {
    val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}
