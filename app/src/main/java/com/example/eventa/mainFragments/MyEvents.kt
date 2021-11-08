package com.example.eventa.mainFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.eventa.R
import com.example.eventa.User
import com.example.eventa.recyclerViews.myEventsPagerAdapter
import com.example.eventa.viewModels.followedEventsViewModel
import com.example.eventa.viewModels.orgEventsViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MyEvents : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val i = inflater.inflate(R.layout.fragment_my_events, container, false)

        activity?.title = "My events"

        viewPager = i.findViewById(R.id.viewPager)
        tabLayout = i.findViewById(R.id.tabLayout)

        val pagerAdapger = activity?.let { myEventsPagerAdapter(it)}
        viewPager.adapter = pagerAdapger

        TabLayoutMediator(tabLayout, viewPager){tab, position ->
            when(position){
                0 -> tab.text = getText(R.string.followed)
                1 -> tab.text = getText(R.string.organised)
            }
        }.attach()

        return i
    }

}
