package goodtime.training.wod.timer.ui.main.custom

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
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


//TODO: make the view setup and updates more efficient, especially the Custom section
class AddEditSessionDialog : BottomSheetDialogFragment(), KodeinAware, SessionEditTextHelper.Listener {
    override val kodein by closestKodein()
    private val repo: AppRepository by instance()
    private lateinit var binding: DialogAddSessionToCustomWorkoutBinding
    private lateinit var listener: Listener
    private lateinit var candidate: SessionSkeleton
    private var candidateIdx = INVALID_CANDIDATE_IDX

    private lateinit var favorites: List<SessionSkeleton>
    private lateinit var sessionEditTextHelper: SessionEditTextHelper
    private lateinit var inflater: LayoutInflater

    interface Listener {
        fun onSessionAdded(session: SessionSkeleton)
        fun onSessionEdit(idx: Int, session: SessionSkeleton)
    }

    companion object {
        const val INVALID_CANDIDATE_IDX = -1

        fun newInstance(
                listener: Listener, candidateIdx: Int = INVALID_CANDIDATE_IDX,
                candidate: SessionSkeleton = SessionSkeleton()
        ): AddEditSessionDialog {
            val dialog = AddEditSessionDialog()
            dialog.listener = listener
            dialog.candidateIdx = candidateIdx
            if (dialog.isEditMode()) {
                dialog.candidate = candidate
            }
            return dialog
        }
    }

    private fun isEditMode() = candidateIdx != INVALID_CANDIDATE_IDX


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogAddSessionToCustomWorkoutBinding.inflate(layoutInflater)
        this.inflater = inflater
        binding.title.text = if (isEditMode()) "Edit session" else "Add session"
        togglePositiveButtonState(false)

        setupButtons()
        initSessionEditTextHelper()
        setupSpinner()
        setupRadioGroup()
        setupEditCandidate()

        return binding.root
    }

    private fun setupButtons() {
        binding.closeButton.setOnClickListener { dismiss() }
        binding.saveButton.setOnClickListener {
            if (isEditMode()) {
                listener.onSessionEdit(
                        candidateIdx,
                        if (isInCustomSection()) sessionEditTextHelper.generateFromCurrentSelection()
                        else candidate)
            } else {
                listener.onSessionAdded(sessionEditTextHelper.generateFromCurrentSelection())
            }
            dismiss()
            hideKeyboardFrom(requireContext(), binding.root)
        }
    }

    private fun initSessionEditTextHelper() {
        sessionEditTextHelper = SessionEditTextHelper(
                this,
                binding.genericMinutesLayout.editText,
                binding.genericSecondsLayout.editText,
                binding.emomRoundsLayout.editText,
                binding.emomMinutesLayout.editText,
                binding.emomSecondsLayout.editText,
                binding.hiitRoundsLayout.editText,
                binding.hiitSecondsWorkLayout.editText,
                binding.hiitSecondsRestLayout.editText,
                if (isEditMode()) candidate.type else SessionType.AMRAP
        )
    }

    private fun setupSpinner() {
        binding.sessionTypeSpinner.adapter =
                SessionTypeSpinnerAdapter(
                        requireContext(),
                        resources.getStringArray(R.array.session_types)
                )
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
                if (isEditMode() && sessionType == candidate.type) {
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
                togglePositiveButtonState(true)
                binding.favoritesContainer.isVisible = false
                binding.customSection.isVisible = true
                setDescription(StringUtils.toFavoriteDescriptionDetailed(sessionEditTextHelper.generateFromCurrentSelection()))
            } else if (checkedId == R.id.radio_button_from_favorites) {
                togglePositiveButtonState(false)
                binding.favoritesContainer.isVisible = true
                binding.customSection.isVisible = false
                hideKeyboardFrom(requireContext(), binding.root)
            }
        }
    }

    private fun setupEditCandidate() {
        if (isEditMode()) {
            binding.sessionTypeSpinner.setSelection(candidate.type.value)
            sessionEditTextHelper.updateEditTexts(candidate)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupFavorites(sessionType: SessionType) {
        repo.getSessionSkeletons(sessionType).observe(
                this, {
            favorites = it
            val favoritesChipGroup = binding.favorites
            favoritesChipGroup.removeAllViews()

            for (favorite in favorites) {
                val chip = inflater.inflate(R.layout.choice_chip, favoritesChipGroup, false) as Chip
                chip.apply {
                    text = StringUtils.toFavoriteFormat(favorite)
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            candidate = favorite
                            togglePositiveButtonState(true)
                        }
                    }
                }
                favoritesChipGroup.addView(chip)
                binding.emptyState.isVisible = false
            }
            if (favorites.isEmpty()) {
                binding.emptyState.isVisible = true
            }
        })
    }

    private fun togglePositiveButtonState(enabled: Boolean) {
        binding.saveButton.isEnabled = enabled
    }

    private fun refreshActiveSection(sessionType: SessionType) {
        when (sessionType) {
            SessionType.AMRAP, SessionType.FOR_TIME, SessionType.REST -> {
                binding.genericSection.isVisible = true
                binding.emomSection.isVisible = false
                binding.hiitSection.isVisible = false
            }
            SessionType.EMOM -> {
                binding.genericSection.isVisible = false
                binding.emomSection.isVisible = true
                binding.hiitSection.isVisible = false
            }
            SessionType.HIIT -> {
                binding.genericSection.isVisible = false
                binding.emomSection.isVisible = false
                binding.hiitSection.isVisible = true
            }
        }
        binding.customSessionDescription.isVisible = true
    }

    private fun setDescription(value: String) {
        binding.customSessionDescription.text = value
    }

    override fun onTextChanged(isValid: Boolean, sessionSkeleton: SessionSkeleton) {
        if (isInCustomSection()) togglePositiveButtonState(isValid)

        setDescription(
                if (isValid)
                    StringUtils.toFavoriteDescriptionDetailed(sessionSkeleton)
                else "Please enter valid values."
        )
    }

    private fun isInCustomSection() = binding.radioGroup.checkedRadioButtonId == R.id.radio_button_select_custom
}