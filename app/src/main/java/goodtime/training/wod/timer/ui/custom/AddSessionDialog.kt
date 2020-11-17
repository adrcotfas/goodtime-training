package goodtime.training.wod.timer.ui.custom

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.*
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.data.model.TypeConverter
import goodtime.training.wod.timer.data.repository.AppRepository
import goodtime.training.wod.timer.databinding.DialogAddSessionToCustomWorkoutBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import java.security.InvalidParameterException

class AddSessionDialog: DialogFragment(), KodeinAware {
    override val kodein by closestKodein()
    private val repo: AppRepository by instance()
    private lateinit var binding: DialogAddSessionToCustomWorkoutBinding
    private lateinit var listener: Listener

    private lateinit var favorites : List<SessionSkeleton>
    private var sessionType = SessionType.AMRAP

    private lateinit var genericMinutesEt: TextInputEditText
    private lateinit var genericSecondsEt: TextInputEditText
    private lateinit var emomRoundsEt: TextInputEditText
    private lateinit var emomMinutesEt: TextInputEditText
    private lateinit var emomSecondsEt: TextInputEditText
    private lateinit var hiitRoundsEt: TextInputEditText
    private lateinit var hiitSecondsWorkEt: TextInputEditText
    private lateinit var hiitSecondsRestEt: TextInputEditText

    interface Listener {
        fun onSessionAdded(session: SessionSkeleton)
    }

    companion object {
        fun newInstance(listener: Listener) : AddSessionDialog {
            val dialog = AddSessionDialog()
            dialog.listener = listener
            return dialog
        }
    }

    override fun onCreateDialog(savedInstBundle: Bundle?): Dialog {
        val b = AlertDialog.Builder(requireContext())
        binding = DialogAddSessionToCustomWorkoutBinding.inflate(layoutInflater)

        setupEditTexts()
        setupSpinner()
        setupRadioGroup()

        b.apply {
            setView(binding.root)
            setPositiveButton(android.R.string.ok) { _, _ ->
                listener.onSessionAdded(generateFromCurrentSelection(sessionType))
                hideKeyboardFrom(requireContext(), binding.root)
            }
        }
        return b.create()
    }

    private fun setupEditTexts() {
        genericMinutesEt = binding.genericMinutesLayout.editText
        genericSecondsEt = binding.genericSecondsLayout.editText

        emomRoundsEt = binding.emomRoundsLayout.editText
        emomMinutesEt = binding.emomMinutesLayout.editText
        emomSecondsEt = binding.emomSecondsLayout.editText

        hiitRoundsEt = binding.hiitRoundsLayout.editText
        hiitSecondsWorkEt = binding.secondsWorkLayout.editText
        hiitSecondsRestEt = binding.secondsRestLayout.editText

        setupTextEditSections()
    }

