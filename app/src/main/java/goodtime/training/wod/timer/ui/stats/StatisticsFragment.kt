package goodtime.training.wod.timer.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.databinding.FragmentStatisticsBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein

class StatisticsFragment : Fragment(), KodeinAware {
    override val kodein by closestKodein()

    private lateinit var binding: FragmentStatisticsBinding
    private lateinit var pagerAdapter: StatisticsViewPagerAdapter

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

    fun onFilterButtonClicked() {
        // open bottom sheet
    }
}
