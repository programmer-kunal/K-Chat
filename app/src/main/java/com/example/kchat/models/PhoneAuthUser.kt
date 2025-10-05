package com.example.kchat.models

data class PhoneAuthUser(
    var userId: String = "",
    var email: String = "",      // renamed from phoneNumber
    var name: String = "",
    var status: String = "",
    var profileImage: String? = null
)
