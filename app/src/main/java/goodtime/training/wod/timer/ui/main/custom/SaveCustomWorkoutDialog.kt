package goodtime.training.wod.timer.ui.main.custom

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstBundle: Bundle?): Dialog {
        val b = MaterialAlertDialogBuilder(requireContext())
        binding = DialogSaveCustomWorkoutBinding.inflate(layoutInflater)

        if (isFresh) {
            binding.overwriteRadioButton.visibility = View.GONE
            binding.saveAsRadioButton.isChecked = true
            binding.saveAsRadioButton.buttonDrawable = null
            binding.workoutName.visibility = View.GONE
            binding.editText.setText(originalName)
        } else {
            binding.workoutName.text = "$originalName "
            binding.workoutName.setOnClickListener{ binding.overwriteRadioButton.isChecked = true}
            binding.overwriteRadioButton.setOnCheckedChangeListener{ _, isChecked ->
                binding.saveAsRadioButton.isChecked = !isChecked
                togglePositiveButtonState(true)
                if (isChecked) {
                    hideKeyboardFrom(requireContext(), binding.root)
                }
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
                    if (isFresh) {
                        customName
                    } else {
                        if (binding.overwriteRadioButton.isChecked) originalName
                        else customName
                    })
                hideKeyboardFrom(requireContext(), binding.root)
            }
        }
        return b.create()
    }

    private fun refreshPositiveButtonVisibility(it: Editable?) {
        if (it.isNullOrEmpty() || it.toString().trim() == "") {
            togglePositiveButtonState(false)
        } else {
            togglePositiveButtonState(true)
            val trim = it.toString().trim()
            customName = trim
        }
    }

    private fun togglePositiveButtonState(enabled: Boolean) {
        val dialog = dialog as AlertDialog?
        if (dialog != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = enabled
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