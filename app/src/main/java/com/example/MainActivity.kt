package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.AppDatabase
import com.example.data.DecisionRepository
import com.example.ui.DecisionViewModel
import com.example.ui.DecisionViewModelFactory
import com.example.ui.TiebreakerAppScreen
import com.example.ui.theme.MyApplicationTheme
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Setup Room local database and repository layers
    val database = AppDatabase.getDatabase(applicationContext)
    val repository = DecisionRepository(database.decisionDao())

    // Instantiate business logic handlers
    val factory = DecisionViewModelFactory(application, repository)
    val viewModel = ViewModelProvider(this, factory)[DecisionViewModel::class.java]

    setContent {
      MyApplicationTheme {
        TiebreakerAppScreen(viewModel = viewModel)
      }
    }
  }
}
