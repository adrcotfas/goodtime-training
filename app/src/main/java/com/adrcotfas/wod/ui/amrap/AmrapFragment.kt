package com.adrcotfas.wod.ui.amrap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.adrcotfas.wod.common.TimerUtils
import com.adrcotfas.wod.common.TimerUtils.Companion.secondsToMinutesAndSeconds
import com.adrcotfas.wod.common.calculateRowHeight
import com.adrcotfas.wod.common.number_picker.NumberPicker
import com.adrcotfas.wod.common.preferences.PrefUtil.Companion.generatePreWorkoutSession
import com.adrcotfas.wod.common.sessionsToString
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType
import com.adrcotfas.wod.databinding.FragmentAmrapBinding
import com.adrcotfas.wod.ui.common.FavoritesAdapter
import com.adrcotfas.wod.ui.common.ViewModelFactory
import com.adrcotfas.wod.ui.common.ui.SaveFavoriteDialog
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
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
    private lateinit var favoritesRecycler: RecyclerView

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

        favoritesRecycler = binding.favorites
        setupFavorites()

        val startButton = binding.startButton
        startButton.setOnClickListener {view ->
            val action = AmrapFragmentDirections.startWorkoutAction(
                sessionsToString(generatePreWorkoutSession(), session))
            view.findNavController().navigate(action)
        }

        viewModel.timeData.get().observe(
            viewLifecycleOwner, Observer { duration ->
                session = SessionMinimal(duration = duration, breakDuration = 0, numRounds = 0, type = SessionType.AMRAP)
            }
        )
        return binding.root
    }

    private fun setupFavorites() {
        val layoutManager = FlexboxLayoutManager(context)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_START
        favoritesRecycler.layoutManager = layoutManager

        val adapter = FavoritesAdapter(object: FavoritesAdapter.Listener {
            override fun onClick(session: SessionMinimal) {
                if (this@AmrapFragment.session.duration == session.duration) {
                    return
                }
                //TODO: change the background of the current item
                triggerListener = false
                val duration = secondsToMinutesAndSeconds(session.duration)
                viewModel.setDuration(duration)
                minutePicker.setValue(duration.first)
                secondsPicker.setValue(duration.second)
                triggerListener = true
            }
            override fun onLongClick(session: SessionMinimal): Boolean {
                SaveFavoriteDialog.newInstance(true, session)
                    .show(childFragmentManager, this.javaClass.toString())
                return true
            }
        })
        favoritesRecycler.adapter = adapter
        viewModel.favorites.observe( viewLifecycleOwner, Observer { favorites ->
            adapter.data = favorites
        })
    }

    private fun setupNumberPickers() {
        val rowHeight = calculateRowHeight(layoutInflater)
        minutePicker = NumberPicker(
            requireContext(), binding.pickerMinutes,
            TimerUtils.generateNumbers(0, 60, 1),
            15, rowHeight, listener = object: NumberPicker.Listener {
                override fun onScroll(value: Int) {
                    if (triggerListener) {
                        viewModel.timeData.setMinutes(value)
                    }
                }
            }
        )
        secondsPicker = NumberPicker(
            requireContext(), binding.pickerSeconds,
            TimerUtils.generateNumbers(0, 55, 5),
            0, rowHeight, listener = object: NumberPicker.Listener {
                override fun onScroll(value: Int) {
                    if (triggerListener) {
                        viewModel.timeData.setSeconds(value)
                    }
                }
            }
        )
    }

    fun openSaveFavoriteDialog() {
        SaveFavoriteDialog.newInstance(false, session)
            .show(childFragmentManager, this.javaClass.toString())
    }
}