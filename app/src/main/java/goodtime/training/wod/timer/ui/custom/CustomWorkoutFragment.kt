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

class CustomWorkoutFragment :
    WorkoutTypeFragment(),
    CustomWorkoutAdapter.Listener,
    SelectCustomWorkoutDialog.Listener, AddSessionDialog.Listener {

    private lateinit var viewModel: CustomWorkoutViewModel
    private lateinit var binding: FragmentCustomBinding

    private val touchHelper = ItemTouchHelper(ItemTouchCallback())
    private lateinit var listAdapter : CustomWorkoutAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CustomWorkoutViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomBinding.inflate(inflater, container, false)

        binding.title.text = viewModel.customWorkout.name

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            listAdapter = CustomWorkoutAdapter(viewModel.customWorkout.sessions, context, this@CustomWorkoutFragment)
            adapter = listAdapter
        }
        touchHelper.attachToRecyclerView(binding.recycler)

        binding.addSessionButton.addSessionButton.setOnClickListener{
            //TODO: show add session dialog
            // positive case: data changed -> show save button
            AddSessionDialog.newInstance(this).show(parentFragmentManager, "")
        }

        return binding.root
    }

    override fun onStartWorkout() {
        val action = CustomWorkoutFragmentDirections.toWorkout(
            TypeConverter.toString(sessions = arrayOf(PrefUtil.generatePreWorkoutSession()) + getSelectedSessions().toTypedArray() ))
        findNavController().navigate(action)
    }

    override fun getSelectedSessions(): ArrayList<SessionSkeleton> = viewModel.customWorkout.sessions

    override fun onDeleteButtonClicked(position: Int) {
        //TODO: data changed -> show save button
    }

    override fun onDataReordered() {
        //TODO: data changed -> show save button
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
    }

    override fun onSessionAdded(session: SessionSkeleton) {
        viewModel.customWorkout.sessions.add(session)
        listAdapter.notifyDataSetChanged()
    }
}