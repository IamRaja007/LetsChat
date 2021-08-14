package com.example.letschat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letschat.adapters.ChatsAdapter
import com.example.letschat.modals.InboxModel
import com.example.letschat.utils.isSameDayAs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.ios.IosEmojiProvider
import kotlinx.android.synthetic.main.activity_message_inbox.*
import kotlinx.android.synthetic.main.item_row.*


const val USER_NAME = "name"
const val PHOTO_URI = "photoURI"
const val USER_ID = "id"

class MessageInboxActivity : AppCompatActivity() {
    var likedStatusChanged: ((messageID:String) -> Unit)? = null

    companion object {

        fun createMessageInboxActivity(
            context: Context,
            id: String,
            name: String,
            image: String
        ): Intent {
            val intent = Intent(context, MessageInboxActivity::class.java)
            intent.putExtra(USER_ID, id)
            intent.putExtra(USER_NAME, name)
            intent.putExtra(PHOTO_URI, image)

            return intent
        }
    }

    private val friendID by lazy {
        intent.getStringExtra(USER_ID)
    }
    private val friendUserName by lazy {
        intent.getStringExtra(USER_NAME)
    }

    private val friendUserPhoto by lazy {
        intent.getStringExtra(PHOTO_URI)
    }
    private val myID by lazy {
        FirebaseAuth.getInstance().uid
    }

    private val database by lazy {
        FirebaseDatabase.getInstance()
    }

    lateinit var CurrentUser: UserModel
    lateinit var chatsAdapter: ChatsAdapter
    private val messages = mutableListOf<Formatter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(IosEmojiProvider())   // we need to call this before setting the content view to avoid crashes
        setContentView(R.layout.activity_message_inbox)

        markAsRead()

        FirebaseFirestore.getInstance().collection("users").document(myID!!).get()
            .addOnSuccessListener { documentSnapshot ->
                CurrentUser = documentSnapshot.toObject(UserModel::class.java)!!
            }

        chatsAdapter = ChatsAdapter(messages, myID!!, this)

        rvshowmessages.apply {
            layoutManager = LinearLayoutManager(this@MessageInboxActivity)
            adapter = chatsAdapter
        }

        tvFriendName.text = friendUserName
        val emojiPopup = EmojiPopup.Builder.fromRootView(rootviewInbox).build(eetmessage)
        ivsmileyEmoji.setOnClickListener {
            emojiPopup.toggle()
        }
        listenToMessages()
        ivsendbtn.setOnClickListener {
            eetmessage.text?.let {
                if (it.isNotEmpty()) {
                    sendMessage(it.toString())
                    it.clear()
                }
            }
        }

        chatsAdapter.heartIt = { messageId: String, likedStatus: Boolean,position:Int ->
            updateLikedStatus(messageId, likedStatus,position)
        }
    }

    private fun updateLikedStatus(messageId: String, likedStatus: Boolean,position:Int) {

        getMessagesFromMessagingID(friendID!!).child(messageId)
            .updateChildren(mapOf("liked" to likedStatus))

        val see=messages[position]
        if(see is MessageStructure){

            see.liked=likedStatus

        }

    }

    private fun sendMessage(message: String) {
        val IDkey = getMessagesFromMessagingID(friendID!!).push().key //unique key will be generated
        checkNotNull(IDkey) {    //will throw an exception if IDkey is null
            "CANNOT BE NULL"
        }
        val messageMap = MessageStructure(IDkey, message, myID!!) //rest all values will be default
        getMessagesFromMessagingID(friendID!!).child(IDkey).setValue(messageMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Message Sent", Toast.LENGTH_SHORT).show()
            }

            updateLastMessage(messageMap)

    }

    private fun listenToMessages() {
        getMessagesFromMessagingID(friendID!!).orderByKey().addChildEventListener(
            object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val msg = snapshot.getValue(MessageStructure::class.java)
                    Log.d(
                        "Tester",
                        msg.toString()
                    )
                    addMessage(msg)
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    //messsage if liked will be added here
                    val see=snapshot.getValue(MessageStructure::class.java)
                    Log.d("LikeCheckp",see.toString())

                    if(myID == see?.senderID){
                        listentoChanges(see?.messageID!!,see)
                    }

                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    TODO("Not yet implemented")
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            }
        )


    }

    private fun addMessage(msg: MessageStructure?) {
        val eventBefore = messages.lastOrNull()
        if ((eventBefore != null && !eventBefore.sentAt.isSameDayAs(msg?.sentAt!!)) || eventBefore == null) { //we only add header if today date and previous day date dont match
            messages.add(
                FormatHeader(this, msg?.sentAt!!)
            )
        }

        messages.add(msg!!)
        chatsAdapter.notifyItemInserted(messages.size - 1)
        rvshowmessages.scrollToPosition(messages.size - 1) //automaticaly scrolls to the new message
    }

    private fun updateLastMessage(messageMap: MessageStructure) {
        val inboxMap = InboxModel(
            messageMap.message,
            friendID!!,
            image = friendUserPhoto!!,
            name = friendUserName!!,
            count = 0
        )

        getInboxChatsID(myID!!, friendID!!).setValue(inboxMap).addOnSuccessListener {
            getInboxChatsID(friendID!!, myID!!).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.getValue(InboxModel::class.java)

                    inboxMap.apply {
                        from = messageMap.senderID
                        name = CurrentUser.name
                        image = CurrentUser.thumbnailImage
                        count=1
                    }

                        value?.let { inboxModel ->         //read-unread system
                            if (inboxModel.from == messageMap.senderID) {
                                inboxMap.count = value.count + 1
                            }
                        }
                    getInboxChatsID(friendID!!, myID!!).setValue(inboxMap)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Error occurred", error.message)
                }

            })
        }

    }

    private fun markAsRead() {
        getInboxChatsID(myID!!, friendID!!).child("count").setValue(0)
    }

    private fun getInboxChatsID(
        toUser: String,
        fromUser: String
    ): DatabaseReference { //to create the id for the chats node which will be shown in the messages fragment
        return database.reference.child("chats/$toUser/$fromUser")
    }

    private fun getMessagesFromMessagingID(friendID: String): DatabaseReference { //to retrieve the messages between the two persons
        return database.reference.child("messages/${getMessagingID(friendID)}")
    }

    private fun getMessagingID(friendID: String): String { //to create the id for the messages node under which the conversations will occur
        return if (friendID > myID!!) {
            myID + friendID
        } else {
            friendID + myID
        }
    }

    private fun listentoChanges(ID:String,messageStructure:MessageStructure){
        getMessagesFromMessagingID(friendID!!).child(ID).addChildEventListener(
            object : ChildEventListener{
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    //Implement the live realtime likedStatus feature here
                    val see=snapshot.value
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        markAsRead()
    }
}