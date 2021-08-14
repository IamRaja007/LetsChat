package com.example.letschat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letschat.modals.InboxModel
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_messages.*
import kotlinx.android.synthetic.main.item_row.*

class ChatsFragment:Fragment() {

    private lateinit var mAdapter: FirebaseRecyclerAdapter<InboxModel, MessagesViewHolderclass>  // we are not using paging adapter because there is a problem with it in terms of showing realtime updates
    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    val database by lazy {
        FirebaseDatabase.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setupAdapter()

        return inflater.inflate(R.layout.fragment_messages, container, false)

    }

    private fun setupAdapter() {
        val baseQuery = database.reference.child("chats").child(auth.uid!!)

        val options = FirebaseRecyclerOptions.Builder<InboxModel>()
            .setLifecycleOwner(viewLifecycleOwner)
            .setQuery(baseQuery, InboxModel::class.java)
            .build()

        mAdapter = object : FirebaseRecyclerAdapter<InboxModel, MessagesViewHolderclass>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): MessagesViewHolderclass {
                val inflater = layoutInflater
                return MessagesViewHolderclass(inflater.inflate(R.layout.item_row, parent, false))
            }

            override fun onBindViewHolder(
                holder: MessagesViewHolderclass,
                position: Int,
                model: InboxModel
            ) {
                holder.bind(model) { name: String, photo: String, id: String ->
                    startActivity(
                        MessageInboxActivity.createMessageInboxActivity(
                            requireContext(),
                            id,
                            name,
                            photo
                        )
                    )
                }


            }

        }
    }

    override fun onStart() {
        super.onStart()
        mAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        mAdapter.stopListening()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvMessages.layoutManager = LinearLayoutManager(requireContext())
        rvMessages.adapter=mAdapter
    }
}



