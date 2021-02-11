package goodtime.training.wod.timer.ui.stats

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.data.model.Session
import goodtime.training.wod.timer.databinding.FragmentStatisticsListBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class StatisticsListFragment : Fragment(), KodeinAware, ActionModeCallback.Listener {

    override val kodein by closestKodein()
    private lateinit var binding: FragmentStatisticsListBinding

    private val viewModelFactory: StatisticsViewModelFactory by instance()
    private lateinit var viewModel: StatisticsViewModel
    private lateinit var allSessionsLd: LiveData<List<Session>>
    private lateinit var filteredSessionsLd: LiveData<List<Session>>

    private lateinit var actionMode: ActionMode
    private var actionModeCallback = ActionModeCallback(this)

    private val itemClickListener = object : StatisticsAdapter.Listener {
        override fun onClick(id: Long) {
            if (!logAdapter.isInActionMode) {
                if (parentFragmentManager.findFragmentByTag("AddEditCompletedWorkout") == null) {
                    EditCompletedWorkoutDialog.newInstance(id).show(parentFragmentManager, "AddEditCompletedWorkout")
                }
            }
        }

        override fun onLongClick(id: Long) {
            if (!logAdapter.isInActionMode) {
                actionMode = requireActivity().startActionMode(actionModeCallback)!!
            }
        }

        override fun notifyEmpty() = actionMode.finish()
    }

    private val logAdapter: StatisticsAdapter = StatisticsAdapter(itemClickListener)

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
                if (this::filteredSessionsLd.isInitialized) {
                    removeObserverFromFilteredSessions()
                }
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
        allSessionsLd = viewModel.getSessions()
        allSessionsLd.observe(viewLifecycleOwner, { sessions ->
            //TODO: use DiffUtil like in Goodtime
            logAdapter.data = sessions
            binding.recyclerView.isVisible = sessions.isNotEmpty()
            binding.emptyState.isVisible = sessions.isEmpty()
        })
    }

    private fun removeObserverFromAllSessions() = allSessionsLd.removeObservers(viewLifecycleOwner)

    private fun observeFilteredSessions() {
        filteredSessionsLd = viewModel.getCustomSessions(viewModel.filteredWorkoutName.value)
        filteredSessionsLd.observe(viewLifecycleOwner, { sessions ->
            //TODO: use DiffUtil like in Goodtime
            logAdapter.data = sessions
            logAdapter.personalRecordSessionId = viewModel.findPersonalRecord(sessions.filter { it.isCompleted })
            binding.recyclerView.isVisible = sessions.isNotEmpty()
            binding.emptyState.isVisible = sessions.isEmpty()
        })
    }

    private fun removeObserverFromFilteredSessions() {
        filteredSessionsLd.removeObservers(viewLifecycleOwner)
        logAdapter.personalRecordSessionId = -1
    }

    override fun onSelectAllItems() {
        logAdapter.selectAll()
    }

    override fun onDeleteItem() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle("Delete selected items?")
            setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.deleteCompletedWorkouts(logAdapter.selectedItems)
                logAdapter.isInActionMode = false
                logAdapter.selectedItems.clear()
                actionMode.finish()
            }
        }.create().show()
    }

    override fun onCloseActionMode() {
        logAdapter.isInActionMode = false
        logAdapter.selectedItems.clear()
        logAdapter.notifyDataSetChanged()
        if (this::actionMode.isInitialized) actionMode.finish()
    }

    override fun onPause() {
        super.onPause()
        onCloseActionMode()
    }
}
