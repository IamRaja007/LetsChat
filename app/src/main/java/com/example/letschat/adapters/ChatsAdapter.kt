package com.example.letschat.adapters

import android.content.Context
import android.opengl.Visibility
import android.util.Log
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.example.letschat.*
import com.example.letschat.utils.formattingAsTime
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.item_date_header.view.*
import kotlinx.android.synthetic.main.item_inbox_message_received.view.*

class ChatsAdapter(val list: MutableList<Formatter>, val mCurrentID:String,val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){ //we will not create a viewholder here. we will use the previous one that we had created. always we should try to make generic viewholders

    var heartIt: ((id: String, status: Boolean,position:Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflate={ layoutID:Int ->    // we have written a higher order function. equivalent normal function will be ---- fun(layoutID:Int){return LayoutInflater.from(parent.context).inflate(layoutID,parent,false)}
            LayoutInflater.from(parent.context).inflate(layoutID,parent,false)
        }

        return when(viewType){
            MESSAGE_SENT->{
                messageViewHolder(inflate(R.layout.item_inbox_message_sent))
            }
            MESSAGE_RECEIVED ->{
                messageViewHolder(inflate(R.layout.item_inbox_message_received))

            }
            DATE_HEADER->{
                dateViewHolder(inflate(R.layout.item_date_header))
            }
            else -> {
                messageViewHolder(inflate(R.layout.item_inbox_message_sent))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(val item=list[position]){
            is FormatHeader ->{
                holder.itemView.tvdateheader.text=item.date
            }
            is MessageStructure->{
                holder.itemView.apply {
                    tvmessagecontent.text=item.message
                    tvmessagetime.text=item.sentAt.formattingAsTime()
                    Log.d("CHecker",item.liked.toString())
                    if(item.liked == true){
                              ivheart.visibility=View.VISIBLE //this is for showing heart on sender's phone that somebody liked the message

                    }


                }

                when(getItemViewType(position)){
                    MESSAGE_RECEIVED ->{

                        holder.itemView.tvmessagecontent.setOnClickListener(object : DoubleClickListener(){
                            override fun onDoubleClick(v: View) {
                                if(item.liked == false) {
                                    holder.itemView.ivheart.visibility = View.VISIBLE
                                }
                                else{
                                    holder.itemView.ivheart.visibility = View.INVISIBLE
                                }
                                heartIt?.invoke(item.messageID,!item.liked,position)  //we are using not because we will implement for both liked and not liked

                            }
                        })

                        }

                    }
                }
            }
        }


    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        val event=list[position]
        return when(event){
            is MessageStructure ->{
                if((event.senderID == mCurrentID)){
                    MESSAGE_SENT
                }
                else{
                    MESSAGE_RECEIVED

                }
            }     //is keyword checks the type of class
            is FormatHeader ->{
                DATE_HEADER
            }
            else -> UNSUPPORTED       //to handle exceptions
        }
    }

    class dateViewHolder(view:View):RecyclerView.ViewHolder(view)
    class messageViewHolder(view:View):RecyclerView.ViewHolder(view)

    companion object {
        private const val UNSUPPORTED = -1
        private const val MESSAGE_SENT = 1
        private const val MESSAGE_RECEIVED = 0
        private const val DATE_HEADER = 2
    }

}

abstract class DoubleClickListener : View.OnClickListener { // we are implementing view.onclicklistener because if we call onClickListener on any view,button anything, then in arguement we have to pass something which extends ciew.onclicklistener
    private var lastClickTime: Long = 0
    override fun onClick(v: View) {
        val clickTime = System.currentTimeMillis()
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            onDoubleClick(v)
            lastClickTime = 0
        }
        lastClickTime = clickTime
    }
    abstract fun onDoubleClick(v: View)
    companion object {
        private const val DOUBLE_CLICK_TIME_DELTA: Long = 300 //milliseconds
    }
}


