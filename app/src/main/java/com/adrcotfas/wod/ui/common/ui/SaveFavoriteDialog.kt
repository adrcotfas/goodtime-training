package com.adrcotfas.wod.ui.common.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.adrcotfas.wod.R
import com.adrcotfas.wod.common.StringUtils.Companion.toFavoriteDescriptionDetailed
import com.adrcotfas.wod.common.StringUtils.Companion.toFavoriteFormat
import com.adrcotfas.wod.common.StringUtils.Companion.toString
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.repository.SessionsRepository
import com.adrcotfas.wod.databinding.DialogSelectFavoriteBinding
import com.google.android.material.chip.Chip
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class SaveFavoriteDialog : DialogFragment(), KodeinAware {
    override val kodein by closestKodein()

    private val repo: SessionsRepository by instance()
    private lateinit var favoriteCandidate : SessionMinimal
    private lateinit var favorites : List<SessionMinimal>
    private lateinit var binding: DialogSelectFavoriteBinding
    private lateinit var listener: Listener

    interface Listener {
        fun onFavoriteSelected(session: SessionMinimal)
    }

    companion object {
        fun newInstance(session: SessionMinimal, listener : Listener) : SaveFavoriteDialog {
            val dialog = SaveFavoriteDialog()
            dialog.favoriteCandidate = session
            dialog.listener = listener
            return dialog
        }
    }

    override fun onCreateDialog(savedInstBundle: Bundle?): Dialog {
        val b = AlertDialog.Builder(requireContext())

        binding = DialogSelectFavoriteBinding.inflate(layoutInflater)
        setupFavorites()
        binding.currentSelectionDescription.text = toFavoriteDescriptionDetailed(favoriteCandidate)
        b.apply {
            setView(binding.root)
            binding.favoriteCandidateChip.text = toFavoriteFormat(favoriteCandidate)
            binding.favoriteCandidateChip.setOnClickListener{
                repo.addSessionMinimal(favoriteCandidate)
                binding.currentSelectionSection.visibility = View.GONE
            }
        }
        return b.create()
    }

    @SuppressLint("SetTextI18n")
    private fun setupFavorites() {
        binding.selectFavoriteTitle.text = "Select ${toString(favoriteCandidate.type)} favorite"
        repo.getSessionsMinimal(favoriteCandidate.type).observe(
            this, Observer { favorites = it

                binding.selectFavoriteTitle.visibility = if (favorites.isEmpty()) View.GONE else View.VISIBLE

                val favoritesChipGroup = binding.favorites
                favoritesChipGroup.isSingleSelection = true
                favoritesChipGroup.removeAllViews()
                for (favorite in favorites) {
                    val chip = Chip(requireContext()).apply {
                        text = toFavoriteFormat(favorite)
                    }
                    chip.setOnCloseIconClickListener { repo.removeSessionMinimal(favorite.id) }
                    chip.setOnClickListener {
                        listener.onFavoriteSelected(favorite)
                        dismiss()
                    }
                    favoritesChipGroup.addView(chip)
                }
                if (!favorites.contains(favoriteCandidate)) {
                    binding.currentSelectionSection.visibility = View.VISIBLE
                }
            })
    }
}
