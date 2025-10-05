package com.example.kchat.presentation.viewmodels

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.kchat.models.Message
import com.example.kchat.presentation.splashscreen.homescreen.ChatDesignModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

class BaseViewModel : ViewModel() {

    // ✅ Search user by email instead of phone number
    fun searchUserByEmail(email: String, callBack: (ChatDesignModel?) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e("BaseViewModel", "User is not authenticated")
            callBack(null)
            return
        }

        val databaseReference = FirebaseDatabase.getInstance().getReference("users")
        databaseReference.orderByChild("email")
            .equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.children.first().getValue(ChatDesignModel::class.java)
                        callBack(user)
                    } else {
                        callBack(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(
                        "BaseViewModel",
                        "Error fetching user: ${error.message}, Details: ${error.details}"
                    )
                    callBack(null)
                }
            })
    }

    // ✅ Get chats for a user using email
    fun getChatForUser(userEmail: String, callBack: (List<ChatDesignModel>) -> Unit) {
        val chatRef = FirebaseDatabase.getInstance().getReference("users/$userEmail/chats")
        chatRef.orderByChild("userId").equalTo(userEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val chatList = mutableListOf<ChatDesignModel>()
                    for (childSnapshot in snapshot.children) {
                        val chat = childSnapshot.getValue(ChatDesignModel::class.java)
                        if (chat != null) chatList.add(chat)
                    }
                    callBack(chatList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("BaseViewModel", "Error fetching user chats: ${error.message}")
                    callBack(emptyList())
                }
            })
    }

    private val _chatList = MutableStateFlow<List<ChatDesignModel>>(emptyList())
    val chatList = _chatList.asStateFlow()

    init {
        loadChatData()
    }

    private fun loadChatData() {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
        if (currentUserEmail != null) {
            val chatRef = FirebaseDatabase.getInstance().getReference("chats")
            chatRef.orderByChild("userId").equalTo(currentUserEmail)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val chatList = mutableListOf<ChatDesignModel>()
                        for (childSnapshot in snapshot.children) {
                            val chat = childSnapshot.getValue(ChatDesignModel::class.java)
                            if (chat != null) chatList.add(chat)
                        }
                        _chatList.value = chatList
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("BaseViewModel", "Error fetching chat data: ${error.message}")
                    }
                })
        }
    }

    fun addChat(newChat: ChatDesignModel) {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
        if (currentUserEmail != null) {
            val newChatRef = FirebaseDatabase.getInstance().getReference("chats").push()
            val chatWithUser = newChat.copy(currentUserEmail)
            newChatRef.setValue(chatWithUser)
                .addOnSuccessListener { Log.d("BaseViewModel", "Chat added successfully to Firebase") }
                .addOnFailureListener { exception ->
                    Log.e("BaseViewModel", "Failed to add chat to Firebase: ${exception.message}")
                }
        } else {
            Log.e("BaseViewModel", "Current user is not authenticated")
        }
    }

    private val dataBaseReference = FirebaseDatabase.getInstance().reference

    // ✅ Send message with emails
    fun sendMessage(senderEmail: String, receiverEmail: String, messageText: String) {
        val messageId = dataBaseReference.push().key ?: return
        val message = Message(
            senderPhoneNumber = senderEmail,
            message = messageText,
            timeStamp = System.currentTimeMillis()
        )

        dataBaseReference.child("messages")
            .child(senderEmail)
            .child(receiverEmail)
            .child(messageId)
            .setValue(message)

        dataBaseReference.child("messages")
            .child(receiverEmail)
            .child(senderEmail)
            .child(messageId)
            .setValue(message)
    }

    fun getMessage(senderEmail: String, receiverEmail: String, onNewMessage: (Message) -> Unit) {
        val messageRef = dataBaseReference.child("messages")
            .child(senderEmail)
            .child(receiverEmail)
        messageRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                if (message != null) onNewMessage(message)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun fetchLastMessageForChat(
        senderEmail: String,
        receiverEmail: String,
        onLastMessageFetched: (String, String) -> Unit
    ) {
        val chatRef = dataBaseReference.child("messages")
            .child(senderEmail)
            .child(receiverEmail)
        chatRef.orderByChild("timestamp").limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val lastMessage =
                            snapshot.children.firstOrNull()?.child("message")?.value as? String
                        val timestamp =
                            snapshot.children.firstOrNull()?.child("timestamp")?.value as? String
                        onLastMessageFetched(lastMessage ?: "No Message", timestamp ?: "--:--")
                    } else {
                        onLastMessageFetched("No Message", "--:--")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onLastMessageFetched("No Message", "--:--")
                }
            })
    }

    fun loadChatList(
        currentUserEmail: String,
        onChatListLoaded: (List<ChatDesignModel>) -> Unit
    ) {
        val chatList = mutableListOf<ChatDesignModel>()
        val chatRef = dataBaseReference.child("chats").child(currentUserEmail)
        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.forEach { child ->
                        val email = child.key ?: return@forEach
                        val name = child.child("name").value as? String ?: "Unknown"
                        val image = child.child("image").value as? String
                        val profileImageBitmap = image?.let { decodeBase64toBitmap(it) }

                        fetchLastMessageForChat(currentUserEmail, email) { lastMessage, time ->
                            chatList.add(
                                ChatDesignModel(
                                    name = name,
                                    profileBitmap = profileImageBitmap,
                                    message = lastMessage,
                                    time = time
                                )
                            )

                            if (chatList.size == snapshot.childrenCount.toInt()) {
                                onChatListLoaded(chatList)
                            }
                        }
                    }
                } else {
                    onChatListLoaded(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onChatListLoaded(emptyList())
            }
        })
    }

    private fun decodeBase64toBitmap(base64Image: String): Bitmap? {
        return try {
            val decodedByte = Base64.decode(base64Image, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
        } catch (e: IOException) {
            null
        }
    }

    fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedByte = Base64.decode(base64String, Base64.DEFAULT)
            val inputStream: InputStream = ByteArrayInputStream(decodedByte)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            null
        }
    }
}
