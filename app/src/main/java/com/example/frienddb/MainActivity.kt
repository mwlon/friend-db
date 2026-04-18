package com.example.frienddb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.frienddb.ui.navigation.NavGraph
import com.example.frienddb.ui.theme.FriendDbTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FriendDbTheme {
                val app = LocalContext.current.applicationContext as FriendDbApp
                val repository = remember { app.repository }
                NavGraph(repository = repository)
            }
        }
    }
}
