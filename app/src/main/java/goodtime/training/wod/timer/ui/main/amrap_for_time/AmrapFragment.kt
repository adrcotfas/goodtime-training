package goodtime.training.wod.timer.ui.main.amrap_for_time

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import goodtime.training.wod.timer.data.model.SessionType
import org.kodein.di.generic.instance

class AmrapFragment : MinutesAndSecondsFragment<AmrapViewModel>(SessionType.AMRAP) {

    private val viewModelFactory: AmrapViewModelFactory by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory).get(AmrapViewModel::class.java)
    }
}