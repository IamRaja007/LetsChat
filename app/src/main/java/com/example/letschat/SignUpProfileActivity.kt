package com.example.letschat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_signup.*
import java.net.URI

private const val PICK_IMAGE_REQUEST_CODE=111

class SignUpProfileActivity:AppCompatActivity() {

    var uploadedImageURL:String?=null

    val storage by lazy {
        FirebaseStorage.getInstance()
    }

    val auth by lazy {
        FirebaseAuth.getInstance()
    }

    val database by lazy {
        FirebaseFirestore.getInstance()
    }

    lateinit var downloadUrl:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        civUser.setOnClickListener {
            checkPermissions()
        }

        btnproceed.setOnClickListener{
            btnproceed.isEnabled=false
            val enteredName=etUserName.text.toString()
            val enteredStatus=etStatus.text.toString()
            if(enteredName.isEmpty()){
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }else if(!::downloadUrl.isInitialized){
                Toast.makeText(this, "Please set an profile image", Toast.LENGTH_SHORT).show()
            }
            else{

                val user=UserModel(enteredName,auth.uid!!,downloadUrl,downloadUrl)    //thumbnail image is set to original image, change it later

                if(enteredStatus.isNotEmpty()){ user.status = enteredStatus }
                database.collection("users").document(auth.uid!!).set(user)   //inside document we need unique identifier so we gave auth.uid as it will be unique for different users
                    .addOnSuccessListener {
                    val intent=Intent(this,MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener {
                        btnproceed.isEnabled=true
                        Toast.makeText(this, "Couldn't Upload. Try again", Toast.LENGTH_SHORT).show()
                    }
            }
        }


    }

    private fun checkPermissions() {
        if((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)){

            val permissionWrite= arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val permissionRead= arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

            requestPermissions(permissionRead,100)//Providing an request code
            requestPermissions(permissionWrite,101)//Providing an request code
        }
        else{
            pickImageFromGallery()
        }
    }

    private fun pickImageFromGallery() {
        val intent=Intent(Intent.ACTION_PICK)
        intent.type= "image/*"

        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE){
            data?.data.let { imageURI ->
                civUser.setImageURI(imageURI)
                uploadImage(imageURI)
                println("Printing imageURI : $imageURI")
            }
        }

    }

    private fun uploadImage(imageURI:Uri?) {
        btnproceed.isEnabled=false
        val storageReference=storage.reference.child("uploads/"+auth.uid.toString())  //auth.id makes a unique path for every user
        val uploadTask=storageReference.putFile(imageURI!!)
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task->
            if(!task.isSuccessful){
                task.exception?.let { exception->
                    throw exception
                }
            }
            return@Continuation storageReference.downloadUrl

        }).addOnCompleteListener { task->
            btnproceed.isEnabled=true
            if(task.isSuccessful){
                downloadUrl=task.result.toString()
                uploadedImageURL=downloadUrl
                Log.i("URLi","downloadURL: $downloadUrl")
            }
            else{
                Toast.makeText(this, "Image not uploaded. Try again", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception->
            Toast.makeText(this, exception.toString(), Toast.LENGTH_SHORT).show()
        }


    }
}