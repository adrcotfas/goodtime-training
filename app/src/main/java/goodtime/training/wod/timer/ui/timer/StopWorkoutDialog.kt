package goodtime.training.wod.timer.ui.timer

import android.R.string
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.core.content.ContextCompat.startForegroundService
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import goodtime.training.wod.timer.data.workout.TimerState
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class StopWorkoutDialog : DialogFragment(), KodeinAware {

    override val kodein by closestKodein()

    private val viewModelFactory: TimerViewModelFactory by instance()
    private lateinit var viewModel: TimerViewModel

    override fun onCreateDialog(savedInstBundle: Bundle?): Dialog {
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(TimerViewModel::class.java)


        isCancelable = false
        if (viewModel.getTimerState().value == TimerState.ACTIVE) {
            viewModel.toggle()
        }
        val b = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Stop workout?")
            .setMessage("Are you sure you want to stop this workout?")
            .setPositiveButton(string.ok) { _: DialogInterface?, _: Int ->
                val intent = IntentWithAction(requireContext(), TimerService::class.java, TimerService.FINALIZE)
                startForegroundService(requireContext(), intent)
                viewModel.abandon()

                //seems to work fine but is it a good idea?
                findNavController().apply {
                    popBackStack()
                    popBackStack()
                }
            }
            .setNegativeButton(string.cancel) { _: DialogInterface?, _: Int ->
                if (viewModel.getTimerState().value == TimerState.PAUSED) {
                    viewModel.toggle()
                }
            }
        val d: Dialog = b.create()
        d.setCanceledOnTouchOutside(false)
        return d
    }
}