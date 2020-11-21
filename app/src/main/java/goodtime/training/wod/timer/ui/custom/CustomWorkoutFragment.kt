package goodtime.training.wod.timer.ui.custom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import goodtime.training.wod.timer.common.preferences.PrefUtil
import goodtime.training.wod.timer.data.model.CustomWorkoutSkeleton
import goodtime.training.wod.timer.data.model.SessionSkeleton
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

        return binding.root
    }

    private fun initCurrentWorkout() {
        viewModel.customWorkoutList.observe(viewLifecycleOwner, {
            if (it.isEmpty()) {
                //TODO: display an empty state
            } else {
                //TODO: check the preferences for the last selected custom workout
                viewModel.customWorkout = it.first()
                updateWorkoutTitle(viewModel.customWorkout.name)
            }
            setupRecycler()
            // observe once, no need to repeat this when new data is added to the custom workouts
            viewModel.customWorkoutList.removeObservers(viewLifecycleOwner)
        })
    }

    private fun setupRecycler() {
        updateWorkoutTitle(viewModel.customWorkout.name)
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
        binding.addSessionButton.addSessionButton.setOnClickListener {
            AddSessionDialog.newInstance(this).show(parentFragmentManager, "")
        }
    }

    override fun onStartWorkout() {
        val action = CustomWorkoutFragmentDirections.toWorkout(
            TypeConverter.toString(sessions = arrayOf(PrefUtil.generatePreWorkoutSession()) + getSelectedSessions().toTypedArray() ))
        findNavController().navigate(action)
    }

    override fun getSelectedSessions(): ArrayList<SessionSkeleton> = viewModel.customWorkout.sessions

    override fun onDeleteButtonClicked(position: Int) {
        setSaveButtonVisibility(true)
    }

    override fun onDataReordered() {
        setSaveButtonVisibility(true)
    }

    override fun onScrollHandleRelease() {
        setSaveButtonVisibility(true)
    }

    override fun onScrollHandleTouch(holder: CustomWorkoutAdapter.ViewHolder) {
        touchHelper.startDrag(holder)
    }

    override fun onFavoriteSelected(session: SessionSkeleton) { /* do nothing*/ }

    override fun onFavoriteSelected(workout: CustomWorkoutSkeleton) {
        viewModel.customWorkout = workout
        updateWorkoutTitle(viewModel.customWorkout.name)
        listAdapter.data = viewModel.customWorkout.sessions
        listAdapter.notifyDataSetChanged()
        setSaveButtonVisibility(false)
    }

    override fun onSessionAdded(session: SessionSkeleton) {
        viewModel.customWorkout.sessions.add(session)
        listAdapter.notifyDataSetChanged()
        setSaveButtonVisibility(true)
    }

    private fun setSaveButtonVisibility(visible: Boolean) {
        binding.saveButton.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onCustomWorkoutSaved(name: String) {

        viewModel.saveCurrentSelection()
        updateWorkoutTitle(name)
        setSaveButtonVisibility(false)
    }

    private fun updateWorkoutTitle(name: String) {
        binding.title.text = "$name "
    }
}