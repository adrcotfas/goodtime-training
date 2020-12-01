package goodtime.training.wod.timer.ui.main.amrap_for_time

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import goodtime.training.wod.timer.data.model.SessionType
import org.kodein.di.generic.instance

class ForTimeFragment : MinutesAndSecondsFragment<ForTimeViewModel>(SessionType.FOR_TIME) {

    private val viewModelFactory: ForTimeViewModelFactory by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ForTimeViewModel::class.java)
    }
}