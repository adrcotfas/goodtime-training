package com.adrcotfas.wod.ui.for_time

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.adrcotfas.wod.common.StringUtils
import com.adrcotfas.wod.common.calculateRowHeight
import com.adrcotfas.wod.common.number_picker.NumberPicker
import com.adrcotfas.wod.common.preferences.PrefUtil
import com.adrcotfas.wod.data.model.CustomWorkoutSkeleton
import com.adrcotfas.wod.data.model.SessionSkeleton
import com.adrcotfas.wod.data.model.SessionType
import com.adrcotfas.wod.data.model.TypeConverter
import com.adrcotfas.wod.databinding.FragmentAmrapBinding
import com.adrcotfas.wod.ui.common.WorkoutTypeFragment

class ForTimeFragment : WorkoutTypeFragment() {

    private lateinit var viewModel: ForTimeViewModel

    private lateinit var binding: FragmentAmrapBinding
    private lateinit var minutePicker: NumberPicker
    private lateinit var secondsPicker: NumberPicker

    private val minuteListener = object: NumberPicker.ScrollListener {
        override fun onScroll(value: Int) { viewModel.timeData.setMinutes(value) }
    }

    private val secondsListener = object: NumberPicker.ScrollListener {
        override fun onScroll(value: Int) { viewModel.timeData.setSeconds(value) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ForTimeViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAmrapBinding.inflate(inflater, container, false)
        setupNumberPickers()

        viewModel.timeData.get().observe(
            viewLifecycleOwner, Observer { duration ->
                viewModel.session = SessionSkeleton(
                    duration = duration, breakDuration = 0, numRounds = 0,
                    type = SessionType.FOR_TIME
                )
                updateMainButtonsState(duration)
            }
        )
        return binding.root
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
        val action = ForTimeFragmentDirections.toWorkout(
            TypeConverter.toString(sessions = arrayOf(PrefUtil.generatePreWorkoutSession()) + getSelectedSessions().toTypedArray())
        )
        findNavController().navigate(action)
    }

    override fun getSelectedSessions(): ArrayList<SessionSkeleton> = arrayListOf(viewModel.session)

    override fun onFavoriteSelected(session: SessionSkeleton) {
        val duration = StringUtils.secondsToMinutesAndSeconds(session.duration)
        minutePicker.smoothScrollToPosition(duration.first)
        secondsPicker.smoothScrollToPosition(duration.second)
    }

    override fun onFavoriteSelected(workout: CustomWorkoutSkeleton) {/* Do nothing */ }
}