package com.example.firebaseappchat.messages

import com.example.firebaseappchat.R
import com.example.firebaseappchat.model.ChatMessage
import com.example.firebaseappchat.registerlogin.SignUpActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LateMessagesRow(val chatMessage: ChatMessage) : Item<GroupieViewHolder>() {
    var chatPartnerUser: SignUpActivity.getUser? = null
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.message_textview_latest_messages.text = chatMessage.text

        val chatPartnerId: String
        if (chatMessage.formId == FirebaseAuth.getInstance().uid) {

            chatPartnerId = chatMessage.toId
        } else {
            chatPartnerId = chatMessage.formId
        }

        val ref = FirebaseDatabase.getInstance().getReference("/user/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatPartnerUser = snapshot.getValue((SignUpActivity.getUser::class.java))

                val targetImageView = viewHolder.itemView.imageview_lastest_messages
                viewHolder.itemView.username_textView_latestmessage.text = chatPartnerUser?.name
                Picasso.get().load(chatPartnerUser?.Urlphoto).into(targetImageView)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


    }

    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }
}
