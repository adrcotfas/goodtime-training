package com.adrcotfas.wod.ui.emom

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
import com.adrcotfas.wod.common.sessionsToString
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein

class EmomFragment : Fragment(), KodeinAware {
    override val kodein by closestKodein()

    private lateinit var viewModel: EmomViewModel
    private lateinit var minutePicker: NumberPicker
    private lateinit var secondsPicker: NumberPicker
    private lateinit var roundsPicker: NumberPicker

    private lateinit var session : SessionMinimal

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
            1, rowHeight, listener = minuteListener
        )

        secondsPicker = NumberPicker(
            requireContext(), root.findViewById(R.id.picker_seconds),
            TimerUtils.generateNumbers(0, 55, 5),
            0, rowHeight, listener = secondsListener
        )

        roundsPicker = NumberPicker(
            requireContext(), root.findViewById(R.id.picker_rounds),
            TimerUtils.generateNumbers(0, 90, 1),
            20, rowHeight, prefixWithZero = false, listener = roundsListener
        )

        viewModel.emomData.get().observe(
            viewLifecycleOwner, Observer { data ->
                session = SessionMinimal(data.first, 0, data.second, SessionType.EMOM)
            }
        )

        val startButton = root.findViewById<ExtendedFloatingActionButton>(R.id.start_button)
        startButton.setOnClickListener {view ->
            val action = EmomFragmentDirections.startWorkoutAction(sessionsToString(session))
            view.findNavController().navigate(action)
        }

        return root
    }
}