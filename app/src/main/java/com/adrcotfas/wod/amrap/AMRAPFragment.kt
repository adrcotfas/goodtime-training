package com.adrcotfas.wod.amrap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.adrcotfas.wod.R
import com.adrcotfas.wod.common.TimerUtils.Companion.generateTimeValuesMinutes
import com.adrcotfas.wod.common.TimerUtils.Companion.generateTimeValuesSeconds
import com.shawnlin.numberpicker.NumberPicker

class AMRAPFragment : Fragment() {

    private lateinit var viewModel: AMRAPViewModel
    private lateinit var pickerMinutes: NumberPicker
    private lateinit var pickerSeconds: NumberPicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AMRAPViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_amrap, container, false)

        pickerMinutes = root.findViewById(R.id.picker_minutes)
        pickerSeconds = root.findViewById(R.id.picker_seconds)
        setupPickers()

        pickerMinutes.setOnValueChangedListener{_, _, newVal -> viewModel.minutes.value = newVal }
        pickerSeconds.setOnValueChangedListener { _, _, newVal -> viewModel.seconds.value = newVal }

        viewModel.seconds.observe(viewLifecycleOwner, Observer<Int> { seconds -> pickerSeconds.value = seconds })
        viewModel.minutes.observe(viewLifecycleOwner, Observer<Int> { minutes -> pickerMinutes.value = minutes })

        return root
    }

    private fun setupPickers() {
        val dataMinutes = generateTimeValuesMinutes(60)
        pickerMinutes.minValue = 0
        pickerMinutes.maxValue = dataMinutes.size - 1
        pickerMinutes.displayedValues = dataMinutes.toTypedArray()
        pickerMinutes.value = 15

        val dataSeconds = generateTimeValuesSeconds(60)
        pickerSeconds.minValue = 0
        pickerSeconds.maxValue = dataSeconds.size - 1
        pickerSeconds.displayedValues = dataSeconds.toTypedArray()
        pickerSeconds.value = 0
    }

}