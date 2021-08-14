package com.example.letschat

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity:AppCompatActivity() {

    val auth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(auth.currentUser == null){
            val intent=Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }else{
            val intent=Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

        finish()
    }
}