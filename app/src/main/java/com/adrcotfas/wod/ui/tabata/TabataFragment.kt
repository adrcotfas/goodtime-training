package com.adrcotfas.wod.ui.tabata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.adrcotfas.wod.MainActivity
import com.adrcotfas.wod.common.StringUtils
import com.adrcotfas.wod.common.calculateRowHeight
import com.adrcotfas.wod.common.number_picker.NumberPicker
import com.adrcotfas.wod.common.number_picker.NumberPicker.Companion.Color
import com.adrcotfas.wod.common.number_picker.NumberPicker.Companion.PickerSize
import com.adrcotfas.wod.common.preferences.PrefUtil.Companion.generatePreWorkoutSession
import com.adrcotfas.wod.common.sessionsToString
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType
import com.adrcotfas.wod.databinding.FragmentTabataBinding
import com.adrcotfas.wod.ui.common.PortraitFragment
import com.adrcotfas.wod.ui.common.ui.ConfirmDeleteFavoriteDialog
import com.adrcotfas.wod.ui.common.ui.SaveFavoriteDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class TabataFragment : PortraitFragment(), KodeinAware {

    override val kodein by closestKodein()
    private val viewModelFactory: TabataViewModelFactory by instance()
    private lateinit var viewModel: TabataViewModel

    private lateinit var binding: FragmentTabataBinding
    private lateinit var minuteWorkPicker:   NumberPicker
    private lateinit var secondsWorkPicker:  NumberPicker
    private lateinit var minuteBreakPicker:  NumberPicker
    private lateinit var secondsBreakPicker: NumberPicker
    private lateinit var roundsPicker: NumberPicker
    private lateinit var favoritesChipGroup: ChipGroup

    private val minuteWorkListener = object: NumberPicker.ScrollListener {
        override fun onScroll(value: Int) { viewModel.tabataData.setMinutesWork(value) }
    }

    private val secondsWorkListener = object: NumberPicker.ScrollListener {
        override fun onScroll(value: Int) { viewModel.tabataData.setSecondsWork(value) }
    }

    private val minuteBreakListener = object: NumberPicker.ScrollListener {
        override fun onScroll(value: Int) { viewModel.tabataData.setMinutesBreak(value) }
    }

    private val secondsBreakListener = object: NumberPicker.ScrollListener {
        override fun onScroll(value: Int) { viewModel.tabataData.setSecondsBreak(value) }
    }

    private val roundsListener = object: NumberPicker.ScrollListener {
        override fun onScroll(value: Int) { viewModel.tabataData.setRounds(value) }
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
        viewModel = ViewModelProvider(this, viewModelFactory).get(TabataViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentTabataBinding.inflate(inflater, container, false)
        setupNumberPickers()
        setupFavorites()

        viewModel.tabataData.get().observe(
            viewLifecycleOwner, Observer { tabataData ->
                viewModel.session =
                    SessionMinimal(duration = tabataData.first, breakDuration = tabataData.second,
                        numRounds = tabataData.third, type = SessionType.TABATA)
                (requireActivity() as MainActivity)
                    .setStartButtonState(viewModel.session.duration != 0 && viewModel.session.breakDuration != 0)
            }
        )
        return binding.root
    }

    private fun setupNumberPickers() {
        val rowHeight = calculateRowHeight(layoutInflater, PickerSize.SMALL)

        minuteWorkPicker = NumberPicker(
            requireContext(), binding.pickerMinutesWork,
            viewModel.minutesPickerData,
            0, rowHeight, textSize = PickerSize.SMALL, scrollListener = minuteWorkListener,
            clickListener = saveFavoriteHandler
        )

        secondsWorkPicker = NumberPicker(
            requireContext(), binding.pickerSecondsWork,
            viewModel.secondsPickerData,
            20, rowHeight, textSize = PickerSize.SMALL, scrollListener = secondsWorkListener,
            clickListener = saveFavoriteHandler
        )

        minuteBreakPicker = NumberPicker(
            requireContext(), binding.pickerMinutesBreak,
            viewModel.minutesPickerData,
            0, rowHeight, textSize = PickerSize.SMALL, textColor = Color.RED, scrollListener = minuteBreakListener,
            clickListener = saveFavoriteHandler
        )

        secondsBreakPicker = NumberPicker(
            requireContext(), binding.pickerSecondsBreak,
            viewModel.secondsPickerData,
            10, rowHeight, textSize = PickerSize.SMALL, textColor = Color.RED, scrollListener = secondsBreakListener,
            clickListener = saveFavoriteHandler
        )

        roundsPicker = NumberPicker(
            requireContext(),
            binding.pickerRounds,
            viewModel.roundsPickerData,
            8,
            rowHeight,
            prefixWithZero = false,
            textSize = PickerSize.SMALL,
            textColor = Color.NEUTRAL,
            scrollListener = roundsListener,
            clickListener = saveFavoriteHandler
        )
    }

    private fun setupFavorites() {
        binding.separator1.setOnClickListener{openSaveFavoriteDialog()}
        binding.separator2.setOnClickListener{openSaveFavoriteDialog()}
        binding.separator3.setOnClickListener{openSaveFavoriteDialog()}

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
                    val durationWork = StringUtils.secondsToMinutesAndSeconds(favorite.duration)
                    val breakDuration = StringUtils.secondsToMinutesAndSeconds(favorite.breakDuration)
                    viewModel.setTabataData(durationWork, breakDuration, favorite.numRounds)
                    minuteWorkPicker.setValue(durationWork.first)
                    secondsWorkPicker.setValue(durationWork.second)
                    minuteBreakPicker.setValue(breakDuration.first)
                    secondsBreakPicker.setValue(breakDuration.second)
                    roundsPicker.setValue(favorite.numRounds)
                    triggerListener = true
                }
                favoritesChipGroup.addView(chip)
            }
        })
    }

    fun openSaveFavoriteDialog() {
        if (viewModel.session.duration != 0 && viewModel.session.breakDuration != 0) {
            SaveFavoriteDialog.newInstance(viewModel.session)
                .show(childFragmentManager, this.javaClass.toString())
        }
    }

    fun onStartWorkout() {
        val action = TabataFragmentDirections.startWorkoutAction(
            sessionsToString(generatePreWorkoutSession(),  viewModel.session))
        view?.findNavController()?.navigate(action)
    }
}
