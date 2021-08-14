package com.example.letschat

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.letschat.modals.InboxModel
import com.example.letschat.utils.formatAsListItem
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_row.view.*

class MessagesViewHolderclass(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(item: InboxModel, onClick: (name: String, photo: String, id: String) -> Unit) =
        with(itemView) {
            tvmessagecount.isVisible = item.count > 0
            tvmessagecount.text = item.count.toString()
            tvdatereceive.text = item.time.formatAsListItem(context)

            tvNameProfile.text = item.name
            tvmessage.text = item.message
            if(item.image.isEmpty()){        //changed
                tvprofileimage.setImageResource(R.drawable.default_avatar)
            }
            else{
                Picasso.get()
                    .load(item.image)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .into(tvprofileimage)
            }

            setOnClickListener {
                onClick.invoke(item.name, item.image, item.from)
            }
        }
}