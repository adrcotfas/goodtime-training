package goodtime.training.wod.timer.ui.stats

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.ResourcesHelper
import goodtime.training.wod.timer.common.StringUtils
import goodtime.training.wod.timer.common.hideKeyboardFrom
import goodtime.training.wod.timer.data.model.CustomWorkoutSkeleton
import goodtime.training.wod.timer.data.model.Session
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.data.repository.AppRepository
import goodtime.training.wod.timer.databinding.DialogAddToStatisticsBinding
import goodtime.training.wod.timer.databinding.SectionAddEditSessionBinding
import goodtime.training.wod.timer.ui.common.DatePickerDialogHelper
import goodtime.training.wod.timer.ui.common.TimePickerDialogBuilder
import goodtime.training.wod.timer.ui.main.SessionEditTextHelper
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import java.time.*

class AddEditCompletedWorkoutDialog : BottomSheetDialogFragment(), KodeinAware, SessionEditTextHelper.Listener, TimePickerDialogBuilder.Listener {
    override val kodein by closestKodein()
    private val repo: AppRepository by instance()

    private lateinit var binding: DialogAddToStatisticsBinding
    private lateinit var sectionAddEdit: SectionAddEditSessionBinding

    private lateinit var candidate: Session
    private lateinit var customWorkoutSelection: CustomWorkoutSkeleton

    private var candidateIdx = INVALID_CANDIDATE_IDX

    private lateinit var sessionEditTextHelper: SessionEditTextHelper
    private lateinit var inflater: LayoutInflater

    private fun isEditMode() = candidateIdx != INVALID_CANDIDATE_IDX

