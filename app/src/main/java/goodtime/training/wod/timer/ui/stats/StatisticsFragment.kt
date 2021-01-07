package goodtime.training.wod.timer.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.Events
import goodtime.training.wod.timer.databinding.FragmentStatisticsBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein


class StatisticsFragment : Fragment(), KodeinAware, FilterDialog.Listener {
    override val kodein by closestKodein()

    private lateinit var binding: FragmentStatisticsBinding
    private lateinit var pagerAdapter: StatisticsViewPagerAdapter

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pagerAdapter = StatisticsViewPagerAdapter(childFragmentManager)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatisticsBinding.inflate(layoutInflater, container, false)
        binding.pager.adapter = pagerAdapter
        binding.tabLayout.setupWithViewPager(binding.pager)
        binding.tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_eye)
        binding.tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_list)
        return binding.root
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: Events.Companion.FilterButtonClickEvent) {
        if (parentFragmentManager.findFragmentByTag("SelectFilter") == null) {
            FilterDialog.newInstance(this).show(parentFragmentManager, "SelectFilter")
        }
    }

    override fun onFavoriteSelected(name: String) {
        EventBus.getDefault().post(Events.Companion.FilterSelectedEvent(name))
        //TODO update the charts
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: Events.Companion.FilterClearButtonClickEvent) {
        //TODO update the charts with the default values
    }
}
