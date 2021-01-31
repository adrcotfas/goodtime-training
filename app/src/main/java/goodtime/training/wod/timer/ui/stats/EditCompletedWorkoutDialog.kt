package goodtime.training.wod.timer.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import goodtime.training.wod.timer.common.*
import goodtime.training.wod.timer.data.model.CustomWorkoutSkeleton
import goodtime.training.wod.timer.data.model.Session
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.data.repository.AppRepository
import goodtime.training.wod.timer.databinding.DialogEditCompletedWorkoutBinding
import goodtime.training.wod.timer.ui.common.DatePickerDialogHelper
import goodtime.training.wod.timer.ui.common.TimePickerDialogBuilder
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class EditCompletedWorkoutDialog : BottomSheetDialogFragment(), KodeinAware,
    MinutesAndSecondsEditTexts.Listener {
    override val kodein by closestKodein()
    private val repo: AppRepository by instance()
    private lateinit var binding: DialogEditCompletedWorkoutBinding

    private var candidateIdx = -1L
    private lateinit var candidate: Session
    private lateinit var customWorkoutSelection: CustomWorkoutSkeleton
    private var minimumMinutesAndSeconds = -1
    private var maximumMinutesAndSeconds = -1

    private lateinit var activeTimeEts: MinutesAndSecondsEditTexts

    companion object {
        fun newInstance(candidateIdx: Long): EditCompletedWorkoutDialog {
            val dialog = EditCompletedWorkoutDialog()
            dialog.candidateIdx = candidateIdx
            return dialog
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogEditCompletedWorkoutBinding.inflate(inflater)

        repo.getSession(candidateIdx).observe(this, { session ->
            candidate = session
            if (candidate.isCustom() && candidate.name != null) {
                repo.getCustomWorkoutSkeleton(candidate.name!!).observe(this, { customWorkout ->
                    customWorkoutSelection = customWorkout
                    minimumMinutesAndSeconds =
                        Session.calculateMinimumToComplete(customWorkout.sessions)
                    maximumMinutesAndSeconds = Session.calculateTotal(customWorkout.sessions)
                    doSetup()
                })
            } else {
                minimumMinutesAndSeconds =
                    if (candidate.skeleton.type == SessionType.FOR_TIME) 1
                    else candidate.skeleton.getActualDuration()
                maximumMinutesAndSeconds = candidate.skeleton.getActualDuration()
                doSetup()
            }
        })
        return binding.root
    }

    private fun doSetup() {
        setupButtons()
        setupSessionDescription()
        setupIsCompleted()
        setupDateAndTimePickers()
        setupActiveTime()
        setupRoundsAndReps()
        setupNotes()
    }

    private fun setupRoundsAndReps() {
        binding.roundsLayout.editText.setText(candidate.actualRounds.toString())
        binding.enableRounds.setOnClickListener {
            binding.enableRounds.isChecked = !binding.enableRounds.isChecked
            binding.roundsSection.isVisible = binding.enableRounds.isChecked
        }
        binding.enableRounds.isChecked = candidate.actualRounds != 0 || candidate.actualReps != 0
        binding.roundsSection.isVisible = binding.enableRounds.isChecked
        binding.repsLayout.editText.setText(candidate.actualReps.toString())
    }

    private fun setupNotes() {
        binding.notesLayout.editText.setText(candidate.notes)
    }

    private fun setupIsCompleted() {
        binding.isCompleted.isChecked = candidate.isCompleted
        binding.isCompleted.setOnClickListener {
            binding.isCompleted.isChecked = !binding.isCompleted.isChecked
            if (binding.isCompleted.isChecked) {
                activeTimeEts.setMaximum(true)
            }
            activeTimeEts.isTimeBasedCompleted = binding.isCompleted.isChecked
        }
    }

    private fun setupButtons() {
        binding.saveButton.setOnClickListener {
            candidate.isCompleted = binding.isCompleted.isChecked
            candidate.actualDuration = activeTimeEts.getCurrentDuration()
            if (binding.enableRounds.isChecked) {
                candidate.actualRounds = toInt(binding.roundsLayout.editText.text.toString())
                candidate.actualReps = toInt(binding.repsLayout.editText.text.toString())
            } else {
                candidate.actualRounds = 0
                candidate.actualReps = 0
            }
            candidate.notes = binding.notesLayout.editText.text.toString()
            repo.editSession(candidate)
            dismiss()
        }
    }

    private fun setupActiveTime() {
        activeTimeEts = MinutesAndSecondsEditTexts(
            this,
            candidate.isCompleted,
            binding.activeTimeMinutes.editText,
            binding.activeTimeSeconds.editText,
            candidate.actualDuration,
            minimumMinutesAndSeconds,
            maximumMinutesAndSeconds
        )
    }

    private fun setupSessionDescription() {
        if (candidate.isCustom()) {
            // empty string for registered custom workouts whose skeleton was deleted
            binding.sessionDescription.text = candidate.name ?: "Custom workout"
            binding.icon.setImageDrawable(ResourcesHelper.getCustomWorkoutDrawable())
        } else {
            binding.icon.setImageDrawable(ResourcesHelper.getDrawableFor(candidate.skeleton.type))
            binding.sessionDescription.text =
                StringUtils.toFavoriteFormatExtended(candidate.skeleton)
        }
    }

    //TODO: move to common place - in DatePicker class
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

    override fun onValidityChanged(isValid: Boolean) {
        binding.saveButton.isEnabled = isValid
    }

    override fun onMaxTimeSet(isMaxTime: Boolean) {
        if (candidate.isTimeBased) return
        binding.isCompleted.isChecked = isMaxTime
    }

    override fun onForTimeMinimumConditionViolated() {
        binding.isCompleted.isChecked = false
    }
}