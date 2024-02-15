package com.example.studybuddy.Activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.studybuddy.R
import com.example.studybuddy.fragments.CourseFragment
import com.example.studybuddy.fragments.HomeFragment
import com.example.studybuddy.fragments.SettingFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class AdminHomepageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val fragmentAdapter = MyPagerAdapter(this)
        fragmentAdapter.addFragment(CourseFragment())
        fragmentAdapter.addFragment(HomeFragment())
        fragmentAdapter.addFragment(SettingFragment())
        viewPager.adapter = fragmentAdapter
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // Set the text or icon for each tab
            when (position) {
                0 -> tab.setIcon(R.drawable.graduate_cap)
                1 -> tab.setIcon(R.drawable.reading_book)
                2 -> tab.setIcon(R.drawable.settings__2_)
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
