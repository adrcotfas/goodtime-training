package goodtime.training.wod.timer.ui.main.intervals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import goodtime.training.wod.timer.common.Events
import goodtime.training.wod.timer.common.StringUtils
import goodtime.training.wod.timer.common.calculateRowHeight
import goodtime.training.wod.timer.common.number_picker.NumberPicker
import goodtime.training.wod.timer.common.number_picker.NumberPicker.Companion.Color
import goodtime.training.wod.timer.common.number_picker.NumberPicker.Companion.PickerSize
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import goodtime.training.wod.timer.data.model.CustomWorkoutSkeleton
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.data.model.TypeConverter
import goodtime.training.wod.timer.databinding.FragmentIntervalsBinding
import goodtime.training.wod.timer.ui.main.CustomBalloonFactory
import goodtime.training.wod.timer.ui.main.WorkoutTypeFragment
import org.greenrobot.eventbus.EventBus
import org.kodein.di.generic.instance

class IntervalsFragment : WorkoutTypeFragment() {

    private val preferenceHelper: PreferenceHelper by instance()
    private val viewModelFactory: IntervalsViewModelFactory by instance()
    private lateinit var viewModel: IntervalsViewModel

    private lateinit var binding: FragmentIntervalsBinding
    private lateinit var minutePicker: NumberPicker
    private lateinit var secondsPicker: NumberPicker
    private lateinit var roundsPicker: NumberPicker

    private val minuteListener = object : NumberPicker.ScrollListener {
        override fun onScrollFinished(value: Int) {
            viewModel.data.setMinutes(value)
            EventBus.getDefault().post(Events.Companion.SetStartButtonState(true))
        }

        override fun onScroll() {
            EventBus.getDefault().post(Events.Companion.SetStartButtonState(false))
        }
    }

    private val secondsListener = object : NumberPicker.ScrollListener {
        override fun onScrollFinished(value: Int) {
            viewModel.data.setSeconds(value)
            EventBus.getDefault().post(Events.Companion.SetStartButtonState(true))
        }

        override fun onScroll() {
            EventBus.getDefault().post(Events.Companion.SetStartButtonState(false))
        }
    }

    private val roundsListener = object : NumberPicker.ScrollListener {
        override fun onScrollFinished(value: Int) {
            viewModel.data.setRounds(value)
            EventBus.getDefault().post(Events.Companion.SetStartButtonState(true))
        }

        override fun onScroll() {
            EventBus.getDefault().post(Events.Companion.SetStartButtonState(false))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory).get(IntervalsViewModel::class.java)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        binding = FragmentIntervalsBinding.inflate(inflater, container, false)

        val favoritesLd = viewModel.getFavorites()
        favoritesLd.observe(viewLifecycleOwner, { favorites ->
            val id = preferenceHelper.getCurrentFavoriteId(SessionType.INTERVALS)
            val idx = favorites.indexOfFirst { it.id == id }

            //TODO: extract defaults to constants
            val minutesAndSeconds = StringUtils.secondsToMinutesAndSeconds(if (idx != -1) favorites[idx].duration else 60)
            val rounds = if (idx != -1) favorites[idx].numRounds else 20
            viewModel.data = IntervalsSpinnerData(minutesAndSeconds.first, minutesAndSeconds.second, rounds)
            favoritesLd.removeObservers(viewLifecycleOwner)

            setupNumberPickers()

            viewModel.data.get().observe(
                    viewLifecycleOwner, { data ->
                val duration = data.first
                viewModel.session = SessionSkeleton(duration = duration, breakDuration = 0,
                        numRounds = data.second, type = SessionType.INTERVALS)
                updateMainButtonsState(duration)
            })
        })

        showBalloonsIfNeeded()

        return binding.root
    }

    private fun showBalloonsIfNeeded() {
        if (preferenceHelper.showIntervalsBalloons()) {
            preferenceHelper.setIntervalsBalloons(false)
            binding.separator1.post {
                val balloon = CustomBalloonFactory.create(
                        requireContext(), this,
                        "With INTERVALS you can select the number of rounds and their duration."
                )
                balloon.showAlignTop(binding.separator1)
            }
        }
    }

    private fun setupNumberPickers() {
        val rowHeight = calculateRowHeight(layoutInflater, PickerSize.MEDIUM)

        minutePicker = NumberPicker(
                requireContext(), binding.pickerMinutes,
                viewModel.minutesPickerData,
                viewModel.data.getMinutes(), rowHeight, textSize = PickerSize.MEDIUM, scrollListener = minuteListener
        )

        secondsPicker = NumberPicker(
                requireContext(), binding.pickerSeconds,
                viewModel.secondsPickerData,
                viewModel.data.getSeconds(), rowHeight, textSize = PickerSize.MEDIUM, scrollListener = secondsListener
        )

        roundsPicker = NumberPicker(
                requireContext(), binding.pickerRounds,
                viewModel.roundsPickerData,
                viewModel.data.getRounds(), rowHeight,
                textSize = PickerSize.MEDIUM,
                textColor = Color.NEUTRAL,
                prefixWithZero = true,
                scrollListener = roundsListener
        )
    }

    override fun onStartWorkout() {
        val action = IntervalsFragmentDirections.toWorkout(
                sessions =
                TypeConverter.toString(sessions = arrayOf(PreferenceHelper.generatePreWorkoutSession(preferenceHelper.getPreWorkoutCountdown()))
                        + getSelectedSessions().toTypedArray())
        )
        findNavController().navigate(action)
    }

    override fun getSelectedSessions(): ArrayList<SessionSkeleton> = arrayListOf(viewModel.session)

    override fun onFavoriteSelected(session: SessionSkeleton) {
        val duration = StringUtils.secondsToMinutesAndSeconds(session.duration)
        minutePicker.smoothScrollToValue(duration.first)
        secondsPicker.smoothScrollToValue(duration.second)
        roundsPicker.smoothScrollToValue(session.numRounds)
        preferenceHelper.setCurrentFavoriteId(SessionType.INTERVALS, session.id)
    }

    override fun onFavoriteSelected(workout: CustomWorkoutSkeleton) {/* Do nothing */
    }

    override fun onFavoriteDeleted(name: String) {/* Do nothing */
    }
}