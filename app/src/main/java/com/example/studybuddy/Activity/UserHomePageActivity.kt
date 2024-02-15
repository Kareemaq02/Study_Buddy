package com.example.studybuddy.Activity

import UserSemestersManagerFragment
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.studybuddy.R
import com.example.studybuddy.UserFragments.UserEnrolledLessonsFragment
import com.example.studybuddy.UserFragments.UserRequestFragment
import com.example.studybuddy.UserFragments.UserSettingsFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class UserHomePageActivity : AppCompatActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_userhomepage)

        val viewPager: ViewPager2 = findViewById(R.id.viewPagerUser)
        val fragmentAdapter = MyPagerAdapter(this)
        fragmentAdapter.addFragment(UserSemestersManagerFragment())
        fragmentAdapter.addFragment(UserRequestFragment())
        fragmentAdapter.addFragment(UserEnrolledLessonsFragment())
        fragmentAdapter.addFragment(UserSettingsFragment())
        viewPager.adapter = fragmentAdapter
        val tabLayout: TabLayout = findViewById(R.id.tabLayoutUser)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // Set the text or icon for each tab
            when (position) {
                0 -> tab.setIcon(R.drawable.graduate_cap)
                1 -> tab.setIcon(R.drawable.reading_book)
                2 -> tab.setIcon(R.drawable.calendar__3_)
                3 -> tab.setIcon(R.drawable.settings__2_)
            }
        }.attach()

        val postRequest: FloatingActionButton = findViewById(R.id.addRequest)
        postRequest.setOnClickListener{
            val intent = Intent(this, UserMakeRequestActivity::class.java)
            startActivity(intent)

        }

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