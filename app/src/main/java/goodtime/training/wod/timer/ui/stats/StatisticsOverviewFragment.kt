package goodtime.training.wod.timer.ui.stats

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.StringUtils.Companion.formatSecondsToOverviewTime
import goodtime.training.wod.timer.data.model.Session
import goodtime.training.wod.timer.databinding.FragmentStatisticsOverviewBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class StatisticsOverviewFragment : Fragment(), KodeinAware {

    override val kodein by closestKodein()

    private lateinit var binding: FragmentStatisticsOverviewBinding
    private lateinit var historyChartWrapper: HistoryChartWrapper

    private val viewModelFactory : LogViewModelFactory by instance()
    private lateinit var viewModel: StatisticsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireParentFragment(), viewModelFactory).get(StatisticsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatisticsOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        historyChartWrapper = HistoryChartWrapper(binding.chart, setupSpinner())
        viewModel.filteredWorkoutName.observe(viewLifecycleOwner, {
            if (it == null) {
                removeObserverFromFilteredSessions()
                observeAllSessions()
            } else { // a filtered session was selected
                removeObserverFromAllSessions()
                observeFilteredSessions()
            }
        })
    }

    private fun observeAllSessions() {
        viewModel.getSessions().observe(viewLifecycleOwner, { sessions ->
            refreshStats(sessions)
            historyChartWrapper.refreshHistoryChart(sessions)
        })
    }

    private fun removeObserverFromAllSessions() = viewModel.getSessions().removeObservers(viewLifecycleOwner)

    private fun observeFilteredSessions() {
        viewModel.getCustomSessions(viewModel.filteredWorkoutName.value).observe(viewLifecycleOwner, { sessions ->
            refreshStats(sessions)
            historyChartWrapper.refreshHistoryChart(sessions)
        })
    }

    private fun removeObserverFromFilteredSessions() = viewModel.getCustomSessions(
            viewModel.filteredWorkoutName.value).removeObservers(viewLifecycleOwner)


    private fun setupSpinner() : Spinner {
        val rangeTypeAdapter = ArrayAdapter.createFromResource(requireContext(),
            R.array.spinner_range_type, R.layout.spinner_item)
        rangeTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.rangeType.adapter = rangeTypeAdapter
        return binding.rangeType
    }

    @SuppressLint("SetTextI18n")
    private fun refreshStats(sessions: List<Session>) {
        val stats = viewModel.calculateOverviewStats(sessions)

        binding.todayValue.text = formatSecondsToOverviewTime(stats.today)
        binding.weekValue.text = formatSecondsToOverviewTime(stats.week)
        binding.monthValue.text = formatSecondsToOverviewTime(stats.month)
        binding.totalValue.text = formatSecondsToOverviewTime(stats.total)

        binding.weekDescription.text =
            "${resources.getString(R.string.statistics_week)} ${viewModel.getThisWeekNumber()}"
        binding.monthDescription.text = viewModel.getCurrentMonthString()
    }
}
