package com.adrcotfas.wod.ui.workout

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkInfo
import com.adrcotfas.wod.common.TimerUtils
import com.adrcotfas.wod.common.preferences.PrefUtil
import com.adrcotfas.wod.databinding.FragmentWorkoutBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class WorkoutFragment : Fragment(), KodeinAware {
    override val kodein by closestKodein()

    private val preferences : PrefUtil by instance()
    private lateinit var workViewModel : WorkoutViewModel
    private lateinit var binding: FragmentWorkoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        workViewModel = ViewModelProvider(this).get(WorkoutViewModel::class.java)
    }

    override fun onDestroy() {
        workViewModel.cancelWork()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        workViewModel.startWorkout(preferences.getSessionList())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentWorkoutBinding.inflate(layoutInflater, container, false)
        workViewModel.outputWorkInfo
            .observe(requireActivity(), buildWorkInfosObserver())

        return binding.root
    }

    private fun buildWorkInfosObserver(): Observer<List<WorkInfo>> {
        return Observer { listOfWorkInfo ->
            if (listOfWorkInfo.isNullOrEmpty()) {
                return@Observer
            }

            val workInfo = listOfWorkInfo[0]
            Log.e("WOD", "state: ${workInfo.state}")

            if (workInfo.state.isFinished) {
                // TODO: check if state is paused or finished and show the appropriate screen
                //showWorkFinished()
            } else {
                //TODO: show current tick, current round, current workout
                //showWorkInProgress()
                val progress = workInfo.progress
                val value = progress.getInt(WorkoutWorker.PROGRESS, 0)
                binding.timer.text = TimerUtils.secondsToTimerFormat(value)
            }
        }
    }

}
