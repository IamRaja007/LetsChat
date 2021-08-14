package com.example.letschat

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragactivity:FragmentActivity):FragmentStateAdapter(fragactivity) {
    override fun getItemCount(): Int =2   //2tabs are there

    override fun createFragment(position: Int): Fragment = when(position){
            0 -> {
                ChatsFragment()

            }
            else -> {
                ConnectionsFragment()
            }
        }


}