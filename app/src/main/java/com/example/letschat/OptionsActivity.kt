package com.example.letschat

import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiPopup
import kotlinx.android.synthetic.main.activity_message_inbox.*
import kotlinx.android.synthetic.main.activity_message_inbox.ivsmileyEmoji
import kotlinx.android.synthetic.main.activity_options.*
import kotlinx.android.synthetic.main.activity_options.view.*
import kotlinx.android.synthetic.main.dialogue_bottom_sheet.*
import kotlinx.android.synthetic.main.item_row.view.*
import java.net.URI

class OptionsActivity:AppCompatActivity(),DialogBottomSheet.changeField{
    val TAG="OptionsActivity"

    val auth by lazy {
        FirebaseAuth.getInstance()
    }

    val database by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        val name=intent.getStringExtra("NameAccount")
        val status=intent.getStringExtra("StatusAccount")
        val id=intent.getStringExtra("IdAccount")
        val imageURL=intent.getStringExtra("ImageAccount")

        tvNameOptionsHolder.text = name
        tvStatusOptionsHolder.text= status
        Picasso.get()
            .load(imageURL)
            .placeholder(R.drawable.default_avatar)
            .error(R.drawable.default_avatar)
            .into(civUserOptions)


        btnchangeName.setOnClickListener {
            DialogBottomSheet(false,id =id!!,this ).show(
                supportFragmentManager,""
            )
        }

        btnchangeStatus.setOnClickListener {
            val dialogBottomSheet=DialogBottomSheet(true,id!!,this)
            dialogBottomSheet.show(
                supportFragmentManager,""
            )
        }


    }

    override fun setTextToField(text: String,field:String) {
        if(field == "STATUS"){
            tvStatusOptionsHolder.text= text
        }
        else{
            tvNameOptionsHolder.text=text
        }

    }

}