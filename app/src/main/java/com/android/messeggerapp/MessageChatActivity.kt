package com.android.messeggerapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.messeggerapp.Adapter.ChatAdapter
import com.android.messeggerapp.Model.Chat
import com.android.messeggerapp.Model.Users
import com.android.messeggerapp.databinding.ActivityMessageChatBinding
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso

class MessageChatActivity : AppCompatActivity() {

    private var reference: DatabaseReference?=null
    lateinit var binding: ActivityMessageChatBinding

    //    todo 4 send text and image
    var userIdVisit: String = ""
    var firebaseUser: FirebaseUser? = null

    //todo 6 read and display message
    var chatAdapter: ChatAdapter? = null
    var mChatList: List<Chat>? = null
    lateinit var recycler_view_chats:RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageChatBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //    todo 4 display chatlist and total number
        setSupportActionBar(binding.toolbarMessageChat)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarMessageChat.setNavigationOnClickListener {
            val intent = Intent(this,WelcomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        //    todo 5 send text and image
        intent = intent
        userIdVisit = intent.getStringExtra("visit_id") ?: ""
        firebaseUser = FirebaseAuth.getInstance().currentUser

        //todo 6 read and display message
        recycler_view_chats = findViewById(R.id.recycler_view_chats)
        recycler_view_chats.setHasFixedSize(true)
        var linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        recycler_view_chats.layoutManager = linearLayoutManager

        //    todo 7 send text and image
        reference = FirebaseDatabase.getInstance().reference
            .child("Users").child(userIdVisit)
        reference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val user: Users? = p0.getValue(Users::class.java)
                binding.usernameMchat.text = user?.getUsername()
//                Picasso.get().load(user?.getProfile()).into(binding.profileImageMchat)

                //todo 7 read and display message
                retriveMessages(firebaseUser?.uid,userIdVisit,user?.getProfile())

            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })


//        todo 2 send text and image (next UserAdapter)
        binding.sendMessageBtn.setOnClickListener {
            val message = binding.textMessage.text.toString()
            if (message.isNullOrEmpty()) {
                Toast.makeText(this, "Pesan kosong", Toast.LENGTH_SHORT).show()
            } else {
                //    todo 6 send text and image
                sendMessageToUser(firebaseUser?.uid, userIdVisit, message)
            }
            binding.textMessage.setText("")
        }

        //    todo 8 send text and image
        binding.attactImageFileBtn.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Pick Image"), 438)
        }

        //    todo 7 display chatlist and total number (next MainActivity)
        seenMessage(userIdVisit)
    }

    //    todo 6 send text and image
    private fun sendMessageToUser(senderId: String?, receiverId: String, message: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key

        val messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = senderId
        messageHashMap["message"] = message
        messageHashMap["receiver"] = receiverId
        messageHashMap["isseen"] = false
        messageHashMap["url"] = ""
        messageHashMap["messageId"] = messageKey

        reference.child("Chats")
            .child(messageKey ?: "")
            .setValue(messageHashMap)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val chatsListReference = FirebaseDatabase.getInstance()
                        .reference
                        .child("ChatLists")
                        .child(firebaseUser?.uid ?: "")
                        .child(userIdVisit)

                    chatsListReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            if (!p0.exists()) {
                                chatsListReference.child("id").setValue(userIdVisit)
                            }

                            val chatsListReceiverRef = FirebaseDatabase.getInstance()
                                .reference
                                .child("ChatLists")
                                .child(userIdVisit)
                                .child(firebaseUser?.uid ?: "")
                            chatsListReceiverRef.child("id").setValue(firebaseUser?.uid)
                        }

                        override fun onCancelled(p0: DatabaseError) {
                        }

                    })


                    val reference = FirebaseDatabase.getInstance().reference
                        .child("Users").child(firebaseUser?.uid ?: "")

                    //implement the push notifications using fcm
                }
            }
    }

    //    todo 9 send text and image (finish)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data?.data != null) {
            val progressBar = ProgressDialog(this)
            progressBar.setMessage("Image is uploading, please wait...")
            progressBar.show()

            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key
            val filePath = storageReference.child("$messageId.jpg")

            var uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)

            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> {
                if (!it.isSuccessful) {
                    it.exception.let { error ->
                        throw error!!
                    }
                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener {
                if (it.isSuccessful) {
                    val downloadUrl = it.result
                    val url = downloadUrl.toString()

                    val messageHashMap = HashMap<String, Any?>()
                    messageHashMap["sender"] = firebaseUser?.uid
                    messageHashMap["message"] = "sent you an image"
                    messageHashMap["receiver"] = userIdVisit
                    messageHashMap["isseen"] = false
                    messageHashMap["url"] = url
                    messageHashMap["messageId"] = messageId

                    ref.child("Chats").child(messageId ?: "").setValue(messageHashMap)

                    progressBar.dismiss()
                }
            }
        }
    }

    //todo 7 read and display message (finish)
    private fun retriveMessages(senderId: String?, receiverId: String?, receiverImageUrl: String?) {
        mChatList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")
        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                (mChatList as ArrayList<Chat>).clear()
                for (snapshot in p0.children){
                    val chat = snapshot.getValue(Chat::class.java)

                    if (chat?.getReceiver().equals(senderId) && chat?.getSender().equals(receiverId)
                        || chat?.getReceiver().equals(receiverId) && chat?.getSender().equals(senderId)){
                        (mChatList as ArrayList<Chat>).add(chat!!)
                    }
                    chatAdapter = ChatAdapter(this@MessageChatActivity,(mChatList as ArrayList<Chat>),receiverImageUrl?:"")
                    recycler_view_chats.adapter = chatAdapter

                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })
    }

    //    todo 5 display chatlist and total number
    var seenListener:ValueEventListener?=null
    private fun seenMessage(userId:String){
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")
        seenListener = reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                for(dataSnapshot in p0.children){
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if (chat?.getReceiver().equals(firebaseUser?.uid) && chat?.getSender().equals(userId)){
                        val hashMap = HashMap<String,Any>()
                        hashMap["isseen"] = true
                        dataSnapshot.ref.updateChildren(hashMap)
                    }

                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    //    todo 6 display chatlist and total number
    override fun onPause() {
        super.onPause()

        reference?.removeEventListener(seenListener!!)
    }
}