package goodtime.training.wod.timer.ui.stats

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.StringUtils.Companion.formatSecondsToMinutes
import goodtime.training.wod.timer.data.model.Session
import goodtime.training.wod.timer.databinding.FragmentStatisticsBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class StatisticsFragment : Fragment(), KodeinAware {
    override val kodein by closestKodein()

    private val viewModelFactory : LogViewModelFactory by instance()

    private lateinit var binding: FragmentStatisticsBinding
    private lateinit var viewModel: StatisticsViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var historyChartWrapper: HistoryChartWrapper

    private val itemClickListener = object: StatisticsAdapter.Listener {
        override fun onClick(position: Int) {
            //TODO: implement this
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        binding = FragmentStatisticsBinding.inflate(layoutInflater, container, false)
        historyChartWrapper = HistoryChartWrapper(binding.history.chart, setupSpinner())

        setupToolbar()

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val logAdapter = StatisticsAdapter(itemClickListener)
        recyclerView.adapter = logAdapter

        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, LinearLayout.VERTICAL)
        dividerItemDecoration.setDrawable(resources.getDrawable(R.drawable.recycler_separator))
        recyclerView.addItemDecoration(dividerItemDecoration)

        viewModel = ViewModelProvider(this, viewModelFactory).get(StatisticsViewModel::class.java)
        viewModel.getSessions().observe(viewLifecycleOwner, { sessions ->
            logAdapter.data = sessions
            refreshStats(sessions)
            historyChartWrapper.refreshHistoryChart(sessions)
        })

        return binding.root
    }

    private fun setupSpinner() : Spinner {
        val rangeTypeAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.spinner_range_type, R.layout.spinner_item)
        rangeTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.history.rangeType.adapter = rangeTypeAdapter
        return binding.history.rangeType
    }

    private fun setupToolbar() {
        val appBarLayout = binding.appBar
        val arrowImageView = binding.arrowImageView
        arrowImageView.setOnClickListener {
            val fullyExpanded = appBarLayout.height - appBarLayout.bottom == 0
            appBarLayout.setExpanded(!fullyExpanded, true)
        }
        binding.toolbar.setNavigationOnClickListener {
            NavHostFragment.findNavController(this).apply {
                popBackStack()
            }
        }

        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { layout, verticalOffset ->
            val totalScrollRange: Int = layout.totalScrollRange
            val progress = (-verticalOffset).toFloat() / totalScrollRange.toFloat()
            arrowImageView.rotation = progress * 180
        })
    }

    @SuppressLint("SetTextI18n")
    private fun refreshStats(sessions: List<Session>) {
        val stats = viewModel.calculateOverviewStats(sessions)

        binding.overview.todayValue.text = formatSecondsToMinutes(stats.today)
        binding.overview.weekValue.text = formatSecondsToMinutes(stats.week)
        binding.overview.monthValue.text = formatSecondsToMinutes(stats.month)
        binding.overview.totalValue.text = formatSecondsToMinutes(stats.total)

        binding.overview.weekDescription.text =
                "${resources.getString(R.string.statistics_week)} ${viewModel.getThisWeekNumber()}"
        binding.overview.monthDescription.text = viewModel.getCurrentMonthString()
    }
}
