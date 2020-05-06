package com.adrcotfas.wod.ui.emom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.adrcotfas.wod.R
import com.adrcotfas.wod.common.TimerUtils
import com.adrcotfas.wod.common.TimerUtils.Companion.SECONDS_STEP_15
import com.shawnlin.numberpicker.NumberPicker

class EmomFragment : Fragment() {

    private lateinit var viewModel: EmomViewModel
    private lateinit var pickerMinutes: NumberPicker
    private lateinit var pickerSeconds: NumberPicker
    private lateinit var pickerRound: NumberPicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(EmomViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_emom, container, false)

        pickerMinutes = root.findViewById(R.id.picker_minutes)
        pickerSeconds = root.findViewById(R.id.picker_seconds)
        pickerRound = root.findViewById(R.id.picker_rounds)

        setupPickers()

        pickerMinutes.setOnValueChangedListener{_, _, newVal -> viewModel.timeSpinnerData.setMinutes(newVal) }
        pickerSeconds.setOnValueChangedListener { _, _, newVal -> viewModel.timeSpinnerData.setSeconds(newVal * SECONDS_STEP_15)}
        pickerRound.setOnValueChangedListener { _, _, newVal -> viewModel.roundSpinnerData.setRounds(newVal)}

        viewModel.timeSpinnerData.getSeconds().observe(
            viewLifecycleOwner, Observer<Int> { seconds -> pickerSeconds.value = seconds / SECONDS_STEP_15 })
        viewModel.timeSpinnerData.getMinutes().observe(
            viewLifecycleOwner, Observer<Int> { minutes -> pickerMinutes.value = minutes })
        viewModel.roundSpinnerData.getRounds().observe(
            viewLifecycleOwner, Observer<Int> { rounds -> pickerRound.value = rounds })

        return root
    }

    private fun setupPickers() {
        val dataMinutes = TimerUtils.generateTimeValuesMinutes(20)
        pickerMinutes.minValue = 0
        pickerMinutes.maxValue = dataMinutes.size - 1
        pickerMinutes.displayedValues = dataMinutes.toTypedArray()
        pickerMinutes.value = 15
        pickerMinutes.wrapSelectorWheel = false

        val dataSeconds = TimerUtils.generateTimeValuesSeconds(SECONDS_STEP_15)
        pickerSeconds.minValue = 0
        pickerSeconds.maxValue = dataSeconds.size - 1
        pickerSeconds.displayedValues = dataSeconds.toTypedArray()
        pickerSeconds.value = 0
        pickerSeconds.wrapSelectorWheel = false
    }
}