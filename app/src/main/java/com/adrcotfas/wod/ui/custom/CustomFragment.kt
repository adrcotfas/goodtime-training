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
import com.adrcotfas.wod.common.sessionsToString
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType
import com.adrcotfas.wod.databinding.FragmentCustomBinding
import com.adrcotfas.wod.ui.common.WorkoutTypeFragment
import java.util.concurrent.TimeUnit

class CustomFragment : WorkoutTypeFragment(), CustomFragmentSessionAdapter.Listener, CustomFragmentAddSessionAdapter.Listener {

    private lateinit var viewModel: CustomViewModel
    private lateinit var binding: FragmentCustomBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CustomViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomBinding.inflate(inflater, container, false)

        viewModel.sessions =
            arrayListOf(
                SessionMinimal(0, TimeUnit.MINUTES.toSeconds(10).toInt(), type = SessionType.AMRAP),
                SessionMinimal(0, TimeUnit.MINUTES.toSeconds(15).toInt(), type = SessionType.AMRAP),
                SessionMinimal(0, TimeUnit.MINUTES.toSeconds(20).toInt(), type = SessionType.AMRAP),
                SessionMinimal(0, TimeUnit.MINUTES.toSeconds(15).toInt(), type = SessionType.FOR_TIME)
            )

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            val listAdapter = CustomFragmentSessionAdapter(viewModel.sessions, context, this@CustomFragment)
            val footerAdapter = CustomFragmentAddSessionAdapter(this@CustomFragment)
            adapter = ConcatAdapter(listAdapter, footerAdapter)
        }

        return binding.root
    }

    override fun onStartWorkout() {
        val action = CustomFragmentDirections.toWorkout(
            sessionsToString(sessions = arrayOf(PrefUtil.generatePreWorkoutSession()) + getSelectedSessions().toTypedArray() ))
        findNavController().navigate(action)
    }

    override fun getSelectedSessions(): ArrayList<SessionMinimal> = viewModel.sessions

    override fun onFavoriteSelected(session: SessionMinimal) {
        TODO("Not yet implemented")
    }

    override fun onCloseButtonClicked() {
        Toast.makeText(requireContext(), "Close", Toast.LENGTH_SHORT).show()
    }

    override fun onScrollHandleTouch() {
        Toast.makeText(requireContext(), "Scroll", Toast.LENGTH_SHORT).show()
    }

    override fun onAddSessionClicked() {
        Toast.makeText(requireContext(), "Add", Toast.LENGTH_SHORT).show()
    }

}