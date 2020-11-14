package com.adrcotfas.wod.ui.common.ui

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.adrcotfas.wod.data.model.CustomWorkoutSkeleton
import com.adrcotfas.wod.data.repository.AppRepository
import com.adrcotfas.wod.databinding.DialogSelectCustomWorkoutBinding
import com.google.android.material.chip.Chip
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class SelectCustomWorkoutDialog: DialogFragment(), KodeinAware {
    override val kodein by closestKodein()

    private val repo: AppRepository by instance()

    private lateinit var favorites : List<CustomWorkoutSkeleton>
    private lateinit var binding: DialogSelectCustomWorkoutBinding
    private lateinit var listener: Listener

    interface Listener {
        fun onFavoriteSelected(workout: CustomWorkoutSkeleton)
    }

    companion object {
        fun newInstance(listener : Listener) : SelectCustomWorkoutDialog {
            val dialog = SelectCustomWorkoutDialog()
            dialog.listener = listener
            return dialog
        }
    }

    override fun onCreateDialog(savedInstBundle: Bundle?): Dialog {
        val b = AlertDialog.Builder(requireContext())

        binding = DialogSelectCustomWorkoutBinding.inflate(layoutInflater)
        setupFavorites()
        b.apply {
            setView(binding.root)
        }
        return b.create()
    }

    private fun setupFavorites() {
        repo.getCustomWorkoutSkeletons().observe(
            this, Observer { favorites = it
                val favoritesChipGroup = binding.favorites
                favoritesChipGroup.isSingleSelection = true
                favoritesChipGroup.removeAllViews()
                for (favorite in favorites) {
                    val chip = Chip(requireContext()).apply {
                        text = favorite.name
                    }
                    chip.setOnCloseIconClickListener { repo.removeCustomWorkoutSkeleton(favorite.name) }
                    chip.setOnClickListener {
                        listener.onFavoriteSelected(favorite)
                        dismiss()
                    }
                    favoritesChipGroup.addView(chip)
                }
            })
    }
}