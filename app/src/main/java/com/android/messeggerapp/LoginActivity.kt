package com.android.messeggerapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.android.messeggerapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding

    //todo 4 login,logout,register with firebase
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        todo 8 login_signup (finish)
        setSupportActionBar(binding.toolbarLogin)
        supportActionBar?.title = "Login"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarLogin.setNavigationOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        //todo 5 login,logout,register with firebase
        mAuth = FirebaseAuth.getInstance()

        //todo 6 login,logout,register with firebase
        binding.loginBtn.setOnClickListener {
            loginUser()
        }
    }

    //todo 6 login,logout,register with firebase (next MainActivity)
    private fun loginUser() {
        val email = binding.emailLogin.text.toString()
        val password = binding.passwordLogin.text.toString()

        if (email.isNullOrEmpty()){
            Toast.makeText(this, "email kosong", Toast.LENGTH_SHORT).show()
        }else if (password.isNullOrEmpty()){
            Toast.makeText(this, "password kosong", Toast.LENGTH_SHORT).show()
        }else{
            mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }else{
                        Toast.makeText(this, "Error message"+it.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

}