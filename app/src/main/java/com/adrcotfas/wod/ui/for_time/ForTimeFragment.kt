package com.adrcotfas.wod.ui.for_time

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.adrcotfas.wod.MainActivity
import com.adrcotfas.wod.R
import com.adrcotfas.wod.common.StringUtils
import com.adrcotfas.wod.common.calculateRowHeight
import com.adrcotfas.wod.common.number_picker.NumberPicker
import com.adrcotfas.wod.common.preferences.PrefUtil.Companion.generatePreWorkoutSession
import com.adrcotfas.wod.common.sessionsToString
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType
import com.adrcotfas.wod.databinding.FragmentAmrapBinding
import com.adrcotfas.wod.ui.common.PortraitFragment
import com.adrcotfas.wod.ui.common.ui.ConfirmDeleteFavoriteDialog
import com.adrcotfas.wod.ui.common.ui.SaveFavoriteDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class ForTimeFragment : PortraitFragment(), KodeinAware {

    override val kodein by closestKodein()
    private val viewModelFactory: ForTimeViewModelFactory by instance()
    private lateinit var viewModel: ForTimeViewModel

    private lateinit var binding: FragmentAmrapBinding
    private lateinit var minutePicker: NumberPicker
    private lateinit var secondsPicker: NumberPicker
    private lateinit var favoritesChipGroup: ChipGroup

    private val minuteListener = object: NumberPicker.ScrollListener {
        override fun onScroll(value: Int) { viewModel.timeData.setMinutes(value) }
    }

    private val secondsListener = object: NumberPicker.ScrollListener {
        override fun onScroll(value: Int) { viewModel.timeData.setSeconds(value) }
    }

    private val saveFavoriteHandler = object: NumberPicker.ClickListener {
        override fun onClick() {
            openSaveFavoriteDialog()
        }
    }

    /**
     * Used to avoid the loop of pickers updating liveData updating pickers
     */
    private var triggerListener = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ForTimeViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAmrapBinding.inflate(inflater, container, false)
        setupNumberPickers()
        setupFavorites()

        viewModel.timeData.get().observe(
            viewLifecycleOwner, Observer { duration ->
                viewModel.session = SessionMinimal(
                    duration = duration, breakDuration = 0, numRounds = 0,
                    type = SessionType.FOR_TIME
                )
                (requireActivity() as MainActivity).setStartButtonState(duration != 0)
            }
        )
        return binding.root
    }

    private fun setupNumberPickers() {
        val rowHeight = calculateRowHeight(layoutInflater)

        minutePicker = NumberPicker(
            requireContext(), binding.pickerMinutes,
            viewModel.minutesPickerData,
            15, rowHeight, scrollListener = minuteListener, clickListener = saveFavoriteHandler
        )

        secondsPicker = NumberPicker(
            requireContext(), binding.pickerSeconds,
            viewModel.secondsPickerData,
            0, rowHeight, scrollListener = secondsListener, clickListener = saveFavoriteHandler
        )
    }

    private fun setupFavorites() {
        binding.pickerSeparator.setOnClickListener{openSaveFavoriteDialog()}

        favoritesChipGroup = binding.favorites
        favoritesChipGroup.isSingleSelection = true
        viewModel.favorites.observe( viewLifecycleOwner, Observer { favorites ->
            favoritesChipGroup.removeAllViews()
            for (favorite in favorites) {
                val chip = Chip(requireContext()).apply {
                    text = StringUtils.toFavoriteFormat(favorite)
                }
                chip.setOnLongClickListener(object : View.OnLongClickListener{
                    override fun onLongClick(v: View?): Boolean {
                        ConfirmDeleteFavoriteDialog.newInstance(favorite)
                            .show(childFragmentManager, this.javaClass.toString())
                        return true
                    }
                })
                chip.setOnClickListener {
                    if (favorite == viewModel.session) return@setOnClickListener
                    triggerListener = false
                    val duration = StringUtils.secondsToMinutesAndSeconds(favorite.duration)
                    viewModel.setDuration(duration)
                    minutePicker.setValue(duration.first)
                    secondsPicker.setValue(duration.second)
                    triggerListener = true
                }
                favoritesChipGroup.addView(chip)
            }
        })
    }

    fun openSaveFavoriteDialog() {
        if (viewModel.session.duration != 0) {
            SaveFavoriteDialog.newInstance(viewModel.session)
                .show(childFragmentManager, this.javaClass.toString())
        }
    }

    fun onStartWorkout() {
        val action = ForTimeFragmentDirections.startWorkoutAction(
            sessionsToString(generatePreWorkoutSession(), viewModel.session))
        view?.findNavController()?.navigate(action)
    }
}