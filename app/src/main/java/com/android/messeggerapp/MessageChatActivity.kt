package com.android.messeggerapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.android.messeggerapp.Model.Users
import com.android.messeggerapp.databinding.ActivityMessageChatBinding
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso

class MessageChatActivity : AppCompatActivity() {

    lateinit var binding: ActivityMessageChatBinding

    //    todo 4 send text and image
    var userIdVisit: String = ""
    var firebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //    todo 5 send text and image
        intent = intent
        userIdVisit = intent.getStringExtra("visit_id") ?: ""
        firebaseUser = FirebaseAuth.getInstance().currentUser


        //    todo 7 send text and image
        val reference = FirebaseDatabase.getInstance().reference
            .child("Users").child(userIdVisit)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val user: Users? = p0.getValue(Users::class.java)
                binding.usernameMchat.text = user?.getUsername()
//                Picasso.get().load(user?.getProfile()).into(binding.profileImageMchat)

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
                        .child(firebaseUser?.uid?:"")
                        .child(userIdVisit)

                    chatsListReference.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(p0: DataSnapshot) {
                            if (!p0.exists()){
                                chatsListReference.child("id").setValue(userIdVisit)
                            }

                            val chatsListReceiverRef = FirebaseDatabase.getInstance()
                                .reference
                                .child("ChatLists")
                                .child(userIdVisit)
                                .child(firebaseUser?.uid?:"")
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

        if (requestCode == 438 && resultCode == RESULT_OK && data!=null && data?.data != null) {
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
                if (it.isSuccessful){
                    val downloadUrl = it.result
                    val url = downloadUrl.toString()

                    val messageHashMap = HashMap<String, Any?>()
                    messageHashMap["sender"] = firebaseUser?.uid
                    messageHashMap["message"] = "sent you an image"
                    messageHashMap["receiver"] = userIdVisit
                    messageHashMap["isseen"] = false
                    messageHashMap["url"] = url
                    messageHashMap["messageId"] = messageId

                    ref.child("Chats").child(messageId?:"").setValue(messageHashMap)

                    progressBar.dismiss()
                }
            }
        }
    }
}