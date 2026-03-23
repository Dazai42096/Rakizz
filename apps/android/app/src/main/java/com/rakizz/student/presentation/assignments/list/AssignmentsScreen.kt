package com.rakizz.student.presentation.assignments.list

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.domain.model.Assignment
import com.rakizz.student.presentation.common.UiState
import com.rakizz.student.presentation.common.components.EmptyView
import com.rakizz.student.presentation.common.components.ErrorView
import com.rakizz.student.presentation.common.components.LoadingView

@Composable
fun AssignmentsScreen(
    viewModel: AssignmentsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    when (state) {
        is UiState.Loading -> LoadingView()
        is UiState.Empty -> EmptyView()
        is UiState.Error -> ErrorView(message = (state as UiState.Error).message)
        is UiState.Success -> {
            val assignments = (state as UiState.Success<List<Assignment>>).data
            LazyColumn {
                items(assignments) { assignment ->
                    Text(text = "${assignment.title} - ${assignment.status}")
                }
            }
        }
    }
}
