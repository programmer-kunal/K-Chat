package com.example.kchat.presentation.splashscreen.homescreen

import android.graphics.Bitmap

data class ChatDesignModel(
    val name: String? = null,
    val email: String = "",           // ⚡ Make email non-nullable with default empty string
    val image: Int? = null,           // still keep for drawable IDs
    val userId: String? = null,
    val time: String? = null,
    val message: String? = null,
    val profileImage: String? = null,
    val profileBitmap: Bitmap? = null // ✅ new field for decoded Bitmap
) {
    constructor() : this("", "", null, null, null, null, null, null)
}
