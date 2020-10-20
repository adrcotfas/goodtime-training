package com.adrcotfas.wod.ui.emom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.adrcotfas.wod.MainActivity
import com.adrcotfas.wod.common.StringUtils
import com.adrcotfas.wod.common.calculateRowHeight
import com.adrcotfas.wod.common.number_picker.NumberPicker
import com.adrcotfas.wod.common.number_picker.NumberPicker.Companion.Color
import com.adrcotfas.wod.common.number_picker.NumberPicker.Companion.PickerSize
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType
import com.adrcotfas.wod.databinding.FragmentEmomBinding
import com.adrcotfas.wod.ui.common.WorkoutTypeFragment

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
    ): View? {

        binding = FragmentEmomBinding.inflate(inflater, container, false)

        setupNumberPickers()

        viewModel.emomData.get().observe(
            viewLifecycleOwner, Observer { data ->
                val duration = data.first
                viewModel.session = SessionMinimal(duration = duration, breakDuration = 0,
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

    override fun getSelectedSession(): SessionMinimal = viewModel.session

    override fun onFavoriteSelected(session: SessionMinimal) {
        val duration = StringUtils.secondsToMinutesAndSeconds(session.duration)
        minutePicker.smoothScrollToPosition(duration.first)
        secondsPicker.smoothScrollToPosition(duration.second)
        roundsPicker.smoothScrollToPosition(session.numRounds - 1)
    }
}