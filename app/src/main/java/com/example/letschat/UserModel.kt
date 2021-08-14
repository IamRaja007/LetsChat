package com.example.letschat

import com.google.firebase.firestore.FieldValue

data class UserModel(
    val name:String,
    val profileImageURL:String,
    val thumbnailImage:String,
    val deviceToken:String,     //will be used to send notification
    var status:String,         //like hey i am using LetsChat
    val statusONorOFF: String,
    val uid:String            //Usr id that we get from authentication
){
    //Empty Constructor for Firebase (REQUIRED)
    constructor() : this("","","","","","","")

    constructor(name:String,uid: String,thumbnailImage: String,profileImageURL: String):this(
        name,
        profileImageURL,
        thumbnailImage,
        "",
        "Connect with me on LetsChat",      //default Status
        "",
        uid
    )
}