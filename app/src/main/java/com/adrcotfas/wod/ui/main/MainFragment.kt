package com.adrcotfas.wod.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.adrcotfas.wod.R
import com.adrcotfas.wod.databinding.FragmentMainBinding
import com.adrcotfas.wod.ui.common.WorkoutTypeFragment
import com.adrcotfas.wod.ui.workout.FADE_ANIMATION_DURATION

//TODO: persist last page and don't allow reselection
class MainFragment: Fragment() {
    private lateinit var pagerAdapter: MainPagerAdapter
    private lateinit var binding : FragmentMainBinding

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pagerAdapter = MainPagerAdapter(childFragmentManager)
        binding.pager.adapter = pagerAdapter
        binding.pager.setOnTouchListener { _, _ -> true } // disable swipe

        //TODO: remove this when Custom workouts are implemented
        binding.workoutMenu.menu[4].isEnabled = false

        binding.workoutMenu.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_amrap -> {
                    hideContent()
                    binding.pager.setCurrentItem(0,  false)
                    showContent()
                }
                R.id.nav_for_time -> {
                    hideContent()
                    binding.pager.setCurrentItem(1,  false)
                    showContent()
                }
                R.id.nav_emom -> {
                    hideContent()
                    binding.pager.setCurrentItem(2,  false)
                    showContent()
                }
                R.id.nav_hiit -> {
                    hideContent()
                    binding.pager.setCurrentItem(3,  false)
                    showContent()
                }
                R.id.nav_custom -> {
                }
            }
            true
        }
    }

    private fun hideContent() {
        binding.pager.apply {
            alpha = 1f
            visibility = View.VISIBLE
            animate()
                .alpha(0f)
                .setDuration(FADE_ANIMATION_DURATION)
                .setListener(null)
        }
    }

    private fun showContent() {
        binding.pager.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .setDuration(FADE_ANIMATION_DURATION)
                .setListener(null)
        }
    }

    fun getFragment() : WorkoutTypeFragment {
        return pagerAdapter.getItem(binding.pager.currentItem) as WorkoutTypeFragment
    }
}