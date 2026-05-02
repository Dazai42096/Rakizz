package com.rakizz.student.presentation.assignments.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.rakizz.student.domain.model.Assignment
import com.rakizz.student.presentation.navigation.NavRoutes
import com.rakizz.student.presentation.theme.RakizzColors
import androidx.compose.ui.text.style.TextAlign

@Composable
fun AssignmentsScreen(
    navController: NavController,
    viewModel: AssignmentsViewModel = hiltViewModel()
) {
    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val savedStateHandle = currentBackStackEntry?.savedStateHandle

    val assignmentCreatedFlow = savedStateHandle?.getStateFlow(
        key = "assignment_created",
        initialValue = false
    )

    val assignmentCreated = assignmentCreatedFlow
        ?.collectAsState()
        ?.value ?: false

    val assignmentSaveMessageFlow = savedStateHandle?.getStateFlow<String?>(
        key = "assignment_save_message",
        initialValue = null
    )

    val assignmentSaveMessage = assignmentSaveMessageFlow
        ?.collectAsState()
        ?.value

    LaunchedEffect(assignmentCreated) {
        if (assignmentCreated) {
            // reload after adding a new assignment
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

    AssignmentsScreenContent(
        navController = navController,
        snackbarHostState = snackbarHostState,
        viewModel = viewModel
    )
}

@Composable
private fun AssignmentsScreenContent(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    viewModel: AssignmentsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = RakizzColors.Background,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(NavRoutes.AddAssignment.route)
                },
                containerColor = RakizzColors.Primary,
                contentColor = RakizzColors.White,
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add assignment"
                )
            }
        },
        bottomBar = {
            AssignmentsBottomBar(
                onOpenHome = {
                    navController.navigate(NavRoutes.Home.route) {
                        launchSingleTop = true
                    }
                },
                onOpenLibrary = {
                    navController.navigate(NavRoutes.MaterialsList.route) {
                        launchSingleTop = true
                    }
                },
                onOpenQuizzes = {
                    navController.navigate(NavRoutes.QuizzesList.route) {
                        launchSingleTop = true
                    }
                },
                onOpenFocus = {
                    navController.navigate(NavRoutes.Focus.route) {
                        launchSingleTop = true
                    }
                },
                onOpenProfile = {
                    navController.navigate(NavRoutes.Profile.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            RakizzColors.Background,
                            RakizzColors.BackgroundSoft
                        )
                    )
                )
                .padding(paddingValues)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .statusBarsPadding(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 14.dp,
                bottom = 110.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TopBar(
                    onRefreshClick = {
                        viewModel.loadAssignments()
                    }
                )
            }

            item {
                AssignmentsMainCard(
                    onAddClick = {
                        navController.navigate(NavRoutes.AddAssignment.route)
                    }
                )
            }

            item {
                AssignmentSummaryCard(
                    assignments = uiState.assignments
                )
            }

            item {
                SectionTitle(
                    title = "Homework list",
                    subtitle = if (uiState.assignments.isEmpty()) {
                        "No assignments saved yet"
                    } else {
                        "${uiState.assignments.size} assignment(s) saved"
                    }
                )
            }

            when {
                uiState.isLoading && uiState.assignments.isEmpty() -> {
                    items(4) {
                        LoadingAssignmentCard()
                    }
                }

                uiState.errorMessage != null && uiState.assignments.isEmpty() -> {
                    item {
                        ErrorCard(
                            message = uiState.errorMessage,
                            onRetry = {
                                viewModel.loadAssignments()
                            }
                        )
                    }
                }

                uiState.assignments.isEmpty() -> {
                    item {
                        EmptyAssignmentsCard(
                            onAddClick = {
                                navController.navigate(NavRoutes.AddAssignment.route)
                            }
                        )
                    }
                }

                else -> {
                    if (uiState.errorMessage != null) {
                        item {
                            MessageCard(
                                title = "Warning",
                                message = uiState.errorMessage,
                                color = RakizzColors.Warning
                            )
                        }
                    }

                    items(
                        items = uiState.assignments,
                        key = { assignment -> assignment.id }
                    ) { assignment ->
                        AssignmentCard(
                            assignment = assignment
                        )
                    }
                }
            }

            item {
                CommitteeNoteCard()
            }
        }
    }
}

@Composable
private fun TopBar(
    onRefreshClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Assignments",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Homework, due dates, and reminders.",
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        FilledIconButton(
            onClick = onRefreshClick,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = RakizzColors.PrimarySoft,
                contentColor = RakizzColors.Primary
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Refresh"
            )
        }
    }
}

