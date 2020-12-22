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
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import goodtime.training.wod.timer.data.model.CustomWorkoutSkeleton
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.data.model.TypeConverter
import goodtime.training.wod.timer.databinding.FragmentHiitBinding
import goodtime.training.wod.timer.ui.main.WorkoutTypeFragment
import org.kodein.di.generic.instance

class HiitFragment : WorkoutTypeFragment() {

    private val preferenceHelper: PreferenceHelper by instance()
    private val viewModelFactory: HiitViewModelFactory by instance()
    private lateinit var viewModel: HiitViewModel

    private lateinit var binding: FragmentHiitBinding
    private lateinit var secondsWorkPicker:  NumberPicker
    private lateinit var secondsBreakPicker: NumberPicker
    private lateinit var roundsPicker: NumberPicker

    private val secondsWorkListener = object: NumberPicker.ScrollListener {
        override fun onScroll(value: Int) { viewModel.hiitData.setSecondsWork(value) }
    }

    private val secondsBreakListener = object: NumberPicker.ScrollListener {
        override fun onScroll(value: Int) { viewModel.hiitData.setSecondsBreak(value) }
    }

    private val roundsListener = object: NumberPicker.ScrollListener {
        override fun onScroll(value: Int) { viewModel.hiitData.setRounds(value) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory).get(HiitViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHiitBinding.inflate(inflater, container, false)

        val favoritesLd = viewModel.getFavorites()
        favoritesLd.observe(viewLifecycleOwner, { favorites ->
            val id = preferenceHelper.getCurrentFavoriteId(SessionType.HIIT)
            val idx = favorites.indexOfFirst{it.id == id}

            viewModel.hiitData = HiitSpinnerData(
                if (idx != -1) favorites[idx].duration else 20,
                if (idx != -1) favorites[idx].breakDuration else 10,
                if (idx != -1) favorites[idx].numRounds else 8)
            favoritesLd.removeObservers(viewLifecycleOwner)

            setupNumberPickers()

            viewModel.hiitData.get().observe(
                viewLifecycleOwner, { hiitData ->
                    viewModel.session =
                        SessionSkeleton(duration = hiitData.first, breakDuration = hiitData.second,
                            numRounds = hiitData.third, type = SessionType.HIIT)
                }
            )
        })
        return binding.root
    }

    private fun setupNumberPickers() {
        val rowHeight = calculateRowHeight(layoutInflater, PickerSize.MEDIUM)

        secondsWorkPicker = NumberPicker(
            requireContext(), binding.pickerSecondsWork,
            viewModel.secondsPickerData,
            viewModel.hiitData.getSecondsWork(), rowHeight, textSize = PickerSize.MEDIUM, scrollListener = secondsWorkListener
        )

        secondsBreakPicker = NumberPicker(
            requireContext(), binding.pickerSecondsBreak,
            viewModel.secondsPickerData,
            viewModel.hiitData.getSecondsBreak(), rowHeight, textSize = PickerSize.MEDIUM, textColor = Color.RED, scrollListener = secondsBreakListener
        )

        roundsPicker = NumberPicker(
            requireContext(),
            binding.pickerRounds,
            viewModel.roundsPickerData,
            viewModel.hiitData.getRounds(),
            rowHeight,
            prefixWithZero = true,
            textSize = PickerSize.MEDIUM,
            textColor = Color.NEUTRAL,
            scrollListener = roundsListener
        )
    }

    override fun onStartWorkout() {
        val action = HiitFragmentDirections.toWorkout(
            TypeConverter.toString(sessions = arrayOf(PreferenceHelper.generatePreWorkoutSession(preferenceHelper.getPreWorkoutCountdown()))
                    + getSelectedSessions().toTypedArray())
        )
        findNavController().navigate(action)
    }

    override fun getSelectedSessions(): ArrayList<SessionSkeleton> = arrayListOf(viewModel.session)

    override fun onFavoriteSelected(session: SessionSkeleton) {
        secondsWorkPicker.smoothScrollToValue(session.duration)
        secondsBreakPicker.smoothScrollToValue(session.breakDuration)
        roundsPicker.smoothScrollToValue(session.numRounds)
        preferenceHelper.setCurrentFavoriteId(SessionType.HIIT, session.id)
    }

    override fun onFavoriteSelected(workout: CustomWorkoutSkeleton) {/* Do nothing */ }
    override fun onFavoriteDeleted(name: String) {/* Do nothing */ }
}
