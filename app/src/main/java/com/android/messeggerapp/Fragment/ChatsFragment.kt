package com.android.messeggerapp.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.messeggerapp.Adapter.UserAdapter
import com.android.messeggerapp.Model.Chatlist
import com.android.messeggerapp.Model.Users
import com.android.messeggerapp.Notifications.Token
import com.android.messeggerapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId

class ChatsFragment : Fragment() {

    //    todo 2 display chatlist and total number
    private var userAdapter: UserAdapter? = null
    private var mUsers: List<Users>? = null
    private var usersChatList: List<Chatlist>? = null
    lateinit var recycler_view_chatlist:RecyclerView
    private var firebaseUser: FirebaseUser?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_chats, container, false)

        //    todo 2 display chatlist and total number
        recycler_view_chatlist = view.findViewById(R.id.recycler_view_chatlist)
        recycler_view_chatlist.setHasFixedSize(true)
        recycler_view_chatlist.layoutManager = LinearLayoutManager(context)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        usersChatList = ArrayList()
        val ref = FirebaseDatabase.getInstance().reference.child("ChatLists").child(firebaseUser?.uid?:"")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                (usersChatList as ArrayList).clear()
                for (dataSnapshot in p0.children){
                    val chatlist = dataSnapshot.getValue(Chatlist::class.java)
                    (usersChatList as ArrayList).add(chatlist!!)
                }
                retriveChatList()
            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })

        //todo 10 push notification
        updateToken(FirebaseInstanceId.getInstance().token)


        return view
    }

    //todo 10 push notification
    private fun updateToken(token: String?)
    {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val token1 = Token(token!!)
        ref.child(firebaseUser!!.uid).setValue(token1)
    }


    //    todo 3 display chatlist and total number (next MessageChatActivity)
    fun retriveChatList() {
        mUsers = ArrayList()

        val ref = FirebaseDatabase.getInstance().reference.child("Users")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                (mUsers as ArrayList).clear()

                for (dataSnapshot in p0.children){
                    val user = dataSnapshot.getValue(Users::class.java)

                    for (eachChatList in usersChatList!!){
                        if (user?.getUID().equals(eachChatList.getId())){
                            (mUsers as ArrayList).add(user!!)
                        }
                    }
                }
                userAdapter = UserAdapter(requireContext(),(mUsers as ArrayList<Users>),true)
                recycler_view_chatlist.adapter = userAdapter
            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })
    }
}