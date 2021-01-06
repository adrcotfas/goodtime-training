package goodtime.training.wod.timer.ui.main.custom

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import goodtime.training.wod.timer.data.model.CustomWorkoutSkeleton
import goodtime.training.wod.timer.data.repository.AppRepository
import goodtime.training.wod.timer.databinding.DialogSelectCustomWorkoutBinding
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import goodtime.training.wod.timer.ui.main.DeleteConfirmationDialog
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class SelectCustomWorkoutDialog: DialogFragment(), KodeinAware, DeleteConfirmationDialog.Listener {
    override val kodein by closestKodein()
    private val preferenceHelper: PreferenceHelper by instance()

    private val repo: AppRepository by instance()

    private lateinit var favorites : List<CustomWorkoutSkeleton>
    private lateinit var binding: DialogSelectCustomWorkoutBinding
    private lateinit var listener: Listener

    interface Listener {
        fun onFavoriteSelected(workout: CustomWorkoutSkeleton)
        fun onFavoriteDeleted(name: String)
    }

    companion object {
        fun newInstance(listener : Listener) : SelectCustomWorkoutDialog {
            val dialog = SelectCustomWorkoutDialog()
            dialog.listener = listener
            return dialog
        }
    }

    override fun onCreateDialog(savedInstBundle: Bundle?): Dialog {
        val b = MaterialAlertDialogBuilder(requireContext())

        binding = DialogSelectCustomWorkoutBinding.inflate(layoutInflater)
        setupFavorites()
        b.apply {
            setView(binding.root)
        }
        return b.create()
    }

    private fun setupFavorites() {
        repo.getCustomWorkoutSkeletons().observe(
            this, {
                binding.emptyState.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                favorites = it
                val favoritesChipGroup = binding.favorites
                favoritesChipGroup.isSingleSelection = true
                favoritesChipGroup.removeAllViews()
                for (favorite in favorites) {
                    val chip = Chip(requireContext()).apply {
                        text = favorite.name
                    }
                    chip.setOnCloseIconClickListener {
                        if (parentFragmentManager.findFragmentByTag("DeleteConfirmation") == null) {
                            if (preferenceHelper.showDeleteConfirmationDialog()) {
                                DeleteConfirmationDialog.newInstance(this, 0, favorite.name)
                                    .show(parentFragmentManager, "DeleteConfirmation")
                            } else {
                                onDeleteConfirmation(0, favorite.name)
                            }
                        }
                    }
                    chip.setOnClickListener {
                        listener.onFavoriteSelected(favorite)
                        dismiss()
                    }
                    favoritesChipGroup.addView(chip)
                }
            })
    }

    override fun onDeleteConfirmation(id: Long, name: String) {
        repo.removeCustomWorkoutSkeleton(name)
        listener.onFavoriteDeleted(name)
    }
}