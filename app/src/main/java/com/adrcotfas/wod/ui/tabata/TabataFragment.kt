package com.adrcotfas.wod.ui.tabata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.adrcotfas.wod.R
import com.adrcotfas.wod.common.TimerUtils
import com.adrcotfas.wod.common.TimerUtils.Companion.SECONDS_STEP_5
import com.shawnlin.numberpicker.NumberPicker

class TabataFragment : Fragment() {

    private lateinit var viewModel: TabataViewModel
    private lateinit var pickerMinutesWork: NumberPicker
    private lateinit var pickerSecondsWork: NumberPicker
    private lateinit var pickerMinutesRest: NumberPicker
    private lateinit var pickerSecondsRest: NumberPicker
    private lateinit var pickerRound: NumberPicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TabataViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_tabata, container, false)

        pickerMinutesWork = root.findViewById(R.id.picker_minutes)
        pickerSecondsWork = root.findViewById(R.id.picker_seconds)
        pickerMinutesRest = root.findViewById(R.id.picker_minutes_rest)
        pickerSecondsRest = root.findViewById(R.id.picker_seconds_rest)
        pickerRound = root.findViewById(R.id.picker_rounds)

        setupPickers()

        pickerMinutesWork.setOnValueChangedListener{ _, _, newVal -> viewModel.workSpinnerData.setMinutes(newVal) }
        pickerSecondsWork.setOnValueChangedListener { _, _, newVal -> viewModel.workSpinnerData.setSeconds(newVal * SECONDS_STEP_5)}

        pickerMinutesRest.setOnValueChangedListener{ _, _, newVal -> viewModel.restSpinnerData.setMinutes(newVal) }
        pickerSecondsRest.setOnValueChangedListener { _, _, newVal -> viewModel.restSpinnerData.setSeconds(newVal * SECONDS_STEP_5)}

        pickerRound.setOnValueChangedListener { _, _, newVal -> viewModel.roundSpinnerData.setRounds(newVal)}

        viewModel.workSpinnerData.getSeconds().observe(
            viewLifecycleOwner, Observer<Int> { seconds -> pickerSecondsWork.value = seconds / SECONDS_STEP_5})

        viewModel.workSpinnerData.getMinutes().observe(
            viewLifecycleOwner, Observer<Int> { minutes -> pickerMinutesWork.value = minutes })

        viewModel.restSpinnerData.getSeconds().observe(
            viewLifecycleOwner, Observer<Int> { seconds -> pickerSecondsRest.value = seconds / SECONDS_STEP_5})

        viewModel.restSpinnerData.getMinutes().observe(
            viewLifecycleOwner, Observer<Int> { minutes -> pickerMinutesRest.value = minutes })

        viewModel.roundSpinnerData.getRounds().observe(
            viewLifecycleOwner, Observer<Int> { rounds -> pickerRound.value = rounds })

        return root
    }

    private fun setupPickers() {
        val dataMinutes = TimerUtils.generateTimeValuesMinutes(5 + 1)
        pickerMinutesWork.minValue = 0
        pickerMinutesWork.maxValue = dataMinutes.size - 1
        pickerMinutesWork.displayedValues = dataMinutes.toTypedArray()
        pickerMinutesWork.value = 0

        pickerMinutesRest.minValue = 0
        pickerMinutesRest.maxValue = dataMinutes.size - 1
        pickerMinutesRest.displayedValues = dataMinutes.toTypedArray()
        pickerMinutesRest.value = 0

        val dataSeconds = TimerUtils.generateTimeValuesSeconds(SECONDS_STEP_5)
        pickerSecondsWork.minValue = 0
        pickerSecondsWork.maxValue = dataSeconds.size - 1
        pickerSecondsWork.displayedValues = dataSeconds.toTypedArray()
        pickerSecondsWork.value = 20 / SECONDS_STEP_5

        pickerSecondsRest.minValue = 0
        pickerSecondsRest.maxValue = dataSeconds.size - 1
        pickerSecondsRest.displayedValues = dataSeconds.toTypedArray()
        pickerSecondsRest.value = 10 / SECONDS_STEP_5
    }
}