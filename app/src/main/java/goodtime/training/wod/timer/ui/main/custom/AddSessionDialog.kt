package goodtime.training.wod.timer.ui.main.custom

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.chip.Chip
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.*
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.data.model.TypeConverter
import goodtime.training.wod.timer.data.repository.AppRepository
import goodtime.training.wod.timer.databinding.DialogAddSessionToCustomWorkoutBinding
import goodtime.training.wod.timer.ui.main.SessionEditTextHelper
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class AddSessionDialog: DialogFragment(), KodeinAware, SessionEditTextHelper.Listener {
    override val kodein by closestKodein()
    private val repo: AppRepository by instance()
    private lateinit var binding: DialogAddSessionToCustomWorkoutBinding
    private lateinit var listener: Listener
    private lateinit var candidateToEdit: SessionSkeleton
    private var candidateIdx = INVALID_CANDIDATE_IDX

    private lateinit var favorites : List<SessionSkeleton>
    private lateinit var sessionEditTextHelper: SessionEditTextHelper

    interface Listener {
        fun onSessionAdded(session: SessionSkeleton)
        fun onSessionEdit(idx: Int, session: SessionSkeleton)
    }

    companion object {
        const val INVALID_CANDIDATE_IDX = -1

        fun newInstance(listener: Listener, candidateIdx: Int = INVALID_CANDIDATE_IDX,
                        candidate: SessionSkeleton = SessionSkeleton()) : AddSessionDialog {
            val dialog = AddSessionDialog()
            dialog.listener = listener
            dialog.candidateIdx = candidateIdx
            if (dialog.isEditMode()) {
                dialog.candidateToEdit = candidate
            }
            return dialog
        }
    }

    private fun isEditMode() = candidateIdx != INVALID_CANDIDATE_IDX

    override fun onCreateDialog(savedInstBundle: Bundle?): Dialog {
        val b = AlertDialog.Builder(requireContext())
        binding = DialogAddSessionToCustomWorkoutBinding.inflate(layoutInflater)

        initSessionEditTextHelper()
        setupSpinner()
        setupRadioGroup()
        setupEditCandidate()

        b.apply {
            setView(binding.root)
            setTitle(if (isEditMode()) "Edit session" else "Add session")
            setPositiveButton(android.R.string.ok) { _, _ ->
                if(isEditMode()) {
                    listener.onSessionEdit(candidateIdx, sessionEditTextHelper.generateFromCurrentSelection())
                } else {
                    listener.onSessionAdded(sessionEditTextHelper.generateFromCurrentSelection())
                }
                hideKeyboardFrom(requireContext(), binding.root)
            }
        }
        return b.create()
    }

    private fun initSessionEditTextHelper() {
        sessionEditTextHelper = SessionEditTextHelper(this,
            binding.genericMinutesLayout.editText,
            binding.genericSecondsLayout.editText,
            binding.emomRoundsLayout.editText,
            binding.emomMinutesLayout.editText,
            binding.emomSecondsLayout.editText,
            binding.hiitRoundsLayout.editText,
            binding.hiitSecondsWorkLayout.editText,
            binding.hiitSecondsRestLayout.editText,
            if (isEditMode()) candidateToEdit.type else SessionType.AMRAP)
    }

    private fun setupSpinner() {
        binding.sessionTypeSpinner.adapter =
            SessionTypeSpinnerAdapter(requireContext(), resources.getStringArray(R.array.session_types))
        binding.sessionTypeSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val sessionType = TypeConverter().fromInt(position)
                sessionEditTextHelper.updateSessionType(sessionType)
                setupFavorites(sessionType)
                refreshActiveSection(sessionType)
                if (isEditMode() && sessionType == candidateToEdit.type) {
                    setupEditCandidate()
                } else {
                    sessionEditTextHelper.resetToDefaults()
                }
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
                setDescription(StringUtils.toFavoriteDescriptionDetailed(sessionEditTextHelper.generateFromCurrentSelection()))
            } else if (checkedId == R.id.radio_button_from_favorites) {
                binding.favoritesContainer.visibility = View.VISIBLE
                binding.customSection.visibility = View.GONE
                togglePositiveButtonVisibility(false)
                hideKeyboardFrom(requireContext(), binding.root)
            }
        }
    }

    private fun setupEditCandidate() {
        if (isEditMode()) {
            binding.radioGroup.check(R.id.radio_button_select_custom)
            binding.sessionTypeSpinner.setSelection(candidateToEdit.type.value)
            sessionEditTextHelper.updateEditTexts(candidateToEdit)
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
                        if (isEditMode()) {
                            listener.onSessionEdit(candidateIdx, favorite)
                        } else {
                            listener.onSessionAdded(favorite)
                        }
                        dismiss()
                    }
                    favoritesChipGroup.addView(chip)
                    binding.emptyState.visibility = View.GONE
                }
                if (favorites.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
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

    private fun isInFavoritesSection() =
        binding.radioGroup.checkedRadioButtonId == R.id.radio_button_from_favorites

    private fun setDescription(value: String) {
        binding.customSessionDescription.text = value
    }

    override fun onResume() {
        super.onResume()
        togglePositiveButtonVisibility(!isInFavoritesSection())
    }

    override fun onTextChanged(isValid: Boolean, sessionSkeleton: SessionSkeleton) {
        togglePositiveButtonState(isValid)
        setDescription(
            if (isValid)
                StringUtils.toFavoriteDescriptionDetailed(sessionSkeleton)
            else "Please enter valid values.")
    }
}