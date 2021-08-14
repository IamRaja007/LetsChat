package com.example.letschat

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_row.view.*

class ViewHolderUser(itemView:View):RecyclerView.ViewHolder(itemView) {

    fun bind(userModel: UserModel, onClick:(name:String,photoURI:String,id:String) -> Unit){
        with(itemView){
            tvmessagecount.isVisible=false
            tvdatereceive.isVisible=false

            tvNameProfile.text=userModel.name
            tvmessage.text=userModel.status

            Picasso.get().load(userModel.thumbnailImage)
                .placeholder(R.drawable.default_avatar)    //this will be shown when image is loading
                .error(R.drawable.default_avatar) //this will be shown if there is loading error of the image
                .into(tvprofileimage)

            setOnClickListener {
                onClick.invoke(userModel.name,userModel.thumbnailImage, userModel.uid)
            }
        }
    }
}