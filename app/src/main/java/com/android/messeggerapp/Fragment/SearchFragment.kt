package com.android.messeggerapp.Fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.messeggerapp.Adapter.UserAdapter
import com.android.messeggerapp.Model.Users
import com.android.messeggerapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SearchFragment : Fragment() {

    //todo 3 search user using firebase queries
    private var userAdapter:UserAdapter?=null
    private var mUsers:List<Users>?=null
    private var recyclerView:RecyclerView?=null

    private var searchUsersET:EditText?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_search, container, false)


        searchUsersET = view.findViewById(R.id.searchUsersET)
        recyclerView = view.findViewById(R.id.searchList)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        //todo 4 search user using firebase queries
        mUsers = ArrayList()
        retriveAllUsers()

        //todo 5 search user using firebase queries
        searchUsersET?.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(cs: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchForUsers(cs.toString().toLowerCase())
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        return view
    }

    //todo 4 search user using firebase queries (finish)
    private fun retriveAllUsers() {
        var firebaseUserId = FirebaseAuth.getInstance().currentUser?.uid
        val refUsers = FirebaseDatabase.getInstance().reference.child("Users")

        refUsers.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                (mUsers as ArrayList<Users>).clear()
                if (searchUsersET?.text.toString()==""){
                    for(snapshot in p0.children){
                        val user = snapshot.getValue(Users::class.java)
                        if (!(user?.getUID()).equals(firebaseUserId)){
                            (mUsers as ArrayList<Users>).add(user!!)
                        }
                    }
                    userAdapter = UserAdapter(context!!,mUsers!!,false)
                    recyclerView?.adapter = userAdapter
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })

    }

    private fun searchForUsers(str:String){
        var firebaseUserId = FirebaseAuth.getInstance().currentUser?.uid
        val queryUsers = FirebaseDatabase.getInstance().reference
            .child("Users").orderByChild("search")
            .startAt(str)
            .endAt(str+"\uf8ff")

        queryUsers.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                (mUsers as ArrayList<Users>).clear()

                for(snapshot in p0.children){
                    val user = snapshot.getValue(Users::class.java)
                    if (!(user?.getUID()).equals(firebaseUserId)){
                        (mUsers as ArrayList<Users>).add(user!!)
                    }
                }


                userAdapter = UserAdapter(context!!,mUsers!!,false)
                recyclerView?.adapter = userAdapter
            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })
    }

}