package com.adrcotfas.wod.ui.tabata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.adrcotfas.wod.common.calculateRowHeight
import com.adrcotfas.wod.common.number_picker.NumberPicker
import com.adrcotfas.wod.common.number_picker.NumberPicker.Companion.Color
import com.adrcotfas.wod.common.number_picker.NumberPicker.Companion.PickerSize
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType
import com.adrcotfas.wod.databinding.FragmentTabataBinding
import com.adrcotfas.wod.ui.common.WorkoutTypeFragment

class TabataFragment : WorkoutTypeFragment() {

    private lateinit var viewModel: TabataViewModel

    private lateinit var binding: FragmentTabataBinding
    private lateinit var secondsWorkPicker:  NumberPicker
    private lateinit var secondsBreakPicker: NumberPicker
    private lateinit var roundsPicker: NumberPicker

    private val secondsWorkListener = object: NumberPicker.ScrollListener {
        override fun onScroll(value: Int) { viewModel.tabataData.setSecondsWork(value) }
    }

    private val secondsBreakListener = object: NumberPicker.ScrollListener {
        override fun onScroll(value: Int) { viewModel.tabataData.setSecondsBreak(value) }
    }

    private val roundsListener = object: NumberPicker.ScrollListener {
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

        binding = FragmentTabataBinding.inflate(inflater, container, false)
        setupNumberPickers()

        viewModel.tabataData.get().observe(
            viewLifecycleOwner, Observer { tabataData ->
                viewModel.session =
                    SessionMinimal(duration = tabataData.first, breakDuration = tabataData.second,
                        numRounds = tabataData.third, type = SessionType.TABATA)
            }
        )
        return binding.root
    }

    private fun setupNumberPickers() {
        val rowHeight = calculateRowHeight(layoutInflater, PickerSize.MEDIUM)

        secondsWorkPicker = NumberPicker(
            requireContext(), binding.pickerSecondsWork,
            viewModel.secondsPickerData,
            20, rowHeight, textSize = PickerSize.MEDIUM, scrollListener = secondsWorkListener
        )

        secondsBreakPicker = NumberPicker(
            requireContext(), binding.pickerSecondsBreak,
            viewModel.secondsPickerData,
            10, rowHeight, textSize = PickerSize.MEDIUM, textColor = Color.RED, scrollListener = secondsBreakListener
        )

        roundsPicker = NumberPicker(
            requireContext(),
            binding.pickerRounds,
            viewModel.roundsPickerData,
            8,
            rowHeight,
            prefixWithZero = true,
            textSize = PickerSize.MEDIUM,
            textColor = Color.NEUTRAL,
            scrollListener = roundsListener
        )
    }

    override fun getSelectedSession(): SessionMinimal = viewModel.session

    override fun onFavoriteSelected(session: SessionMinimal) {
        secondsWorkPicker.smoothScrollToPosition(session.duration - 1)
        secondsBreakPicker.smoothScrollToPosition(session.breakDuration - 1)
        roundsPicker.smoothScrollToPosition(session.numRounds - 1)
    }
}
