package com.example.letschat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.ios.IosEmojiProvider
import kotlinx.android.synthetic.main.activity_options.*
import kotlinx.android.synthetic.main.dialogue_bottom_sheet.*

class DialogBottomSheet(val change:Boolean,val id:String,private val interaction: changeField? = null):BottomSheetDialogFragment() {

    var newName:String?=null
    var newStatus:String?=null

    val auth by lazy {
        FirebaseAuth.getInstance()
    }

    val database by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialogue_bottom_sheet,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (change) {
            tvShowFieldNameBottomSheet.text = getString(R.string.enterStatus)
            etBottomSheet.hint = "Enter your status"

            btnBottomSheetSave.setOnClickListener {
                newStatus = etBottomSheet.text.toString()

                database.collection("users").document(id).update(
                    mapOf("status" to newStatus)
                ).addOnSuccessListener {
                    interaction?.setTextToField(newStatus!!, "STATUS")
                }


            }
        } else {
            btnBottomSheetSave.setOnClickListener {
                newName = etBottomSheet.text.toString()

                database.collection("users").document(id).update(
                    mapOf("name" to newName)
                ).addOnSuccessListener {
                    interaction?.setTextToField(newName!!, "NAME")
                }
            }

        }

        btnBottomSheetCancel.setOnClickListener {
            dismiss()
        }
    }
    interface changeField{
        fun setTextToField(text:String,field:String)
    }

    override fun dismiss() {
        super.dismiss()
    }
}