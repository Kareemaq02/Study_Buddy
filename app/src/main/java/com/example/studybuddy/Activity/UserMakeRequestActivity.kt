package com.example.studybuddy.Activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.studybuddy.R
import com.example.studybuddy.fragments.RequestLessonFragment
import com.example.studybuddy.fragments.ScheduleClassFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class UserMakeRequestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_request)
        val customToolbar = findViewById<TextView>(R.id.action_bar_title)
        customToolbar.text = " "
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()
        }
        val viewPager: ViewPager2 = findViewById(R.id.viewPagerRequest)
        val tabLayout: TabLayout = findViewById(R.id.tabLayoutRequest)

        val fragmentAdapter = MyPagerAdapter(this)
        fragmentAdapter.addFragment(ScheduleClassFragment())
        fragmentAdapter.addFragment(RequestLessonFragment())
        viewPager.adapter = fragmentAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Schedule a Class"
                1 -> tab.text = "Request a Lesson"
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
