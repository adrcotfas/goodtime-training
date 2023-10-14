package goodtime.training.wod.timer.ui.main.amrap_for_time

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import goodtime.training.wod.timer.common.Events
import goodtime.training.wod.timer.common.StringUtils
import goodtime.training.wod.timer.common.calculateRowHeight
import goodtime.training.wod.timer.common.number_picker.NumberPicker
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import goodtime.training.wod.timer.data.model.CustomWorkoutSkeleton
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.data.model.TypeConverter
import goodtime.training.wod.timer.databinding.FragmentAmrapForTimeBinding
import goodtime.training.wod.timer.ui.main.CustomBalloonFactory
import goodtime.training.wod.timer.ui.main.WorkoutTypeFragment
import org.greenrobot.eventbus.EventBus
import org.kodein.di.generic.instance

open class MinutesAndSecondsFragment<ViewModelType : MinutesAndSecondsViewModel>(
    private val sessionType: SessionType
) : WorkoutTypeFragment() {

    private var pickersAreSetup = false
    private val preferenceHelper: PreferenceHelper by instance()
    protected lateinit var viewModel: ViewModelType

    private lateinit var binding: FragmentAmrapForTimeBinding
    private lateinit var minutePicker: NumberPicker
    private lateinit var secondsPicker: NumberPicker

    private val minuteListener = object : NumberPicker.ScrollListener {
        override fun onScrollFinished(value: Int) {
            viewModel.timeData.setMinutes(value)
            EventBus.getDefault().post(Events.Companion.SetStartButtonState(true))
        }

        override fun onScroll() {
            EventBus.getDefault().post(Events.Companion.SetStartButtonState(false))
        }
    }

    private val secondsListener = object : NumberPicker.ScrollListener {
        override fun onScrollFinished(value: Int) {
            viewModel.timeData.setSeconds(value)
            EventBus.getDefault().post(Events.Companion.SetStartButtonState(true))
        }

        override fun onScroll() {
            EventBus.getDefault().post(Events.Companion.SetStartButtonState(false))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAmrapForTimeBinding.inflate(inflater, container, false)

        val favoritesLd = viewModel.getFavorites()
        favoritesLd.observe(viewLifecycleOwner, { favorites ->
            val id = preferenceHelper.getCurrentFavoriteId(sessionType)
            val idx = favorites.indexOfFirst { it.id == id }

            val minutesAndSeconds = StringUtils.secondsToMinutesAndSeconds(if (idx != -1) favorites[idx].duration else 900)
            viewModel.timeData = TimeSpinnerData(minutesAndSeconds.first, minutesAndSeconds.second)
            favoritesLd.removeObservers(viewLifecycleOwner)

            setupNumberPickers()

            viewModel.timeData.get().observe(
                viewLifecycleOwner, { duration ->
                    viewModel.session = SessionSkeleton(
                        duration = duration,
                        breakDuration = 0,
                        numRounds = 0,
                        type = sessionType
                    )
                    updateMainButtonsState(duration)
                }
            )
        })

        showBalloonsIfNeeded()

        return binding.root
    }

    private fun showBalloonsIfNeeded() {
        if (!isAdded) {
            return
        }
        if (sessionType == SessionType.FOR_TIME && preferenceHelper.showForTimeBalloons()) {
            preferenceHelper.setForTimeBalloons(false)
            binding.pickerSeparator.post {
                val balloon = CustomBalloonFactory.create(
                    requireContext(), this,
                    "FOR TIME enforces a time cap and the goal is to complete the workout as fast as possible."
                )
                val anotherBalloon = CustomBalloonFactory.create(
                    requireContext(), this,
                    "Use the time pickers to change the time cap.",
                    false, 0.5f
                )
                balloon.relayShowAlignTop(anotherBalloon, binding.topGuideline)
                balloon.showAlignTop(binding.bottomGuideline)
            }
        } else if (sessionType == SessionType.AMRAP && preferenceHelper.showMainBalloons()) {
            preferenceHelper.setMainBalloons(false)
            binding.root.post {
                val bottomMenuBalloon = CustomBalloonFactory.create(
                    requireContext(), this,
                    "Use the bottom menu to change the workout type."
                )
                val amrapBalloon = CustomBalloonFactory.create(
                    requireContext(), this,
                    "The goal for AMRAP workouts is to complete as many rounds as possible in the allocated time.",
                    false, 0.05f
                )
                val timePickersBalloon = CustomBalloonFactory.create(
                    requireContext(), this,
                    "Use the time pickers to change the duration.",
                    false, 0.5f
                )
                val favoriteButtonBalloon = CustomBalloonFactory.create(
                    requireContext(), this,
                    "Use the favorites section to save, remove and load timer presets.",
                    true, 0.83f
                )
                val startButtonBalloon = CustomBalloonFactory.create(
                    requireContext(), this,
                    "Press the action button to start the workout using the current selection.",
                    false, 0.5f
                )
                bottomMenuBalloon.relayShowAlignBottom(amrapBalloon, binding.bottomGuideline)
                    .relayShowAlignTop(timePickersBalloon, binding.pickerSeparator)
                    .relayShowAlignTop(favoriteButtonBalloon, binding.topGuideline)
                    .relayShowAlignBottom(startButtonBalloon, binding.startButtonGuideline)
                bottomMenuBalloon.showAlignBottom(binding.bottomGuideline)
            }
        }
    }

    private fun setupNumberPickers() {
        val rowHeight = calculateRowHeight(layoutInflater)
        minutePicker = NumberPicker(
            requireContext(), binding.pickerMinutes,
            viewModel.minutesPickerData,
            viewModel.timeData.getMinutes(), rowHeight, scrollListener = minuteListener
        )
        secondsPicker = NumberPicker(
            requireContext(), binding.pickerSeconds,
            viewModel.secondsPickerData,
            viewModel.timeData.getSeconds(), rowHeight, scrollListener = secondsListener
        )
    }

    override fun onStartWorkout() {
        val action = AmrapFragmentDirections.toWorkout(
            sessions =
            TypeConverter.toString(
                sessions = arrayOf(
                    PreferenceHelper.generatePreWorkoutSession(preferenceHelper.getPreWorkoutCountdown())
                )
                        + getSelectedSessions().toTypedArray()
            )
        )
        findNavController().navigate(action)
    }

    override fun getSelectedSessions(): List<SessionSkeleton> = arrayListOf(viewModel.session)

    //TODO: extract the two functions to a common interface;
    //use the viewModel to access the repo and get the corresponding workout or session
    override fun onFavoriteSelected(session: SessionSkeleton) {
        val duration = StringUtils.secondsToMinutesAndSeconds(session.duration)
        minutePicker.smoothScrollToValue(duration.first)
        secondsPicker.smoothScrollToValue(duration.second)
        preferenceHelper.setCurrentFavoriteId(sessionType, session.id)
    }

    //TODO: extract the two functions to a common interface;
    //use the viewModel to access the repo and get the corresponding workout or session
    override fun onFavoriteSelected(workout: CustomWorkoutSkeleton) {/* Do nothing */
    }

    override fun onFavoriteDeleted(name: String) {/* Do nothing */
    }
}