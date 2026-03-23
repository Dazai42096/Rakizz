package com.rakizz.student.presentation.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.presentation.common.UiState
import com.rakizz.student.presentation.common.components.ErrorView
import com.rakizz.student.presentation.common.components.LoadingView

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state) {
        if (state is UiState.Success) {
            onLoginSuccess()
        }
    }

    when (state) {
        is UiState.Loading -> LoadingView()
        is UiState.Error -> ErrorView(message = (state as UiState.Error).message)
        else -> {
            Column {
                Text(text = "Login")
                Button(onClick = { viewModel.login("student@rakizz.com", "pass") }) {
                    Text("Login")
                }
            }
        }
    }
}
