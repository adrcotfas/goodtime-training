package goodtime.training.wod.timer.ui.main.amrap_for_time

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import goodtime.training.wod.timer.data.model.SessionType

class ForTimeFragment : MinutesAndSecondsFragment<ForTimeViewModel>(SessionType.FOR_TIME) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ForTimeViewModel::class.java)
    }
}