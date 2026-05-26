package com.rakizz.student.presentation.progress

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.rakizz.student.domain.model.Assignment
import com.rakizz.student.domain.model.FocusPolicy
import com.rakizz.student.domain.model.Quiz
import kotlinx.coroutines.delay

@Composable
fun StudentProgressScreen(
    viewModel: StudentProgressViewModel? = null,
    onBackClick: () -> Unit = {},
    onRefreshClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {},
    onOpenHome: () -> Unit = {},
    onOpenMaterials: () -> Unit = {},
    onOpenQuizzes: () -> Unit = {},
    onOpenAssignments: () -> Unit = {},
    onOpenFocus: () -> Unit = {},
    onOpenProfile: () -> Unit = {}
) {
    val colors = progressColors()
    val progressViewModel = viewModel ?: hiltViewModel()
    val uiState by progressViewModel.uiState.collectAsState()

    var selectedTab by remember { mutableStateOf(ProgressTab.OVERVIEW) }
    var visibleMessage by remember { mutableStateOf("") }
    var messageIsError by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "progress_animation")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progress_glow"
    )

    LaunchedEffect(Unit) {
        progressViewModel.loadProgress()
    }

    LaunchedEffect(uiState.actionMessage) {
        val message = uiState.actionMessage

        if (!message.isNullOrBlank()) {
            visibleMessage = message
            messageIsError = false
            progressViewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage

        if (!message.isNullOrBlank()) {
            visibleMessage = cleanProgressMessage(message)
            messageIsError = true
            progressViewModel.clearMessages()
        }
    }

    LaunchedEffect(visibleMessage) {
        if (visibleMessage.isNotBlank()) {
            delay(2600)
            visibleMessage = ""
            messageIsError = false
        }
    }

    fun refreshProgress() {
        visibleMessage = "Refreshing progress from backend..."
        messageIsError = false
        progressViewModel.refreshProgress()
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
                .size(300.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-135).dp)
                .graphicsLayer {
                    scaleX = glowScale
                    scaleY = glowScale
                    alpha = 0.9f
                }
                .background(
                    brush = Brush.radialGradient(
                        listOf(
                            colors.primary.copy(alpha = 0.42f),
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

            ProgressTopBar(
                colors = colors,
                onBackClick = onBackClick,
                onRefreshClick = { refreshProgress() }
            )

            Spacer(modifier = Modifier.height(22.dp))

            ProgressHeroCard(
                summary = uiState.summary,
                colors = colors
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = visibleMessage.isNotBlank()) {
                ProgressMessageCard(
                    message = visibleMessage,
                    isError = messageIsError,
                    colors = colors
                )
            }

            if (visibleMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.isLoading) {
                LoadingProgressCard(colors = colors)

                Spacer(modifier = Modifier.height(16.dp))
            }

            ProgressStatsRow(
                summary = uiState.summary,
                colors = colors
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProgressTabsCard(
                selectedTab = selectedTab,
                colors = colors,
                onTabSelected = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                ProgressTab.OVERVIEW -> {
                    OverviewProgressCard(
                        summary = uiState.summary,
                        colors = colors,
                        onOpenQuizzes = onOpenQuizzes,
                        onOpenAssignments = onOpenAssignments,
                        onOpenFocus = onOpenFocus
                    )
                }

                ProgressTab.QUIZZES -> {
                    QuizProgressCard(
                        quizzes = uiState.quizzes,
                        colors = colors,
                        onOpenQuizzes = onOpenQuizzes
                    )
                }

                ProgressTab.ASSIGNMENTS -> {
                    AssignmentProgressCard(
                        assignments = uiState.assignments,
                        colors = colors,
                        onOpenAssignments = onOpenAssignments
                    )
                }

                ProgressTab.FOCUS -> {
                    FocusProgressCard(
                        policies = uiState.policies,
                        protectedApps = uiState.summary.protectedApps,
                        colors = colors,
                        onOpenFocus = onOpenFocus
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            HonestTrendCard(colors = colors)

            Spacer(modifier = Modifier.height(16.dp))

            ProgressNavigationCard(
                colors = colors,
                onOpenHome = onOpenHome,
                onOpenMaterials = onOpenMaterials,
                onOpenQuizzes = onOpenQuizzes,
                onOpenAssignments = onOpenAssignments,
                onOpenFocus = onOpenFocus,
                onOpenProfile = onOpenProfile
            )

            Spacer(modifier = Modifier.height(16.dp))

            ReportCard(
                colors = colors,
                onDownloadClick = onDownloadClick
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ProgressTopBar(
    colors: ProgressColors,
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
                text = "Student Progress",
                color = colors.textPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Real learning, assignment, and focus data",
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
private fun ProgressHeroCard(
    summary: StudentProgressSummary,
    colors: ProgressColors
) {
    val hasOverallScore = summary.overallScore != null

    val headline = if (hasOverallScore) {
        "${summary.overallScore}%"
    } else {
        "No score yet"
    }

    val description = if (hasOverallScore) {
        "Calculated from real quiz attempts and assignment completion."
    } else {
        "Complete quizzes and assignments to build a real progress score."
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            colors.primary.copy(alpha = 0.30f),
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
                    color = colors.success.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, colors.success.copy(alpha = 0.34f))
                ) {
                    Text(
                        text = "Backend progress data",
                        color = colors.success,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Current progress score",
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = headline,
                    color = colors.textPrimary,
                    fontSize = if (hasOverallScore) 46.sp else 34.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 42.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (hasOverallScore) {
                    MainProgressBar(
                        progress = summary.overallScore ?: 0,
                        colors = colors
                    )
                } else {
                    EmptyProgressNote(colors = colors)
                }
            }
        }
    }
}

@Composable
private fun EmptyProgressNote(colors: ProgressColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.warning.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, colors.warning.copy(alpha = 0.30f))
    ) {
        Text(
            text = "No fake analytics are shown. This section will update after real activity is recorded.",
            color = colors.warning,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(12.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MainProgressBar(
    progress: Int,
    colors: ProgressColors
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Real progress strength",
                color = colors.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "$progress%",
                color = colors.success,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        MiniProgressBar(
            progress = progress,
            color = colors.success,
            colors = colors,
            height = 12
        )
    }
}

@Composable
private fun ProgressMessageCard(
    message: String,
    isError: Boolean,
    colors: ProgressColors
) {
    val messageColor = if (isError) {
        colors.danger
    } else {
        colors.success
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = messageColor.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, messageColor.copy(alpha = 0.32f))
    ) {
        Text(
            text = message,
            color = messageColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(14.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LoadingProgressCard(colors: ProgressColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = colors.card,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Text(
            text = "Loading progress from Rakizz backend...",
            color = colors.textSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(18.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProgressStatsRow(
    summary: StudentProgressSummary,
    colors: ProgressColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ProgressStatCard(
            title = "Quizzes",
            value = summary.quizAverage?.let { "$it%" } ?: "No data",
            subtitle = "${summary.attemptedQuizCount}/${summary.totalQuizCount} attempted",
            mainColor = colors.primary,
            colors = colors,
            modifier = Modifier.weight(1f)
        )

        ProgressStatCard(
            title = "Tasks",
            value = summary.assignmentCompletion?.let { "$it%" } ?: "No data",
            subtitle = "${summary.completedAssignmentCount}/${summary.totalAssignmentCount} done",
            mainColor = colors.accent,
            colors = colors,
            modifier = Modifier.weight(1f)
        )

        ProgressStatCard(
            title = "Focus",
            value = "${summary.focusRuleCount}",
            subtitle = "parent rules",
            mainColor = if (summary.focusRuleCount > 0) colors.success else colors.warning,
            colors = colors,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ProgressStatCard(
    title: String,
    value: String,
    subtitle: String,
    mainColor: Color,
    colors: ProgressColors,
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
            Text(
                text = value,
                color = mainColor,
                fontSize = if (value.length > 6) 14.sp else 20.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

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
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun ProgressTabsCard(
    selectedTab: ProgressTab,
    colors: ProgressColors,
    onTabSelected: (ProgressTab) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "Progress Sections",
                subtitle = "Switch between real data categories",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProgressTab.entries.forEach { tab ->
                    ProgressTabChip(
                        tab = tab,
                        selected = selectedTab == tab,
                        colors = colors,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabSelected(tab) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressTabChip(
    tab: ProgressTab,
    selected: Boolean,
    colors: ProgressColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) colors.primary.copy(alpha = 0.18f) else colors.cardAlt,
        border = BorderStroke(
            1.dp,
            if (selected) colors.primary.copy(alpha = 0.45f) else colors.border
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = tab.label,
                color = if (selected) colors.primary else colors.textPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun OverviewProgressCard(
    summary: StudentProgressSummary,
    colors: ProgressColors,
    onOpenQuizzes: () -> Unit,
    onOpenAssignments: () -> Unit,
    onOpenFocus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "Overview",
                subtitle = "Only real backend data is shown here",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            OverviewRouteButton(
                title = "Quiz Performance",
                subtitle = if (summary.attemptedQuizCount > 0) {
                    "${summary.attemptedQuizCount} attempted quiz(es), ${summary.passedQuizCount} passed."
                } else {
                    "No completed quiz attempts yet."
                },
                value = summary.quizAverage?.let { "$it%" } ?: "No data",
                color = colors.primary,
                colors = colors,
                onClick = onOpenQuizzes
            )

            ItemDivider(colors)

            OverviewRouteButton(
                title = "Assignment Completion",
                subtitle = if (summary.totalAssignmentCount > 0) {
                    "${summary.completedAssignmentCount} of ${summary.totalAssignmentCount} assignment(s) completed."
                } else {
                    "No assignments created yet."
                },
                value = summary.assignmentCompletion?.let { "$it%" } ?: "No data",
                color = colors.accent,
                colors = colors,
                onClick = onOpenAssignments
            )

            ItemDivider(colors)

            OverviewRouteButton(
                title = "Focus Protection",
                subtitle = if (summary.focusRuleCount > 0) {
                    "${summary.focusRuleCount} parent rule(s), ${summary.protectedAppCount} protected app(s)."
                } else {
                    "No parent focus rules active yet."
                },
                value = "${summary.focusRuleCount} rules",
                color = if (summary.focusRuleCount > 0) colors.success else colors.warning,
                colors = colors,
                onClick = onOpenFocus
            )
        }
    }
}

@Composable
private fun OverviewRouteButton(
    title: String,
    subtitle: String,
    value: String,
    color: Color,
    colors: ProgressColors,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color.copy(alpha = 0.14f))
                .border(1.dp, color.copy(alpha = 0.30f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title.firstOrNull()?.uppercase() ?: "P",
                color = color,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = value,
                    color = color,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                color = colors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun QuizProgressCard(
    quizzes: List<Quiz>,
    colors: ProgressColors,
    onOpenQuizzes: () -> Unit
) {
    DataListCard(
        title = "Quiz Performance",
        subtitle = "Real quizzes loaded from backend",
        isEmpty = quizzes.isEmpty(),
        emptyText = "No quizzes found yet. Generate and solve a quiz from uploaded materials.",
        colors = colors,
        onActionClick = onOpenQuizzes
    ) {
        quizzes.forEachIndexed { index, quiz ->
            QuizRow(
                quiz = quiz,
                colors = colors,
                onClick = onOpenQuizzes
            )

            if (index != quizzes.lastIndex) {
                ItemDivider(colors)
            }
        }
    }
}

@Composable
private fun QuizRow(
    quiz: Quiz,
    colors: ProgressColors,
    onClick: () -> Unit
) {
    val score = quiz.score
    val statusColor = when {
        score == null -> colors.warning
        score >= 70 -> colors.success
        else -> colors.danger
    }

    val scoreText = score?.let { "$it%" } ?: "Not attempted"

    ProgressDataRow(
        title = quiz.title,
        subtitle = "${quiz.totalQuestions} question(s) - $scoreText",
        value = scoreText,
        color = statusColor,
        colors = colors,
        onClick = onClick
    )
}

@Composable
private fun AssignmentProgressCard(
    assignments: List<Assignment>,
    colors: ProgressColors,
    onOpenAssignments: () -> Unit
) {
    DataListCard(
        title = "Assignment Progress",
        subtitle = "Real assignments loaded from backend",
        isEmpty = assignments.isEmpty(),
        emptyText = "No assignments created yet.",
        colors = colors,
        onActionClick = onOpenAssignments
    ) {
        assignments.forEachIndexed { index, assignment ->
            AssignmentRow(
                assignment = assignment,
                colors = colors,
                onClick = onOpenAssignments
            )

            if (index != assignments.lastIndex) {
                ItemDivider(colors)
            }
        }
    }
}

@Composable
private fun AssignmentRow(
    assignment: Assignment,
    colors: ProgressColors,
    onClick: () -> Unit
) {
    val statusColor = if (assignment.isCompleted) {
        colors.success
    } else {
        colors.warning
    }

    val statusText = if (assignment.isCompleted) {
        "Completed"
    } else {
        assignment.status.ifBlank { "Pending" }
    }

    ProgressDataRow(
        title = assignment.title,
        subtitle = "Due: ${assignment.dueAtDisplay}",
        value = statusText,
        color = statusColor,
        colors = colors,
        onClick = onClick
    )
}

@Composable
private fun FocusProgressCard(
    policies: List<FocusPolicy>,
    protectedApps: List<String>,
    colors: ProgressColors,
    onOpenFocus: () -> Unit
) {
    DataListCard(
        title = "Focus Protection",
        subtitle = "Parent rules loaded from backend",
        isEmpty = policies.isEmpty(),
        emptyText = "No parent focus rules active yet.",
        colors = colors,
        onActionClick = onOpenFocus
    ) {
        policies.forEachIndexed { index, policy ->
            FocusPolicyRow(
                policy = policy,
                colors = colors,
                onClick = onOpenFocus
            )

            if (index != policies.lastIndex) {
                ItemDivider(colors)
            }
        }

        if (protectedApps.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Protected apps: ${protectedApps.joinToString(", ")}",
                color = colors.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun FocusPolicyRow(
    policy: FocusPolicy,
    colors: ProgressColors,
    onClick: () -> Unit
) {
    val timeText = when {
        !policy.startTime.isNullOrBlank() && !policy.endTime.isNullOrBlank() -> {
            "${policy.startTime} - ${policy.endTime}"
        }

        policy.dailyLimitMinutes != null -> {
            "${policy.dailyLimitMinutes} minute limit"
        }

        else -> {
            "Parent-controlled rule"
        }
    }

    ProgressDataRow(
        title = policy.ruleType.ifBlank { "Focus Rule" },
        subtitle = timeText,
        value = "Active",
        color = colors.success,
        colors = colors,
        onClick = onClick
    )
}

@Composable
private fun DataListCard(
    title: String,
    subtitle: String,
    isEmpty: Boolean,
    emptyText: String,
    colors: ProgressColors,
    onActionClick: () -> Unit,
    content: @Composable () -> Unit
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

            if (isEmpty) {
                EmptySectionText(
                    text = emptyText,
                    colors = colors
                )
            } else {
                content()
            }

            Spacer(modifier = Modifier.height(14.dp))

            MainGradientButton(
                text = "Open Related Screen",
                colors = colors,
                onClick = onActionClick
            )
        }
    }
}

@Composable
private fun ProgressDataRow(
    title: String,
    subtitle: String,
    value: String,
    color: Color,
    colors: ProgressColors,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(color.copy(alpha = 0.14f))
                .border(1.dp, color.copy(alpha = 0.30f), RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title.firstOrNull()?.uppercase() ?: "P",
                color = color,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title.ifBlank { "Untitled" },
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.weight(1f),
                    lineHeight = 18.sp
                )

                Text(
                    text = value,
                    color = color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                color = colors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun EmptySectionText(
    text: String,
    colors: ProgressColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colors.cardAlt,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Text(
            text = text,
            color = colors.textSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(14.dp),
            textAlign = TextAlign.Center,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun MiniProgressBar(
    progress: Int,
    color: Color,
    colors: ProgressColors,
    height: Int = 8
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(colors.track)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0, 100) / 100f)
                .height(height.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(color)
        )
    }
}

@Composable
private fun HonestTrendCard(
    colors: ProgressColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "Weekly Trend",
                subtitle = "Hidden until dated activity exists",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            EmptySectionText(
                text = "Not enough dated quiz attempts, assignment updates, or usage events are available to calculate a real weekly trend yet.",
                colors = colors
            )
        }
    }
}

@Composable
private fun ProgressNavigationCard(
    colors: ProgressColors,
    onOpenHome: () -> Unit,
    onOpenMaterials: () -> Unit,
    onOpenQuizzes: () -> Unit,
    onOpenAssignments: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "Quick Navigation",
                subtitle = "Open related real app screens",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            SecondaryProgressButton("Home Dashboard", "Return to main dashboard", "H", colors, onOpenHome)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryProgressButton("Materials Library", "Open uploaded study materials", "M", colors, onOpenMaterials)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryProgressButton("AI Quizzes", "Open generated quiz list", "Q", colors, onOpenQuizzes)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryProgressButton("Assignments", "Open homework tracking", "A", colors, onOpenAssignments)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryProgressButton("Focus Shield", "Open parent-managed focus status", "F", colors, onOpenFocus)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryProgressButton("Profile", "Open account information", "P", colors, onOpenProfile)
        }
    }
}

@Composable
private fun ReportCard(
    colors: ProgressColors,
    onDownloadClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onDownloadClick),
        shape = RoundedCornerShape(24.dp),
        color = colors.primary.copy(alpha = 0.11f),
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.28f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Progress Report",
                color = colors.primary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Share a simple progress report using the available real activity shown on this screen.",
                color = colors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun MainGradientButton(
    text: String,
    colors: ProgressColors,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(colors.primary, colors.accent)
                )
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun SecondaryProgressButton(
    text: String,
    subtitle: String,
    iconText: String,
    colors: ProgressColors,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardAlt,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(colors.primary.copy(alpha = 0.14f))
                    .border(1.dp, colors.primary.copy(alpha = 0.28f), RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iconText,
                    color = colors.primary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = text,
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = subtitle,
                    color = colors.textSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }

            Text(
                text = ">",
                color = colors.textSecondary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String,
    colors: ProgressColors
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
    colors: ProgressColors,
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

@Composable
private fun ItemDivider(colors: ProgressColors) {
    HorizontalDivider(
        color = colors.border,
        thickness = 1.dp,
        modifier = Modifier.padding(start = 56.dp, top = 12.dp, bottom = 12.dp)
    )
}

private fun cleanProgressMessage(message: String): String {
    val clean = message.lowercase()

    return when {
        "401" in clean || "403" in clean || "unauthorized" in clean -> {
            "Your login session may have expired. Please login again."
        }

        "network" in clean ||
            "timeout" in clean ||
            "failed to connect" in clean ||
            "unable to resolve host" in clean -> {
            "Unable to reach Rakizz server. Check your connection and try again."
        }

        "500" in clean ||
            "502" in clean ||
            "503" in clean ||
            "504" in clean -> {
            "Rakizz server is temporarily unavailable. Please try again."
        }

        else -> {
            message.ifBlank { "Something went wrong. Please try again." }
        }
    }
}

@Composable
private fun progressColors(): ProgressColors {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    return if (isDark) {
        ProgressColors(
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
        ProgressColors(
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

private enum class ProgressTab(
    val label: String
) {
    OVERVIEW("Overview"),
    QUIZZES("Quizzes"),
    ASSIGNMENTS("Tasks"),
    FOCUS("Focus")
}

private data class ProgressColors(
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