package com.example.letschat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_connections.*
import java.lang.Exception

private val VIEW_DELETED=1
private val VIEW_NORMAL=2


class ConnectionsFragment:Fragment(){
    lateinit var pagingAdapter: FirestorePagingAdapter<UserModel,RecyclerView.ViewHolder>

    val auth by lazy{
        FirebaseAuth.getInstance()
    }

    val database by lazy {
        FirebaseFirestore.getInstance().collection("users")
            .orderBy("name", Query.Direction.DESCENDING)  //ORDERING BY NAME
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setupAdapter()
        return inflater.inflate(R.layout.fragment_connections,container,false)
    }

    private fun setupAdapter() {
        val config= PagedList.Config.Builder()
            .setPrefetchDistance(2)
            .setPageSize(5)
            .setEnablePlaceholders(false)
            .build()


        val options=FirestorePagingOptions.Builder<UserModel> ()
            .setLifecycleOwner(this)  //we want to paginate only when we are using the UI
            .setQuery(database,config,UserModel::class.java)
            .build()

        pagingAdapter=object : FirestorePagingAdapter<UserModel,RecyclerView.ViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

                return when(viewType){                   //We are doing this because we need to remove our own profile from connections
                    VIEW_NORMAL ->{
                        val view= LayoutInflater.from(parent.context).inflate(R.layout.item_row,parent,false)
                        ViewHolderUser(view)
                    }
                    else ->{
                        val view= LayoutInflater.from(parent.context).inflate(R.layout.rv_empty_view,parent,false)
                        EmptyViewHolder(view)
                    }
                }

            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: UserModel) {
                if(holder is ViewHolderUser){
                    holder.bind(model){name, photoURI, id ->    //higher order function we are using

                        startActivity(
                            MessageInboxActivity.createMessageInboxActivity(
                                requireContext(),
                                id,
                                name,
                                photoURI
                            )
                        )

                    }
                }
            }

            override fun onLoadingStateChanged(state: LoadingState) {
                super.onLoadingStateChanged(state)
                when(state){
                    LoadingState.FINISHED ->{

                    }
                    LoadingState.LOADING_INITIAL -> {

                    }
                    LoadingState.LOADING_MORE -> {

                    }
                    LoadingState.LOADED -> {

                    }
                    LoadingState.ERROR -> {
                        Toast.makeText(
                            context,
                            "Error Occurred!",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }
            }

            override fun onError(e: Exception) {
                super.onError(e)
            }

            override fun getItemViewType(position: Int): Int {   //We are doing this because we need to remove our own profile from connections
                val item=getItem(position)?.toObject(UserModel::class.java)   //we are getting each view of the recycler view and and converting it into usermodel object so we can see whats inside it
                return if(auth.uid == item!!.uid){
                    VIEW_DELETED
                }
                else{
                    VIEW_NORMAL
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvConnections.apply{
            layoutManager= LinearLayoutManager(context)
            adapter= pagingAdapter
        }
    }

    override fun onStart() {
        super.onStart()
        if(::pagingAdapter.isInitialized) {
            pagingAdapter.startListening()
        }
    }

    override fun onStop() {
        super.onStop()
        if(::pagingAdapter.isInitialized) {
            pagingAdapter.stopListening()
        }
    }
}