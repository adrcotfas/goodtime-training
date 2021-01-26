package goodtime.training.wod.timer.ui.main

import android.annotation.SuppressLint
import android.text.Editable
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import goodtime.training.wod.timer.common.StringUtils
import goodtime.training.wod.timer.common.toInt
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import java.lang.IllegalArgumentException
import java.security.InvalidParameterException

data class SessionEditTextHelper(
    private var listener: Listener,
    private var genericMinutesEt: TextInputEditText? = null,
    private var genericSecondsEt: TextInputEditText? = null,
    private var emomRoundsEt: TextInputEditText? = null,
    private var emomMinutesEt: TextInputEditText? = null,
    private var emomSecondsEt: TextInputEditText? = null,
    private var hiitRoundsEt: TextInputEditText? = null,
    private var hiitSecondsWorkEt: TextInputEditText? = null,
    private var hiitSecondsRestEt: TextInputEditText? = null,
    private var sessionType: SessionType,
){

    init {
        val initAll = !listOf(genericMinutesEt, genericSecondsEt, emomRoundsEt,
            emomMinutesEt, emomSecondsEt, hiitRoundsEt, hiitSecondsWorkEt, hiitSecondsRestEt).contains(null)
        setupTextEditSections(initAll)
    }

    interface Listener {
        fun onTextChanged(isValid: Boolean, sessionSkeleton: SessionSkeleton)
    }

    fun updateSessionType(sessionType: SessionType) {
        this.sessionType = sessionType
    }

    private fun setupTextEditSections(initAll: Boolean) {
        val genericEts = listOf(genericMinutesEt, genericSecondsEt)
        val emomEts = listOf(emomRoundsEt, emomMinutesEt, emomSecondsEt)
        val hiitEts = listOf(hiitRoundsEt, hiitSecondsWorkEt, hiitSecondsRestEt)
        val allEts = genericEts + emomEts + hiitEts

        val lastGenericEt = genericSecondsEt
        val lastEmomEt = emomSecondsEt
        val lastHiitEt = hiitSecondsRestEt
        val allLastEts = listOf(lastGenericEt, lastEmomEt, lastHiitEt)

        if (initAll) {
            for (it in allEts) setupEditTextBehaviorOnFocus(it!!)
            for (it in allLastEts) it!!.imeOptions = EditorInfo.IME_ACTION_DONE
            initGenericSection()
            initEmomSection()
            initHiitSection()
        } else {
            when (sessionType) {
                SessionType.AMRAP, SessionType.FOR_TIME, SessionType.REST -> {
                    for (it in genericEts) setupEditTextBehaviorOnFocus(it!!)
                    lastGenericEt!!.imeOptions = EditorInfo.IME_ACTION_DONE
                    initGenericSection()
                }
                SessionType.EMOM -> {
                    for (it in emomEts) setupEditTextBehaviorOnFocus(it!!)
                    lastEmomEt!!.imeOptions = EditorInfo.IME_ACTION_DONE
                    initEmomSection()
                }
                SessionType.HIIT -> {
                    for (it in hiitEts) setupEditTextBehaviorOnFocus(it!!)
                    lastHiitEt!!.imeOptions = EditorInfo.IME_ACTION_DONE
                    initHiitSection()
                }
                else -> { }
            }
        }
    }

    private fun initGenericSection() {
        genericMinutesEt!!.addTextChangedListener {
            setupEditTextLimit(it, genericMinutesEt!!, 60)
            val minutes = toInt(it.toString())
            val seconds = toInt(genericSecondsEt!!.text.toString())
            val enabled = minutes != 0 || seconds != 0
            listener.onTextChanged(enabled, generateFromCurrentSelection())
        }

        genericSecondsEt!!.addTextChangedListener {
            setupEditTextLimit(it, genericSecondsEt!!, 59)
            val minutes = toInt(genericMinutesEt!!.text.toString())
            val seconds = toInt(it.toString())
            val enabled = minutes != 0 || seconds != 0
            listener.onTextChanged(enabled, generateFromCurrentSelection())
        }
    }

    private fun initEmomSection() {
        emomRoundsEt!!.addTextChangedListener {
            setupEditTextLimit(it, emomRoundsEt!!, 60)
            val rounds = toInt(it.toString())
            val minutes = toInt(emomMinutesEt!!.text.toString())
            val seconds = toInt(emomSecondsEt!!.text.toString())
            val enabled = rounds != 0 && (minutes != 0 || seconds != 0)
            listener.onTextChanged(enabled, generateFromCurrentSelection())
        }
        emomMinutesEt!!.addTextChangedListener {
            setupEditTextLimit(it, emomMinutesEt!!, 10)
            val rounds = toInt(emomRoundsEt!!.text.toString())
            val minutes = toInt(it.toString())
            val seconds = toInt(emomSecondsEt!!.text.toString())
            val enabled = rounds != 0 && (minutes != 0 || seconds != 0)
            listener.onTextChanged(enabled, generateFromCurrentSelection())
        }
        emomSecondsEt!!.addTextChangedListener {
            setupEditTextLimit(it, emomSecondsEt!!, 59)
            val rounds = toInt(emomRoundsEt!!.text.toString())
            val minutes = toInt(emomMinutesEt!!.text.toString())
            val seconds = toInt(it.toString())
            val enabled = rounds != 0 && (minutes != 0 || seconds != 0)
            listener.onTextChanged(enabled, generateFromCurrentSelection())
        }
    }

    private fun initHiitSection() {
        hiitRoundsEt!!.addTextChangedListener {
            setupEditTextLimit(it, hiitRoundsEt!!, 60)
            val rounds = toInt(it.toString())
            val secondsWork = toInt(hiitSecondsWorkEt!!.text.toString())
            val secondsRest = toInt(hiitSecondsRestEt!!.text.toString())
            val enabled = rounds != 0 && (secondsWork != 0 && secondsRest != 0)
            listener.onTextChanged(enabled, generateFromCurrentSelection())
        }
        hiitSecondsWorkEt!!.addTextChangedListener {
            setupEditTextLimit(it, hiitSecondsWorkEt!!, 90)
            val rounds = toInt(hiitRoundsEt!!.text.toString())
            val secondsWork = toInt(it.toString())
            val secondsRest = toInt(hiitSecondsRestEt!!.text.toString())
            val enabled = rounds != 0 && (secondsWork != 0 && secondsRest != 0)
            listener.onTextChanged(enabled, generateFromCurrentSelection())
        }
        hiitSecondsRestEt!!.addTextChangedListener {
            setupEditTextLimit(it, hiitSecondsRestEt!!, 90)
            val rounds = toInt(hiitRoundsEt!!.text.toString())
            val secondsWork = toInt(hiitSecondsWorkEt!!.text.toString())
            val secondsRest = toInt(it.toString())
            val enabled = rounds != 0 && (secondsWork != 0 && secondsRest != 0)
            listener.onTextChanged(enabled, generateFromCurrentSelection())
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupEditTextLimit(editable: Editable?, textInputEditText: TextInputEditText, limit: Int) {
        if(toInt(editable.toString()) > limit) {
            textInputEditText.setText(if (limit < 10) "0$limit" else limit.toString())
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupEditTextBehaviorOnFocus(textInputEditText: TextInputEditText) {
        textInputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (textInputEditText.editableText.isNullOrEmpty()) {
                    textInputEditText.setText("00")
                }
                // prefix single digits with a zero
                if (textInputEditText.editableText?.length == 1) {
                    textInputEditText.text?.insert(0, "0")
                }
            }
        }
    }

    fun generateFromCurrentSelection() : SessionSkeleton {
        return when(sessionType) {
            SessionType.AMRAP, SessionType.FOR_TIME, SessionType.REST
            -> SessionSkeleton(0,
                getCurrentSelectionDuration(sessionType), 0, 0, sessionType)
            SessionType.EMOM
            -> SessionSkeleton(0,
                getCurrentSelectionDuration(sessionType), 0, toInt(emomRoundsEt!!.text.toString()), sessionType)
            SessionType.HIIT
            -> SessionSkeleton(0,
                toInt(hiitSecondsWorkEt!!.text.toString()), toInt(hiitSecondsRestEt!!.text.toString()), toInt(hiitRoundsEt!!.text.toString()), sessionType)
            else -> throw IllegalArgumentException("invalid for custom workout types")
        }
    }

    private fun getCurrentSelectionDuration(sessionType: SessionType): Int {
        return when(sessionType) {
            SessionType.AMRAP, SessionType.FOR_TIME, SessionType.REST
            -> toInt(genericMinutesEt!!.text.toString()) * 60 + toInt(genericSecondsEt!!.text.toString())
            SessionType.EMOM -> toInt(emomMinutesEt!!.text.toString()) * 60 + toInt(emomSecondsEt!!.text.toString())
            else -> throw InvalidParameterException("wrong session type: $sessionType")
        }
    }

    fun resetToDefaults() {
        when (sessionType) {
            SessionType.AMRAP -> {
                //TODO: extract constants
                setEditTextValue(genericMinutesEt!!, "15")
                setEditTextValue(genericSecondsEt!!, "00")
            }
            SessionType.FOR_TIME -> {
                setEditTextValue(genericMinutesEt!!, "15")
                setEditTextValue(genericSecondsEt!!, "00")
            }
            SessionType.EMOM -> {
                setEditTextValue(emomRoundsEt!!, "10")
                setEditTextValue(emomMinutesEt!!, "01")
                setEditTextValue(emomSecondsEt!!, "00")
            }
            SessionType.HIIT -> {
                setEditTextValue(hiitRoundsEt!!, "08")
                setEditTextValue(hiitSecondsWorkEt!!, "20")
                setEditTextValue(hiitSecondsRestEt!!, "10")
            }
            SessionType.REST -> {
                setEditTextValue(genericMinutesEt!!, "01")
                setEditTextValue(genericSecondsEt!!, "00")
            }
            else -> { /* do nothing*/ }
        }
    }

    private fun setEditTextValue(textInputEditText: TextInputEditText, value: String) {
        textInputEditText.setText(value)
    }

    fun updateEditTexts(session: SessionSkeleton) {
        when (session.type) {
            SessionType.AMRAP, SessionType.FOR_TIME, SessionType.REST -> {
                val minutesAndSeconds = StringUtils.secondsToMinutesAndSeconds(session.duration)
                genericMinutesEt!!.setText(addPrefixIfNeeded(minutesAndSeconds.first.toString()))
                genericSecondsEt!!.setText(addPrefixIfNeeded(minutesAndSeconds.second.toString()))
            }
            SessionType.EMOM -> {
                emomRoundsEt!!.setText(addPrefixIfNeeded(session.numRounds.toString()))
                val minutesAndSeconds = StringUtils.secondsToMinutesAndSeconds(session.duration)
                emomMinutesEt!!.setText(addPrefixIfNeeded(minutesAndSeconds.first.toString()))
                emomSecondsEt!!.setText(addPrefixIfNeeded(minutesAndSeconds.second.toString()))
            }
            SessionType.HIIT -> {
                hiitRoundsEt!!.setText(addPrefixIfNeeded(session.numRounds.toString()))
                hiitSecondsWorkEt!!.setText(addPrefixIfNeeded(session.duration.toString()))
                hiitSecondsRestEt!!.setText(addPrefixIfNeeded(session.breakDuration.toString()))
            }
            else -> throw IllegalArgumentException("invalid for custom workout types")
        }
    }

    private fun addPrefixIfNeeded(value: String): String {
        return if (value.length == 1) {
            "0$value"
        } else {
            value
        }
    }
}