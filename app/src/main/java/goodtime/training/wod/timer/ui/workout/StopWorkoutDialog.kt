package goodtime.training.wod.timer.ui.workout

import android.R
import androidx.appcompat.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import goodtime.training.wod.timer.data.workout.TimerState
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class StopWorkoutDialog : DialogFragment(), KodeinAware {

    override val kodein by closestKodein()

    private val viewModelFactory : WorkoutViewModelFactory by instance()
    private lateinit var viewModel : WorkoutViewModel

    override fun onCreateDialog(savedInstBundle: Bundle?): Dialog {
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(WorkoutViewModel::class.java)
        isCancelable = false
        if (viewModel.timerState.value == TimerState.ACTIVE) {
            viewModel.toggleTimer()
        }
        val b = AlertDialog.Builder(requireContext())
            .setTitle("Stop workout?")
            .setMessage("Are you sure you want to stop this workout?")
            .setPositiveButton(
                R.string.ok
            ) { _: DialogInterface?, _: Int ->
                viewModel.stopTimer()
                //TODO: seems to work fine but is it a good idea?
                NavHostFragment.findNavController(this).popBackStack()
                NavHostFragment.findNavController(this).popBackStack()
            }
            .setNegativeButton(R.string.cancel) { _: DialogInterface?, _: Int ->
                if (viewModel.timerState.value == TimerState.PAUSED) {
                    viewModel.toggleTimer()
                }
            }
        val d: Dialog = b.create()
        d.setCanceledOnTouchOutside(false)
        return d
    }
}