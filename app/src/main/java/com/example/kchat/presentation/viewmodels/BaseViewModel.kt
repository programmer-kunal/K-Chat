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

    private val dataBaseReference = FirebaseDatabase.getInstance().reference

    // ✅ StateFlow for chat list
    private val _chatList = MutableStateFlow<List<ChatDesignModel>>(emptyList())
    val chatList = _chatList.asStateFlow()

    // 🔹 Update chat list manually
    fun updateChatList(newList: List<ChatDesignModel>) {
        _chatList.value = newList
    }

    // 🔹 Sanitize Firebase path for emails
    private fun sanitizeEmail(email: String): String {
        return email.replace(".", "_").replace("@", "_at_")
    }

    // 🔹 Search user by email
    fun searchUserByEmail(email: String, callBack: (ChatDesignModel?) -> Unit) {
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
                    Log.e("BaseViewModel", "Error fetching user: ${error.message}")
                    callBack(null)
                }
            })
    }

    // 🔹 Load chat list for current user and update StateFlow automatically
    fun loadChatList(currentUserEmail: String) {
        val currentUserKey = sanitizeEmail(currentUserEmail)
        val chatRef = dataBaseReference.child("chats").child(currentUserKey)

        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatList = mutableListOf<ChatDesignModel>()
                if (snapshot.exists()) {
                    snapshot.children.forEach { child ->
                        val email = child.key ?: return@forEach
                        val name = child.child("name").value as? String ?: "Unknown"
                        val image = child.child("image").value as? String
                        val profileBitmap = image?.let { decodeBase64toBitmap(it) }

                        fetchLastMessageForChat(currentUserEmail, email) { lastMessage, time ->
                            chatList.add(
                                ChatDesignModel(
                                    name = name,
                                    profileBitmap = profileBitmap,
                                    message = lastMessage,
                                    time = time,
                                    email = email
                                )
                            )
                            if (chatList.size == snapshot.childrenCount.toInt()) {
                                // ✅ Update StateFlow directly
                                _chatList.value = chatList
                            }
                        }
                    }
                } else {
                    _chatList.value = emptyList()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _chatList.value = emptyList()
            }
        })
    }

    // 🔹 Add new chat under both users (fixed)
    fun addChat(newChat: ChatDesignModel) {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
        val currentUserKey = sanitizeEmail(currentUserEmail)
        val otherUserEmail = newChat.email ?: return
        val otherUserKey = sanitizeEmail(otherUserEmail)

        // ✅ Add chat for current user
        val chatForCurrent = ChatDesignModel(
            name = newChat.name ?: "Unknown",
            email = otherUserEmail,
            profileBitmap = newChat.profileBitmap,
            message = "",
            time = ""
        )
        dataBaseReference.child("chats").child(currentUserKey).child(otherUserKey)
            .setValue(chatForCurrent)
            .addOnSuccessListener { Log.d("BaseViewModel", "Chat added for current user") }
            .addOnFailureListener { e -> Log.e("BaseViewModel", "Error adding chat: ${e.message}") }

        // ✅ Add chat for other user (with current user info)
        val chatForOther = ChatDesignModel(
            name = currentUserEmail, // current user is "name" for other user's chat
            email = currentUserEmail,
            profileBitmap = null,
            message = "",
            time = ""
        )
        dataBaseReference.child("chats").child(otherUserKey).child(currentUserKey)
            .setValue(chatForOther)
            .addOnSuccessListener { Log.d("BaseViewModel", "Chat added for other user") }
    }

    // 🔹 Send message
    fun sendMessage(senderEmail: String, receiverEmail: String, messageText: String) {
        val senderKey = sanitizeEmail(senderEmail)
        val receiverKey = sanitizeEmail(receiverEmail)
        val messageId = dataBaseReference.push().key ?: return

        val message = Message(
            senderPhoneNumber = senderEmail,
            message = messageText,
            timeStamp = System.currentTimeMillis()
        )

        dataBaseReference.child("messages").child(senderKey).child(receiverKey).child(messageId).setValue(message)
        dataBaseReference.child("messages").child(receiverKey).child(senderKey).child(messageId).setValue(message)
    }

    // 🔹 Listen for messages
    fun getMessage(senderEmail: String, receiverEmail: String, onNewMessage: (Message) -> Unit) {
        val senderKey = sanitizeEmail(senderEmail)
        val receiverKey = sanitizeEmail(receiverEmail)
        val messageRef = dataBaseReference.child("messages").child(senderKey).child(receiverKey)

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

    // 🔹 Fetch last message
    fun fetchLastMessageForChat(senderEmail: String, receiverEmail: String, onLastMessageFetched: (String, String) -> Unit) {
        val senderKey = sanitizeEmail(senderEmail)
        val receiverKey = sanitizeEmail(receiverEmail)
        val chatRef = dataBaseReference.child("messages").child(senderKey).child(receiverKey)

        chatRef.orderByChild("timestamp").limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val lastMessage = snapshot.children.firstOrNull()?.child("message")?.value as? String
                        val timestamp = snapshot.children.firstOrNull()?.child("timestamp")?.value as? String
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

    // 🔹 Decode Base64 to Bitmap
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
