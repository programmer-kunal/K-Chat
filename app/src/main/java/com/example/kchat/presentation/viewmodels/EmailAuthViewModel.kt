package com.example.kchat.presentation.viewmodels

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.ViewModel
import com.example.kchat.models.PhoneAuthUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayOutputStream

@HiltViewModel
class EmailAuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val database: FirebaseDatabase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Ideal)
    val authState = _authState.asStateFlow()

    private val userRef = database.reference.child("users")

    // ✅ Existing register with email
    fun registerWithEmail(email: String, password: String, context: Context) {
        _authState.value = AuthState.Loading
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    val emailUser = PhoneAuthUser(
                        userId = user?.uid ?: "",
                        email = user?.email ?: "" // Reused model — using email instead
                    )
                    markUserAsSignedIn(context)
                    _authState.value = AuthState.Success(emailUser)
                    fetchUserProfile(user?.uid ?: "")
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Registration failed")
                }
            }
    }

    // ✅ Existing login with email
    fun loginWithEmail(email: String, password: String, context: Context) {
        _authState.value = AuthState.Loading
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    val emailUser = PhoneAuthUser(
                        userId = user?.uid ?: "",
                        email = user?.email ?: ""
                    )
                    markUserAsSignedIn(context)
                    _authState.value = AuthState.Success(emailUser)
                    fetchUserProfile(user?.uid ?: "")
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Login failed")
                }
            }
    }

    // ✅ Wrapper functions to match UserRegistrationScreen
    fun registerUser(email: String, password: String, context: Context) {
        registerWithEmail(email, password, context)
    }

    fun loginUser(email: String, password: String, context: Context) {
        loginWithEmail(email, password, context)
    }

    private fun markUserAsSignedIn(context: Context) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("isSignedIn", true).apply()
    }

    private fun fetchUserProfile(userId: String) {
        val userNode = userRef.child(userId)
        userNode.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val userProfile = snapshot.getValue(PhoneAuthUser::class.java)
                if (userProfile != null) {
                    _authState.value = AuthState.Success(userProfile)
                }
            }
        }.addOnFailureListener {
            _authState.value = AuthState.Error("Failed to fetch user profile")
        }
    }

    fun saveUserProfile(userId: String, name: String, status: String, profileImage: Bitmap?) {
        val database = FirebaseDatabase.getInstance().reference
        val encodedImage = profileImage?.let { convertBitmapToBase64(it) }
        val userProfile = PhoneAuthUser(
            userId = userId,
            name = name,
            status = status,
            email = Firebase.auth.currentUser?.email ?: "", // ✅ email instead of phone
            profileImage = encodedImage
        )
        database.child("users").child(userId).setValue(userProfile)
    }

    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun resetAuthState() {
        _authState.value = AuthState.Ideal
    }

    fun signOut(activity: Activity) {
        firebaseAuth.signOut()
        val sharedPreferences = activity.getSharedPreferences("app_prefs", Activity.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("isSignedIn", false).apply()
    }
}

// ✅ Same sealed class reused — no change needed
sealed class AuthState {
    object Ideal : AuthState()
    object Loading : AuthState()
    data class Success(val user: PhoneAuthUser) : AuthState()
    data class Error(val message: String) : AuthState()
}
