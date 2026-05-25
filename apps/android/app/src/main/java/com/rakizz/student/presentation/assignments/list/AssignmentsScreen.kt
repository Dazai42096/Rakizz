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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
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
import androidx.navigation.NavController

@Composable
fun AssignmentsScreen(
    navController: NavController? = null,
    onBackClick: () -> Unit = {},
    onAddAssignmentClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onAssignmentClick: (String) -> Unit = {},
    onOpenAssignmentClick: (String) -> Unit = {},
    onRefreshClick: () -> Unit = {}
) {
    val colors = assignmentsScreenColors()

    // Default assignment data.
    // Later this can be replaced with ViewModel/API data.
    val assignments = remember {
        mutableStateListOf(
            AssignmentUiItem(
                id = "1",
                title = "AI Quiz Generation Report",
                subject = "Project Work",
                description = "Prepare a short explanation about how materials are converted into MCQ quizzes.",
                dueLabel = "Today",
                status = AssignmentStatus.DUE_SOON,
                progress = 78
            ),
            AssignmentUiItem(
                id = "2",
                title = "Database Mapping Review",
                subject = "Backend",
                description = "Check users, materials, quizzes, assignments, and parent-student links.",
                dueLabel = "Tomorrow",
                status = AssignmentStatus.IN_PROGRESS,
                progress = 55
            ),
            AssignmentUiItem(
                id = "3",
                title = "Functional Test Cases",
                subject = "Documentation",
                description = "Review passed test cases for the main Rakizz functional requirements.",
                dueLabel = "May 28",
                status = AssignmentStatus.IN_PROGRESS,
                progress = 40
            ),
            AssignmentUiItem(
                id = "4",
                title = "Quiz Unlock Flow",
                subject = "Focus Mode",
                description = "Practice explaining how a blocked app can be unlocked after passing a quiz.",
                dueLabel = "Completed",
                status = AssignmentStatus.COMPLETED,
                progress = 100
            ),
            AssignmentUiItem(
                id = "5",
                title = "Material Upload",
                subject = "Android",
                description = "Show the upload and material preview flow in the mobile app.",
                dueLabel = "Completed",
                status = AssignmentStatus.COMPLETED,
                progress = 100
            )
        )
    }

    val completedCount = assignments.count { it.status == AssignmentStatus.COMPLETED }
    val dueSoonCount = assignments.count { it.status == AssignmentStatus.DUE_SOON }
    val activeCount = assignments.count { it.status == AssignmentStatus.IN_PROGRESS }

    val overallProgress = if (assignments.isNotEmpty()) {
        assignments.sumOf { it.progress } / assignments.size
    } else {
        0
    }

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
        // Soft animated background glow.
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
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            AssignmentsTopBar(
                colors = colors,
                onBackClick = {
                    if (navController != null) {
                        navController.popBackStack()
                    } else {
                        onBackClick()
                    }
                },
                onRefreshClick = onRefreshClick
            )

            Spacer(modifier = Modifier.height(22.dp))

            AssignmentsHeroCard(
                overallProgress = overallProgress,
                completedCount = completedCount,
                totalCount = assignments.size,
                colors = colors
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AssignmentSummaryCard(
                    title = "Due Soon",
                    value = dueSoonCount.toString(),
                    subtitle = "needs attention",
                    iconText = "⚠",
                    mainColor = colors.warning,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                AssignmentSummaryCard(
                    title = "Active",
                    value = activeCount.toString(),
                    subtitle = "in progress",
                    iconText = "⏳",
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
                onClick = {
                    onAddAssignmentClick()
                    onAddClick()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AssignmentsSection(
                title = "Needs Attention",
                subtitle = "Assignments with close deadlines",
                assignments = assignments.filter { it.status == AssignmentStatus.DUE_SOON },
                emptyText = "No urgent assignments right now.",
                colors = colors,
                onAssignmentClick = {
                    onAssignmentClick(it.id)
                    onOpenAssignmentClick(it.id)
                },
                onToggleComplete = { selected ->
                    val index = assignments.indexOfFirst { it.id == selected.id }
                    if (index != -1) {
                        assignments[index] = selected.copy(
                            status = AssignmentStatus.COMPLETED,
                            dueLabel = "Completed",
                            progress = 100
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AssignmentsSection(
                title = "In Progress",
                subtitle = "Current study tasks and homework",
                assignments = assignments.filter { it.status == AssignmentStatus.IN_PROGRESS },
                emptyText = "No active assignments.",
                colors = colors,
                onAssignmentClick = {
                    onAssignmentClick(it.id)
                    onOpenAssignmentClick(it.id)
                },
                onToggleComplete = { selected ->
                    val index = assignments.indexOfFirst { it.id == selected.id }
                    if (index != -1) {
                        assignments[index] = selected.copy(
                            status = AssignmentStatus.COMPLETED,
                            dueLabel = "Completed",
                            progress = 100
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AssignmentsSection(
                title = "Completed",
                subtitle = "Finished tasks for progress tracking",
                assignments = assignments.filter { it.status == AssignmentStatus.COMPLETED },
                emptyText = "Completed assignments will appear here.",
                colors = colors,
                onAssignmentClick = {
                    onAssignmentClick(it.id)
                    onOpenAssignmentClick(it.id)
                },
                onToggleComplete = {}
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoCard(colors = colors)

            Spacer(modifier = Modifier.height(28.dp))
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
            text = "←",
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
                text = "Track homework, deadlines, and reminders",
                color = colors.textSecondary,
                fontSize = 13.sp
            )
        }

        CircleIconButton(
            text = "↻",
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
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
            Column {
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = colors.primary.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.32f))
                ) {
                    Text(
                        text = "● Smart task tracker",
                        color = colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Stay ahead of every assignment.",
                    color = colors.textPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 31.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Rakizz helps students organize homework, follow deadlines, and keep parents informed about progress.",
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
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = subtitle,
                color = colors.textSecondary,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
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
private fun AssignmentsSection(
    title: String,
    subtitle: String,
    assignments: List<AssignmentUiItem>,
    emptyText: String,
    colors: AssignmentsScreenColors,
    onAssignmentClick: (AssignmentUiItem) -> Unit,
    onToggleComplete: (AssignmentUiItem) -> Unit
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
                    onToggleComplete = { onToggleComplete(assignment) }
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
    onToggleComplete: () -> Unit
) {
    val statusColor = when (assignment.status) {
        AssignmentStatus.DUE_SOON -> colors.warning
        AssignmentStatus.IN_PROGRESS -> colors.primary
        AssignmentStatus.COMPLETED -> colors.success
        AssignmentStatus.OVERDUE -> colors.danger
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
                text = if (assignment.status == AssignmentStatus.COMPLETED) "✓" else "○",
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
                    text = assignment.subject,
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
                text = assignment.description,
                color = colors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            MiniProgressBar(
                progress = assignment.progress,
                color = statusColor,
                colors = colors
            )
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
private fun MiniProgressBar(
    progress: Int,
    color: Color,
    colors: AssignmentsScreenColors
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(colors.track)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0, 100) / 100f)
                .height(8.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(color)
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
            modifier = Modifier.padding(18.dp)
        )
    }
}

@Composable
private fun InfoCard(colors: AssignmentsScreenColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.success.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, colors.success.copy(alpha = 0.32f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "✓",
                color = colors.success,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )

            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = "How Rakizz works",
                    color = colors.success,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "This screen supports the requirement for tracking, organizing, and reminding students about homework and deadlines.",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
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
            fontSize = 12.sp
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
            fontSize = 21.sp,
            fontWeight = FontWeight.Black
        )
    }
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

private enum class AssignmentStatus {
    DUE_SOON,
    IN_PROGRESS,
    COMPLETED,
    OVERDUE
}

private data class AssignmentUiItem(
    val id: String,
    val title: String,
    val subject: String,
    val description: String,
    val dueLabel: String,
    val status: AssignmentStatus,
    val progress: Int
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