    companion object {
        const val INVALID_CANDIDATE_IDX = -1L

        fun newInstance(candidateIdx: Long = INVALID_CANDIDATE_IDX): AddEditCompletedWorkoutDialog {
            val dialog = AddEditCompletedWorkoutDialog()
            dialog.candidateIdx = candidateIdx
            if (!dialog.isEditMode()) {
                dialog.candidate = Session()
                dialog.candidate.skeleton.type = SessionType.AMRAP
            }
            return dialog
        }

        private fun millisToSecondOfDay(millis: Long): Long {
            return Instant.ofEpochMilli(millis)
                    .atZone(ZoneId.systemDefault()).toLocalTime().toSecondOfDay().toLong()
        }

        private fun millisToLocalDate(millis: Long): LocalDate {
            return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogAddToStatisticsBinding.inflate(layoutInflater)
        sectionAddEdit = binding.sectionAddEdit
        sectionAddEdit.customSessionDescription.isVisible = false

        this.inflater = inflater

        binding.title.text = if (isEditMode()) "Edit session" else "Add session"
        togglePositiveButtonState(false)

        if (isEditMode()) {
            repo.getSession(candidateIdx).observe(this, {
                candidate = it
                doSetup()
            })
        } else {
            doSetup()
        }
        return binding.root
    }

    private fun doSetup() {
        setupButtons()
        initSessionEditTextHelper()
        setupSessionTypeChips()
        setupRadioGroup()
        setupDateAndTimePickers()
    }

    private fun setupDateAndTimePickers() {
        val localTime = LocalTime.ofSecondOfDay(millisToSecondOfDay(candidate.timestamp))
        binding.editDate.text = StringUtils.formatDateLong(millisToLocalDate(candidate.timestamp))
        binding.editTime.text = StringUtils.formatTime(localTime)

        binding.editDate.setOnClickListener {
            val picker = DatePickerDialogHelper.buildDatePicker(candidate.timestamp)
            picker.addOnPositiveButtonClickListener {
                val localDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                candidate.timestamp = LocalDateTime.of(localDate, localTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                binding.editDate.text = StringUtils.formatDateLong(localDate)
            }
            picker.show(parentFragmentManager, "MaterialDatePicker")
        }

        binding.editTime.setOnClickListener {
            val dialog = TimePickerDialogBuilder(requireContext(), this)
                    .buildDialog(millisToSecondOfDay(candidate.timestamp).toInt())
            dialog.show(parentFragmentManager, "MaterialTimePicker")
        }
    }

    override fun onTimeSet(secondOfDay: Long) {
        val localDate = millisToLocalDate(candidate.timestamp)
        val localTime = LocalTime.ofSecondOfDay(secondOfDay)
        candidate.timestamp = LocalDateTime.of(localDate, localTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        binding.editTime.text = StringUtils.formatTime(localTime)
    }

    private fun setupButtons() {
        binding.closeButton.setOnClickListener { dismiss() }
        binding.saveButton.setOnClickListener {
            if (isEditMode()) {
                repo.editSession(candidate)
            } else {
                repo.addSession(candidate)
            }
            dismiss()
            hideKeyboardFrom(requireContext(), binding.root)
        }
    }

    private fun initSessionEditTextHelper() {
        sessionEditTextHelper = SessionEditTextHelper(
                this,
                sectionAddEdit.genericMinutesLayout.editText,
                sectionAddEdit.genericSecondsLayout.editText,
                sectionAddEdit.emomRoundsLayout.editText,
                sectionAddEdit.emomMinutesLayout.editText,
                sectionAddEdit.emomSecondsLayout.editText,
                sectionAddEdit.hiitRoundsLayout.editText,
                sectionAddEdit.hiitSecondsWorkLayout.editText,
                sectionAddEdit.hiitSecondsRestLayout.editText,
                if (isEditMode()) candidate.skeleton.type else SessionType.AMRAP
        )
    }

    private fun setupSessionTypeChips() {
        sectionAddEdit.sessionTypeChips
        for (sessionType in SessionType.values()) {
            if (sessionType == SessionType.REST) {
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
                        togglePositiveButtonState(false)
                        refreshActiveSection(sessionType)
                        if (isInCustomSection() && sessionType != SessionType.CUSTOM) {
                            sessionEditTextHelper.resetToDefaults()
                        }
                        toggleCustomWorkoutFavoritesView(sessionType == SessionType.CUSTOM)
                        setupFavorites(sessionType)
                    }
                }
            }
            sectionAddEdit.sessionTypeChips.addView(chip)
        }
        sectionAddEdit.sessionTypeChips.check(if (isEditMode()) candidate.skeleton.type.value else 0)
        if (!isEditMode()) refreshActiveSection(SessionType.AMRAP)
    }

    private fun setupRadioGroup() {
        sectionAddEdit.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radio_button_select_custom) {
                togglePositiveButtonState(true)
                sectionAddEdit.favoritesContainer.isVisible = false
                sectionAddEdit.customSection.isVisible = true
                if (candidate.skeleton.type != sessionEditTextHelper.sessionType) {
                    // this is to refresh new selections
                    // but to not refresh the edit texts when editing a session
                    sessionEditTextHelper.resetToDefaults()
                }
            } else if (checkedId == R.id.radio_button_from_favorites) {
                togglePositiveButtonState(false)
                sectionAddEdit.favoritesContainer.isVisible = true
                sectionAddEdit.customSection.isVisible = false
                hideKeyboardFrom(requireContext(), binding.root)
            }
        }
        if (isEditMode() && !candidate.isCustom()) {
            sectionAddEdit.radioButtonSelectCustom.isChecked = true
            refreshActiveSection(candidate.skeleton.type)
            sessionEditTextHelper.updateEditTexts(candidate.skeleton)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupFavorites(sessionType: SessionType) {
        val favorites =
                if (sessionType == SessionType.CUSTOM) {
                    repo.getCustomWorkoutSkeletons()
                } else {
                    repo.getSessionSkeletons(sessionType)
                }
        favorites.observe(
                this, {
            val favoritesChipGroup = sectionAddEdit.favorites
            favoritesChipGroup.removeAllViews()

            for (favorite in it) {
                val chip = inflater.inflate(R.layout.chip_choice, favoritesChipGroup, false) as Chip
                chip.apply {
                    isCloseIconVisible = false
                    text = if (favorite is SessionSkeleton) StringUtils.toFavoriteFormat(favorite) else (favorite as CustomWorkoutSkeleton).name
                    if (isEditMode() && candidate.isCustom() && text == candidate.name) {
                        isChecked = true
                        togglePositiveButtonState(true)
                    }
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            if (favorite is SessionSkeleton) candidate.skeleton = favorite
                            else {
                                //TODO: handle active time / isForTime here
                                customWorkoutSelection = favorite as CustomWorkoutSkeleton
                                candidate.name = customWorkoutSelection.name
                                candidate.isCustom()
                                candidate.skeleton.type = SessionType.CUSTOM
                            }
                            togglePositiveButtonState(true)
                        }
                    }
                }
                favoritesChipGroup.addView(chip)
                binding.sectionAddEdit.emptyState.isVisible = false
            }
            if (it.isEmpty()) {
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
                sectionAddEdit.genericSection.isVisible = true
                sectionAddEdit.emomSection.isVisible = false
                sectionAddEdit.hiitSection.isVisible = false
            }
            SessionType.EMOM -> {
                sectionAddEdit.genericSection.isVisible = false
                sectionAddEdit.emomSection.isVisible = true
                sectionAddEdit.hiitSection.isVisible = false
            }
            SessionType.HIIT -> {
                sectionAddEdit.genericSection.isVisible = false
                sectionAddEdit.emomSection.isVisible = false
                sectionAddEdit.hiitSection.isVisible = true
            }
            else -> {
            }
        }
        sessionEditTextHelper.sessionType = sessionType
    }

    private fun toggleCustomWorkoutFavoritesView(visible: Boolean) {
        if (visible) {
            sectionAddEdit.genericSection.isVisible = false
            sectionAddEdit.emomSection.isVisible = false
            sectionAddEdit.hiitSection.isVisible = false
            sectionAddEdit.radioButtonSelectCustom.isVisible = false
            sectionAddEdit.radioButtonFromFavorites.isChecked = true
        } else {
            sectionAddEdit.radioButtonSelectCustom.isVisible = true
        }
    }

    override fun onTextChanged(isValid: Boolean, sessionSkeleton: SessionSkeleton) {
        if (isInCustomSection()) {
            togglePositiveButtonState(isValid)
            candidate.skeleton = sessionEditTextHelper.generateFromCurrentSelection()
        }
    }

    private fun isInCustomSection() = sectionAddEdit.radioGroup.checkedRadioButtonId == R.id.radio_button_select_custom
}