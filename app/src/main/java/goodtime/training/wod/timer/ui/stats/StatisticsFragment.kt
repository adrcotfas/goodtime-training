package goodtime.training.wod.timer.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.Events
import goodtime.training.wod.timer.databinding.FragmentStatisticsBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class StatisticsFragment : Fragment(), KodeinAware, FilterDialog.Listener {
    override val kodein by closestKodein()

    private lateinit var binding: FragmentStatisticsBinding

    private val viewModelFactory : LogViewModelFactory by instance()
    private lateinit var viewModel: StatisticsViewModel

    private lateinit var pagerAdapter: StatisticsViewPagerAdapter

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroy() {
        EventBus.getDefault().post(Events.Companion.FilterClearButtonClickEvent())
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory).get(StatisticsViewModel::class.java)
        pagerAdapter = StatisticsViewPagerAdapter(childFragmentManager)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatisticsBinding.inflate(layoutInflater, container, false)
        setupViewPager()
        return binding.root
    }

    private fun setupViewPager() {
        binding.pager.adapter = pagerAdapter
        binding.tabLayout.setupWithViewPager(binding.pager)
        binding.tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_eye)
        binding.tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_list)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: Events.Companion.FilterButtonClickEvent) {
        if (parentFragmentManager.findFragmentByTag("SelectFilter") == null) {
            FilterDialog.newInstance(this).show(parentFragmentManager, "SelectFilter")
        }
    }

    override fun onFavoriteSelected(name: String) {
        EventBus.getDefault().post(Events.Companion.FilterSelectedEvent(name))
        viewModel.filteredWorkoutName.value = name
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: Events.Companion.FilterClearButtonClickEvent) {
        viewModel.filteredWorkoutName.value = null
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: Events.Companion.AddToStatisticsClickEvent) {
        if (parentFragmentManager.findFragmentByTag("AddEditCompletedWorkout") == null) {
            AddEditCompletedWorkoutDialog.newInstance().show(parentFragmentManager, "AddEditCompletedWorkout")
        }
    }
}
