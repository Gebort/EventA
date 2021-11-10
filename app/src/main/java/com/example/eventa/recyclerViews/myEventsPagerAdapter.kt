package com.example.eventa.recyclerViews

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

import com.example.eventa.mainFragments.FollowedEvents
import com.example.eventa.mainFragments.OrginisedEvents

class myEventsPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa){
    val orginisedEvents = OrginisedEvents()
    val followedEvents = FollowedEvents()

    init{

    }

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        if(position == 1)
            return orginisedEvents
        else
            return followedEvents
    }
}