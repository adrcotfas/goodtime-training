package goodtime.training.wod.timer.ui.main.custom

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.*
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.data.repository.AppRepository
import goodtime.training.wod.timer.databinding.DialogAddSessionToCustomWorkoutBinding
import goodtime.training.wod.timer.databinding.SectionAddEditSessionBinding
import goodtime.training.wod.timer.databinding.SectionEditTextViewsBinding
import goodtime.training.wod.timer.ui.main.SessionEditTextHelper
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class AddEditSessionDialog : BottomSheetDialogFragment(), KodeinAware, SessionEditTextHelper.Listener {
    override val kodein by closestKodein()
    private val repo: AppRepository by instance()

    private lateinit var binding: DialogAddSessionToCustomWorkoutBinding
    private lateinit var sectionAddEdit: SectionAddEditSessionBinding
    private lateinit var sectionEditTexts: SectionEditTextViewsBinding

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
            } else {
                dialog.candidate = SessionSkeleton()
                dialog.candidate.type = SessionType.AMRAP
            }
            return dialog
        }
    }

    private fun isEditMode() = candidateIdx != INVALID_CANDIDATE_IDX

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogAddSessionToCustomWorkoutBinding.inflate(layoutInflater)
        sectionAddEdit = binding.sectionAddEdit
        sectionEditTexts = binding.sectionAddEdit.customSectionContainer
        this.inflater = inflater

        binding.title.text = if (isEditMode()) "Edit session" else "Add session"
        togglePositiveButtonState(false)

        setupButtons()
        initSessionEditTextHelper()
        setupSessionTypeChips()
        setupRadioGroup()

        return binding.root
    }

    private fun setupButtons() {
        binding.closeButton.setOnClickListener { dismiss() }
        binding.saveButton.setOnClickListener {
            val session = if (isInCustomSection()) sessionEditTextHelper.generateFromCurrentSelection() else candidate
            if (isEditMode()) {
                listener.onSessionEdit(candidateIdx, session)
            } else {
                listener.onSessionAdded(session)
            }
            dismiss()
            hideKeyboardFrom(requireContext(), binding.root)
        }
    }

    private fun initSessionEditTextHelper() {
        sessionEditTextHelper = SessionEditTextHelper(
                this,
                sectionEditTexts.genericMinutesLayout.editText,
                sectionEditTexts.genericSecondsLayout.editText,
                sectionEditTexts.intervalsRoundsLayout.editText,
                sectionEditTexts.intervalsMinutesLayout.editText,
                sectionEditTexts.intervalsSecondsLayout.editText,
                sectionEditTexts.hiitRoundsLayout.editText,
                sectionEditTexts.hiitSecondsWorkLayout.editText,
                sectionEditTexts.hiitSecondsRestLayout.editText,
                if (isEditMode()) candidate.type else SessionType.AMRAP
        )
    }

    private fun setupSessionTypeChips() {
        for (sessionType in SessionType.values()) {
            if (sessionType == SessionType.CUSTOM) {
                continue
            }
            val chip = inflater.inflate(R.layout.chip_choice_small, sectionAddEdit.sessionTypeChips, false) as Chip
            chip.apply {
                text = StringUtils.toString(sessionType)
                chipIcon = ResourcesHelper.getDrawableFor(sessionType)
                isChipIconVisible = true
                id = sessionType.ordinal
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        binding.sectionAddEdit.radioGroup.isVisible = true // hiding details until a session type is selected
                        togglePositiveButtonState(false)
                        refreshActiveSection(sessionType)
                        if (isInCustomSection()) {
                            binding.sectionAddEdit.customSectionContainer.customSection.isVisible = true
                            sessionEditTextHelper.resetToDefaults()
                            setDescription(StringUtils.toFavoriteDescriptionDetailed(sessionEditTextHelper.generateFromCurrentSelection()))
                        } else {
                            // hiding details until a session type is selected
                            binding.sectionAddEdit.favoritesContainer.isVisible = true
                        }
                        setupFavorites(sessionType)
                    }
                }
            }
            sectionAddEdit.sessionTypeChips.addView(chip)
        }
        if (isEditMode()){
            sectionAddEdit.sessionTypeChips.check(candidate.type.value)
        }
    }

    private fun setupRadioGroup() {
        sectionAddEdit.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radio_button_select_custom) {
                togglePositiveButtonState(true)
                sectionAddEdit.favoritesContainer.isVisible = false
                sectionAddEdit.customSectionContainer.customSection.isVisible = true
                sessionEditTextHelper.resetToDefaults()
                setDescription(StringUtils.toFavoriteDescriptionDetailed(sessionEditTextHelper.generateFromCurrentSelection()))
            } else if (checkedId == R.id.radio_button_from_favorites) {
                togglePositiveButtonState(false)
                sectionAddEdit.favoritesContainer.isVisible = true
                sectionAddEdit.customSectionContainer.customSection.isVisible = false
                hideKeyboardFrom(requireContext(), binding.root)
            }
        }
        if (isEditMode()) {
            sectionAddEdit.radioButtonSelectCustom.isChecked = true
            refreshActiveSection(candidate.type)
            sessionEditTextHelper.updateEditTexts(candidate)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupFavorites(sessionType: SessionType) {
        repo.getSessionSkeletons(sessionType).observe(
                this, {
            favorites = it
            val favoritesChipGroup = sectionAddEdit.favorites
            favoritesChipGroup.removeAllViews()

            for (favorite in favorites) {
                val chip = inflater.inflate(R.layout.chip_choice, favoritesChipGroup, false) as Chip
                chip.apply {
                    isCloseIconVisible = false
                    text = StringUtils.toFavoriteFormat(favorite)
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            candidate = favorite
                            togglePositiveButtonState(true)
                        }
                    }
                }
                favoritesChipGroup.addView(chip)
                binding.sectionAddEdit.emptyState.isVisible = false
            }
            if (favorites.isEmpty()) {
                binding.sectionAddEdit.emptyState.isVisible = true
            }
        })
    }

    private fun togglePositiveButtonState(enabled: Boolean) {
        binding.saveButton.isEnabled = enabled
    }

    private fun refreshActiveSection(sessionType: SessionType) {
        when (sessionType) {
            SessionType.AMRAP, SessionType.FOR_TIME, SessionType.REST -> {
                sectionEditTexts.genericSection.isVisible = true
                sectionEditTexts.intervalsSection.isVisible = false
                sectionEditTexts.hiitSection.isVisible = false
            }
            SessionType.INTERVALS -> {
                sectionEditTexts.genericSection.isVisible = false
                sectionEditTexts.intervalsSection.isVisible = true
                sectionEditTexts.hiitSection.isVisible = false
            }
            SessionType.HIIT -> {
                sectionEditTexts.genericSection.isVisible = false
                sectionEditTexts.intervalsSection.isVisible = false
                sectionEditTexts.hiitSection.isVisible = true
            }
            else -> {}
        }
        sessionEditTextHelper.sessionType = sessionType
        sectionEditTexts.customSessionDescription.isVisible = true
    }

    private fun setDescription(value: String) {
        sectionEditTexts.customSessionDescription.text = value
    }

    override fun onTextChanged(isValid: Boolean, sessionSkeleton: SessionSkeleton) {
        if (isInCustomSection()) togglePositiveButtonState(isValid)

        setDescription(
                if (isValid)
                    StringUtils.toFavoriteDescriptionDetailed(sessionSkeleton)
                else "Please enter valid values."
        )
    }

    private fun isInCustomSection() = sectionAddEdit.radioGroup.checkedRadioButtonId == R.id.radio_button_select_custom
}