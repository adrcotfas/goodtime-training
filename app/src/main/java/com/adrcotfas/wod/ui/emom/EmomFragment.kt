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
import com.adrcotfas.wod.common.calculateRowHeight
import com.adrcotfas.wod.common.number_picker.NumberPicker
import com.adrcotfas.wod.data.model.Session
import com.adrcotfas.wod.data.workout.WorkoutManager
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class EmomFragment : Fragment(), KodeinAware {
    override val kodein by closestKodein()

    private val workoutManager : WorkoutManager by instance()

    private lateinit var viewModel: EmomViewModel
    private lateinit var minutePicker: NumberPicker
    private lateinit var secondsPicker: NumberPicker
    private lateinit var roundsPicker: NumberPicker

    private val minuteListener = object: NumberPicker.Listener {
        override fun onScroll(value: Int) { viewModel.emomData.setMinutes(value) }
    }

    private val secondsListener = object: NumberPicker.Listener {
        override fun onScroll(value: Int) { viewModel.emomData.setSeconds(value) }
    }

    private val roundsListener = object: NumberPicker.Listener {
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

        val root = inflater.inflate(R.layout.fragment_emom, container, false)
        val rowHeight = calculateRowHeight(layoutInflater)

        minutePicker = NumberPicker(
            requireContext(), root.findViewById(R.id.picker_minutes),
            TimerUtils.generateNumbers(0, 5, 1),
            1, rowHeight, true, true, minuteListener)

        secondsPicker = NumberPicker(
            requireContext(), root.findViewById(R.id.picker_seconds),
            TimerUtils.generateNumbers(0, 55, 5),
            0, rowHeight, prefixWithZero = true, largeText = true, listener = secondsListener
        )

        roundsPicker = NumberPicker(
            requireContext(), root.findViewById(R.id.picker_rounds),
            TimerUtils.generateNumbers(0, 90, 1),
            20, rowHeight, prefixWithZero = false, largeText = true, listener = roundsListener
        )

        workoutManager.session.type = Session.SessionType.EMOM
        viewModel.emomData.get().observe(
            viewLifecycleOwner, Observer { data ->
                run {
                    workoutManager.session.apply {
                        duration = data.first
                        numRounds = data.second
                    }
                } })
        return root
    }
}