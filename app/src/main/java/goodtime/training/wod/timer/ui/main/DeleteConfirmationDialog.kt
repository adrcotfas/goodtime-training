package goodtime.training.wod.timer.ui.main

import android.app.Dialog
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.core.text.italic
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import goodtime.training.wod.timer.common.preferences.PrefUtil
import goodtime.training.wod.timer.databinding.DialogDeleteConfirmationBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class DeleteConfirmationDialog: DialogFragment(), KodeinAware {

    override val kodein by closestKodein()

    private lateinit var binding: DialogDeleteConfirmationBinding
    private lateinit var listener: Listener
    private var favoriteId = 0
    private lateinit var name: String

    interface Listener {
        fun onDeleteConfirmation(id: Int, name: String)
    }

    companion object {
        fun newInstance(listener: Listener, id: Int, name: String): DeleteConfirmationDialog {
            val dialog = DeleteConfirmationDialog()
            dialog.listener = listener
            dialog.name = name
            dialog.favoriteId = id
            return dialog
        }
    }

    override fun onCreateDialog(savedInstBundle: Bundle?): Dialog {
        val b = MaterialAlertDialogBuilder(requireContext())
        binding = DialogDeleteConfirmationBinding.inflate(layoutInflater)

        b.apply {
            setView(binding.root)
            setTitle("Delete favorite?")

            val message = SpannableStringBuilder().append("This will delete ")
                .italic { append(name) }.append(" from the favorites.")

            setMessage(message)
            setPositiveButton(android.R.string.ok) { _, _ ->
                if (binding.checkbox.isChecked) {
                    val prefUtils: PrefUtil by instance()
                    prefUtils.setShowDeleteConfirmationDialog(false)
                }
                listener.onDeleteConfirmation(favoriteId, name)
            }
            setNegativeButton(android.R.string.cancel) { _, _ -> }
        }
        return b.create()
    }
}