package goodtime.training.wod.timer.ui.main.custom

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import goodtime.training.wod.timer.MainActivity
import goodtime.training.wod.timer.common.ResourcesHelper
import goodtime.training.wod.timer.common.StringUtils
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import goodtime.training.wod.timer.data.model.CustomWorkoutSkeleton
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.data.model.TypeConverter
import goodtime.training.wod.timer.databinding.FragmentCustomBinding
import goodtime.training.wod.timer.ui.main.CustomBalloonFactory
import goodtime.training.wod.timer.ui.main.WorkoutTypeFragment
import org.kodein.di.generic.instance

class CustomWorkoutFragment:
        WorkoutTypeFragment(),
        CustomWorkoutAdapter.Listener,
        SelectCustomWorkoutDialog.Listener, AddEditSessionDialog.Listener,
        SaveCustomWorkoutDialog.Listener {

    private val viewModelFactory: CustomWorkoutViewModelFactory by instance()
    private val preferenceHelper: PreferenceHelper by instance()

    private lateinit var viewModel: CustomWorkoutViewModel
    private lateinit var binding: FragmentCustomBinding

    private val touchHelper = ItemTouchHelper(ItemTouchCallback())
    private lateinit var listAdapter : CustomWorkoutAdapter

    private var isFresh = false

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
            if (parentFragmentManager.findFragmentByTag("SaveCustomWorkoutDialog") == null) {
                SaveCustomWorkoutDialog.newInstance(viewModel.currentWorkout.name, this, isFresh)
                        .show(parentFragmentManager, "SaveCustomWorkoutDialog")
            }
        }

        if (viewModel.hasUnsavedSession) {
            setSaveButtonVisibility(true)
        }
        binding.addSessionButton.setOnClickListener {
            if (parentFragmentManager.findFragmentByTag("AddEditSessionDialog") == null) {
                AddEditSessionDialog.newInstance(this).show(parentFragmentManager, "AddEditSessionDialog")
            }
        }

        showBalloonsIfNeeded()
        return binding.root
    }

    private fun showBalloonsIfNeeded() {
        if (preferenceHelper.showCustomBalloons()) {
            preferenceHelper.setCustomBalloons(false)
            val balloon = CustomBalloonFactory.create(
                    requireContext(), this,
                    "Add, remove, edit and rearrange sessions in any combination for a custom workout."
            )
            val anotherBalloon = CustomBalloonFactory.create(
                    requireContext(), this,
                    "Create new presets and add them to the favorites."
            )

            binding.cardContainer.post {
                balloon.relayShowAlignTop(anotherBalloon, binding.cardContainer)
                balloon.showAlignTop(binding.cardContainer)
            }
        }
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
                val name = preferenceHelper.getCurrentCustomWorkoutFavoriteName()
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
                viewModel.currentWorkout.name,
                TypeConverter.toString(sessions = arrayOf(PreferenceHelper.generatePreWorkoutSession(preferenceHelper.getPreWorkoutCountdown()))
                        + getSelectedSessions().toTypedArray() ))
        findNavController().navigate(action)
    }

    //TODO: extract duplicate code
    override fun getSelectedSessions(): ArrayList<SessionSkeleton> = viewModel.currentWorkout.sessions

    private fun toggleEmptyState(visible: Boolean) {
        if (visible) {
            binding.emptyState.isVisible = true
            binding.saveButton.isVisible = false
            binding.totalTime.isVisible = false
            binding.title.text = "New workout"
            viewModel.currentWorkout.name = "New workout"
            viewModel.hasUnsavedSession = false
        } else {
            binding.emptyState.isVisible = false
            binding.totalTime.isVisible = true
        }
    }

    private fun setupRecycler() {
        binding.title.text = viewModel.currentWorkout.name
        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            listAdapter = CustomWorkoutAdapter(
                    viewModel.currentWorkout.sessions,
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
            isFresh = true
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

        preferenceHelper.setCurrentCustomWorkoutFavoriteName(workout.name)
        isFresh = false
    }

    override fun onFavoriteDeleted(name: String) {
        if (viewModel.currentWorkout.name == name && viewModel.currentWorkout.sessions.isNotEmpty()) {
            setSaveButtonVisibility(true)
            preferenceHelper.setCurrentCustomWorkoutFavoriteName("")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setSaveButtonVisibility(visible: Boolean) {
        binding.saveButton.isVisible = visible
        binding.title.setTextColor(if (visible) ResourcesHelper.grey800 else ResourcesHelper.grey500)
    }

    override fun onCustomWorkoutSaved(name: String) {
        viewModel.currentWorkout.name = name
        viewModel.saveCurrentSelection()
        binding.title.text = name
        setSaveButtonVisibility(false)
        viewModel.hasUnsavedSession = false
        preferenceHelper.setCurrentCustomWorkoutFavoriteName(name)
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
        binding.totalTime.isVisible = total != 0
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
        isFresh = true
    }

    private fun refreshStartButtonState(state: Boolean = viewModel.currentWorkout.sessions.isNotEmpty()) {
        (requireActivity() as MainActivity).setStartButtonState(state)
    }
}