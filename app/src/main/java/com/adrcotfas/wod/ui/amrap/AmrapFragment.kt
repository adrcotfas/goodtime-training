package com.adrcotfas.wod.ui.amrap

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

class AmrapFragment : Fragment(), KodeinAware {
    override val kodein by closestKodein()

    private val workoutManager : WorkoutManager by instance()

    private lateinit var viewModel: AmrapViewModel
    private lateinit var minutePicker: NumberPicker
    private lateinit var secondsPicker: NumberPicker

    private val minuteListener = object: NumberPicker.Listener {
        override fun onScroll(value: Int) { viewModel.timeData.setMinutes(value) }
    }

    private val secondsListener = object: NumberPicker.Listener {
        override fun onScroll(value: Int) { viewModel.timeData.setSeconds(value) }
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
        val rowHeight = calculateRowHeight(layoutInflater)

        minutePicker = NumberPicker(
            requireContext(), root.findViewById(R.id.picker_minutes),
            TimerUtils.generateNumbers(0,45, 1),
            15, rowHeight, prefixWithZero = true, largeText = true, listener = minuteListener
        )

        secondsPicker = NumberPicker(
            requireContext(), root.findViewById(R.id.picker_seconds),
            TimerUtils.generateNumbers(0, 45, 15),
            0, rowHeight, prefixWithZero = true, largeText = true, listener = secondsListener
        )

        workoutManager.session.type = Session.SessionType.AMRAP
        viewModel.timeData.get().observe(
            viewLifecycleOwner, Observer { duration ->
                run {
                    workoutManager.session.duration = duration
                } })

        return root
    }
}