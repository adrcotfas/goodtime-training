package goodtime.training.wod.timer.ui.stats

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class StatisticsViewPagerAdapter(fa: FragmentManager) : FragmentPagerAdapter(fa, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private var fragments = arrayListOf<Fragment>(StatisticsOverviewFragment(), StatisticsListFragment())

    override fun getCount() = fragments.size
    override fun getItem(position: Int) = fragments[position]
}
