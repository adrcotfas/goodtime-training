package com.adrcotfas.wod.ui.for_time

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.adrcotfas.wod.R
import com.shawnlin.numberpicker.NumberPicker

class ForTimeFragment : Fragment() {

    private lateinit var viewModel: ForTimeViewModel
    private lateinit var pickerMinutes: NumberPicker
    private lateinit var pickerSeconds: NumberPicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ForTimeViewModel::class.java)
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

        return root
    }

    private fun setupPickers() {
    }
}