package goodtime.training.wod.timer.ui.main.custom

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import goodtime.training.wod.timer.common.hideKeyboardFrom
import goodtime.training.wod.timer.databinding.DialogSaveCustomWorkoutBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein

class SaveCustomWorkoutDialog: DialogFragment(), KodeinAware {
    override val kodein by closestKodein()
    private lateinit var binding: DialogSaveCustomWorkoutBinding
    private lateinit var listener: Listener

    private lateinit var originalName : String
    private lateinit var customName : String

    interface Listener {
        fun onCustomWorkoutSaved(name: String)
    }

    companion object {
        fun newInstance(name: String, listener: Listener): SaveCustomWorkoutDialog {
            val dialog = SaveCustomWorkoutDialog()
            dialog.listener = listener
            dialog.originalName = name
            return dialog
        }
    }

    override fun onCreateDialog(savedInstBundle: Bundle?): Dialog {
        val b = AlertDialog.Builder(requireContext())
        binding = DialogSaveCustomWorkoutBinding.inflate(layoutInflater)

        binding.workoutName.text = "$originalName "
        binding.workoutName.setOnClickListener{ binding.overwriteRadioButton.isChecked = true}

        binding.overwriteRadioButton.setOnCheckedChangeListener{ _, isChecked ->
            binding.saveAsRadioButton.isChecked = !isChecked
            togglePositiveButtonState(true)
            if (isChecked) {
                hideKeyboardFrom(requireContext(), binding.root)
            }
        }

        binding.saveAsRadioButton.setOnCheckedChangeListener{ _, isChecked ->
            binding.overwriteRadioButton.isChecked = !isChecked
            refreshPositiveButtonVisibility(binding.editText.text)
        }

        binding.editText.setOnClickListener {
            binding.saveAsRadioButton.isChecked = true
        }

        binding.editText.addTextChangedListener {
            refreshPositiveButtonVisibility(it)
        }

        b.apply {
            setView(binding.root)
            setPositiveButton(android.R.string.ok) { _, _ ->
                listener.onCustomWorkoutSaved(
                    if (binding.overwriteRadioButton.isChecked) originalName
                    else customName)
                hideKeyboardFrom(requireContext(), binding.root)
            }
        }
        return b.create()
    }

    private fun refreshPositiveButtonVisibility(it: Editable?) {
        if (it.isNullOrEmpty()) {
            togglePositiveButtonState(false)
        } else {
            togglePositiveButtonState(true)
            customName = it.toString()
        }
    }

    private fun togglePositiveButtonState(visible: Boolean) {
        val dialog = dialog as AlertDialog?
        if (dialog != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = visible
        }
    }

    override fun onResume() {
        super.onResume()
        if (binding.overwriteRadioButton.isChecked) {
            togglePositiveButtonState(true)
        } else {
            refreshPositiveButtonVisibility(binding.editText.text)
        }
    }
}