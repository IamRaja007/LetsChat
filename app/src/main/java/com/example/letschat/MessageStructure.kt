package com.example.letschat

import android.content.Context
import com.example.letschat.utils.formatAsHeader
import java.util.*

interface Formatter{
    val sentAt: Date
}

data class MessageStructure(
    val messageID:String,
    val message:String,
    val senderID:String,
    val type:String="TEXT",
    var liked:Boolean=false,
    val messageStatus:Int=1,   //read or delivered statuses
    override val sentAt: Date = Date()
):Formatter {
    constructor():this("","","","",false,1, Date(0L))
}

data class FormatHeader(
    val context: Context, override val sentAt: Date = Date()
):Formatter {
   val date : String =sentAt.formatAsHeader(context)
}