package com.rakizz.student.presentation.quizzes.list

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.domain.model.Quiz
import com.rakizz.student.presentation.common.UiState
import com.rakizz.student.presentation.common.components.EmptyView
import com.rakizz.student.presentation.common.components.ErrorView
import com.rakizz.student.presentation.common.components.LoadingView

@Composable
fun QuizzesScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: QuizzesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    when (state) {
        is UiState.Loading -> LoadingView()
        is UiState.Empty -> EmptyView()
        is UiState.Error -> ErrorView(message = (state as UiState.Error).message)
        is UiState.Success -> {
            val quizzes = (state as UiState.Success<List<Quiz>>).data
            LazyColumn {
                items(quizzes) { quiz ->
                    Text(text = "${quiz.title} (Score: ${quiz.score})")
                }
            }
        }
    }
}
