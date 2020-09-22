package com.adrcotfas.wod.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.adrcotfas.wod.ui.amrap.AmrapFragment
import com.adrcotfas.wod.ui.emom.EmomFragment
import com.adrcotfas.wod.ui.for_time.ForTimeFragment
import com.adrcotfas.wod.ui.tabata.TabataFragment
import java.lang.IllegalArgumentException


class MainPagerAdapter(fm: FragmentManager)
    : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private var fragments = arrayListOf<Fragment>(AmrapFragment(), ForTimeFragment(), EmomFragment(), TabataFragment(), TabataFragment())

    override fun getCount(): Int = 5

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "AMRAP"
            1 -> "FOR TIME"
            2 -> "EMOM"
            3 -> "HIIT"
            4 -> "CUSTOM"
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}