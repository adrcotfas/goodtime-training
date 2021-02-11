package goodtime.training.wod.timer.ui.stats

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import goodtime.training.wod.timer.data.model.WeeklyGoal
import goodtime.training.wod.timer.data.repository.AppRepository
import goodtime.training.wod.timer.databinding.DialogEditWeeklyGoalBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class EditWeeklyGoalDialog : DialogFragment(), KodeinAware {
    override val kodein by closestKodein()

    private val repo: AppRepository by instance()
    private lateinit var binding: DialogEditWeeklyGoalBinding

    private lateinit var weeklyGoal: WeeklyGoal

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstBundle: Bundle?): Dialog {
        val b = MaterialAlertDialogBuilder(requireContext())
        binding = DialogEditWeeklyGoalBinding.inflate(layoutInflater)
        binding.slider.isVisible = false
        val goalLd = repo.getWeeklyGoal()
        goalLd.observe(this, {
            weeklyGoal = it
            binding.slider.value = weeklyGoal.minutes.toFloat()
            goalLd.removeObservers(this)
            binding.slider.isVisible = true
        })

        binding.slider.addOnChangeListener { _, value, _ ->
            weeklyGoal.minutes = value.toInt()
        }

        b.apply {
            setView(binding.root)
            setTitle("Weekly goal")
            setPositiveButton(android.R.string.ok) { _, _ ->
                repo.updateWeeklyGoal(weeklyGoal)
            }
        }

        return b.create()
    }
}