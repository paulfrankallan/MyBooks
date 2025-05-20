package com.paulallan.mybooks.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.paulallan.mybooks.app.theme.MyBooksTheme
import com.paulallan.mybooks.feature.list.presentation.BookListScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyBooksTheme {
                // In a production app, this would live in a NavHost.
                BookListScreen()
            }
        }
    }
}