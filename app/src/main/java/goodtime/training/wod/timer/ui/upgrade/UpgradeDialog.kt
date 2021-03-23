package goodtime.training.wod.timer.ui.upgrade

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.databinding.DialogUpgradeBinding

class UpgradeDialog : DialogFragment() {

    interface Listener {
        fun onUpgradeButtonClicked()
    }

    private lateinit var binding: DialogUpgradeBinding
    private lateinit var listener: Listener

    override fun onCreateDialog(savedInstBundle: Bundle?): Dialog {
        val b = AlertDialog.Builder(requireContext())
        binding = DialogUpgradeBinding.inflate(layoutInflater)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ExtraFeaturesAdapter(
                context,
                listOf(
                    Pair("Unlock favorites" + "\n" + "Unlimited custom workout favorites", R.drawable.ic_favorite),
                    Pair("Vibration notification", R.drawable.ic_vibration),
                    Pair("Flash notification", R.drawable.ic_flash_notification),
                    Pair("Configurable sound and voice profiles(male and female)", R.drawable.ic_notifications),
                    Pair("Fullscreen mode", R.drawable.ic_screen),
                    Pair("Do not Disturb mode", R.drawable.ic_dnd),
                    Pair("Log incomplete workouts", R.drawable.ic_incomplete_workouts),
                    Pair("Backup export and import" + "\n" + "Import CSV backup from SmartWOD", R.drawable.ic_cloud),
                    Pair("Manually add completed workouts to the statistics", R.drawable.ic_trending),
                    Pair("Vote for next features", R.drawable.ic_vote),
                    Pair("One time payment" + "\n" + "All future features for free", R.drawable.ic_resource_try),
                )
            )
        }

        binding.buttonPro.setOnClickListener {
            listener.onUpgradeButtonClicked()
            dismiss()
        }
        b.apply {
            setView(binding.root)
        }
        return b.create()
    }

    companion object {
        @JvmStatic
        fun newInstance(listener: Listener): UpgradeDialog {
                val dialog = UpgradeDialog()
                dialog.listener = listener
                return dialog
            }
    }
}