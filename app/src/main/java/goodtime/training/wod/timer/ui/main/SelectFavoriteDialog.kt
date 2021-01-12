package goodtime.training.wod.timer.ui.main

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import goodtime.training.wod.timer.common.StringUtils.Companion.toFavoriteDescriptionDetailed
import goodtime.training.wod.timer.common.StringUtils.Companion.toFavoriteFormat
import goodtime.training.wod.timer.common.StringUtils.Companion.toString
import goodtime.training.wod.timer.common.hideKeyboardFrom
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.repository.AppRepository
import goodtime.training.wod.timer.databinding.DialogSelectFavoriteBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class SelectFavoriteDialog: DialogFragment(), KodeinAware,
        DeleteConfirmationDialog.Listener {
    override val kodein by closestKodein()
    private val preferenceHelper: PreferenceHelper by instance()

    private val repo: AppRepository by instance()
    private lateinit var favoriteCandidate: SessionSkeleton
    private lateinit var favorites: List<SessionSkeleton>
    private lateinit var binding: DialogSelectFavoriteBinding
    private lateinit var listener: Listener

    interface Listener {
        fun onFavoriteSelected(session: SessionSkeleton)
    }

    companion object {
        fun newInstance(session: SessionSkeleton, listener: Listener) : SelectFavoriteDialog {
            val dialog = SelectFavoriteDialog()
            dialog.favoriteCandidate = session
            dialog.listener = listener
            return dialog
        }
    }

    override fun onCreateDialog(savedInstBundle: Bundle?): Dialog {
        val b = MaterialAlertDialogBuilder(requireContext())

        binding = DialogSelectFavoriteBinding.inflate(layoutInflater)
        setupFavorites()
        binding.customSessionDescription.text = toFavoriteDescriptionDetailed(favoriteCandidate)

        b.apply {
            setView(binding.root)
        }
        return b.create()
    }

    @SuppressLint("SetTextI18n")
    private fun setupFavorites() {
        binding.selectFavoriteTitle.text = "Select ${toString(favoriteCandidate.type)} favorite"


        binding.favoriteCandidateChip.text = toFavoriteFormat(favoriteCandidate)
        binding.favoriteCandidateChip.setOnClickListener {
            repo.addSessionSkeleton(favoriteCandidate)
            binding.currentSelectionSection.visibility = View.GONE
        }

        repo.getSessionSkeletons(favoriteCandidate.type).observe(
                this, { favorites ->
            this.favorites = favorites
            binding.selectFavoriteTitle.isVisible = favorites.isNotEmpty()
            val favoritesChipGroup = binding.favorites
            favoritesChipGroup.isSingleSelection = true
            favoritesChipGroup.removeAllViews()
            for (favorite in this.favorites) {
                val chip = Chip(requireContext()).apply {
                    text = toFavoriteFormat(favorite)
                }
                chip.setOnCloseIconClickListener {
                    if (parentFragmentManager.findFragmentByTag("DeleteConfirmation") == null) {
                        if (preferenceHelper.showDeleteConfirmationDialog()) {
                            DeleteConfirmationDialog.newInstance(this, favorite.id, chip.text.toString())
                                    .show(parentFragmentManager, "DeleteConfirmation")
                        } else {
                            onDeleteConfirmation(favorite.id, "")
                        }
                    }
                }
                chip.setOnClickListener {
                    listener.onFavoriteSelected(favorite)
                    hideKeyboardFrom(requireContext(), binding.root)
                    dismiss()
                }
                favoritesChipGroup.addView(chip)
            }
            binding.currentSelectionSection.isVisible = this.favorites.find { it.isSame(favoriteCandidate) } == null
        })
    }

    override fun onDeleteConfirmation(id: Long, name: String) {
        repo.removeSessionSkeleton(id)
    }
}