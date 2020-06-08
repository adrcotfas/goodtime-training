package com.adrcotfas.wod.ui.tabata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.adrcotfas.wod.R
import com.adrcotfas.wod.common.TimerUtils
import com.adrcotfas.wod.common.calculateRowHeight
import com.adrcotfas.wod.common.number_picker.NumberPicker
import com.adrcotfas.wod.common.preferences.PrefUtil.Companion.generatePreWorkoutSession
import com.adrcotfas.wod.common.sessionsToString
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class TabataFragment : Fragment() {

    private lateinit var viewModel: TabataViewModel
    private lateinit var minuteWorkPicker:   NumberPicker
    private lateinit var secondsWorkPicker:  NumberPicker
    private lateinit var minuteBreakPicker:  NumberPicker
    private lateinit var secondsBreakPicker: NumberPicker
    private lateinit var roundsPicker: NumberPicker

    private lateinit var session : SessionMinimal

    private val minuteWorkListener = object: NumberPicker.Listener {
        override fun onScroll(value: Int) { viewModel.tabataData.setMinutesWork(value) }
    }

    private val secondsWorkListener = object: NumberPicker.Listener {
        override fun onScroll(value: Int) { viewModel.tabataData.setSecondsWork(value) }
    }

    private val minuteBreakListener = object: NumberPicker.Listener {
        override fun onScroll(value: Int) { viewModel.tabataData.setMinutesBreak(value) }
    }

    private val secondsBreakListener = object: NumberPicker.Listener {
        override fun onScroll(value: Int) { viewModel.tabataData.setSecondsBreak(value) }
    }

    private val roundsListener = object: NumberPicker.Listener {
        override fun onScroll(value: Int) { viewModel.tabataData.setRounds(value) }
    }

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
        val rowHeight = calculateRowHeight(layoutInflater, false)

        minuteWorkPicker = NumberPicker(
            requireContext(), root.findViewById(R.id.picker_minutes_work),
            TimerUtils.generateNumbers(0, 5, 1),
            0, rowHeight, largeText = false, listener = minuteWorkListener
        )

        secondsWorkPicker = NumberPicker(
            requireContext(), root.findViewById(R.id.picker_seconds_work),
            TimerUtils.generateNumbers(0, 59, 1),
            20, rowHeight, largeText = false, listener = secondsWorkListener
        )

        minuteBreakPicker = NumberPicker(
            requireContext(), root.findViewById(R.id.picker_minutes_break),
            TimerUtils.generateNumbers(0, 5, 1),
            0, rowHeight, largeText = false, listener = minuteBreakListener
        )

        secondsBreakPicker = NumberPicker(
            requireContext(), root.findViewById(R.id.picker_seconds_break),
            TimerUtils.generateNumbers(0, 59, 1),
            10, rowHeight, largeText = false, listener = secondsBreakListener
        )

        roundsPicker = NumberPicker(
            requireContext(), root.findViewById(R.id.picker_rounds),
            TimerUtils.generateNumbers(0, 90, 1),
            18, rowHeight, prefixWithZero = false, largeText = false, listener = roundsListener
        )

        viewModel.tabataData.get().observe(
            viewLifecycleOwner, Observer { tabataData ->
                session =
                    SessionMinimal(tabataData.first, tabataData.second, tabataData.third, SessionType.TABATA)
            }
        )

        val startButton = root.findViewById<ExtendedFloatingActionButton>(R.id.start_button)
        startButton.setOnClickListener {view ->
            val action = TabataFragmentDirections.startWorkoutAction(
                sessionsToString(generatePreWorkoutSession(),  session))
            view.findNavController().navigate(action)
        }
        return root
    }
}
