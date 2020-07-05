package com.adrcotfas.wod.ui.common.ui

import android.R
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.adrcotfas.wod.common.StringUtils.Companion.toFavoriteDescription
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.repository.SessionsRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class ConfirmDeleteFavoriteDialog : DialogFragment(), KodeinAware {
    override val kodein by closestKodein()

    private val repo: SessionsRepository by instance()
    private lateinit var favoriteCandidate : SessionMinimal

    companion object {
        fun newInstance(favorite : SessionMinimal) : ConfirmDeleteFavoriteDialog {
            val dialog = ConfirmDeleteFavoriteDialog()
            dialog.favoriteCandidate = favorite
            return dialog
        }
    }

    override fun onCreateDialog(savedInstBundle: Bundle?): Dialog {
        val b = MaterialAlertDialogBuilder(requireContext())
        b.apply {
            setTitle("Delete this favorite?")
            setMessage(toFavoriteDescription(favoriteCandidate))
            setPositiveButton(
                R.string.ok
            ) { _: DialogInterface?, _: Int ->
                repo.removeSessionMinimal(favoriteCandidate.id)
            }
            setNegativeButton(R.string.cancel) { _: DialogInterface?, _: Int -> }
        }
        return b.create()
    }
}