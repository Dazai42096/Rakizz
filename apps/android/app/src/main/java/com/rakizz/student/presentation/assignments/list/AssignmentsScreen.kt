package com.rakizz.student.presentation.assignments.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.rakizz.student.domain.model.Assignment
import com.rakizz.student.presentation.navigation.NavRoutes

@Composable
fun AssignmentsScreen(
    navController: NavController,
    viewModel: AssignmentsViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val savedStateHandle = currentBackStackEntry?.savedStateHandle

    val assignmentCreatedFlow = savedStateHandle?.getStateFlow("assignment_created", false)
    val assignmentCreated = assignmentCreatedFlow?.collectAsState()?.value ?: false

    val assignmentSaveMessageFlow =
        savedStateHandle?.getStateFlow<String?>("assignment_save_message", null)
    val assignmentSaveMessage = assignmentSaveMessageFlow?.collectAsState()?.value

    LaunchedEffect(assignmentCreated) {
        if (assignmentCreated) {
            viewModel.loadAssignments(showBlockingLoader = false)
            savedStateHandle?.set("assignment_created", false)
        }
    }

    LaunchedEffect(assignmentSaveMessage) {
        val message = assignmentSaveMessage?.trim().orEmpty()
        if (message.isNotEmpty()) {
            snackbarHostState.showSnackbar(message)
            savedStateHandle?.set("assignment_save_message", null)
        }
    }

    AssignmentsScreen(
        onAddAssignmentClick = { navController.navigate(NavRoutes.AddAssignment.route) },
        snackbarHostState = snackbarHostState,
        viewModel = viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentsScreen(
    onAddAssignmentClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: AssignmentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assignments") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAssignmentClick) {
                Text("+")
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { padding ->
        AssignmentsContent(
            padding = padding,
            uiState = uiState,
            onRetry = { viewModel.loadAssignments() }
        )
    }
}

@Composable
private fun AssignmentsContent(
    padding: PaddingValues,
    uiState: AssignmentsUiState,
    onRetry: () -> Unit
) {
    when {
        uiState.isLoading && uiState.assignments.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading assignments...")
            }
        }

        uiState.errorMessage != null && uiState.assignments.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }
        }

        uiState.assignments.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No assignments yet.",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Use the + button to create your first assignment.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.errorMessage != null) {
                    item {
                        Text(
                            text = uiState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                items(uiState.assignments, key = { it.id }) { assignment ->
                    AssignmentCard(assignment = assignment)
                }
            }
        }
    }
}

@Composable
private fun AssignmentCard(
    assignment: Assignment
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = assignment.title,
                style = MaterialTheme.typography.titleMedium
            )

            if (assignment.description.isNotBlank()) {
                Text(
                    text = assignment.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Due: ${assignment.dueAtDisplay}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = if (assignment.isCompleted) "Status: Completed" else "Status: Pending",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}