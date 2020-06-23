package com.adrcotfas.wod.ui.common.ui

import android.R
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.adrcotfas.wod.common.TimerUtils.Companion.insertPrefixZero
import com.adrcotfas.wod.common.TimerUtils.Companion.secondsToMinutesAndSeconds
import com.adrcotfas.wod.common.afterTextChanged
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
        setupView()

        val b = MaterialAlertDialogBuilder(activity)
        b.apply {
            setTitle(if (isEditDialog) {"Edit favorite session"} else {"Add favorite session"})
            setPositiveButton(
                R.string.ok
            ) { _: DialogInterface?, _: Int ->
                if (isEditDialog) {
                    repo.editSessionMinimal(session.id, session)
                } else {
                    repo.addSessionMinimal(session)
                }
            }
            if (isEditDialog) {
                setNegativeButton("Delete") { _: DialogInterface?, _: Int -> repo.removeSessionMinimal(session.id) }
            }
            setNeutralButton(R.string.cancel) { _: DialogInterface?, _: Int -> }
            setView(binding.root)
        }
        return b.create()
    }

    private fun setupView() {
        val name = binding.name
        when(session.type) {
            SessionType.AMRAP -> {
                binding.amrapSection.visibility = View.VISIBLE
                val minutes = binding.amrapSection.minutes
                val seconds = binding.amrapSection.seconds
                val minutesAndSeconds = secondsToMinutesAndSeconds(session.duration)
                minutes.setText(insertPrefixZero(minutesAndSeconds.first))
                seconds.setText(insertPrefixZero(minutesAndSeconds.second))

                minutes.setOnClickListener { minutes.requestFocus() }
                seconds.setOnClickListener { seconds.requestFocus() }

                minutes.afterTextChanged {
                    val dialog = dialog as AlertDialog
                    if (it.isEmpty() || (it.toInt() == 0 && (seconds.text.isEmpty() || seconds.text.toString().toInt() == 0))) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                    } else {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                        if (it.length == 1 && it.toInt() > 6) {
                            minutes.setText(insertPrefixZero(it.toInt()))
                        } else if (it.length == 2 && it.elementAt(0) == '6' && it.elementAt(1) != '0') {
                            minutes.setText(60.toString())
                            seconds.requestFocus()
                        } else if (it.length == 2) {
                            seconds.requestFocus()
                        }
                    }
                }

                seconds.afterTextChanged {
                    val dialog = dialog as AlertDialog
                    if (it.isEmpty() || (it.toInt() == 0 && (minutes.text.isEmpty() || minutes.text.toString().toInt() == 0))) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                    } else {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                        if (it.length == 1 && it.toInt() > 5) {
                            seconds.setText(insertPrefixZero(it.toInt()))
                            seconds.clearFocus()
                        } else if (it.length == 2) {
                            seconds.clearFocus()
                        }
                    }
                }
            }
        }
    }
}
