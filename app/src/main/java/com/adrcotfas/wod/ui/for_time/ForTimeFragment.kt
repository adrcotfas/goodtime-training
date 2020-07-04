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
import com.adrcotfas.wod.common.calculateRowHeight
import com.adrcotfas.wod.common.number_picker.NumberPicker
import com.adrcotfas.wod.common.preferences.PrefUtil.Companion.generatePreWorkoutSession
import com.adrcotfas.wod.common.sessionsToString
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType

class ForTimeFragment : Fragment() {

    private lateinit var viewModel: ForTimeViewModel
    private lateinit var minutePicker: NumberPicker
    private lateinit var secondsPicker: NumberPicker

    private lateinit var session : SessionMinimal

    private val minuteListener = object: NumberPicker.ScrollListener {
        override fun onScroll(value: Int) { viewModel.timeData.setMinutes(value) }
    }

    private val secondsListener = object: NumberPicker.ScrollListener {
        override fun onScroll(value: Int) { viewModel.timeData.setSeconds(value) }
    }

    private val saveFavoriteHandler = object: NumberPicker.ClickListener {
        override fun onClick() {
        }
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
            ArrayList<Int>().apply{ addAll(0..60)},
            15, rowHeight, scrollListener = minuteListener, clickListener = saveFavoriteHandler
        )

        secondsPicker = NumberPicker(
            requireContext(), root.findViewById(R.id.picker_seconds),
            ArrayList<Int>().apply{ addAll(0..59)},
            0, rowHeight, scrollListener = secondsListener, clickListener = saveFavoriteHandler
        )

        viewModel.timeData.get().observe(
            viewLifecycleOwner, Observer { duration ->
                session = SessionMinimal(duration = duration, breakDuration = 0, numRounds = 0,
                    type = SessionType.AMRAP)
            }
        )
        return root
    }

    fun onStartWorkout() {
        val action = ForTimeFragmentDirections.startWorkoutAction(
            sessionsToString(generatePreWorkoutSession(), session))
        view?.findNavController()?.navigate(action)
    }
}