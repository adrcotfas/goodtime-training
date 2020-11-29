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
import goodtime.training.wod.timer.common.preferences.PrefUtil
import goodtime.training.wod.timer.data.model.CustomWorkoutSkeleton
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.data.model.TypeConverter
import goodtime.training.wod.timer.databinding.FragmentEmomBinding
import goodtime.training.wod.timer.ui.main.WorkoutTypeFragment

class EmomFragment : WorkoutTypeFragment() {

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
        viewModel = ViewModelProvider(this).get(EmomViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentEmomBinding.inflate(inflater, container, false)

        setupNumberPickers()

        viewModel.emomData.get().observe(
            viewLifecycleOwner, { data ->
                val duration = data.first
                viewModel.session = SessionSkeleton(duration = duration, breakDuration = 0,
                    numRounds = data.second, type = SessionType.EMOM)
                updateMainButtonsState(duration)
            }
        )
        return binding.root
    }

    private fun setupNumberPickers() {
        val rowHeight = calculateRowHeight(layoutInflater, PickerSize.MEDIUM)

        minutePicker = NumberPicker(
            requireContext(), binding.pickerMinutes,
            viewModel.minutesPickerData,
            1, rowHeight, textSize = PickerSize.MEDIUM, scrollListener = minuteListener
        )

        secondsPicker = NumberPicker(
            requireContext(), binding.pickerSeconds,
            viewModel.secondsPickerData,
            0, rowHeight, textSize = PickerSize.MEDIUM, scrollListener = secondsListener
        )

        roundsPicker = NumberPicker(
            requireContext(), binding.pickerRounds,
            viewModel.roundsPickerData,
            20, rowHeight,
            textSize = PickerSize.MEDIUM,
            textColor = Color.NEUTRAL,
            prefixWithZero = true,
            scrollListener = roundsListener
        )
    }

    override fun onStartWorkout() {
        val action = EmomFragmentDirections.toWorkout(
            TypeConverter.toString(sessions = arrayOf(PrefUtil.generatePreWorkoutSession()) + getSelectedSessions().toTypedArray())
        )
        findNavController().navigate(action)
    }

    override fun getSelectedSessions(): ArrayList<SessionSkeleton> = arrayListOf(viewModel.session)

    override fun onFavoriteSelected(session: SessionSkeleton) {
        val duration = StringUtils.secondsToMinutesAndSeconds(session.duration)
        minutePicker.smoothScrollToValue(duration.first)
        secondsPicker.smoothScrollToValue(duration.second)
        roundsPicker.smoothScrollToValue(session.numRounds)
    }

    override fun onFavoriteSelected(workout: CustomWorkoutSkeleton) {/* Do nothing */ }
}