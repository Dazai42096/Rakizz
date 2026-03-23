package com.rakizz.student.presentation.quizzes.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.domain.model.Quiz
import com.rakizz.student.presentation.common.UiState
import com.rakizz.student.presentation.common.components.ErrorView
import com.rakizz.student.presentation.common.components.LoadingView

@Composable
fun QuizDetailScreen(
    quizId: String,
    viewModel: QuizDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(quizId) {
        viewModel.loadQuiz(quizId)
    }

    val state by viewModel.uiState.collectAsState()

    when (state) {
        is UiState.Loading -> LoadingView()
        is UiState.Error -> ErrorView(message = (state as UiState.Error).message)
        is UiState.Success -> {
            val quiz = (state as UiState.Success<Quiz>).data
            Column {
                Text(text = "Metadata Review Only")
                Text(text = "Title: ${quiz.title}")
                Text(text = "Description: ${quiz.description}")
                Text(text = "Score: ${quiz.score}")
            }
        }
        else -> {}
    }
}
