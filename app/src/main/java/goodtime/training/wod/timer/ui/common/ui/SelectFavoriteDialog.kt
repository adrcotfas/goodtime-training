package goodtime.training.wod.timer.ui.common.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import goodtime.training.wod.timer.common.StringUtils.Companion.toFavoriteDescriptionDetailed
import goodtime.training.wod.timer.common.StringUtils.Companion.toFavoriteFormat
import goodtime.training.wod.timer.common.StringUtils.Companion.toString
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.repository.AppRepository
import goodtime.training.wod.timer.databinding.DialogSelectFavoriteBinding
import com.google.android.material.chip.Chip
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class SelectFavoriteDialog: DialogFragment(), KodeinAware {
    override val kodein by closestKodein()

    private val repo: AppRepository by instance()
    private lateinit var favoriteCandidate : SessionSkeleton
    private lateinit var favorites : List<SessionSkeleton>
    private lateinit var binding: DialogSelectFavoriteBinding
    private lateinit var listener: Listener

    interface Listener {
        fun onFavoriteSelected(session: SessionSkeleton)
    }

    companion object {
        fun newInstance(session: SessionSkeleton, listener : Listener) : SelectFavoriteDialog {
            val dialog = SelectFavoriteDialog()
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
                repo.addSessionSkeleton(favoriteCandidate)
                binding.currentSelectionSection.visibility = View.GONE
            }
        }
        return b.create()
    }

    @SuppressLint("SetTextI18n")
    private fun setupFavorites() {
        binding.selectFavoriteTitle.text = "Select ${toString(favoriteCandidate.type)} favorite"
        repo.getSessionSkeletons(favoriteCandidate.type).observe(
            this, Observer { favorites = it

                binding.selectFavoriteTitle.visibility = if (favorites.isEmpty()) View.GONE else View.VISIBLE

                val favoritesChipGroup = binding.favorites
                favoritesChipGroup.isSingleSelection = true
                favoritesChipGroup.removeAllViews()
                for (favorite in favorites) {
                    val chip = Chip(requireContext()).apply {
                        text = toFavoriteFormat(favorite)
                    }
                    chip.setOnCloseIconClickListener { repo.removeSessionSkeleton(favorite.id) }
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
