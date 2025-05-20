package com.paulallan.mybooks.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
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
                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.Companion.fillMaxSize(),
                    ) { innerPadding ->
                        BookListScreen(
                            modifier = Modifier.Companion.padding(innerPadding)
                        )
                    }
            }
        }
    }
}