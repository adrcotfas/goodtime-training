package goodtime.training.wod.timer.ui.settings

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.sound_and_vibration.SoundPlayer
import goodtime.training.wod.timer.databinding.DialogSoundProfileBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class SoundProfileDialog: DialogFragment(), KodeinAware {

    override val kodein by closestKodein()
    private val soundPlayer: SoundPlayer by instance()

    private lateinit var binding: DialogSoundProfileBinding
    private var valueIndex: Int = -1
    private lateinit var listener: Listener
    private var isVoice : Boolean = false

    interface Listener {
        fun onSoundProfileSelected(idx: Int)
    }

    companion object {
        fun newInstance(valueIndex: Int, listener: Listener, isVoice : Boolean = false) : SoundProfileDialog {
            val dialog = SoundProfileDialog()
            dialog.valueIndex = valueIndex
            dialog.listener = listener
            dialog.isVoice = isVoice
            return dialog
        }
    }

    override fun onCreateDialog(savedInstBundle: Bundle?): Dialog {
        binding = DialogSoundProfileBinding.inflate(layoutInflater)

        val soundProfiles =
            if (isVoice) {
                resources.getStringArray(R.array.pref_voice_profile_entries)
            } else {
                resources.getStringArray(R.array.pref_sound_profile_entries)
            }

        for (i in soundProfiles.withIndex()) {
            val button = layoutInflater.inflate(R.layout.radio_button, binding.radioGroup, false) as RadioButton
            button.id = i.index
            button.text = i.value
            button.isChecked = valueIndex == i.index
            button.setOnClickListener {
                soundPlayer.stop()
                soundPlayer.play(
                    if (isVoice) {
                        if (it.id == 0) SoundPlayer.GET_READY_KATIE
                        else SoundPlayer.GET_READY_ERIC
                    } else {
                        if (it.id == 0) SoundPlayer.START_COUNTDOWN
                        else SoundPlayer.START_COUNTDOWN_GYM
                    })
            }
            binding.radioGroup.addView(button)
        }

        val b = MaterialAlertDialogBuilder(requireContext())
                .setView(binding.root)
                .setTitle("Select sound profile")
                .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                    soundPlayer.stop()
                    listener.onSoundProfileSelected(binding.radioGroup.checkedRadioButtonId)
                }
                .setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int ->
                    soundPlayer.stop()
                }
                .setOnDismissListener{
                    soundPlayer.stop()
                }
        return b.create()
    }

    override fun onDestroy() {
        soundPlayer.stop()
        super.onDestroy()
    }
}