    private fun setupSpinner() {
        binding.sessionTypeSpinner.adapter =
            CustomAdapter(requireContext(), resources.getStringArray(R.array.session_types))
        binding.sessionTypeSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                sessionType = TypeConverter().fromInt(position)
                setupFavorites(sessionType)
                refreshEditTextSection(sessionType)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRadioGroup() {
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radio_button_select_custom) {
                binding.favoritesContainer.visibility = View.GONE
                binding.customSection.visibility = View.VISIBLE
                togglePositiveButtonVisibility(true)
                setDescription(StringUtils.toFavoriteDescriptionDetailed(generateFromCurrentSelection(sessionType)))
            } else if (checkedId == R.id.radio_button_from_favorites) {
                binding.favoritesContainer.visibility = View.VISIBLE
                binding.customSection.visibility = View.GONE
                togglePositiveButtonVisibility(false)
                hideKeyboardFrom(requireContext(), binding.root)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupFavorites(sessionType: SessionType) {
        repo.getSessionSkeletons(sessionType).observe(
            this, {
                favorites = it
                val favoritesChipGroup = binding.favorites
                favoritesChipGroup.isSingleSelection = true
                favoritesChipGroup.removeAllViews()
                for (favorite in favorites) {
                    val chip = Chip(requireContext()).apply {
                        text = StringUtils.toFavoriteFormat(favorite)
                        isCloseIconVisible = false
                        chipBackgroundColor = if (favorite.type == SessionType.REST) {
                            setTextColor(ResourcesHelper.red)
                            ColorStateList.valueOf(ResourcesHelper.darkRed)
                        } else {
                            setTextColor(ResourcesHelper.green)
                            ColorStateList.valueOf(ResourcesHelper.darkGreen)
                        }
                    }
                    chip.setOnClickListener {
                        listener.onSessionAdded(favorite)
                        dismiss()
                    }
                    favoritesChipGroup.addView(chip)
                }
                if (favorites.isEmpty()) {
                    // TODO: show empty state
                }
            })
    }

    private fun togglePositiveButtonVisibility(visible: Boolean) {
        val dialog = dialog as AlertDialog?
        if (dialog != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).visibility =
                if (visible) View.VISIBLE else View.GONE
        }
    }

    private fun togglePositiveButtonState(enabled: Boolean) {
        val dialog = dialog as AlertDialog?
        if (dialog != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = enabled
        }
    }

    private fun refreshActiveSection(sessionType: SessionType) {
        when (sessionType) {
            SessionType.AMRAP, SessionType.FOR_TIME, SessionType.REST -> {
                binding.genericSection.visibility = View.VISIBLE
                binding.emomSection.visibility = View.GONE
                binding.hiitSection.visibility = View.GONE
            }
            SessionType.EMOM -> {
                binding.genericSection.visibility = View.GONE
                binding.emomSection.visibility = View.VISIBLE
                binding.hiitSection.visibility = View.GONE
            }
            SessionType.TABATA -> {
                binding.genericSection.visibility = View.GONE
                binding.emomSection.visibility = View.GONE
                binding.hiitSection.visibility = View.VISIBLE
            }
        }
    }

    private fun setupTextEditSections() {
        for (it in listOf(genericMinutesEt, genericSecondsEt, emomRoundsEt,
            emomMinutesEt, emomSecondsEt, hiitRoundsEt, hiitSecondsWorkEt, hiitSecondsRestEt)) {
            setupEditTextBehaviorOnFocus(it)
        }
        for (it in listOf(genericSecondsEt, emomSecondsEt, hiitSecondsRestEt)) {
            it.imeOptions = EditorInfo.IME_ACTION_DONE
        }

        genericMinutesEt.addTextChangedListener {
            setupEditTextLimit(it, genericMinutesEt, 60)
            val minutes = toInt(it.toString())
            val seconds = toInt(genericSecondsEt.text.toString())
            val enabled = minutes != 0 || seconds != 0
            togglePositiveButtonState(enabled)
            setDescription(
                if (enabled)
                    StringUtils.toFavoriteDescriptionDetailed(
                        generateFromCurrentSelection(
                            sessionType
                        )
                    )
                else "Please enter valid values.")
        }

        genericSecondsEt.addTextChangedListener {
            setupEditTextLimit(it, genericSecondsEt, 59)
            val minutes = toInt(genericMinutesEt.text.toString())
            val seconds = toInt(it.toString())
            val enabled = minutes != 0 || seconds != 0
            togglePositiveButtonState(enabled)
            setDescription(
                if (enabled)
                    StringUtils.toFavoriteDescriptionDetailed(generateFromCurrentSelection(sessionType))
                else "Please enter valid values.")
        }
        emomRoundsEt.addTextChangedListener {
            setupEditTextLimit(it, emomRoundsEt, 60)
            val rounds = toInt(it.toString())
            val minutes = toInt(emomMinutesEt.text.toString())
            val seconds = toInt(emomSecondsEt.text.toString())
            val enabled = rounds != 0 && (minutes != 0 || seconds != 0)
            togglePositiveButtonState (enabled)
            setDescription(
                if (enabled)
                    StringUtils.toFavoriteDescriptionDetailed(generateFromCurrentSelection(sessionType))
                else "Please enter valid values.")
        }
        emomMinutesEt.addTextChangedListener {
            setupEditTextLimit(it, emomMinutesEt, 10)
            val rounds = toInt(emomRoundsEt.text.toString())
            val minutes = toInt(it.toString())
            val seconds = toInt(emomSecondsEt.text.toString())
            val enabled = rounds != 0 && (minutes != 0 || seconds != 0)
            togglePositiveButtonState (enabled)
            setDescription(
                if (enabled)
                    StringUtils.toFavoriteDescriptionDetailed(generateFromCurrentSelection(sessionType))
                else "Please enter valid values.")
        }
        emomSecondsEt.addTextChangedListener {
            setupEditTextLimit(it, emomSecondsEt, 59)
            val rounds = toInt(emomRoundsEt.text.toString())
            val minutes = toInt(emomMinutesEt.text.toString())
            val seconds = toInt(it.toString())
            val enabled = rounds != 0 && (minutes != 0 || seconds != 0)
            togglePositiveButtonState (enabled)
            setDescription(
                if (enabled)
                    StringUtils.toFavoriteDescriptionDetailed(generateFromCurrentSelection(sessionType))
                else "Please enter valid values.")
        }
        hiitRoundsEt.addTextChangedListener {
            setupEditTextLimit(it, hiitRoundsEt, 60)
            val rounds = toInt(it.toString())
            val secondsWork = toInt(hiitSecondsWorkEt.text.toString())
            val secondsRest = toInt(hiitSecondsRestEt.text.toString())
            val enabled = rounds != 0 && (secondsWork != 0 && secondsRest != 0)
            togglePositiveButtonState(enabled)
            setDescription(
                if (enabled)
                    StringUtils.toFavoriteDescriptionDetailed(generateFromCurrentSelection(sessionType))
                else "Please enter valid values.")
        }
        hiitSecondsWorkEt.addTextChangedListener {
            setupEditTextLimit(it, hiitSecondsWorkEt, 90)
            val rounds = toInt(hiitRoundsEt.text.toString())
            val secondsWork = toInt(it.toString())
            val secondsRest = toInt(hiitSecondsRestEt.text.toString())
            val enabled = rounds != 0 && (secondsWork != 0 && secondsRest != 0)
            togglePositiveButtonState(enabled)
            setDescription(
                if (enabled)
                    StringUtils.toFavoriteDescriptionDetailed(generateFromCurrentSelection(sessionType))
                else "Please enter valid values.")
        }
        hiitSecondsRestEt.addTextChangedListener {
            setupEditTextLimit(it, hiitSecondsRestEt, 90)
            val rounds = toInt(hiitRoundsEt.text.toString())
            val secondsWork = toInt(hiitSecondsWorkEt.text.toString())
            val secondsRest = toInt(it.toString())
            val enabled = rounds != 0 && (secondsWork != 0 && secondsRest != 0)
            togglePositiveButtonState(enabled)
            setDescription(
                if (enabled)
                    StringUtils.toFavoriteDescriptionDetailed(generateFromCurrentSelection(sessionType))
                else "Please enter valid values.")
        }
    }

    private fun refreshEditTextSection(sessionType: SessionType) {
        refreshActiveSection(sessionType)
        when (sessionType) {
            SessionType.AMRAP -> {
                //TODO: extract constants
                setEditTextValue(genericMinutesEt, "15")
                setEditTextValue(genericSecondsEt, "00")
            }
            SessionType.FOR_TIME -> {
                setEditTextValue(genericMinutesEt, "12")
                setEditTextValue(genericSecondsEt, "00")
            }
            SessionType.EMOM -> {
                setEditTextValue(emomRoundsEt, "10")
                setEditTextValue(emomMinutesEt, "01")
                setEditTextValue(emomSecondsEt, "00")
            }
            SessionType.TABATA -> {
                setEditTextValue(hiitRoundsEt, "08")
                setEditTextValue(hiitSecondsWorkEt, "20")
                setEditTextValue(hiitSecondsRestEt, "10")
            }
            SessionType.REST -> {
                setEditTextValue(genericMinutesEt, "01")
                setEditTextValue(genericSecondsEt, "00")
            }
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

    private fun setEditTextValue(textInputEditText: TextInputEditText, value: String) {
        textInputEditText.setText(value)
    }

    private fun generateFromCurrentSelection(sessionType: SessionType) : SessionSkeleton {
        return when(sessionType) {
            SessionType.AMRAP, SessionType.FOR_TIME
            -> SessionSkeleton(0,
                getCurrentSelectionDuration(sessionType), 0, 0, sessionType)
            SessionType.EMOM
            -> SessionSkeleton(0,
                getCurrentSelectionDuration(sessionType), 0, toInt(emomRoundsEt.text.toString()), sessionType)
            SessionType.TABATA
            -> SessionSkeleton(0,
                toInt(hiitSecondsWorkEt.text.toString()), toInt(hiitSecondsRestEt.text.toString()), toInt(hiitRoundsEt.text.toString()), sessionType)
            SessionType.REST -> SessionSkeleton(0,
                0, getCurrentSelectionDuration(sessionType), 0, sessionType)
        }
    }

    private fun getCurrentSelectionDuration(sessionType: SessionType): Int {
        return when(sessionType) {
            SessionType.AMRAP, SessionType.FOR_TIME, SessionType.REST
            -> toInt(genericMinutesEt.text.toString()) * 60 + toInt(genericSecondsEt.text.toString())
            SessionType.EMOM -> toInt(emomMinutesEt.text.toString()) * 60 + toInt(emomSecondsEt.text.toString())
            else -> throw InvalidParameterException("wrong session type: $sessionType")
        }
    }

    private fun isInFavoritesSection() =
        binding.radioGroup.checkedRadioButtonId == R.id.radio_button_from_favorites

    private fun setDescription(value: String) {
        binding.customSessionDescription.text = value
    }

    override fun onResume() {
        super.onResume()
        togglePositiveButtonVisibility(!isInFavoritesSection())
    }
}