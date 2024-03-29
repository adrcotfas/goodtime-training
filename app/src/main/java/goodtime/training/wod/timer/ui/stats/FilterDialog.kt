package goodtime.training.wod.timer.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.data.repository.AppRepository
import goodtime.training.wod.timer.databinding.DialogFilterCustomWorkoutBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class FilterDialog : BottomSheetDialogFragment(), KodeinAware {
    override val kodein by closestKodein()
    private lateinit var binding: DialogFilterCustomWorkoutBinding

    private val repo: AppRepository by instance()
    private lateinit var listener: Listener
    private lateinit var inflater: LayoutInflater

    interface Listener {
        fun onFavoriteSelected(name: String)
    }

    companion object {
        fun newInstance(listener: Listener): FilterDialog {
            val dialog = FilterDialog()
            dialog.listener = listener
            return dialog
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogFilterCustomWorkoutBinding.inflate(layoutInflater)
        this.inflater = inflater
        setupChipGroup()
        return binding.root
    }

    private fun setupChipGroup() {
        repo.getCustomWorkoutSkeletons().observe(this, {
            binding.emptyState.isVisible = it.isEmpty()
            binding.chipGroup.removeAllViews()
            for (customWorkout in it) {
                val chip = inflater.inflate(R.layout.chip_entry, binding.chipGroup, false) as Chip
                chip.apply {
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
