package goodtime.training.wod.timer.ui.stats

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import goodtime.training.wod.timer.data.repository.AppRepository
import goodtime.training.wod.timer.databinding.DialogFilterCustomWorkoutBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class FilterDialog : DialogFragment(), KodeinAware {
    override val kodein by closestKodein()
    private lateinit var binding : DialogFilterCustomWorkoutBinding

    private val repo: AppRepository by instance()
    private lateinit var listener : Listener

    interface Listener {
        fun onFavoriteSelected(name: String)
    }

    companion object {
        fun newInstance(listener: Listener) : FilterDialog {
            val dialog = FilterDialog()
            dialog.listener = listener
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogFilterCustomWorkoutBinding.inflate(layoutInflater)
        setupChipGroup()
        val b = MaterialAlertDialogBuilder(requireContext())
        b.setTitle("Select workout").setView(binding.root)
        return b.create()
    }

    private fun setupChipGroup() {
        repo.getCustomWorkoutSkeletons().observe(this, {
            binding.emptyState.isVisible = it.isEmpty()
            binding.chipGroup.removeAllViews()
            for (customWorkout in it) {
                val chip = Chip(requireContext()).apply {
                    isCloseIconVisible = false
                    text = customWorkout.name
                }
                chip.setOnClickListener {
                    listener.onFavoriteSelected(customWorkout.name)
                    dismiss()
                }
                binding.chipGroup.addView(chip)
            }
        })
    }
}
