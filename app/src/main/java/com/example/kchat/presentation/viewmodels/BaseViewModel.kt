package com.example.kchat.presentation.viewmodels

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.ViewModel
import com.example.kchat.models.Message
import com.example.kchat.presentation.splashscreen.homescreen.ChatDesignModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class BaseViewModel: ViewModel() {
    fun searchUserByPhoneNumber(phoneNumber: String,callBack:(ChatDesignModel?)->Unit){
        val currentUser= FirebaseAuth.getInstance().currentUser
        if (currentUser==null){
            Log.e("BaseViewModel","User is not authenticated")
            callBack(null)
            return
        }
        val databaseReference= FirebaseDatabase.getInstance().getReference("users")
        databaseReference.orderByChild("phoneNumber").equalTo(phoneNumber)
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

                    Log.e("BaseViewModel", "Error fetching user: ${error.message},Details:${error.details}")
                    callBack(null)
                }
            }
            )

            }

    fun getChatForUser(userId:String,callBack:(List<ChatDesignModel>)->Unit){
        val chatref=FirebaseDatabase.getInstance().getReference("users/$userId/chats")
        chatref.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val chatList= mutableListOf<ChatDesignModel>()
                    for (childSnapshot in snapshot.children){
                       val chat=childSnapshot.getValue(ChatDesignModel::class.java)

                       if(chat!=null){
                           chatList.add(chat)
                       }

                       }
                    callBack(chatList)

                    }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("BaseViewModel","Error fetching user chats: ${error.message}")
                    callBack(emptyList())
                }
            }


            )
    }
private val _chatList= MutableStateFlow<List<ChatDesignModel>>(emptyList())
    val chatList=_chatList.asStateFlow()

    init {
        loadChatData()
    }


  private  fun loadChatData(){
        val currentUserId= FirebaseAuth.getInstance().currentUser?.uid
        if(currentUserId!=null){
            val chatRef=FirebaseDatabase.getInstance().getReference("chats")
            chatRef.orderByChild("userId").equalTo(currentUserId)
                .addValueEventListener(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val chatList= mutableListOf<ChatDesignModel>()
                        for (childSnapshot in snapshot.children){
                            val chat=childSnapshot.getValue(ChatDesignModel::class.java)
                            if (chat!=null){
                                chatList.add(chat)
                            }
                        }
                        _chatList.value=chatList
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("BaseViewModel","Error fetching chat data: ${error.message}")
                    }


                })
        }
    }


fun addChat(newChat:ChatDesignModel){
    val currentUserId=FirebaseAuth.getInstance().currentUser?.uid
    if (currentUserId!=null){
        val newChatRef=FirebaseDatabase.getInstance().getReference("chats").push()
        val chatWithUser=newChat.copy(currentUserId)
        newChatRef.setValue(chatWithUser).addOnSuccessListener {
            Log.d("BaseViewModel","Chat added successfully to firebase")

        }.addOnFailureListener { exception ->
            Log.e("BaseViewModel","Failed to add chat to firebase: ${exception.message}")

        }
    }else{
        Log.e("BaseViewModel","Current user is not authenticated")
    }

}
    private val dataBaseReference=FirebaseDatabase.getInstance().reference
    fun sendMessage(senderPhoneNumber:String,receiverPhoneNumber:String,messageText:String){
        val messageId=dataBaseReference.push().key?:return
        val message= Message(
            senderPhoneNumber = senderPhoneNumber,
            message = messageText,
            timeStamp = System.currentTimeMillis()
        )

        dataBaseReference.child("messages")
            .child(senderPhoneNumber)
            .child(receiverPhoneNumber)
            .child(messageId)
            .setValue(message)

        dataBaseReference.child("messages")
            .child(receiverPhoneNumber)
            .child(senderPhoneNumber)
            .child(messageId)
            .setValue(message)
    }
    fun getMessage(
        senderPhoneNumber: String,
        receiverPhoneNumber: String,
        onNewMessage: (Message) -> Unit

    ){
        val messageRef=dataBaseReference.child("messages")
            .child(senderPhoneNumber)
            .child(receiverPhoneNumber)
        messageRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot,previousChildName:String?) {
                val message=snapshot.getValue(Message::class.java)
                if (message!=null){
                    onNewMessage(message)

                }
            }

            override fun onChildChanged(
                snapshot: DataSnapshot,
                previousChildName: String?
            ) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(
                snapshot: DataSnapshot,
                previousChildName: String?
            ) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun fetchLastMessageForChat(
        senderPhoneNumber: String,
        receiverPhoneNumber: String,
        onLastMessageFetched: (String, String) -> Unit
    ) {
        val chatRef= FirebaseDatabase.getInstance().reference
            .child("messages")
            .child(senderPhoneNumber)
            .child(receiverPhoneNumber)
        chatRef.orderByChild("timestamp").limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val lastMessage = snapshot.children.firstOrNull()?.child("message")?.value as? String
                        val timestamp = snapshot.children.firstOrNull()?.child("timestamp")?.value as? String
                        onLastMessageFetched(lastMessage?:"No Message",timestamp?:"--:--")
            }else{
                        onLastMessageFetched("No Message","--:--")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onLastMessageFetched("No Message","--:--")

                }
            })


    }
    fun loadChatList(
        currentUserPhoneNumber: String,
        onChatListLoaded: (List<ChatDesignModel>) -> Unit

    )    {
        val chatList=mutableListOf<ChatDesignModel>()
        val chatRef=FirebaseDatabase.getInstance().reference
            .child("chats")
            .child(currentUserPhoneNumber)
        chatRef.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    snapshot.children.forEach { child->

                        val phoneNumber=child.key?:return@forEach
                        val name=child.child("name").value as? String?:"Unknown"
                        val image=child.child("image").value as? String?
                        val profileImageBitmap=image?.let{decodeBase64toBitmap(it)}

                        fetchLastMessageForChat(currentUserPhoneNumber,phoneNumber) { lastMessage, time ->
                            chatList.add(
                                ChatDesignModel(
                                    name = name,
                                    profileBitmap= profileImageBitmap,
                                    message = lastMessage,
                                    time = time
                                )
                            )

                            if(chatList.size==snapshot.childrenCount.toInt()){
                                onChatListLoaded(chatList)

                            }
                        }

                    }
                }else{
                    onChatListLoaded(emptyList())
                }
            }

            override fun onCancelled(snapshot: DatabaseError) {
                onChatListLoaded(emptyList())
            }

        })


    }
    @OptIn(ExperimentalEncodingApi::class)
    private fun decodeBase64toBitmap(base64Image: String): Bitmap?{
        return try{
            val decodedByte= Base64.decode(base64Image,android.util.Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedByte,0,decodedByte.size)
        }catch (e: IOException){

            null
        }

    }
    @OptIn(ExperimentalEncodingApi::class)
    fun base64ToBitmap(base64String: String): Bitmap? {
        return try{
            val decodedByte= Base64.decode(base64String,android.util.Base64.DEFAULT)
            val inputStream: InputStream = ByteArrayInputStream(decodedByte)
            BitmapFactory.decodeStream(inputStream)

        }catch (e: IOException){
            null
        }

    }



    }


