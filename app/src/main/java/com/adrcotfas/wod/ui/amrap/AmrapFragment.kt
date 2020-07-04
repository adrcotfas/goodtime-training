package com.adrcotfas.wod.ui.amrap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.adrcotfas.wod.R
import com.adrcotfas.wod.common.TimerUtils.Companion.secondsToMinutesAndSeconds
import com.adrcotfas.wod.common.calculateRowHeight
import com.adrcotfas.wod.common.number_picker.NumberPicker
import com.adrcotfas.wod.common.preferences.PrefUtil.Companion.generatePreWorkoutSession
import com.adrcotfas.wod.common.sessionsToString
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType
import com.adrcotfas.wod.databinding.FragmentAmrapBinding
import com.adrcotfas.wod.ui.common.ViewModelFactory
import com.adrcotfas.wod.ui.common.ui.SaveFavoriteDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class AmrapFragment : Fragment(), KodeinAware {

    override val kodein by closestKodein()

    private val viewModelFactory: ViewModelFactory by instance()
    private lateinit var viewModel: AmrapViewModel
    private lateinit var session: SessionMinimal

    private lateinit var binding: FragmentAmrapBinding
    private lateinit var minutePicker: NumberPicker
    private lateinit var secondsPicker: NumberPicker
    private lateinit var favoritesChipGroup: ChipGroup

    private val minuteListener = object: NumberPicker.ScrollListener {
        override fun onScroll(value: Int) {
            if (triggerListener) {
                viewModel.timeData.setMinutes(value)
            }
        }
    }

    private val secondsListener = object: NumberPicker.ScrollListener {
        override fun onScroll(value: Int) {
            if (triggerListener) {
                viewModel.timeData.setSeconds(value)
            }
        }
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
        viewModel = ViewModelProvider(this, viewModelFactory).get(AmrapViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAmrapBinding.inflate(inflater, container, false)
        setupNumberPickers()

        favoritesChipGroup = binding.favorites
        setupFavorites()
        setupSaveFavorite()

        viewModel.timeData.get().observe(
            viewLifecycleOwner, Observer { duration ->
                session = SessionMinimal(duration = duration, breakDuration = 0, numRounds = 0, type = SessionType.AMRAP)
            }
        )
        return binding.root
    }

    private fun setupSaveFavorite() {
        binding.pickerSeparator.setOnClickListener{openSaveFavoriteDialog()}
    }

    private fun setupFavorites() {
        favoritesChipGroup.isSingleSelection = true
        viewModel.favorites.observe( viewLifecycleOwner, Observer { favorites ->
            favoritesChipGroup.removeAllViews()
            for (favorite in favorites) {
                val chip = Chip(requireContext()).apply {
                    text = favorite.duration.toString()
                    val chipDrawable = ChipDrawable.createFromAttributes(requireContext(),
                        null, 0, R.style.ChipStyle)
                    setChipDrawable(chipDrawable)
                }
                chip.setOnLongClickListener(object : View.OnLongClickListener{
                    override fun onLongClick(v: View?): Boolean {
                        SaveFavoriteDialog.newInstance(true, favorite)
                            .show(childFragmentManager, this.javaClass.toString())
                        return true
                    }
                })
                chip.setOnCheckedChangeListener { _, isChecked ->
                    if (!isChecked) return@setOnCheckedChangeListener
                    triggerListener = false
                    val duration = secondsToMinutesAndSeconds(favorite.duration)
                    viewModel.setDuration(duration)
                    minutePicker.setValue(duration.first)
                    secondsPicker.setValue(duration.second)
                    triggerListener = true
                }

                favoritesChipGroup.addView(chip)
            }
        })
    }

    private fun setupNumberPickers() {
        val rowHeight = calculateRowHeight(layoutInflater)
        minutePicker = NumberPicker(
            requireContext(), binding.pickerMinutes,
            ArrayList<Int>().apply{ addAll(0..60)},
            15, rowHeight, scrollListener = minuteListener, clickListener = saveFavoriteHandler
        )
        secondsPicker = NumberPicker(
            requireContext(), binding.pickerSeconds,
            ArrayList<Int>().apply{ addAll(0..59)},
            0, rowHeight, scrollListener = secondsListener, clickListener = saveFavoriteHandler
        )
    }

    fun openSaveFavoriteDialog() {
        SaveFavoriteDialog.newInstance(false, session)
            .show(childFragmentManager, this.javaClass.toString())
    }

    fun onStartWorkout() {
        val action = AmrapFragmentDirections.startWorkoutAction(
            sessionsToString(generatePreWorkoutSession(), session))
        view?.findNavController()?.navigate(action)
    }
}