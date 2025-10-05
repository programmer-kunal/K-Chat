package com.example.kchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.kchat.presentation.navigation.WhatsappNavigationSystem

import com.example.kchat.ui.theme.KChatTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KChatTheme {
               WhatsappNavigationSystem()
                }
            }
        }
    }


