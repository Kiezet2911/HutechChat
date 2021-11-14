package fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.firebaseappchat.ChatAnDanh.ChatLogAnDanhActivity
import com.example.firebaseappchat.ChatAnDanh.LateMessagesRowAnDanh
import com.example.firebaseappchat.NewMessActivity
import com.example.firebaseappchat.R
import com.example.firebaseappchat.messages.ChatLogActivity
import com.example.firebaseappchat.messages.LateMessagesRow
import com.example.firebaseappchat.model.ChatMessage
import com.example.firebaseappchat.registerlogin.SignUpActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_andanhchat.view.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlin.random.Random


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class ChatAnDanhFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var swipere: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_andanhchat, container, false)
        view.recyclerview_latest_messagesandanh.adapter = adapter
        val BtnChatRanDom = view.findViewById<Button>(R.id.BtnRanDomChat)
        BtnChatRanDom.setOnClickListener {
            RanDomChat()
        }
        view.recyclerview_latest_messagesandanh.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )

        swipere = view.findViewById(R.id.swipeRefreshandanh)
        swipere.setOnRefreshListener(this::refreshRecyclerViewMessages)
        ListenForlatesMessages(view)
        return view
    }

    @SuppressLint("LongLogTag")
    private fun RanDomChat() {
        val UserFirebase = FirebaseDatabase.getInstance().getReference("user")
        val NguoiDung = FirebaseAuth.getInstance().currentUser
        val MessageFirebase = FirebaseDatabase.getInstance().getReference("latest-messages-andanh")
        MessageFirebase.get().addOnSuccessListener {
            var coutMessages: Long = 0
            if (NguoiDung != null) {
                coutMessages = it.child(NguoiDung.uid).childrenCount
            }
            UserFirebase.get().addOnSuccessListener {
                val count = it.childrenCount
                if ((count - 1) != coutMessages) {
                    var random: Long = Random.nextLong(1, count + 1)
                    if (NguoiDung != null) {
                        val uidNguoiDung = it.child(NguoiDung.uid).child("STT").value
                        while (random == it.child(NguoiDung.uid).child("STT").value) {
                            random = Random.nextLong(1, count + 1)
                        }
                        UserFirebase.get().addOnSuccessListener {
                            var uid = ""
                            var name = ""
                            var STT = ""
                            var check = "false"
                            var user: SignUpActivity.getUser? = null
                            it.children.forEach() {
                                if (it.child("STT").value == random) {
                                    uid = it.child("uid").value.toString()
                                    name = it.child("name").value.toString()
                                    STT = it.child("STT").value.toString()
                                    user = it.getValue(SignUpActivity.getUser::class.java)
                                }
                            }

                            MessageFirebase.get().addOnSuccessListener {
                                if (it.child(NguoiDung.uid).hasChild(uid)) {
                                    check = "true"
                                }
                                if (check == "true") {
                                    random = Random.nextLong(1, count + 1)
                                    do {
                                        random = Random.nextLong(1, count + 1)
                                        Log.d("RANDOM-TRONG-MessageFirebase", random.toString())
                                    } while (random == uidNguoiDung || random == STT.toLong())
                                    UserFirebase.get().addOnSuccessListener {
                                        var userMessageFirebase: SignUpActivity.getUser? = null
                                        it.children.forEach() {
                                            if (it.child("STT").value == random) {
                                                userMessageFirebase =
                                                    it.getValue(SignUpActivity.getUser::class.java)

                                            }
                                        }
                                        Log.d("UID_TEST", userMessageFirebase?.uid.toString())
                                        Log.d("NAME_TEST", userMessageFirebase?.name.toString())
                                        Log.d("STT_TEST", userMessageFirebase?.STT.toString())
                                        val intent = Intent(context, ChatLogAnDanhActivity::class.java)
                                        intent.putExtra(NewMessActivity.USER_KEY, userMessageFirebase)
                                        startActivity(intent)
                                    }
                                } else {
                                    Log.d("UID_TEST", user?.uid.toString())
                                    Log.d("NAME_TEST", user?.name.toString())
                                    Log.d("STT_TEST", user?.STT.toString())
                                    val intent = Intent(context, ChatLogAnDanhActivity::class.java)
                                    intent.putExtra(NewMessActivity.USER_KEY, user)
                                    startActivity(intent)

                                }
                                Log.d("CHECK-TEST", check)
                            }
                        }
                        Log.d("RANDOM", random.toString())
                    }
                } else {
                    Toast.makeText(context, "Bạn Đã Chat Với Người Bí Ẩn", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    }

    val latestMessagesMap = HashMap<String, ChatMessage>()

    @SuppressLint("LogNotTimber")
    private fun refreshRecyclerViewMessages() {
        adapter.clear()
        latestMessagesMap.values.forEach {
            Handler().postDelayed(Runnable {
                swipere.isRefreshing = false
            }, 4000)
            adapter.add(LateMessagesRowAnDanh(it))
            adapter.setOnItemClickListener { item, view ->
                val row = item as LateMessagesRowAnDanh
                val intent = Intent(view.context, ChatLogAnDanhActivity::class.java)
                intent.putExtra(NewMessActivity.USER_KEY, row.chatPartnerUser)
                startActivity(intent)
            }
        }
    }

    private fun ListenForlatesMessages(view: View) {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages-andanh/$fromId")
        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return

                latestMessagesMap[snapshot.key!!] = chatMessage
                refreshRecyclerViewMessages()

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return

                latestMessagesMap[snapshot.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }


            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            @SuppressLint("LongLogTag")
            override fun onChildRemoved(snapshot: DataSnapshot) {
                Log.d("THÔNG BÁO XÓA TIN NHẮN BÊN HOMEFRAGMENT:", "THÀNH CÔNG")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    val adapter = GroupAdapter<GroupieViewHolder>()

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}