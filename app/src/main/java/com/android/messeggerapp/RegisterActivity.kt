package com.android.messeggerapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.android.messeggerapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    lateinit var binding : ActivityRegisterBinding

    //todo 1 login,logout,register with firebase
    private lateinit var mAuth:FirebaseAuth
    private lateinit var refUsers:DatabaseReference
    private var firebaseUserID:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)


//        todo 7 login_signup (next LoginActivity)
        setSupportActionBar(binding.toolbarRegister)
        supportActionBar?.title = "Register"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarRegister.setNavigationOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        //todo 2 login,logout,register with firebase
        mAuth = FirebaseAuth.getInstance()

        //todo 3 login,logout,register with firebase
        binding.registerBtn.setOnClickListener {
            registerUser()
        }
    }

    //todo 3 login,logout,register with firebase (next LoginActivity)
    private fun registerUser() {
        val username = binding.usernameRegister.text.toString()
        val email = binding.emailRegister.text.toString()
        val password = binding.passwordRegister.text.toString()

        if (username.isNullOrEmpty()){
            Toast.makeText(this, "username kosong", Toast.LENGTH_SHORT).show()
        }else if (email.isNullOrEmpty()){
            Toast.makeText(this, "email kosong", Toast.LENGTH_SHORT).show()
        }else if (password.isNullOrEmpty()){
            Toast.makeText(this, "password kosong", Toast.LENGTH_SHORT).show()
        }else{
            mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        firebaseUserID = mAuth.currentUser?.uid?:""
                        refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUserID)

                        val userHashMap = HashMap<String,Any>()
                        userHashMap["uid"] = firebaseUserID
                        userHashMap["username"] = username
                        userHashMap["profile"] = ""
                        userHashMap["cover"] = ""
                        userHashMap["status"] = "offline"
                        userHashMap["search"] = username.toLowerCase()
                        userHashMap["facebook"] = "My facebook"
                        userHashMap["instagram"] = "My instagram"
                        userHashMap["website"] = "My website"

                        refUsers.updateChildren(userHashMap)
                            .addOnCompleteListener {
                                if (it.isSuccessful){
                                    val intent = Intent(this, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                    }else{
                        Toast.makeText(this, "Error message"+it.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}