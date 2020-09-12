package com.adrcotfas.wod.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pagerAdapter = MainPagerAdapter(childFragmentManager)
        binding.tabLayout.setupWithViewPager(binding.pager)
        binding.pager.adapter = pagerAdapter
        setupIcons()
    }

    private fun setupIcons() {
        binding.tabLayout.getTabAt(0)?.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_menu_camera)
        binding.tabLayout.getTabAt(1)?.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_menu_camera)
        binding.tabLayout.getTabAt(2)?.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_menu_camera)
        binding.tabLayout.getTabAt(3)?.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_menu_camera)
    }

    fun getPosition(): Int = binding.tabLayout.selectedTabPosition

    fun getFragment() : Fragment {
        return pagerAdapter.getItem(binding.pager.currentItem)
    }
}