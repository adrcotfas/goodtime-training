package com.adrcotfas.wod.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.adrcotfas.wod.R
import com.adrcotfas.wod.databinding.FragmentMainBinding

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
        binding.workoutMenu.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_amrap -> {
                    binding.pager.currentItem = 0
                }
                R.id.nav_for_time -> {
                    binding.pager.currentItem = 1
                }
                R.id.nav_emom -> {
                    binding.pager.currentItem = 2
                }
                R.id.nav_hiit -> {
                    binding.pager.currentItem = 3
                }
                R.id.nav_custom -> {
                    binding.pager.currentItem = 4
                }
            }
            true
        }
    }

    fun getFragment() : Fragment {
        return pagerAdapter.getItem(binding.pager.currentItem)
    }
}