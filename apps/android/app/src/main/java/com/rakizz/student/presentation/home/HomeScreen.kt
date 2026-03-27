package com.rakizz.student.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onOpenMaterials: () -> Unit,
    onOpenAssignments: () -> Unit,
    onOpenQuizzes: () -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Rakizz Student",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Choose a section",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onOpenMaterials,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Materials")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onOpenAssignments,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Assignments")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onOpenQuizzes,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Quizzes")
            }
        }
    }
}