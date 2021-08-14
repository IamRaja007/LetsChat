package com.example.letschat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_options.*

class MainActivity : AppCompatActivity() {

    val TAG="MainActivity"
    var name=""
    var status=""
    var id=""
    var imageURL=""

    val storage by lazy {
        FirebaseStorage.getInstance()
    }

    val auth by lazy {
        FirebaseAuth.getInstance()
    }

    val database by lazy {
        FirebaseFirestore.getInstance()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewpager.adapter=ViewPagerAdapter(this)
        TabLayoutMediator(tablayout,viewpager
        ) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Messages"
                }
                1 -> {
                    tab.text = "Connections"
                }
            }
        }.attach()

        tablayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewpager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })

        ivOptions.setOnClickListener {
            showOptionsActivity()
        }

        database.collection("users").document("${auth.uid}").get()
            .addOnSuccessListener { document->
                if (document != null) {
                    val user=document.toObject(UserModel::class.java)
                    name=user?.name!!
                    status=user.status
                    id=user.uid
                    imageURL=user.profileImageURL
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Get failed with ", exception)
            }
    }

    fun showOptionsActivity(){
        val intent= Intent(this,OptionsActivity::class.java)
        intent.putExtra("NameAccount",name)
        intent.putExtra("StatusAccount",status)
        println("HEYHERE : $id")
        intent.putExtra("IdAccount",id)
        intent.putExtra("ImageAccount",imageURL)
        startActivity(intent)
    }
}