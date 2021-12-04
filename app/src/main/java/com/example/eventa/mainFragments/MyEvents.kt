package com.example.eventa.mainFragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.invalidateOptionsMenu
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

        activity?.title = activity?.resources?.getString(R.string.my_events)

        viewPager = i.findViewById(R.id.viewPager)
        tabLayout = i.findViewById(R.id.tabLayout)

        val pagerAdapder = activity?.let { myEventsPagerAdapter(it)}
        viewPager.adapter = pagerAdapder
        viewPager.offscreenPageLimit = 1

        TabLayoutMediator(tabLayout, viewPager){tab, position ->
            when(position){
                0 -> tab.text = getText(R.string.followed)
                1 -> tab.text = getText(R.string.organised)
            }
        }.attach()

        return i
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_search).isVisible = false
        super.onPrepareOptionsMenu(menu)
    }

}