@Composable
private fun AssignmentsMainCard(
    onAddClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = RakizzColors.Primary,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            RakizzColors.Primary,
                            RakizzColors.PrimaryDark
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Text(
                text = "Stay ahead of deadlines",
                color = RakizzColors.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Rakizz stores assignments with due date, due time, and local reminder support.",
                color = RakizzColors.White.copy(alpha = 0.88f),
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RakizzColors.White,
                    contentColor = RakizzColors.PrimaryDark
                )
            ) {
                Text(
                    text = "Add Assignment",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun AssignmentSummaryCard(
    assignments: List<Assignment>
) {
    val pendingCount = assignments.count { assignment ->
        !assignment.isCompleted
    }

    val completedCount = assignments.count { assignment ->
        assignment.isCompleted
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryItem(
                modifier = Modifier.weight(1f),
                value = assignments.size.toString(),
                label = "Total"
            )

            SummaryItem(
                modifier = Modifier.weight(1f),
                value = pendingCount.toString(),
                label = "Pending"
            )

            SummaryItem(
                modifier = Modifier.weight(1f),
                value = completedCount.toString(),
                label = "Done"
            )
        }
    }
}

@Composable
private fun SummaryItem(
    modifier: Modifier,
    value: String,
    label: String
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = RakizzColors.Primary,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            color = RakizzColors.TextSecond,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AssignmentCard(
    assignment: Assignment
) {
    val statusColor = if (assignment.isCompleted) {
        RakizzColors.Success
    } else {
        RakizzColors.Warning
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBubble(
                    icon = if (assignment.isCompleted) {
                        Icons.Filled.CheckCircle
                    } else {
                        Icons.Filled.HourglassBottom
                    },
                    color = statusColor
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = assignment.title,
                        color = RakizzColors.TextMain,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = if (assignment.isCompleted) {
                            "Completed"
                        } else {
                            "Pending"
                        },
                        color = statusColor,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (assignment.description.isNotBlank()) {
                InfoBox(
                    label = "Description",
                    value = assignment.description
                )
            }

            InfoBox(
                label = "Due date",
                value = assignment.dueAtDisplay
            )
        }
    }
}

@Composable
private fun InfoBox(
    label: String,
    value: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = RakizzColors.CardSoft,
        border = androidx.compose.foundation.BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = label.uppercase(),
                color = RakizzColors.TextMuted,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = value,
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String
) {
    Column {
        Text(
            text = title,
            color = RakizzColors.TextMain,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            color = RakizzColors.TextSecond,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun EmptyAssignmentsCard(
    onAddClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconBubble(
                icon = Icons.Filled.Assignment,
                color = RakizzColors.Primary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "No assignments yet",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Add your homework with a due date and Rakizz will save it for later.",
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RakizzColors.Primary,
                    contentColor = RakizzColors.White
                )
            ) {
                Text(
                    text = "Add Assignment",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun LoadingAssignmentCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = RakizzColors.Card,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                color = RakizzColors.Primary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(30.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = "Loading assignments...",
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ErrorCard(
    message: String?,
    onRetry: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, RakizzColors.Error.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Could not load assignments",
                color = RakizzColors.Error,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message ?: "Try again.",
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RakizzColors.Primary,
                    contentColor = RakizzColors.White
                )
            ) {
                Text(
                    text = "Retry",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun MessageCard(
    title: String,
    message: String?,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = RakizzColors.Card,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                color = color,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = message ?: "",
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun CommitteeNoteCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = RakizzColors.PrimarySoft,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Checkpoint explanation",
                color = RakizzColors.PrimaryDark,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "This screen proves that Rakizz tracks homework and deadlines. When an assignment is saved, it persists in the backend and can also trigger a local reminder on the device.",
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

@Composable
private fun IconBubble(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Surface(
        modifier = Modifier.size(42.dp),
        shape = CircleShape,
        color = color.copy(alpha = 0.13f)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun AssignmentsBottomBar(
    onOpenHome: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenQuizzes: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Surface(
        color = RakizzColors.Card,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .border(1.dp, RakizzColors.CardBorder)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomItem("Home", false, onOpenHome)
            BottomItem("Library", false, onOpenLibrary)
            BottomItem("Quizzes", false, onOpenQuizzes)
            BottomItem("Focus", false, onOpenFocus)
            BottomItem("Profile", false, onOpenProfile)
        }
    }
}

@Composable
private fun BottomItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val textColor = if (selected) {
        RakizzColors.Primary
    } else {
        RakizzColors.TextMuted
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable {
                onClick()
            }
            .padding(horizontal = 9.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(if (selected) 8.dp else 6.dp)
                .clip(CircleShape)
                .background(textColor)
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}