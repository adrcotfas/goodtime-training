package com.adrcotfas.wod.ui.tabata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.adrcotfas.wod.R
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

        return root
    }

    private fun setupPickers() {

    }
}