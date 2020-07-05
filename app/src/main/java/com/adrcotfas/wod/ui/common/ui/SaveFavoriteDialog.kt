package com.adrcotfas.wod.ui.common.ui

import android.R
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.adrcotfas.wod.common.StringUtils.Companion.toFavoriteDescription
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.repository.SessionsRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class SaveFavoriteDialog : DialogFragment(), KodeinAware {
    override val kodein by closestKodein()

    private val repo: SessionsRepository by instance()
    private lateinit var favoriteCandidate : SessionMinimal
    private lateinit var favorites : List<SessionMinimal>

    companion object {
        fun newInstance(session: SessionMinimal) : SaveFavoriteDialog {
            val dialog = SaveFavoriteDialog()
            dialog.favoriteCandidate = session
            return dialog
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dialog =
            dialog as AlertDialog?
            dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = false
        repo.getSessionsMinimal(favoriteCandidate.type).observe(this, Observer {
            favorites = it
            dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = true
        })
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateDialog(savedInstBundle: Bundle?): Dialog {
        val b = MaterialAlertDialogBuilder(requireContext())
        b.apply {
            setTitle("Save this favorite?")
            setMessage(toFavoriteDescription(favoriteCandidate))
            setPositiveButton(
                R.string.ok
            ) { _: DialogInterface?, _: Int ->
                    for (f in favorites) {
                        if (f == favoriteCandidate) {
                            Toast.makeText(requireContext(), "Favorite already exists", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }
                    }
                    repo.addSessionMinimal(favoriteCandidate)
            }
            setNegativeButton(R.string.cancel) { _: DialogInterface?, _: Int -> }
        }
        return b.create()
    }
}
