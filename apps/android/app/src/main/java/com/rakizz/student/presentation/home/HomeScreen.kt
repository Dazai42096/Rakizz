package com.rakizz.student.presentation.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun HomeScreen(rootNavController: NavHostController) {
    Scaffold { paddingValues ->
        Text(
            text = "Home Screen Scaffold (Materials, Assignments, Quizzes)",
            modifier = Modifier.padding(paddingValues)
        )
    }
}
