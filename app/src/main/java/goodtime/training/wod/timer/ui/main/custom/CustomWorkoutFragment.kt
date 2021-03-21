package goodtime.training.wod.timer.ui.main.custom

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import goodtime.training.wod.timer.GoodtimeApplication
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.DimensionsUtils.Companion.dpToPx
import goodtime.training.wod.timer.common.Events
import goodtime.training.wod.timer.common.ResourcesHelper
import goodtime.training.wod.timer.common.StringUtils
import goodtime.training.wod.timer.common.calculateTextViewHeight
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import goodtime.training.wod.timer.data.model.*
import goodtime.training.wod.timer.databinding.FragmentCustomBinding
import goodtime.training.wod.timer.ui.main.CustomBalloonFactory
import goodtime.training.wod.timer.ui.main.WorkoutTypeFragment
import org.greenrobot.eventbus.EventBus
import org.kodein.di.generic.instance

//TODO: rewrite this class
class CustomWorkoutFragment :
    WorkoutTypeFragment(),
    CustomWorkoutAdapter.Listener,
    SelectCustomWorkoutDialog.Listener, AddEditSessionDialog.Listener,
    SaveCustomWorkoutDialog.Listener {

    private val viewModelFactory: CustomWorkoutViewModelFactory by instance()
    private val preferenceHelper: PreferenceHelper by instance()

    private lateinit var viewModel: CustomWorkoutViewModel
    private lateinit var binding: FragmentCustomBinding

    private val touchHelper = ItemTouchHelper(ItemTouchCallback())
    private lateinit var listAdapter: CustomWorkoutAdapter

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

        binding.saveButton.root.setOnClickListener {
            if (parentFragmentManager.findFragmentByTag("SaveCustomWorkoutDialog") == null) {
                SaveCustomWorkoutDialog.newInstance(
                    viewModel.currentWorkoutName,
                    this,
                    !viewModel.favoritesContainCurrentWorkoutName()
                )
                    .show(parentFragmentManager, "SaveCustomWorkoutDialog")
            }
        }

        if (viewModel.hasUnsavedSession) {
            setSaveButtonVisibility(true)
        }
        binding.addSessionButton.root.setOnClickListener {
            if (parentFragmentManager.findFragmentByTag("AddEditSessionDialog") == null) {
                AddEditSessionDialog.newInstance(this).show(parentFragmentManager, "AddEditSessionDialog")
            }
        }

        resizeCardViewHeightToFitNicely()

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
            val favorites = CustomBalloonFactory.create(
                requireContext(), this,
                "Create new presets and add them to the favorites.", true, 0.83f
            )

            binding.cardContainer.post {
                balloon.relayShowAlignTop(favorites, binding.root, 0, dpToPx(requireContext(), 56f))
                balloon.showAlignTop(binding.root, 0, dpToPx(requireContext(), 56f))
            }
        }
    }

    override fun onPause() {
        super.onPause()
        refreshStartButtonState(true)
    }

    private fun initCurrentWorkout() {
        viewModel.getWorkoutList().observe(viewLifecycleOwner, {
            viewModel.favorites = it
            if (it.isEmpty()) {
                viewModel.currentWorkoutName = newWorkoutName
                viewModel.currentWorkoutSessions = arrayListOf()
                toggleEmptyState(true)
                refreshStartButtonState()
            } else {
                var found = false
                val name = preferenceHelper.getCurrentCustomWorkoutFavoriteName()
                if (name != null) {
                    for (fav in it) {
                        if (fav.name == name) {
                            viewModel.currentWorkoutName = fav.name
                            viewModel.currentWorkoutSessions = fav.sessions
                            binding.title.text.text = fav.name
                            found = true
                            break
                        }
                    }
                }
                if (!found) {
                    viewModel.currentWorkoutName = it.first().name
                    viewModel.currentWorkoutSessions = it.first().sessions
                    binding.title.text.text = viewModel.currentWorkoutName
                }
                updateTotalDuration()
                toggleEmptyState(false)
                setSaveButtonVisibility(false)
            }
            setupRecycler()
        })
    }

    override fun onStartWorkout() {
        val action = CustomWorkoutFragmentDirections.toWorkout(
            viewModel.currentWorkoutName,
            TypeConverter.toString(
                sessions = arrayOf(PreferenceHelper.generatePreWorkoutSession(preferenceHelper.getPreWorkoutCountdown()))
                        + getSelectedSessions().toTypedArray()
            )
        )
        findNavController().navigate(action)
    }

    //TODO: extract duplicate code
    override fun getSelectedSessions(): ArrayList<SessionSkeleton> = viewModel.currentWorkoutSessions

    private fun toggleEmptyState(visible: Boolean) {
        if (visible) {
            binding.emptyState.isVisible = true
            binding.saveButton.root.isVisible = false
            binding.totalTime.text.isVisible = false
            binding.title.text.text = newWorkoutName
            viewModel.currentWorkoutName = newWorkoutName
            viewModel.hasUnsavedSession = false
        } else {
            binding.emptyState.isVisible = false
            binding.totalTime.text.isVisible = true
        }
    }

    private fun setupRecycler() {
        binding.title.text.text = viewModel.currentWorkoutName
        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            listAdapter = CustomWorkoutAdapter(
                viewModel.currentWorkoutSessions,
                this@CustomWorkoutFragment
            )
            adapter = listAdapter
        }
        touchHelper.attachToRecyclerView(binding.recycler)
    }

    override fun onSessionAdded(session: SessionSkeleton) {
        viewModel.currentWorkoutSessions.add(session)
        viewModel.hasUnsavedSession = true

        toggleEmptyState(false)
        setSaveButtonVisibility(true)
        updateTotalDuration()
        refreshStartButtonState()

        listAdapter.notifyItemInserted(listAdapter.data.size - 1)
        binding.recycler.scrollToPosition(listAdapter.data.size - 1)
    }

    override fun onSessionEdit(idx: Int, session: SessionSkeleton) {
        if (viewModel.currentWorkoutSessions[idx] != session) {
            viewModel.currentWorkoutSessions[idx] = session
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

    override fun onDuplicateButtonClicked(position: Int) {
        updateTotalDuration()
        viewModel.hasUnsavedSession = true
        setSaveButtonVisibility(true)
        binding.recycler.scrollToPosition(position)
    }

    override fun onChipClicked(position: Int) {
        AddEditSessionDialog.newInstance(this, position, listAdapter.data[position])
            .show(parentFragmentManager, "")
    }

    override fun onScrollHandleTouch(holder: CustomWorkoutAdapter.ViewHolder) {
        touchHelper.startDrag(holder)
    }

    override fun onFavoriteSelected(session: SessionSkeleton) { /* do nothing*/
    }

    override fun onFavoriteSelected(workout: CustomWorkoutSkeleton) {
        viewModel.currentWorkoutName = workout.name
        viewModel.currentWorkoutSessions = workout.sessions
        binding.title.text.text = viewModel.currentWorkoutName
        listAdapter.data = viewModel.currentWorkoutSessions
        listAdapter.notifyDataSetChanged()
        setSaveButtonVisibility(false)
        updateTotalDuration()
        toggleEmptyState(false)
        refreshStartButtonState()

        preferenceHelper.setCurrentCustomWorkoutFavoriteName(workout.name)
    }

    override fun onFavoriteDeleted(name: String) {
        if (viewModel.currentWorkoutName == name && viewModel.currentWorkoutSessions.isNotEmpty()) {
            setSaveButtonVisibility(true)
            preferenceHelper.setCurrentCustomWorkoutFavoriteName("")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setSaveButtonVisibility(visible: Boolean) {
        binding.saveButton.root.isVisible = visible
        binding.title.text.setTextColor(if (visible) ResourcesHelper.grey800 else ResourcesHelper.grey500)
    }

    override fun onCustomWorkoutSaved(name: String) {
        viewModel.currentWorkoutName = name
        viewModel.saveCurrentSelection()
        binding.title.text.text = name
        setSaveButtonVisibility(false)
        viewModel.hasUnsavedSession = false
        preferenceHelper.setCurrentCustomWorkoutFavoriteName(name)
    }

    private fun updateTotalDuration() {
        val total = Session.calculateTotal(viewModel.currentWorkoutSessions)
        binding.totalTime.text.isVisible = total != 0
        binding.totalTime.text.text = StringUtils.secondsToNiceFormat(total)
    }

    fun onNewCustomWorkoutButtonClick() {
        viewModel.currentWorkoutSessions.clear()
        listAdapter.notifyDataSetChanged()
        setSaveButtonVisibility(false)
        toggleEmptyState(true)
        updateTotalDuration()
        refreshStartButtonState()
    }

    private fun refreshStartButtonState(state: Boolean = viewModel.currentWorkoutSessions.isNotEmpty()) {
        EventBus.getDefault().post(Events.Companion.SetStartButtonStateWithColor(state))
    }

    /**
     * Resize the main card view height so that we don't see half rows
     */
    private fun resizeCardViewHeightToFitNicely() {
        val titleHeight = calculateTextViewHeight(layoutInflater, R.layout.section_custom_workout_title)
        val subtitleHeight = calculateTextViewHeight(layoutInflater, R.layout.section_custom_workout_subtitle)
        val bottomButtonHeight = requireContext().resources.displayMetrics.density * 42
        val cardVerticalMargin = requireContext().resources.displayMetrics.density * 26

        val cardHeight =
            GoodtimeApplication.windowHeightPortrait - dpToPx(
                requireContext(),
                56f /*toolbar*/ + 12 /*padding top*/ + 56f /*bottom toolbar*/ + 70 /*fab*/ + 12 /*padding between fab and bottom toolbar*/
            )
        val cardHeightExceptRecycler = titleHeight + subtitleHeight + bottomButtonHeight + cardVerticalMargin

        val defaultRecyclerHeight = cardHeight - cardHeightExceptRecycler

        val rowHeight = requireContext().resources.displayMetrics.density * 42
        val newRecyclerHeight = defaultRecyclerHeight - defaultRecyclerHeight % rowHeight

        binding.cardContainer.apply {
            layoutParams.height =
                (newRecyclerHeight + cardHeightExceptRecycler).toInt()
            layoutParams.width = MATCH_PARENT
        }
    }

    companion object {
        const val newWorkoutName = "New workout"
    }
}