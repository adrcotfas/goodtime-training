package goodtime.training.wod.timer.ui.main.hiit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import goodtime.training.wod.timer.common.calculateRowHeight
import goodtime.training.wod.timer.common.number_picker.NumberPicker
import goodtime.training.wod.timer.common.number_picker.NumberPicker.Companion.Color
import goodtime.training.wod.timer.common.number_picker.NumberPicker.Companion.PickerSize
import goodtime.training.wod.timer.common.preferences.PrefUtil
import goodtime.training.wod.timer.data.model.CustomWorkoutSkeleton
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.data.model.TypeConverter
import goodtime.training.wod.timer.databinding.FragmentTabataBinding
import goodtime.training.wod.timer.ui.main.WorkoutTypeFragment

class HiitFragment : WorkoutTypeFragment() {

    private lateinit var viewModel: HiitViewModel

    private lateinit var binding: FragmentTabataBinding
    private lateinit var secondsWorkPicker:  NumberPicker
    private lateinit var secondsBreakPicker: NumberPicker
    private lateinit var roundsPicker: NumberPicker

    private val secondsWorkListener = object: NumberPicker.ScrollListener {
        override fun onScroll(value: Int) { viewModel.tabataData.setSecondsWork(value) }
    }

    private val secondsBreakListener = object: NumberPicker.ScrollListener {
        override fun onScroll(value: Int) { viewModel.tabataData.setSecondsBreak(value) }
    }

    private val roundsListener = object: NumberPicker.ScrollListener {
        override fun onScroll(value: Int) { viewModel.tabataData.setRounds(value) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HiitViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentTabataBinding.inflate(inflater, container, false)
        setupNumberPickers()

        viewModel.tabataData.get().observe(
            viewLifecycleOwner, { tabataData ->
                viewModel.session =
                    SessionSkeleton(duration = tabataData.first, breakDuration = tabataData.second,
                        numRounds = tabataData.third, type = SessionType.HIIT)
            }
        )
        return binding.root
    }

    private fun setupNumberPickers() {
        val rowHeight = calculateRowHeight(layoutInflater, PickerSize.MEDIUM)

        secondsWorkPicker = NumberPicker(
            requireContext(), binding.pickerSecondsWork,
            viewModel.secondsPickerData,
            20, rowHeight, textSize = PickerSize.MEDIUM, scrollListener = secondsWorkListener
        )

        secondsBreakPicker = NumberPicker(
            requireContext(), binding.pickerSecondsBreak,
            viewModel.secondsPickerData,
            10, rowHeight, textSize = PickerSize.MEDIUM, textColor = Color.RED, scrollListener = secondsBreakListener
        )

        roundsPicker = NumberPicker(
            requireContext(),
            binding.pickerRounds,
            viewModel.roundsPickerData,
            8,
            rowHeight,
            prefixWithZero = true,
            textSize = PickerSize.MEDIUM,
            textColor = Color.NEUTRAL,
            scrollListener = roundsListener
        )
    }

    override fun onStartWorkout() {
        val action = HiitFragmentDirections.toWorkout(
            TypeConverter.toString(sessions = arrayOf(PrefUtil.generatePreWorkoutSession()) + getSelectedSessions().toTypedArray())
        )
        findNavController().navigate(action)
    }

    override fun getSelectedSessions(): ArrayList<SessionSkeleton> = arrayListOf(viewModel.session)

    override fun onFavoriteSelected(session: SessionSkeleton) {
        secondsWorkPicker.smoothScrollToValue(session.duration)
        secondsBreakPicker.smoothScrollToValue(session.breakDuration)
        roundsPicker.smoothScrollToValue(session.numRounds)
    }

    override fun onFavoriteSelected(workout: CustomWorkoutSkeleton) {
        // Do nothing
    }
}
