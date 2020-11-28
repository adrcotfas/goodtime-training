package goodtime.training.wod.timer.ui.main.amrap_for_time

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import goodtime.training.wod.timer.data.model.SessionType

class AmrapFragment : MinutesAndSecondsFragment<AmrapViewModel>(SessionType.AMRAP) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(AmrapViewModel::class.java)
    }
}