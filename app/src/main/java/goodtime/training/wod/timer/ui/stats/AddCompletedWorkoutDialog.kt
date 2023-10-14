package goodtime.training.wod.timer.ui.stats

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.*
import goodtime.training.wod.timer.data.model.Session
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.data.repository.AppRepository
import goodtime.training.wod.timer.databinding.DialogAddToStatisticsBinding
import goodtime.training.wod.timer.databinding.SectionEditTextViewsBinding
import goodtime.training.wod.timer.ui.common.DatePickerDialogHelper
import goodtime.training.wod.timer.ui.common.TimePickerDialogBuilder
import goodtime.training.wod.timer.ui.main.SessionEditTextHelper
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import java.time.*

class AddCompletedWorkoutDialog : BottomSheetDialogFragment(), KodeinAware,
    SessionEditTextHelper.Listener,
    MinutesAndSecondsEditTexts.Listener {
    override val kodein by closestKodein()
    private val repo: AppRepository by instance()

    private lateinit var binding: DialogAddToStatisticsBinding
    private lateinit var sectionEditTexts: SectionEditTextViewsBinding

    private lateinit var sessionEditTextHelper: SessionEditTextHelper
    private lateinit var activeTimeEts: MinutesAndSecondsEditTexts

    private lateinit var inflater: LayoutInflater

    private var candidate = Session()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogAddToStatisticsBinding.inflate(layoutInflater)
        sectionEditTexts = binding.sectionEditTextViews
        this.inflater = inflater

        togglePositiveButtonState(false)

        doSetup()
        return binding.root
    }

    private fun doSetup() {
        binding.favoritesContainer.isVisible = false
        setupButtons()
        initSessionEditTextHelper()
        setupSessionTypeChips()
        setupDateAndTimePickers()
        setupFavorites()
        setupRoundsAndReps()
        sectionEditTexts.customSessionDescription.isVisible = false
    }

    private fun setupDateAndTimePickers() {
        val localTime = LocalTime.ofSecondOfDay(TimeUtils.millisToSecondOfDay(candidate.timestamp))
        binding.editDate.text =
            TimeUtils.formatDateLong(TimeUtils.millisToLocalDate(candidate.timestamp))
        binding.editTime.text = TimeUtils.formatTime(localTime)

        binding.editDate.setOnClickListener {
            val picker = DatePickerDialogHelper.buildDatePicker(candidate.timestamp)
            picker.addOnPositiveButtonClickListener {
                val localDate =
                    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                candidate.timestamp =
                    LocalDateTime.of(localDate, localTime).atZone(ZoneId.systemDefault())
                        .toInstant().toEpochMilli()
                binding.editDate.text = TimeUtils.formatDateLong(localDate)
            }
            picker.show(parentFragmentManager, "MaterialDatePicker")
        }

        binding.editTime.setOnClickListener {
            val dialog = TimePickerDialogBuilder(requireContext())
                .buildDialog(TimeUtils.millisToSecondOfDay(candidate.timestamp).toInt())
            dialog.addOnPositiveButtonClickListener {
                val newValue = LocalTime.of(dialog.hour, dialog.minute).toSecondOfDay()
                val localDate = TimeUtils.millisToLocalDate(candidate.timestamp)
                val newLocalTime = LocalTime.ofSecondOfDay(newValue.toLong())
                candidate.timestamp =
                    LocalDateTime.of(localDate, newLocalTime).atZone(ZoneId.systemDefault())
                        .toInstant().toEpochMilli()
                binding.editTime.text = TimeUtils.formatTime(newLocalTime)
            }
            dialog.show(parentFragmentManager, "MaterialTimePicker")
        }
    }

    private fun setupButtons() {
        binding.closeButton.setOnClickListener { dismiss() }
        binding.saveButton.setOnClickListener {
            if (binding.enableRounds.isChecked) {
                candidate.actualRounds = toInt(binding.roundsLayout.editText.text.toString())
                candidate.actualReps = toInt(binding.repsLayout.editText.text.toString())
            } else {
                candidate.actualRounds = 0
                candidate.actualReps = 0
            }
            if (candidate.isCustom() && candidate.isTimeBased) {
                candidate.actualDuration = activeTimeEts.getCurrentDuration()
            }
            candidate.notes = binding.notesLayout.editText.text.toString()
            lifecycleScope.launch {
                repo.addSession(candidate)
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
            SessionType.AMRAP
        )
        sessionEditTextHelper.resetToDefaults()
    }

    private fun setupSessionTypeChips() {
        for (sessionType in SessionType.values()) {
            if (sessionType == SessionType.REST) {
                continue
            }
            val chip = inflater.inflate(
                R.layout.chip_choice_small,
                binding.sessionTypeChips,
                false
            ) as Chip
            chip.apply {
                text = StringUtils.toString(sessionType)
                chipIcon = ResourcesHelper.getDrawableFor(sessionType)
                isChipIconVisible = true
                id = sessionType.ordinal
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        togglePositiveButtonState(false)
                        refreshActiveSection(sessionType)
                        if (sessionType != SessionType.CUSTOM) {
                            sessionEditTextHelper.resetToDefaults()
                        }

                        toggleCustomWorkoutFavoritesView(sessionType == SessionType.CUSTOM)
                        candidate.skeleton.type = sessionType
                    }
                }
            }
            binding.sessionTypeChips.addView(chip)
        }
        binding.sessionTypeChips.check(0)
        refreshActiveSection(SessionType.AMRAP)
    }

    @SuppressLint("SetTextI18n")
    private fun setupFavorites() {
        val favoritesLd = repo.getCustomWorkoutSkeletons()
        favoritesLd.observe(this) { favorites ->
            val favoritesChipGroup = binding.favorites
            favoritesChipGroup.removeAllViews()

            for (favorite in favorites) {
                val chip =
                    inflater.inflate(R.layout.chip_choice, favoritesChipGroup, false) as Chip
                chip.apply {
                    isCloseIconVisible = false
                    text = favorite.name
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            candidate.skeleton = SessionSkeleton()
                            candidate.name = favorite.name
                            candidate.skeleton.type = SessionType.CUSTOM

                            candidate.actualDuration = Session.calculateTotal(favorite.sessions)
                            candidate.isTimeBased =
                                favorite.sessions.find { it.type == SessionType.FOR_TIME } != null

                            binding.activeTimeTopSeparator.isVisible = candidate.isTimeBased
                            binding.activeTimeSection.isVisible = candidate.isTimeBased
                            if (candidate.isTimeBased) {
                                activeTimeEts = MinutesAndSecondsEditTexts(
                                    this@AddCompletedWorkoutDialog,
                                    minutesEt = binding.activeTimeMinutes.editText,
                                    secondsEt = binding.activeTimeSeconds.editText,
                                    actualDuration = candidate.actualDuration,
                                    min = Session.calculateMinimumToComplete(favorite.sessions),
                                    max = Session.calculateTotal(favorite.sessions)
                                )
                            }
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
        }
    }

    private fun togglePositiveButtonState(enabled: Boolean) {
        binding.saveButton.isEnabled = enabled
    }

    private fun refreshActiveSection(sessionType: SessionType) {
        when (sessionType) {
            SessionType.AMRAP, SessionType.FOR_TIME -> {
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

            else -> {
            }
        }
        sessionEditTextHelper.sessionType = sessionType
    }

    private fun toggleCustomWorkoutFavoritesView(visible: Boolean) {
        binding.favoritesContainer.isVisible = visible
        if (visible) {
            sectionEditTexts.genericSection.isVisible = false
            sectionEditTexts.intervalsSection.isVisible = false
            sectionEditTexts.hiitSection.isVisible = false
        }
    }

    private fun setupRoundsAndReps() {
        binding.roundsSection.isVisible = false
        binding.roundsLayout.editText.setText("0")
        binding.repsLayout.editText.setText("0")
        binding.enableRounds.setOnClickListener {
            binding.enableRounds.isChecked = !binding.enableRounds.isChecked
            binding.roundsSection.isVisible = binding.enableRounds.isChecked
        }
    }

    override fun onTextChanged(isValid: Boolean, sessionSkeleton: SessionSkeleton) {
        togglePositiveButtonState(isValid)
        if (isValid) {
            candidate.skeleton = sessionSkeleton
            candidate.actualDuration = candidate.skeleton.getActualDuration()
        }
    }

    override fun onValidityChanged(isValid: Boolean) {} // do nothing
    override fun onMaxTimeSet(isMaxTime: Boolean) {} // do nothing
    override fun onForTimeMinimumConditionViolated() {} // do nothing
}