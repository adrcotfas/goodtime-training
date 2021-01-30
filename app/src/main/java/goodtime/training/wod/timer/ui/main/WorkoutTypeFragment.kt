package goodtime.training.wod.timer.ui.main

import android.view.View
import androidx.fragment.app.Fragment
import goodtime.training.wod.timer.common.Events
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.ui.main.custom.SelectCustomWorkoutDialog
import org.greenrobot.eventbus.EventBus
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein

abstract class WorkoutTypeFragment:
    Fragment(),
    KodeinAware,
    SelectFavoriteDialog.Listener,
    SelectCustomWorkoutDialog.Listener {

    override val kodein by closestKodein()
    private var isDurationValid = true

    override fun onResume() {
        super.onResume()
        showContent()
    }

    override fun onPause() {
        super.onPause()
        hideContent()
    }

    abstract fun onStartWorkout()

    abstract fun getSelectedSessions() : ArrayList<SessionSkeleton>

    fun updateMainButtonsState(duration: Int) {
        if (isDurationValid && duration != 0 || !isDurationValid && duration == 0) {
            return
        }
        isDurationValid = duration != 0
        EventBus.getDefault().post(Events.Companion.SetStartButtonStateWithColor(isDurationValid))
    }

    private fun hideContent() {
        view?.apply {
            alpha = 1f
            visibility = View.VISIBLE
            animate()
                .alpha(0f)
                .setDuration(FADE_ANIMATION_DURATION)
                .setListener(null)
        }
    }

    private fun showContent() {
        view?.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .setDuration(FADE_ANIMATION_DURATION)
                .setListener(null)
        }
    }

    companion object {
        const val FADE_ANIMATION_DURATION = 150L
    }
}