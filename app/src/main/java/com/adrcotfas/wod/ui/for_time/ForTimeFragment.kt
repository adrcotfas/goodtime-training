package com.adrcotfas.wod.ui.for_time

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

class ForTimeFragment : Fragment(), KodeinAware {

    override val kodein by closestKodein()

    private lateinit var viewModel: ForTimeViewModel
    private lateinit var minutePicker: NumberPicker
    private lateinit var secondsPicker: NumberPicker

    private lateinit var session : SessionMinimal

    private val minuteListener = object: NumberPicker.Listener {
        override fun onScroll(value: Int) { viewModel.timeData.setMinutes(value) }
    }

    private val secondsListener = object: NumberPicker.Listener {
        override fun onScroll(value: Int) { viewModel.timeData.setSeconds(value) }
    }

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
        val rowHeight = calculateRowHeight(layoutInflater)

        minutePicker = NumberPicker(
            requireContext(), root.findViewById(R.id.picker_minutes),
            TimerUtils.generateNumbers(0, 45, 1),
            15, rowHeight, listener = minuteListener
        )

        secondsPicker = NumberPicker(
            requireContext(), root.findViewById(R.id.picker_seconds),
            TimerUtils.generateNumbers(0, 45, 15),
            0, rowHeight, listener = secondsListener
        )

        viewModel.timeData.get().observe(
            viewLifecycleOwner, Observer { duration ->
                session = SessionMinimal(duration, 0, 0, SessionType.AMRAP)
            }
        )

        val startButton = root.findViewById<ExtendedFloatingActionButton>(R.id.start_button)
        startButton.setOnClickListener {view ->
            val action = ForTimeFragmentDirections.startWorkoutAction(sessionsToString(session))
            view.findNavController().navigate(action)
        }

        return root
    }
}