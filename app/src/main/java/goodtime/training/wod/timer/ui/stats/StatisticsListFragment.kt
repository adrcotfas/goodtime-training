package goodtime.training.wod.timer.ui.stats

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.data.model.Session
import goodtime.training.wod.timer.databinding.FragmentStatisticsListBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class StatisticsListFragment : Fragment(), KodeinAware {

    override val kodein by closestKodein()
    private lateinit var binding: FragmentStatisticsListBinding

    private val viewModelFactory : LogViewModelFactory by instance()
    private lateinit var viewModel: StatisticsViewModel

    private val itemClickListener = object: StatisticsAdapter.Listener {
        override fun onClick(position: Int) {
            //TODO: open AddEditStatsDialog
        }
    }

    private val logAdapter = StatisticsAdapter(itemClickListener)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireParentFragment(), viewModelFactory).get(StatisticsViewModel::class.java)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatisticsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
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

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupRecyclerView() {
        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = logAdapter

        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, LinearLayout.VERTICAL)
        dividerItemDecoration.setDrawable(resources.getDrawable(R.drawable.recycler_separator))
        recyclerView.addItemDecoration(dividerItemDecoration)
    }

    private fun observeAllSessions() {
        viewModel.getSessions().observe(viewLifecycleOwner, { sessions ->
            logAdapter.data = sessions
            binding.recyclerView.isVisible = sessions.isNotEmpty()
            binding.emptyState.isVisible = sessions.isEmpty()
        })
    }

    private fun removeObserverFromAllSessions() = viewModel.getSessions().removeObservers(viewLifecycleOwner)

    private fun observeFilteredSessions() {
        viewModel.getCustomSessions(viewModel.filteredWorkoutName.value, true).observe(viewLifecycleOwner, { sessions ->
            logAdapter.data = sessions
            logAdapter.personalRecordSessionId = findPersonalRecord(sessions.filter { it.isCompleted })
            binding.recyclerView.isVisible = sessions.isNotEmpty()
            binding.emptyState.isVisible = sessions.isEmpty()
        })
    }

    private fun removeObserverFromFilteredSessions() {
        viewModel.getCustomSessions(viewModel.filteredWorkoutName.value, true).removeObservers(viewLifecycleOwner)
        logAdapter.personalRecordSessionId = -1
    }

    private fun findPersonalRecord(sessions: List<Session>): Long {
        var id = -1L
        if (sessions.isNotEmpty()) {
            if (sessions[0].isTimeBased) {
                id = sessions.minWithOrNull { o1, o2 ->
                    when {
                        o1.actualDuration > o2.actualDuration -> 1
                        o1.actualDuration == o2.actualDuration -> 0
                        else -> -1
                    }
                }?.id ?: -1L
            } else {
                if (sessions.find { it.actualRounds > 0 || it.actualReps > 0 } != null) {
                    id = sessions.maxWithOrNull { o1, o2 ->
                        when {
                            (o1.actualRounds > o2.actualRounds) ||
                                    ((o1.actualRounds == o2.actualRounds) &&
                                            (o1.actualReps > o2.actualReps)) -> 1
                            ((o1.actualRounds == o2.actualRounds) && ((o1.actualReps == o2.actualReps))) -> 0
                            else -> -1
                        }
                    }?.id ?: -1L
                }
            }
        }
        return id
    }
}
