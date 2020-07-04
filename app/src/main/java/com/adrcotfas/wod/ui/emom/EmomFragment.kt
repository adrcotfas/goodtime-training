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
import com.adrcotfas.wod.common.calculateRowHeight
import com.adrcotfas.wod.common.number_picker.NumberPicker
import com.adrcotfas.wod.common.preferences.PrefUtil.Companion.generatePreWorkoutSession
import com.adrcotfas.wod.common.sessionsToString
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType

class EmomFragment : Fragment() {

    private lateinit var viewModel: EmomViewModel
    private lateinit var minutePicker: NumberPicker
    private lateinit var secondsPicker: NumberPicker
    private lateinit var roundsPicker: NumberPicker

    private lateinit var session : SessionMinimal

    private val minuteListener = object: NumberPicker.ScrollListener {
        override fun onScroll(value: Int) { viewModel.emomData.setMinutes(value) }
    }

    private val secondsListener = object: NumberPicker.ScrollListener {
        override fun onScroll(value: Int) { viewModel.emomData.setSeconds(value) }
    }

    private val roundsListener = object: NumberPicker.ScrollListener {
        override fun onScroll(value: Int) { viewModel.emomData.setRounds(value) }
    }

    private val saveFavoriteHandler = object: NumberPicker.ClickListener {
        override fun onClick() {

        }
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
            ArrayList<Int>().apply{ addAll(0..5)},
            1, rowHeight, scrollListener = minuteListener,
            clickListener = saveFavoriteHandler
        )

        secondsPicker = NumberPicker(
            requireContext(), root.findViewById(R.id.picker_seconds),
            ArrayList<Int>().apply{ addAll(0..59)},
            0, rowHeight, scrollListener = secondsListener,
            clickListener = saveFavoriteHandler
        )

        roundsPicker = NumberPicker(
            requireContext(), root.findViewById(R.id.picker_rounds),
            ArrayList<Int>().apply{ addAll(0..50)},
            20, rowHeight, prefixWithZero = false, scrollListener = roundsListener,
            clickListener = saveFavoriteHandler
        )

        viewModel.emomData.get().observe(
            viewLifecycleOwner, Observer { data ->
                session = SessionMinimal(duration = data.first, breakDuration = 0,
                    numRounds = data.second, type = SessionType.EMOM)
            }
        )
        return root
    }

    fun onStartWorkout() {
        val action = EmomFragmentDirections.startWorkoutAction(
            sessionsToString(generatePreWorkoutSession(), session))
        view?.findNavController()?.navigate(action)
    }

}