package com.rakizz.student.presentation.assignments.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rakizz.student.domain.model.Assignment
import com.rakizz.student.presentation.navigation.NavRoutes
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun AssignmentsScreen(
    navController: NavController? = null,
    onBackClick: () -> Unit = {},
    onAddAssignmentClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onAssignmentClick: (String) -> Unit = {},
    onOpenAssignmentClick: (String) -> Unit = {},
    onRefreshClick: () -> Unit = {},
    viewModel: AssignmentsViewModel = hiltViewModel()
) {
    val colors = assignmentsScreenColors()
    val uiState by viewModel.uiState.collectAsState()

    val assignmentCards = uiState.assignments.map {
        it.toAssignmentCard()
    }

    val completedAssignments = assignmentCards.filter {
        it.status == AssignmentUiStatus.COMPLETED
    }

    val urgentAssignments = assignmentCards.filter {
        it.status == AssignmentUiStatus.OVERDUE || it.status == AssignmentUiStatus.DUE_SOON
    }

    val activeAssignments = assignmentCards.filter {
        it.status == AssignmentUiStatus.ACTIVE
    }

    val completedCount = completedAssignments.size
    val totalCount = assignmentCards.size
    val overallProgress = if (totalCount == 0) {
        0
    } else {
        ((completedCount.toDouble() / totalCount.toDouble()) * 100.0).toInt()
    }

    val visibleMessage = uiState.errorMessage ?: uiState.actionMessage
    val messageIsError = uiState.errorMessage != null

    val infiniteTransition = rememberInfiniteTransition(label = "assignments_animation")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "assignments_glow"
    )

    LaunchedEffect(visibleMessage) {
        if (!visibleMessage.isNullOrBlank()) {
            delay(2600)
            viewModel.clearMessages()
        }
    }

    fun openAddAssignment() {
        onAddAssignmentClick()
        onAddClick()

        if (navController != null) {
            navController.navigate(NavRoutes.AddAssignment.route) {
                launchSingleTop = true
            }
        }
    }

    fun goBack() {
        if (navController != null) {
            navController.popBackStack()
        } else {
            onBackClick()
        }
    }

    fun refreshAssignments() {
        viewModel.loadAssignments(showBlockingLoader = false)
        onRefreshClick()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        colors.backgroundTop,
                        colors.backgroundMiddle,
                        colors.backgroundBottom
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.TopEnd)
                .offset(x = 90.dp, y = (-110).dp)
                .graphicsLayer {
                    scaleX = glowScale
                    scaleY = glowScale
                    alpha = 0.85f
                }
                .background(
                    brush = Brush.radialGradient(
                        listOf(
                            colors.primary.copy(alpha = 0.38f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(horizontal = 20.dp)
                .padding(
                    bottom = WindowInsets.navigationBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                )
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            AssignmentsTopBar(
                colors = colors,
                onBackClick = { goBack() },
                onRefreshClick = { refreshAssignments() }
            )

            Spacer(modifier = Modifier.height(22.dp))

            AssignmentsHeroCard(
                overallProgress = overallProgress,
                completedCount = completedCount,
                totalCount = totalCount,
                colors = colors
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = !visibleMessage.isNullOrBlank()) {
                MessageCard(
                    message = visibleMessage.orEmpty(),
                    isError = messageIsError,
                    colors = colors
                )
            }

            if (!visibleMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.isLoading) {
                LoadingAssignmentsCard(colors = colors)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AssignmentSummaryCard(
                    title = "Needs Attention",
                    value = urgentAssignments.size.toString(),
                    subtitle = "due or overdue",
                    iconText = "!",
                    mainColor = colors.warning,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                AssignmentSummaryCard(
                    title = "Active",
                    value = activeAssignments.size.toString(),
                    subtitle = "pending tasks",
                    iconText = "A",
                    mainColor = colors.primary,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                AssignmentSummaryCard(
                    title = "Done",
                    value = completedCount.toString(),
                    subtitle = "completed",
                    iconText = "✓",
                    mainColor = colors.success,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AddAssignmentButton(
                colors = colors,
                onClick = { openAddAssignment() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!uiState.isLoading && assignmentCards.isEmpty()) {
                EmptyAssignmentsState(
                    colors = colors,
                    onAddClick = { openAddAssignment() }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            AssignmentsSection(
                title = "Needs Attention",
                subtitle = "Real assignments with close or missed deadlines",
                assignments = urgentAssignments,
                emptyText = "No urgent assignments right now.",
                colors = colors,
                onAssignmentClick = {
                    onAssignmentClick(it.id)
                    onOpenAssignmentClick(it.id)
                },
                onToggleComplete = {
                    viewModel.markAssignmentCompleted(it.id)
                },
                onDelete = {
                    viewModel.deleteAssignment(it.id)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AssignmentsSection(
                title = "Active",
                subtitle = "Pending assignments loaded from backend",
                assignments = activeAssignments,
                emptyText = "No active assignments.",
                colors = colors,
                onAssignmentClick = {
                    onAssignmentClick(it.id)
                    onOpenAssignmentClick(it.id)
                },
                onToggleComplete = {
                    viewModel.markAssignmentCompleted(it.id)
                },
                onDelete = {
                    viewModel.deleteAssignment(it.id)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AssignmentsSection(
                title = "Completed",
                subtitle = "Assignments marked completed in the backend",
                assignments = completedAssignments,
                emptyText = "Completed assignments will appear here.",
                colors = colors,
                onAssignmentClick = {
                    onAssignmentClick(it.id)
                    onOpenAssignmentClick(it.id)
                },
                onToggleComplete = {
                    viewModel.reopenAssignment(it.id)
                },
                onDelete = {
                    viewModel.deleteAssignment(it.id)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DeploymentInfoCard(colors = colors)

            Spacer(modifier = Modifier.height(28.dp))
        }

        if (uiState.isUpdating) {
            UpdatingOverlay(colors = colors)
        }
    }
}

@Composable
fun AssignmentsListScreen(
    navController: NavController? = null,
    onBackClick: () -> Unit = {},
    onAddAssignmentClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onAssignmentClick: (String) -> Unit = {},
    onOpenAssignmentClick: (String) -> Unit = {},
    onRefreshClick: () -> Unit = {}
) {
    AssignmentsScreen(
        navController = navController,
        onBackClick = onBackClick,
        onAddAssignmentClick = onAddAssignmentClick,
        onAddClick = onAddClick,
        onAssignmentClick = onAssignmentClick,
        onOpenAssignmentClick = onOpenAssignmentClick,
        onRefreshClick = onRefreshClick
    )
}

@Composable
private fun AssignmentsTopBar(
    colors: AssignmentsScreenColors,
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleIconButton(
            text = "<",
            colors = colors,
            onClick = onBackClick
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp)
        ) {
            Text(
                text = "Assignments",
                color = colors.textPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Real homework, deadlines, and completion tracking",
                color = colors.textSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        CircleIconButton(
            text = "R",
            colors = colors,
            onClick = onRefreshClick
        )
    }
}

@Composable
private fun AssignmentsHeroCard(
    overallProgress: Int,
    completedCount: Int,
    totalCount: Int,
    colors: AssignmentsScreenColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            colors.primary.copy(alpha = 0.28f),
                            colors.card,
                            colors.cardAlt
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = colors.success.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, colors.success.copy(alpha = 0.32f))
            ) {
                Text(
                    text = "Backend assignment data",
                    color = colors.success,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Stay ahead of every real assignment.",
                color = colors.textPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 31.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (totalCount == 0) {
                    "No assignments are stored yet. Add your first assignment to start tracking progress."
                } else {
                    "Rakizz calculates progress from assignments saved in the backend."
                },
                color = colors.textSecondary,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            AssignmentProgressBar(
                progress = overallProgress,
                colors = colors
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "$completedCount of $totalCount assignments completed",
                color = colors.textSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun AssignmentProgressBar(
    progress: Int,
    colors: AssignmentsScreenColors
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Overall progress",
                color = colors.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "$progress%",
                color = colors.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(colors.track)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0, 100) / 100f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                colors.primary,
                                colors.accent
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun AssignmentSummaryCard(
    title: String,
    value: String,
    subtitle: String,
    iconText: String,
    mainColor: Color,
    colors: AssignmentsScreenColors,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardAlt),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(mainColor.copy(alpha = 0.16f))
                    .border(1.dp, mainColor.copy(alpha = 0.30f), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iconText,
                    color = mainColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                color = mainColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )

            Text(
                text = subtitle,
                color = colors.textSecondary,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
private fun AddAssignmentButton(
    colors: AssignmentsScreenColors,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        colors.primary,
                        colors.accent
                    )
                )
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+ Add New Assignment",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun LoadingAssignmentsCard(colors: AssignmentsScreenColors) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                color = colors.primary,
                modifier = Modifier.size(28.dp),
                strokeWidth = 3.dp
            )

            Text(
                text = "Loading assignments from backend...",
                color = colors.textSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

@Composable
private fun AssignmentsSection(
    title: String,
    subtitle: String,
    assignments: List<AssignmentUiItem>,
    emptyText: String,
    colors: AssignmentsScreenColors,
    onAssignmentClick: (AssignmentUiItem) -> Unit,
    onToggleComplete: (AssignmentUiItem) -> Unit,
    onDelete: (AssignmentUiItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = title,
                subtitle = subtitle,
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedVisibility(visible = assignments.isEmpty()) {
                EmptyAssignmentsBox(
                    text = emptyText,
                    colors = colors
                )
            }

            assignments.forEachIndexed { index, assignment ->
                AssignmentCard(
                    assignment = assignment,
                    colors = colors,
                    onClick = { onAssignmentClick(assignment) },
                    onToggleComplete = { onToggleComplete(assignment) },
                    onDelete = { onDelete(assignment) }
                )

                if (index != assignments.lastIndex) {
                    HorizontalDivider(
                        color = colors.border,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AssignmentCard(
    assignment: AssignmentUiItem,
    colors: AssignmentsScreenColors,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (assignment.status) {
        AssignmentUiStatus.DUE_SOON -> colors.warning
        AssignmentUiStatus.ACTIVE -> colors.primary
        AssignmentUiStatus.COMPLETED -> colors.success
        AssignmentUiStatus.OVERDUE -> colors.danger
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(statusColor.copy(alpha = 0.15f))
                .border(1.dp, statusColor.copy(alpha = 0.32f), RoundedCornerShape(17.dp))
                .clickable(onClick = onToggleComplete),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (assignment.status == AssignmentUiStatus.COMPLETED) "✓" else "○",
                color = statusColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 13.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = assignment.statusLabel,
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.weight(1f)
                )

                StatusPill(
                    text = assignment.dueLabel,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = assignment.title,
                color = colors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = assignment.description.ifBlank { "No description provided." },
                color = colors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SmallActionText(
                    text = if (assignment.status == AssignmentUiStatus.COMPLETED) {
                        "Reopen"
                    } else {
                        "Mark Done"
                    },
                    color = statusColor,
                    onClick = onToggleComplete
                )

                SmallActionText(
                    text = "Delete",
                    color = colors.danger,
                    onClick = onDelete
                )
            }
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = color.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.28f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun SmallActionText(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.28f)),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun EmptyAssignmentsBox(
    text: String,
    colors: AssignmentsScreenColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardAlt,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Text(
            text = text,
            color = colors.textSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(18.dp),
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun EmptyAssignmentsState(
    colors: AssignmentsScreenColors,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No assignments yet",
                color = colors.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Create a real assignment and it will be saved to the backend.",
                color = colors.textSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            AddAssignmentButton(
                colors = colors,
                onClick = onAddClick
            )
        }
    }
}

@Composable
private fun MessageCard(
    message: String,
    isError: Boolean,
    colors: AssignmentsScreenColors
) {
    val color = if (isError) colors.danger else colors.success

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = color.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.32f))
    ) {
        Text(
            text = message,
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(14.dp)
        )
    }
}

@Composable
private fun UpdatingOverlay(colors: AssignmentsScreenColors) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundBottom.copy(alpha = 0.88f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = colors.card),
            border = BorderStroke(1.dp, colors.border)
        ) {
            Column(
                modifier = Modifier.padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = colors.primary)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Updating assignment...",
                    color = colors.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun DeploymentInfoCard(colors: AssignmentsScreenColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.success.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, colors.success.copy(alpha = 0.32f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Deployment behavior",
                color = colors.success,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "This screen shows only assignments stored in the backend. If there are no records, Rakizz shows an empty state instead of fake tasks.",
                color = colors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String,
    colors: AssignmentsScreenColors
) {
    Column {
        Text(
            text = title,
            color = colors.textPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = subtitle,
            color = colors.textSecondary,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun CircleIconButton(
    text: String,
    colors: AssignmentsScreenColors,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(colors.card)
            .border(1.dp, colors.border, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = colors.textPrimary,
            fontSize = 19.sp,
            fontWeight = FontWeight.Black
        )
    }
}

private fun Assignment.toAssignmentCard(): AssignmentUiItem {
    val dueInstant = parseAssignmentInstant(dueAtRaw)
    val now = Instant.now()
    val completed = isCompleted

    val status = when {
        completed -> AssignmentUiStatus.COMPLETED
        dueInstant != null && dueInstant.isBefore(now) -> AssignmentUiStatus.OVERDUE
        dueInstant != null && Duration.between(now, dueInstant).toHours() <= 48 -> AssignmentUiStatus.DUE_SOON
        else -> AssignmentUiStatus.ACTIVE
    }

    return AssignmentUiItem(
        id = id,
        title = title.ifBlank { "Untitled assignment" },
        description = description,
        dueLabel = buildDueLabel(
            status = status,
            dueInstant = dueInstant,
            fallback = dueAtDisplay
        ),
        status = status,
        statusLabel = when (status) {
            AssignmentUiStatus.OVERDUE -> "Overdue"
            AssignmentUiStatus.DUE_SOON -> "Due Soon"
            AssignmentUiStatus.ACTIVE -> "Active"
            AssignmentUiStatus.COMPLETED -> "Completed"
        }
    )
}

private fun buildDueLabel(
    status: AssignmentUiStatus,
    dueInstant: Instant?,
    fallback: String
): String {
    if (status == AssignmentUiStatus.COMPLETED) {
        return "Completed"
    }

    if (dueInstant == null) {
        return fallback
    }

    val zone = ZoneId.systemDefault()
    val dueDate = dueInstant.atZone(zone).toLocalDate()
    val today = LocalDate.now(zone)

    return when {
        status == AssignmentUiStatus.OVERDUE -> "Overdue"
        dueDate == today -> "Today"
        dueDate == today.plusDays(1) -> "Tomorrow"
        else -> {
            DateTimeFormatter.ofPattern(
                "MMM d",
                Locale.getDefault()
            ).format(dueInstant.atZone(zone))
        }
    }
}

private fun parseAssignmentInstant(raw: String): Instant? {
    val value = raw.trim()
    if (value.isBlank()) return null

    try {
        return Instant.parse(value)
    } catch (_: DateTimeParseException) {
    }

    try {
        return OffsetDateTime.parse(value).toInstant()
    } catch (_: DateTimeParseException) {
    }

    try {
        return ZonedDateTime.parse(value).toInstant()
    } catch (_: DateTimeParseException) {
    }

    try {
        return LocalDateTime.parse(value)
            .atZone(ZoneId.systemDefault())
            .toInstant()
    } catch (_: DateTimeParseException) {
    }

    try {
        return LocalDate.parse(value)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
    } catch (_: DateTimeParseException) {
    }

    return null
}

@Composable
private fun assignmentsScreenColors(): AssignmentsScreenColors {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    return if (isDark) {
        AssignmentsScreenColors(
            backgroundTop = Color(0xFF020617),
            backgroundMiddle = Color(0xFF07111F),
            backgroundBottom = Color(0xFF000000),
            card = Color(0xE60B1220),
            cardAlt = Color(0xCC111A2E),
            track = Color(0x6623344F),
            border = Color(0x334D9DFF),
            primary = Color(0xFF2F80FF),
            accent = Color(0xFF00D4FF),
            success = Color(0xFF22C55E),
            warning = Color(0xFFF59E0B),
            danger = Color(0xFFEF4444),
            textPrimary = Color.White,
            textSecondary = Color(0xFF94A3B8)
        )
    } else {
        AssignmentsScreenColors(
            backgroundTop = Color(0xFFF8FBFF),
            backgroundMiddle = Color(0xFFEAF4FF),
            backgroundBottom = Color(0xFFFFFFFF),
            card = Color(0xFFFFFFFF),
            cardAlt = Color(0xFFF1F7FF),
            track = Color(0xFFE2E8F0),
            border = Color(0x263B82F6),
            primary = Color(0xFF2563EB),
            accent = Color(0xFF06B6D4),
            success = Color(0xFF16A34A),
            warning = Color(0xFFD97706),
            danger = Color(0xFFDC2626),
            textPrimary = Color(0xFF0F172A),
            textSecondary = Color(0xFF64748B)
        )
    }
}

private enum class AssignmentUiStatus {
    DUE_SOON,
    ACTIVE,
    COMPLETED,
    OVERDUE
}

private data class AssignmentUiItem(
    val id: String,
    val title: String,
    val description: String,
    val dueLabel: String,
    val status: AssignmentUiStatus,
    val statusLabel: String
)

private data class AssignmentsScreenColors(
    val backgroundTop: Color,
    val backgroundMiddle: Color,
    val backgroundBottom: Color,
    val card: Color,
    val cardAlt: Color,
    val track: Color,
    val border: Color,
    val primary: Color,
    val accent: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val textPrimary: Color,
    val textSecondary: Color
)