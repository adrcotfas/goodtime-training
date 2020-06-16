package com.adrcotfas.wod.ui.common.ui

import android.R
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.adrcotfas.wod.common.TimerUtils.Companion.secondsToMinutesAndSeconds
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType
import com.adrcotfas.wod.data.repository.SessionsRepository
import com.adrcotfas.wod.databinding.DialogSaveFavoriteBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.dialog_save_favorite.view.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class SaveFavoriteDialog : DialogFragment(), KodeinAware {
    override val kodein by closestKodein()

    private val repo: SessionsRepository by instance()

    private lateinit var binding: DialogSaveFavoriteBinding
    private var isEditDialog: Boolean = false
    private lateinit var session : SessionMinimal

    companion object {
        fun newInstance(isEditDialog: Boolean, session: SessionMinimal) : SaveFavoriteDialog {
            val dialog = SaveFavoriteDialog()
            dialog.isEditDialog = isEditDialog
            dialog.session = session
            return dialog
        }
    }

    override fun onCreateDialog(savedInstBundle: Bundle?): Dialog {
        binding = DialogSaveFavoriteBinding.inflate(layoutInflater, null, false)
        isCancelable = false
        setupView()

        val b = MaterialAlertDialogBuilder(activity)
        b.apply {
            setTitle(if (isEditDialog) {"Edit favorite session"} else {"Add favorite session"})
            setPositiveButton(
                R.string.ok
            ) { _: DialogInterface?, _: Int ->
                if (isEditDialog) {
                    repo.editSessionMinimal(session.id, session);
                } else {
                    repo.addSessionMinimal(session)
                }
            }
            if (isEditDialog) {
                setNegativeButton("Delete") { _: DialogInterface?, _: Int -> repo.removeSessionMinimal(session.id) }
            } else {
                setNegativeButton(R.string.cancel) { _: DialogInterface?, _: Int -> /* do nothing */ }
            }
            setView(binding.root)
        }
        return b.create()
    }

    fun setupView() {
        val name = binding.name
        when(session.type) {
            SessionType.AMRAP -> {
                binding.amrapSection.visibility = View.VISIBLE
                val minutes = binding.amrapSection.minutes
                val seconds = binding.amrapSection.seconds
                val minutesAndSeconds = secondsToMinutesAndSeconds(session.duration)
                minutes.setText(minutesAndSeconds.first.toString())
                seconds.setText(minutesAndSeconds.second.toString())
            }
        }
    }
}
