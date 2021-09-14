package com.android.messeggerapp.Model


//todo 4 read and display message (next ChatAdapter)

class Chatlist {

    private var id: String = ""

    constructor()
    constructor(id: String) {
        this.id = id
    }

    fun getId(): String? {
        return id
    }

    fun setId(id: String?) {
        this.id = id ?: ""
    }
}