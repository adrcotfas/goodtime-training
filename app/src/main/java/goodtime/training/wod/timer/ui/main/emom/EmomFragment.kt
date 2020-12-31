package goodtime.training.wod.timer.ui.main.emom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
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
import goodtime.training.wod.timer.databinding.FragmentEmomBinding
import goodtime.training.wod.timer.ui.main.CustomBalloonFactory
import goodtime.training.wod.timer.ui.main.WorkoutTypeFragment
import org.kodein.di.generic.instance

class EmomFragment : WorkoutTypeFragment() {

    private val preferenceHelper: PreferenceHelper by instance()
    private val viewModelFactory: EmomViewModelFactory by instance()
    private lateinit var viewModel: EmomViewModel

    private lateinit var binding: FragmentEmomBinding
    private lateinit var minutePicker: NumberPicker
    private lateinit var secondsPicker: NumberPicker
    private lateinit var roundsPicker: NumberPicker

    private val minuteListener = object: NumberPicker.ScrollListener {
        override fun onScroll(value: Int) { viewModel.emomData.setMinutes(value) }
    }

    private val secondsListener = object: NumberPicker.ScrollListener {
        override fun onScroll(value: Int) { viewModel.emomData.setSeconds(value) }
    }

    private val roundsListener = object: NumberPicker.ScrollListener {
        override fun onScroll(value: Int) { viewModel.emomData.setRounds(value) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory).get(EmomViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentEmomBinding.inflate(inflater, container, false)

        val favoritesLd = viewModel.getFavorites()
        favoritesLd.observe(viewLifecycleOwner, { favorites ->
            val id = preferenceHelper.getCurrentFavoriteId(SessionType.EMOM)
            val idx = favorites.indexOfFirst{it.id == id}

            //TODO: extract defaults to constants
            val minutesAndSeconds = StringUtils.secondsToMinutesAndSeconds(if (idx != -1) favorites[idx].duration else 60)
            val rounds = if (idx != -1) favorites[idx].numRounds else 20
            viewModel.emomData = EmomSpinnerData(minutesAndSeconds.first, minutesAndSeconds.second, rounds)
            favoritesLd.removeObservers(viewLifecycleOwner)

            setupNumberPickers()

            viewModel.emomData.get().observe(
                viewLifecycleOwner, { data ->
                    val duration = data.first
                    viewModel.session = SessionSkeleton(duration = duration, breakDuration = 0,
                        numRounds = data.second, type = SessionType.EMOM)
                    updateMainButtonsState(duration)
                }
            )
        })

        showBalloonsIfNeeded()

        return binding.root
    }

    private fun showBalloonsIfNeeded() {
        if (preferenceHelper.showIntervalsBalloons()) {
            binding.separator1.post {
                val balloon = CustomBalloonFactory.create(
                        requireContext(), this,
                        "With INTERVALS you can select the number of rounds and their duration."
                )
                balloon.setOnBalloonClickListener { preferenceHelper.setIntervalsBalloons(false) }
                balloon.setOnBalloonOverlayClickListener { preferenceHelper.setIntervalsBalloons(false) }
                balloon.showAlignTop(binding.separator1)
            }
        }
    }

    private fun setupNumberPickers() {
        val rowHeight = calculateRowHeight(layoutInflater, PickerSize.MEDIUM)

        minutePicker = NumberPicker(
            requireContext(), binding.pickerMinutes,
            viewModel.minutesPickerData,
            viewModel.emomData.getMinutes(), rowHeight, textSize = PickerSize.MEDIUM, scrollListener = minuteListener
        )

        secondsPicker = NumberPicker(
            requireContext(), binding.pickerSeconds,
            viewModel.secondsPickerData,
            viewModel.emomData.getSeconds(), rowHeight, textSize = PickerSize.MEDIUM, scrollListener = secondsListener
        )

        roundsPicker = NumberPicker(
            requireContext(), binding.pickerRounds,
            viewModel.roundsPickerData,
            viewModel.emomData.getRounds(), rowHeight,
            textSize = PickerSize.MEDIUM,
            textColor = Color.NEUTRAL,
            prefixWithZero = true,
            scrollListener = roundsListener
        )
    }

    override fun onStartWorkout() {
        val action = EmomFragmentDirections.toWorkout(
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
        preferenceHelper.setCurrentFavoriteId(SessionType.EMOM, session.id)
    }

    override fun onFavoriteSelected(workout: CustomWorkoutSkeleton) {/* Do nothing */ }
    override fun onFavoriteDeleted(name: String) {/* Do nothing */ }
}