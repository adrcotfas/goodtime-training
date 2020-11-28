package goodtime.training.wod.timer.ui.main

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import goodtime.training.wod.timer.common.StringUtils.Companion.toFavoriteDescriptionDetailed
import goodtime.training.wod.timer.common.StringUtils.Companion.toFavoriteFormat
import goodtime.training.wod.timer.common.StringUtils.Companion.toString
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.repository.AppRepository
import goodtime.training.wod.timer.databinding.DialogSelectFavoriteBinding
import com.google.android.material.chip.Chip
import goodtime.training.wod.timer.data.model.SessionType
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class SelectFavoriteDialog: DialogFragment(), KodeinAware, SessionEditTextHelper.Listener {
    override val kodein by closestKodein()

    private val repo: AppRepository by instance()
    private lateinit var favoriteCandidate : SessionSkeleton
    private lateinit var favorites : List<SessionSkeleton>
    private lateinit var binding: DialogSelectFavoriteBinding
    private lateinit var listener: Listener

    private lateinit var sessionEditTextHelper: SessionEditTextHelper

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

        binding.customSessionDescription.text = toFavoriteDescriptionDetailed(favoriteCandidate)
        b.apply {
            setView(binding.root)
            binding.saveButton.setOnClickListener{
                repo.addSessionSkeleton(sessionEditTextHelper.generateFromCurrentSelection())
            }
        }
        return b.create()
    }

    @SuppressLint("SetTextI18n")
    private fun setupFavorites() {
        binding.selectFavoriteTitle.text = "Select ${toString(favoriteCandidate.type)} favorite"
        repo.getSessionSkeletons(favoriteCandidate.type).observe(
            this, {
                favorites = it
                binding.selectFavoriteTitle.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
                refreshActiveSection(favoriteCandidate.type)
                initSessionEditTextHelper()
                binding.saveButton.isEnabled = !it.contains(favoriteCandidate)

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
            })
    }

    private fun initSessionEditTextHelper() {
        val sessionType = favoriteCandidate.type
        sessionEditTextHelper =
            when(sessionType) {
                SessionType.AMRAP, SessionType.FOR_TIME, SessionType.REST -> {
                    SessionEditTextHelper(this,
                        genericMinutesEt = binding.genericMinutesLayout.editText,
                        genericSecondsEt = binding.genericSecondsLayout.editText,
                        sessionType = sessionType
                    )
                }
                SessionType.EMOM -> {
                    SessionEditTextHelper(this,
                        emomRoundsEt = binding.emomRoundsLayout.editText,
                        emomMinutesEt = binding.emomMinutesLayout.editText,
                        emomSecondsEt = binding.emomSecondsLayout.editText,
                        sessionType = sessionType
                    )
                }
                SessionType.TABATA -> {
                    SessionEditTextHelper(this,
                        hiitRoundsEt = binding.hiitRoundsLayout.editText,
                        hiitSecondsWorkEt = binding.hiitSecondsWorkLayout.editText,
                        hiitSecondsRestEt = binding.hiitSecondsRestLayout.editText,
                        sessionType = sessionType
                    )
                }
            }
        sessionEditTextHelper.updateEditTexts(favoriteCandidate)
    }

    override fun onTextChanged(isValid: Boolean, sessionSkeleton: SessionSkeleton) {
        binding.saveButton.isEnabled = isValid
        binding.customSessionDescription.text =
            if (isValid) toFavoriteDescriptionDetailed(sessionSkeleton)
            else "Please enter valid values."
        if (isValid) {
            binding.saveButton.isEnabled = !favorites.contains(sessionEditTextHelper.generateFromCurrentSelection())
        }
    }

    private fun refreshActiveSection(sessionType: SessionType) {
        when (sessionType) {
            SessionType.AMRAP, SessionType.FOR_TIME, SessionType.REST -> {
                binding.genericSection.visibility = View.VISIBLE
                binding.emomSection.visibility = View.GONE
                binding.hiitSection.visibility = View.GONE
            }
            SessionType.EMOM -> {
                binding.genericSection.visibility = View.GONE
                binding.emomSection.visibility = View.VISIBLE
                binding.hiitSection.visibility = View.GONE
            }
            SessionType.TABATA -> {
                binding.genericSection.visibility = View.GONE
                binding.emomSection.visibility = View.GONE
                binding.hiitSection.visibility = View.VISIBLE
            }
        }
        binding.customSection.visibility = View.VISIBLE
    }
}
