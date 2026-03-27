package com.rakizz.student.presentation.quizzes.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun QuizzesScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: QuizzesViewModel = hiltViewModel()
) {
    val isGenerating by viewModel.isGenerating.collectAsState()
    val generateMessage by viewModel.generateMessage.collectAsState()
    val generatedQuizId by viewModel.generatedQuizId.collectAsState()

    var materialId by remember { mutableStateOf("") }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Quizzes",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = materialId,
                onValueChange = {
                    materialId = it
                    viewModel.clearGenerateMessage()
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Material ID") },
                singleLine = true
            )

            Button(
                onClick = { viewModel.generateQuiz(materialId.trim()) },
                enabled = !isGenerating,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isGenerating) "Generating..." else "Generate Quiz")
            }

            if (generateMessage != null) {
                Text(
                    text = generateMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (generatedQuizId != null) {
                Button(
                    onClick = { onNavigateToDetail(generatedQuizId!!) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Generated Quiz")
                }
            }
        }
    }
}