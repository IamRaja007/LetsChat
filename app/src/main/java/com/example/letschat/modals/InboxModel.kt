package com.example.letschat.modals

import java.util.*

data class InboxModel(
    val message:String,
    var from:String,
    val time:Date= Date(),
    var image:String,
    var name:String,
    var count:Int
    ) {

    constructor():this("","",Date(),"","",0)   //empty constructor needed for firbase
}