package com.example.studybuddy.UserFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.studybuddy.R
import com.example.studybuddy.fragments.registredLessonsTrackerFragment
import com.example.studybuddy.fragments.scheduledClassesTrackerFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class UserEnrolledLessonsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.tack_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewPager: ViewPager2 = view.findViewById(R.id.viewPagerRequest22)
        val tabLayout: TabLayout = view.findViewById(R.id.tabLayoutRequest22)

        val fragmentAdapter = MyPagerAdapter(requireActivity())
        fragmentAdapter.addFragment(scheduledClassesTrackerFragment())
        fragmentAdapter.addFragment(registredLessonsTrackerFragment())
        viewPager.adapter = fragmentAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Scheduled classes"
                1 -> tab.text = "Registered lessons"
            }
        }.attach()
    }

    private class MyPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
        private val fragments = mutableListOf<Fragment>()

        fun addFragment(fragment: Fragment) {
            fragments.add(fragment)
        }

        override fun getItemCount(): Int {
            return fragments.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }
    }
}
