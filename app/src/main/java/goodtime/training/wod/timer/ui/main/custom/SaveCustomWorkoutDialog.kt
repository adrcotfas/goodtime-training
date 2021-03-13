package goodtime.training.wod.timer.ui.main.custom

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import goodtime.training.wod.timer.common.hideKeyboardFrom
import goodtime.training.wod.timer.data.model.CustomWorkoutSkeleton
import goodtime.training.wod.timer.data.repository.AppRepository
import goodtime.training.wod.timer.databinding.DialogSaveCustomWorkoutBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class SaveCustomWorkoutDialog : BottomSheetDialogFragment(), KodeinAware {
    override val kodein by closestKodein()

    private val repo: AppRepository by instance()
    private lateinit var favorites: List<CustomWorkoutSkeleton>

    private lateinit var binding: DialogSaveCustomWorkoutBinding
    private lateinit var listener: Listener

    private lateinit var originalName: String
    private lateinit var customName: String

    // the workout to be saved does not already exist
    private var isFresh = false

    interface Listener {
        fun onCustomWorkoutSaved(name: String)
    }

    companion object {
        fun newInstance(name: String, listener: Listener, isFresh: Boolean): SaveCustomWorkoutDialog {
            val dialog = SaveCustomWorkoutDialog()
            dialog.listener = listener
            dialog.originalName = name
            dialog.isFresh = isFresh
            if (isFresh) {
                dialog.customName = name
            }
            return dialog
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogSaveCustomWorkoutBinding.inflate(layoutInflater)

        val customWorkoutSkeletonsLd = repo.getCustomWorkoutSkeletons()
        customWorkoutSkeletonsLd.observe(this, {
            favorites = it
            binding.saveAsRadioButton.setOnCheckedChangeListener { _, isChecked ->
                binding.overwriteRadioButton.isChecked = !isChecked
                refreshPositiveButtonState(binding.editText.text)
                binding.textInputLayout.isEnabled = true
            }
            customWorkoutSkeletonsLd.removeObservers(this)
        })

        if (isFresh) {
            binding.overwriteRadioButton.visibility = View.GONE
            binding.saveAsRadioButton.isChecked = true
            binding.saveAsRadioButton.isVisible = false
            binding.saveAsRadioButton.buttonDrawable = null
            binding.workoutName.visibility = View.GONE
            binding.editText.setText(originalName)
            binding.textInputLayout.isEnabled = true
        } else {
            binding.workoutName.text = "$originalName "
            binding.workoutName.setOnClickListener { binding.overwriteRadioButton.isChecked = true }
            binding.overwriteRadioButton.setOnCheckedChangeListener { _, isChecked ->
                binding.saveAsRadioButton.isChecked = !isChecked
                togglePositiveButtonState(true)
                if (isChecked) {
                    hideKeyboardFrom(requireContext(), binding.root)
                }
                binding.textInputLayout.isEnabled = false
            }
        }

        binding.editText.addTextChangedListener {
            refreshPositiveButtonState(it)
        }

        setupButtons()
        return binding.root
    }

    private fun setupButtons() {
        binding.closeButton.setOnClickListener { dismiss() }
        binding.saveButton.setOnClickListener {
            listener.onCustomWorkoutSaved(
                if (isFresh) {
                    customName
                } else {
                    if (binding.overwriteRadioButton.isChecked) originalName
                    else customName
                }
            )
            dismiss()
            hideKeyboardFrom(requireContext(), binding.root)
        }
    }

    private fun refreshPositiveButtonState(text: Editable?) {
        if (text.isNullOrEmpty() || text.toString().trim() == "") {
            togglePositiveButtonState(false)
            return
        }
        if (favorites.find { it.name == text.toString() } != null) {
            togglePositiveButtonState(false)
            return
        }
        togglePositiveButtonState(true)
        val trim = text.toString().trim()
        customName = trim
    }

    private fun togglePositiveButtonState(enabled: Boolean) {
        binding.saveButton.isEnabled = enabled
        binding.textInputLayout.error = if (enabled) null else "Enter a valid name"
    }

    override fun onResume() {
        super.onResume()
        if (binding.overwriteRadioButton.isChecked) {
            togglePositiveButtonState(true)
        } else {
            refreshPositiveButtonState(binding.editText.text)
        }
    }
}