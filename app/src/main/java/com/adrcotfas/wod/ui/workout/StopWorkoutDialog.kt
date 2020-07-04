package com.adrcotfas.wod.ui.workout

import android.R
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class StopWorkoutDialog : DialogFragment(), KodeinAware {

    override val kodein by closestKodein()

    private val workoutManager : WorkoutManager by instance()

    override fun onCreateDialog(savedInstBundle: Bundle?): Dialog {
        isCancelable = false
        val b = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Stop workout?")
            .setMessage("Are you sure you want to stop this workout?")
            .setPositiveButton(
                R.string.ok
            ) { _: DialogInterface?, _: Int ->
                workoutManager.stopTimer()
                NavHostFragment.findNavController(this)
                    .navigate(StopWorkoutDialogDirections.actionStopWorkoutDialogToNavAmrap())
            }
            .setNegativeButton(R.string.cancel) { _: DialogInterface?, _: Int -> /* do nothing */}
        val d: Dialog = b.create()
        d.setCanceledOnTouchOutside(false)
        return d
    }
}