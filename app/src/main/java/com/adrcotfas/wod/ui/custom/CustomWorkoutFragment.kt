package com.adrcotfas.wod.ui.custom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.adrcotfas.wod.common.preferences.PrefUtil
import com.adrcotfas.wod.data.model.CustomWorkoutSkeleton
import com.adrcotfas.wod.data.model.SessionSkeleton
import com.adrcotfas.wod.data.model.SessionType
import com.adrcotfas.wod.data.model.TypeConverter
import com.adrcotfas.wod.databinding.FragmentCustomBinding
import com.adrcotfas.wod.ui.common.WorkoutTypeFragment
import com.adrcotfas.wod.ui.common.ui.SelectCustomWorkoutDialog
import java.util.concurrent.TimeUnit

class CustomWorkoutFragment :
    WorkoutTypeFragment(),
    CustomWorkoutAdapter.Listener,
    CustomWorkoutAddSessionAdapter.Listener,
    SelectCustomWorkoutDialog.Listener {

    private lateinit var viewModel: CustomWorkoutViewModel
    private lateinit var binding: FragmentCustomBinding

    private lateinit var listAdapter : CustomWorkoutAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CustomWorkoutViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomBinding.inflate(inflater, container, false)

        viewModel.customWorkout = CustomWorkoutSkeleton("Sample workout",
            arrayListOf(
                SessionSkeleton(0, TimeUnit.MINUTES.toSeconds(10).toInt(), type = SessionType.AMRAP),
                SessionSkeleton(0, TimeUnit.MINUTES.toSeconds(15).toInt(), type = SessionType.AMRAP),
                SessionSkeleton(0, TimeUnit.MINUTES.toSeconds(20).toInt(), type = SessionType.AMRAP),
                SessionSkeleton(0, TimeUnit.MINUTES.toSeconds(15).toInt(), type = SessionType.FOR_TIME)
            ))
        binding.title.text = viewModel.customWorkout.name

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            listAdapter = CustomWorkoutAdapter(viewModel.customWorkout.sessions, context, this@CustomWorkoutFragment)
            val footerAdapter = CustomWorkoutAddSessionAdapter(this@CustomWorkoutFragment)
            adapter = ConcatAdapter(listAdapter, footerAdapter)
        }

        return binding.root
    }

    override fun onStartWorkout() {
        val action = CustomWorkoutFragmentDirections.toWorkout(
            TypeConverter.toString(sessions = arrayOf(PrefUtil.generatePreWorkoutSession()) + getSelectedSessions().toTypedArray() ))
        findNavController().navigate(action)
    }

    override fun getSelectedSessions(): ArrayList<SessionSkeleton> = viewModel.customWorkout.sessions

    override fun onCloseButtonClicked() {
        Toast.makeText(requireContext(), "Close", Toast.LENGTH_SHORT).show()
    }

    override fun onScrollHandleTouch() {
        Toast.makeText(requireContext(), "Scroll", Toast.LENGTH_SHORT).show()
    }

    override fun onAddSessionClicked() {
        Toast.makeText(requireContext(), "Add", Toast.LENGTH_SHORT).show()
    }

    override fun onFavoriteSelected(session: SessionSkeleton) { /* do nothing*/ }

    override fun onFavoriteSelected(workout: CustomWorkoutSkeleton) {
        viewModel.customWorkout = workout
        binding.title.text = viewModel.customWorkout.name
        listAdapter.data = viewModel.customWorkout.sessions
        listAdapter.notifyDataSetChanged()
    }
}