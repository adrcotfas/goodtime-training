package com.adrcotfas.wod.ui.amrap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.adrcotfas.wod.R
import com.adrcotfas.wod.common.TimerUtils
import com.adrcotfas.wod.common.TimerUtils.Companion.secondsToTimerFormat
import com.adrcotfas.wod.common.number_picker.NumberPicker
import com.google.android.material.snackbar.Snackbar

class AmrapFragment : Fragment() {

    private lateinit var viewModel: AmrapViewModel
    private lateinit var minutePicker: NumberPicker
    private lateinit var secondsPicker: NumberPicker

    private val minuteListener = object: NumberPicker.Listener {
        override fun onScroll(value: Int) { viewModel.timeSpinnerData.setMinutes(value) }
    }

    private val secondsListener = object: NumberPicker.Listener {
        override fun onScroll(value: Int) { viewModel.timeSpinnerData.setSeconds(value) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AmrapViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_amrap, container, false)
        val rowHeight = calculateRowHeight()

        minutePicker = NumberPicker(
            requireContext(),
            root.findViewById(R.id.picker_minutes),
            TimerUtils.generateNumbers(0,45, 1),
            15,
            rowHeight, minuteListener)

        secondsPicker = NumberPicker(
            requireContext(),
            root.findViewById(R.id.picker_seconds),
            TimerUtils.generateNumbers(0, 45, 5),
            0,
            rowHeight, secondsListener)

        viewModel.timeSpinnerData.getDuration().observe(
            viewLifecycleOwner, Observer<Int> { duration ->
                run {
                    Snackbar.make(requireView(), secondsToTimerFormat(duration), Snackbar.LENGTH_LONG).show()
                }
            })

        return root
    }

    private fun calculateRowHeight(): Float {
        val textView = layoutInflater
            .inflate(R.layout.row_number_picker, null) as TextView
        val fm = textView.paint.fontMetrics
        return fm.descent - fm.ascent
    }
}