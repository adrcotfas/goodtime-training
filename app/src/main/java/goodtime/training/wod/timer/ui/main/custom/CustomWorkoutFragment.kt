package goodtime.training.wod.timer.ui.main.custom

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import goodtime.training.wod.timer.MainActivity
import goodtime.training.wod.timer.common.ResourcesHelper
import goodtime.training.wod.timer.common.StringUtils
import goodtime.training.wod.timer.common.preferences.PrefUtil
import goodtime.training.wod.timer.data.model.CustomWorkoutSkeleton
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.data.model.TypeConverter
import goodtime.training.wod.timer.databinding.FragmentCustomBinding
import goodtime.training.wod.timer.ui.main.WorkoutTypeFragment
import org.kodein.di.generic.instance

class CustomWorkoutFragment:
    WorkoutTypeFragment(),
    CustomWorkoutAdapter.Listener,
    SelectCustomWorkoutDialog.Listener, AddEditSessionDialog.Listener,
    SaveCustomWorkoutDialog.Listener {

    private val viewModelFactory: CustomWorkoutViewModelFactory by instance()
    private val prefUtil: PrefUtil by instance()

    private lateinit var viewModel: CustomWorkoutViewModel
    private lateinit var binding: FragmentCustomBinding

    private val touchHelper = ItemTouchHelper(ItemTouchCallback())
    private lateinit var listAdapter : CustomWorkoutAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory).get(CustomWorkoutViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCustomBinding.inflate(inflater, container, false)

        initCurrentWorkout()

        binding.saveButton.setOnClickListener{
            SaveCustomWorkoutDialog.newInstance(viewModel.currentWorkout.name, this)
                .show(parentFragmentManager, "")
        }
        if (viewModel.hasUnsavedSession) {
            setSaveButtonVisibility(true)
        }
        binding.addSessionButton.setOnClickListener {
            AddEditSessionDialog.newInstance(this).show(parentFragmentManager, "")
        }
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        refreshStartButtonState(true)
    }

    private fun initCurrentWorkout() {
        val workoutList = viewModel.getWorkoutList()
        workoutList.observe(viewLifecycleOwner, {
            if (it.isEmpty()) {
                viewModel.currentWorkout = CustomWorkoutSkeleton("New workout", arrayListOf())
                toggleEmptyState(true)
                refreshStartButtonState()
            } else {
                var found = false
                val name = prefUtil.getCurrentCustomWorkoutFavoriteName()
                if (name != null) {
                    for (fav in it) {
                        if (fav.name == name) {
                            viewModel.currentWorkout = fav
                            binding.title.text = fav.name
                            found = true
                            break
                        }
                    }
                }
                if (!found) {
                    viewModel.currentWorkout = it.first()
                    binding.title.text = viewModel.currentWorkout.name
                }
                updateTotalDuration()
                toggleEmptyState(false)
                setSaveButtonVisibility(false)
            }
            setupRecycler()
            // observe once, no need to repeat this when new data is added to the custom workouts
            workoutList.removeObservers(viewLifecycleOwner)
        })
    }

    override fun onStartWorkout() {
        val action = CustomWorkoutFragmentDirections.toWorkout(
            TypeConverter.toString(sessions = arrayOf(PrefUtil.generatePreWorkoutSession()) + getSelectedSessions().toTypedArray() ))
        findNavController().navigate(action)
    }

    //TODO: extract duplicate code
    override fun getSelectedSessions(): ArrayList<SessionSkeleton> = viewModel.currentWorkout.sessions

    private fun toggleEmptyState(visible: Boolean) {
        if (visible) {
            binding.emptyState.visibility = View.VISIBLE
            binding.saveButton.visibility = View.GONE
            binding.totalTime.visibility = View.GONE
            binding.title.text = "New workout"
            viewModel.currentWorkout.name = "New workout"
            viewModel.hasUnsavedSession = false
        } else {
            binding.emptyState.visibility = View.GONE
            binding.totalTime.visibility = View.VISIBLE
        }
    }

    private fun setupRecycler() {
        binding.title.text = viewModel.currentWorkout.name
        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            listAdapter = CustomWorkoutAdapter(
                viewModel.currentWorkout.sessions,
                context,
                this@CustomWorkoutFragment
            )
            adapter = listAdapter
        }
        touchHelper.attachToRecyclerView(binding.recycler)
    }

    override fun onSessionAdded(session: SessionSkeleton) {
        viewModel.currentWorkout.sessions.add(session)
        viewModel.hasUnsavedSession = true

        toggleEmptyState(false)
        setSaveButtonVisibility(true)
        updateTotalDuration()
        refreshStartButtonState()

        listAdapter.notifyItemInserted(listAdapter.data.size - 1)
        binding.recycler.scrollToPosition(listAdapter.data.size - 1)
    }

    override fun onSessionEdit(idx: Int, session: SessionSkeleton) {
        if(viewModel.currentWorkout.sessions[idx] != session) {
            viewModel.currentWorkout.sessions[idx] = session
            viewModel.hasUnsavedSession = true

            setSaveButtonVisibility(true)
            updateTotalDuration()
            listAdapter.notifyItemChanged(idx)
        }
    }

    override fun onDataReordered() {
        setSaveButtonVisibility(true)
        viewModel.hasUnsavedSession = true
    }

    override fun onDeleteButtonClicked(position: Int) {
        updateTotalDuration()
        viewModel.hasUnsavedSession = true
        if (listAdapter.data.isEmpty()) {
            toggleEmptyState(true)
        } else {
            setSaveButtonVisibility(true)
        }
        refreshStartButtonState()
    }

    override fun onChipSelected(position: Int) {
        AddEditSessionDialog.newInstance(this, position, listAdapter.data[position])
            .show(parentFragmentManager, "")
    }

    override fun onScrollHandleTouch(holder: CustomWorkoutAdapter.ViewHolder) {
        touchHelper.startDrag(holder)
    }

    override fun onFavoriteSelected(session: SessionSkeleton) { /* do nothing*/ }

    override fun onFavoriteSelected(workout: CustomWorkoutSkeleton) {
        viewModel.currentWorkout = workout
        binding.title.text = viewModel.currentWorkout.name
        listAdapter.data = viewModel.currentWorkout.sessions
        listAdapter.notifyDataSetChanged()
        setSaveButtonVisibility(false)
        updateTotalDuration()
        toggleEmptyState(false)
        refreshStartButtonState()

        prefUtil.setCurrentCustomWorkoutFavoriteName(workout.name)
    }

    override fun onFavoriteDeleted(name: String) {
        if (viewModel.currentWorkout.name == name && viewModel.currentWorkout.sessions.isNotEmpty()) {
            setSaveButtonVisibility(true)
            prefUtil.setCurrentCustomWorkoutFavoriteName("")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setSaveButtonVisibility(visible: Boolean) {
        binding.saveButton.visibility = if (visible) View.VISIBLE else View.GONE
        binding.title.setTextColor(if (visible) ResourcesHelper.grey800 else ResourcesHelper.grey500)
    }

    override fun onCustomWorkoutSaved(name: String) {
        viewModel.currentWorkout.name = name
        viewModel.saveCurrentSelection()
        binding.title.text = name
        setSaveButtonVisibility(false)
        viewModel.hasUnsavedSession = false
        prefUtil.setCurrentCustomWorkoutFavoriteName(name)
    }

    private fun updateTotalDuration() {
        var total = 0
        for (session in viewModel.currentWorkout.sessions) {
            total += when (session.type) { //TODO: this ended up being null but how?
                SessionType.AMRAP, SessionType.FOR_TIME, SessionType.REST -> session.duration
                SessionType.EMOM -> (session.duration * session.numRounds)
                SessionType.HIIT -> (session.duration * session.numRounds + session.breakDuration * session.numRounds)
            }
        }
        binding.totalTime.visibility = if (total == 0) View.GONE else View.VISIBLE
        binding.totalTime.text = StringUtils.secondsToNiceFormat(total)
    }

    fun onNewCustomWorkoutButtonClick() {
        val name = "New workout"
        viewModel.currentWorkout.name = name
        viewModel.currentWorkout.sessions.clear()

        binding.title.text = name
        listAdapter.notifyDataSetChanged()
        setSaveButtonVisibility(false)
        toggleEmptyState(true)
        updateTotalDuration()
        refreshStartButtonState()
    }

    private fun refreshStartButtonState(state: Boolean = viewModel.currentWorkout.sessions.isNotEmpty()) {
        (requireActivity() as MainActivity).setStartButtonState(state)
    }
}