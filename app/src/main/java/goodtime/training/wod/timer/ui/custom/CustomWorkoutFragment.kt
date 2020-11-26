package goodtime.training.wod.timer.ui.custom

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import goodtime.training.wod.timer.common.ResourcesHelper
import goodtime.training.wod.timer.common.StringUtils
import goodtime.training.wod.timer.common.preferences.PrefUtil
import goodtime.training.wod.timer.data.model.CustomWorkoutSkeleton
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.data.model.TypeConverter
import goodtime.training.wod.timer.databinding.FragmentCustomBinding
import goodtime.training.wod.timer.ui.common.WorkoutTypeFragment
import goodtime.training.wod.timer.ui.common.ui.SelectCustomWorkoutDialog
import org.kodein.di.generic.instance

class CustomWorkoutFragment :
    WorkoutTypeFragment(),
    CustomWorkoutAdapter.Listener,
    SelectCustomWorkoutDialog.Listener, AddSessionDialog.Listener,
    SaveCustomWorkoutDialog.Listener {

    private val viewModelFactory : CustomWorkoutViewModelFactory by instance()
    private lateinit var viewModel: CustomWorkoutViewModel
    private lateinit var binding: FragmentCustomBinding

    private val touchHelper = ItemTouchHelper(ItemTouchCallback())
    private lateinit var listAdapter : CustomWorkoutAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(CustomWorkoutViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomBinding.inflate(inflater, container, false)

        initCurrentWorkout()

        binding.saveButton.setOnClickListener{
            SaveCustomWorkoutDialog.newInstance(viewModel.customWorkout.name, this).show(parentFragmentManager, "")
        }
        if (viewModel.hasUnsavedSession) {
            setSaveButtonVisibility(true)
        }
        binding.addSessionButton.setOnClickListener {
            AddSessionDialog.newInstance(this).show(parentFragmentManager, "")
        }

        return binding.root
    }

    private fun initCurrentWorkout() {
        viewModel.customWorkoutList.observe(viewLifecycleOwner, {
            if (it.isEmpty()) {
                viewModel.customWorkout = CustomWorkoutSkeleton("New workout", arrayListOf())
                toggleEmptyState(true)
            } else {
                //TODO: check the preferences for the last selected custom workout
                viewModel.customWorkout = it.first()
                binding.title.text = viewModel.customWorkout.name
                updateTotalDuration()
                toggleEmptyState(false)
            }
            setupRecycler()
            // observe once, no need to repeat this when new data is added to the custom workouts
            viewModel.customWorkoutList.removeObservers(viewLifecycleOwner)
        })
    }

    private fun toggleEmptyState(visible: Boolean) {
        if (visible) {
            binding.emptyState.visibility = View.VISIBLE
            binding.saveButton.visibility = View.GONE
            binding.totalTime.visibility = View.GONE
            binding.title.text = "New workout"
            viewModel.customWorkout.name = "New workout"
            viewModel.hasUnsavedSession = false
        } else {
            binding.emptyState.visibility = View.GONE
            binding.totalTime.visibility = View.VISIBLE
        }
    }

    private fun setupRecycler() {
        binding.title.text = viewModel.customWorkout.name
        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            listAdapter = CustomWorkoutAdapter(
                viewModel.customWorkout.sessions,
                context,
                this@CustomWorkoutFragment
            )
            adapter = listAdapter
        }
        touchHelper.attachToRecyclerView(binding.recycler)
    }

    override fun onStartWorkout() {
        val action = CustomWorkoutFragmentDirections.toWorkout(
            TypeConverter.toString(sessions = arrayOf(PrefUtil.generatePreWorkoutSession()) + getSelectedSessions().toTypedArray() ))
        findNavController().navigate(action)
    }

    override fun getSelectedSessions(): ArrayList<SessionSkeleton> = viewModel.customWorkout.sessions

    override fun onDeleteButtonClicked(position: Int) {
        //TODO: if the current workout is deleted, change it
        setSaveButtonVisibility(true)
        updateTotalDuration()
        viewModel.hasUnsavedSession = true
        if (listAdapter.data.isEmpty()) {
            toggleEmptyState(true)
        }
    }

    override fun onChipSelected(position: Int) {
        AddSessionDialog.newInstance(this, position, listAdapter.data[position])
            .show(parentFragmentManager, "")
    }

    override fun onDataReordered() {
        setSaveButtonVisibility(true)
        viewModel.hasUnsavedSession = true
    }

    override fun onScrollHandleTouch(holder: CustomWorkoutAdapter.ViewHolder) {
        touchHelper.startDrag(holder)
    }

    override fun onFavoriteSelected(session: SessionSkeleton) { /* do nothing*/ }

    override fun onFavoriteSelected(workout: CustomWorkoutSkeleton) {
        viewModel.customWorkout = workout
        binding.title.text = viewModel.customWorkout.name
        listAdapter.data = viewModel.customWorkout.sessions
        listAdapter.notifyDataSetChanged()
        setSaveButtonVisibility(false)
        updateTotalDuration()
        toggleEmptyState(false)
    }

    override fun onSessionAdded(session: SessionSkeleton) {
        viewModel.customWorkout.sessions.add(session)
        listAdapter.notifyItemInserted(listAdapter.data.size - 1)
        setSaveButtonVisibility(true)
        viewModel.hasUnsavedSession = true
        binding.recycler.scrollToPosition(listAdapter.data.size - 1)
        toggleEmptyState(false)
        updateTotalDuration()
    }

    override fun onSessionEdit(idx: Int, session: SessionSkeleton) {
        if(viewModel.customWorkout.sessions[idx] != session) setSaveButtonVisibility(true)
        viewModel.customWorkout.sessions[idx] = session
        listAdapter.notifyItemChanged(idx)
    }

    @SuppressLint("SetTextI18n")
    private fun setSaveButtonVisibility(visible: Boolean) {
        binding.saveButton.visibility = if (visible) View.VISIBLE else View.GONE
        binding.title.setTextColor(if (visible) ResourcesHelper.grey800 else ResourcesHelper.grey500)
    }

    override fun onCustomWorkoutSaved(name: String) {
        viewModel.customWorkout.name = name
        viewModel.saveCurrentSelection()
        binding.title.text = name
        setSaveButtonVisibility(false)
        viewModel.hasUnsavedSession = false
    }

    private fun updateTotalDuration() {
        var total = 0
        for (session in viewModel.customWorkout.sessions) {
            total += when (session.type) {
                SessionType.AMRAP, SessionType.FOR_TIME, SessionType.REST -> session.duration
                SessionType.EMOM -> (session.duration * session.numRounds)
                SessionType.TABATA -> (session.duration * session.numRounds + session.breakDuration * session.numRounds)
            }
        }
        binding.totalTime.visibility = if (total == 0) View.GONE else View.VISIBLE
        binding.totalTime.text = StringUtils.secondsToNiceFormat(total)
    }
}