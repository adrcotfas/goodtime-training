package goodtime.training.wod.timer.ui.timer

import android.R
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import goodtime.training.wod.timer.data.workout.TimerState
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class StopWorkoutDialog : DialogFragment(), KodeinAware {

    override val kodein by closestKodein()

    private val viewModelFactory : TimerViewModelFactory by instance()
    private lateinit var viewModel : TimerViewModel

    override fun onCreateDialog(savedInstBundle: Bundle?): Dialog {
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(TimerViewModel::class.java)
        isCancelable = false
        if (viewModel.timerState.value == TimerState.ACTIVE) {
            viewModel.toggleTimer()
        }
        val b = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Stop workout?")
            .setMessage("Are you sure you want to stop this workout?")
            .setPositiveButton(
                R.string.ok
            ) { _: DialogInterface?, _: Int ->
                viewModel.abandonWorkout()
                //TODO: seems to work fine but is it a good idea?
                NavHostFragment.findNavController(this).apply {
                    popBackStack()
                    popBackStack()
                }
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