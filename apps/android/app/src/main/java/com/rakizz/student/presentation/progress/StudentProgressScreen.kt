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
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Suppress("UNUSED_PARAMETER")
@Composable
fun StudentProgressScreen(
    navController: NavController? = null,
    viewModel: Any? = null,

    // Back / refresh callbacks
    onBackClick: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onRefreshClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {},

    // Navigation callbacks
    onOpenHome: () -> Unit = {},
    onOpenLibrary: () -> Unit = {},
    onOpenMaterials: () -> Unit = {},
    onOpenQuizzes: () -> Unit = {},
    onOpenAssignments: () -> Unit = {},
    onOpenFocus: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenPairCode: () -> Unit = {},

    // Extra compatibility callbacks
    onViewQuizClick: () -> Unit = {},
    onViewAssignmentsClick: () -> Unit = {},
    onViewFocusClick: () -> Unit = {},
    onViewMaterialsClick: () -> Unit = {}
) {
    val colors = progressColors()

    var selectedTab by remember { mutableStateOf(ProgressTab.OVERVIEW) }
    var message by remember { mutableStateOf("") }

    val weeklyAccuracy = 88
    val focusScore = 76
    val assignmentScore = 82
    val overallScore = 84

    val quizItems = remember {
        listOf(
            ProgressItem("AI Quiz: Software Engineering", "15 questions • 92% score", 92, ProgressStatus.GOOD),
            ProgressItem("Database Practice", "12 questions • 86% score", 86, ProgressStatus.GOOD),
            ProgressItem("Android Compose Review", "20 questions • 78% score", 78, ProgressStatus.WARNING)
        )
    }

    val assignmentItems = remember {
        listOf(
            ProgressItem("Implementation Report", "Submitted • Passed", 100, ProgressStatus.GOOD),
            ProgressItem("Architecture Diagram", "Submitted • Reviewed", 90, ProgressStatus.GOOD),
            ProgressItem("Testing Document", "Due soon", 65, ProgressStatus.WARNING)
        )
    }

    val focusItems = remember {
        listOf(
            ProgressItem("Focus Mode Sessions", "8 hours this week", 80, ProgressStatus.GOOD),
            ProgressItem("Blocked App Attempts", "5 attempts controlled", 74, ProgressStatus.WARNING),
            ProgressItem("Quiz Unlock Success", "3 successful unlocks", 88, ProgressStatus.GOOD)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "progress_animation")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progress_glow"
    )

    LaunchedEffect(message) {
        if (message.isNotEmpty()) {
            delay(2300)
            message = ""
        }
    }

    fun goBack() {
        if (navController != null) {
            navController.popBackStack()
        } else {
            onBackClick()
            onNavigateBack()
        }
    }

    fun openMaterials() {
        onOpenLibrary()
        onOpenMaterials()
        onViewMaterialsClick()
    }

    fun openQuizzes() {
        onOpenQuizzes()
        onViewQuizClick()
    }

    fun openAssignments() {
        onOpenAssignments()
        onViewAssignmentsClick()
    }

    fun openFocus() {
        onOpenFocus()
        onViewFocusClick()
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
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            ProgressTopBar(
                colors = colors,
                onBackClick = { goBack() },
                onRefreshClick = {
                    message = "Progress refreshed for demo."
                    onRefreshClick()
                }
            )

            Spacer(modifier = Modifier.height(22.dp))

            ProgressHeroCard(
                overallScore = overallScore,
                weeklyAccuracy = weeklyAccuracy,
                focusScore = focusScore,
                assignmentScore = assignmentScore,
                colors = colors
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = message.isNotEmpty()) {
                ProgressMessageCard(
                    message = message,
                    colors = colors
                )
            }

            if (message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProgressStatCard(
                    title = "Quizzes",
                    value = "$weeklyAccuracy%",
                    subtitle = "accuracy",
                    iconText = "🧠",
                    mainColor = colors.primary,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                ProgressStatCard(
                    title = "Focus",
                    value = "$focusScore%",
                    subtitle = "shield",
                    iconText = "🛡",
                    mainColor = colors.success,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                ProgressStatCard(
                    title = "Tasks",
                    value = "$assignmentScore%",
                    subtitle = "done",
                    iconText = "✓",
                    mainColor = colors.accent,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }

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
                        colors = colors,
                        onOpenQuizzes = { openQuizzes() },
                        onOpenAssignments = { openAssignments() },
                        onOpenFocus = { openFocus() }
                    )
                }

                ProgressTab.QUIZZES -> {
                    ProgressListCard(
                        title = "Quiz Performance",
                        subtitle = "Scores from generated AI quizzes",
                        items = quizItems,
                        colors = colors,
                        onActionClick = { openQuizzes() }
                    )
                }

                ProgressTab.ASSIGNMENTS -> {
                    ProgressListCard(
                        title = "Assignment Progress",
                        subtitle = "Homework and deadline completion",
                        items = assignmentItems,
                        colors = colors,
                        onActionClick = { openAssignments() }
                    )
                }

                ProgressTab.FOCUS -> {
                    ProgressListCard(
                        title = "Focus Activity",
                        subtitle = "Blocking, unlocks, and focus behavior",
                        items = focusItems,
                        colors = colors,
                        onActionClick = { openFocus() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            WeeklyTrendCard(colors = colors)

            Spacer(modifier = Modifier.height(16.dp))

            ProgressNavigationCard(
                colors = colors,
                onOpenHome = onOpenHome,
                onOpenMaterials = { openMaterials() },
                onOpenQuizzes = { openQuizzes() },
                onOpenAssignments = { openAssignments() },
                onOpenFocus = { openFocus() },
                onOpenProfile = onOpenProfile,
                onOpenPairCode = onOpenPairCode
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProgressExplanationCard(colors = colors)

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun ProgressScreen(
    navController: NavController? = null,
    viewModel: Any? = null,
    onBackClick: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onRefreshClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {},
    onOpenHome: () -> Unit = {},
    onOpenLibrary: () -> Unit = {},
    onOpenMaterials: () -> Unit = {},
    onOpenQuizzes: () -> Unit = {},
    onOpenAssignments: () -> Unit = {},
    onOpenFocus: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenPairCode: () -> Unit = {},
    onViewQuizClick: () -> Unit = {},
    onViewAssignmentsClick: () -> Unit = {},
    onViewFocusClick: () -> Unit = {},
    onViewMaterialsClick: () -> Unit = {}
) {
    StudentProgressScreen(
        navController = navController,
        viewModel = viewModel,
        onBackClick = onBackClick,
        onNavigateBack = onNavigateBack,
        onRefreshClick = onRefreshClick,
        onOpenHome = onOpenHome,
        onOpenLibrary = onOpenLibrary,
        onOpenMaterials = onOpenMaterials,
        onOpenQuizzes = onOpenQuizzes,
        onOpenAssignments = onOpenAssignments,
        onOpenFocus = onOpenFocus,
        onOpenProfile = onOpenProfile,
        onOpenPairCode = onOpenPairCode,
        onViewQuizClick = onViewQuizClick,
        onViewAssignmentsClick = onViewAssignmentsClick,
        onViewFocusClick = onViewFocusClick,
        onViewMaterialsClick = onViewMaterialsClick
    )
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
                text = "Student Progress",
                color = colors.textPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Learning, focus, and assignment analytics",
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
private fun ProgressHeroCard(
    overallScore: Int,
    weeklyAccuracy: Int,
    focusScore: Int,
    assignmentScore: Int,
    colors: ProgressColors
) {
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
                        text = "● Progress dashboard active",
                        color = colors.success,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Overall learning score",
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$overallScore%",
                    color = colors.textPrimary,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Quiz accuracy $weeklyAccuracy% • Focus score $focusScore% • Assignment score $assignmentScore%",
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                MainProgressBar(
                    progress = overallScore,
                    colors = colors
                )
            }
        }
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
                text = "Progress strength",
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
                            listOf(colors.success, colors.accent)
                        )
                    )
            )
        }
    }
}

@Composable
private fun ProgressMessageCard(
    message: String,
    colors: ProgressColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colors.success.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, colors.success.copy(alpha = 0.32f))
    ) {
        Text(
            text = message,
            color = colors.success,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(14.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProgressStatCard(
    title: String,
    value: String,
    subtitle: String,
    iconText: String,
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
                text = iconText,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                color = mainColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
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
                subtitle = "Switch between overview, quizzes, assignments, and focus",
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
                subtitle = "Main progress signals for checkpoint demo",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            ProgressRouteButton(
                title = "Quiz Performance",
                subtitle = "AI quizzes generated from materials show strong learning progress.",
                progress = 88,
                iconText = "🧠",
                color = colors.primary,
                colors = colors,
                onClick = onOpenQuizzes
            )

            ItemDivider(colors)

            ProgressRouteButton(
                title = "Assignment Completion",
                subtitle = "Homework tracking and deadline reminders support study organization.",
                progress = 82,
                iconText = "📝",
                color = colors.accent,
                colors = colors,
                onClick = onOpenAssignments
            )

            ItemDivider(colors)

            ProgressRouteButton(
                title = "Focus Control",
                subtitle = "Focus mode and quiz unlock reduce distracting app usage.",
                progress = 76,
                iconText = "🛡",
                color = colors.success,
                colors = colors,
                onClick = onOpenFocus
            )
        }
    }
}

@Composable
private fun ProgressRouteButton(
    title: String,
    subtitle: String,
    progress: Int,
    iconText: String,
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
                text = iconText,
                fontSize = 22.sp
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
                    text = "$progress%",
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

            Spacer(modifier = Modifier.height(8.dp))

            MiniProgressBar(
                progress = progress,
                color = color,
                colors = colors
            )
        }
    }
}

@Composable
private fun ProgressListCard(
    title: String,
    subtitle: String,
    items: List<ProgressItem>,
    colors: ProgressColors,
    onActionClick: () -> Unit
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

            items.forEachIndexed { index, item ->
                ProgressItemRow(
                    item = item,
                    colors = colors,
                    onClick = onActionClick
                )

                if (index != items.lastIndex) {
                    ItemDivider(colors)
                }
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
private fun ProgressItemRow(
    item: ProgressItem,
    colors: ProgressColors,
    onClick: () -> Unit
) {
    val color = when (item.status) {
        ProgressStatus.GOOD -> colors.success
        ProgressStatus.WARNING -> colors.warning
        ProgressStatus.DANGER -> colors.danger
    }

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
                text = if (item.status == ProgressStatus.GOOD) "✓" else "!",
                color = color,
                fontSize = 19.sp,
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
                    text = item.title,
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${item.progress}%",
                    color = color,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.subtitle,
                color = colors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            MiniProgressBar(
                progress = item.progress,
                color = color,
                colors = colors
            )
        }
    }
}

@Composable
private fun MiniProgressBar(
    progress: Int,
    color: Color,
    colors: ProgressColors
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
private fun WeeklyTrendCard(
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
                subtitle = "Simple visual summary for presentation",
                colors = colors
            )

            Spacer(modifier = Modifier.height(16.dp))

            listOf(
                "Mon" to 62,
                "Tue" to 70,
                "Wed" to 76,
                "Thu" to 81,
                "Fri" to 84,
                "Sat" to 88,
                "Sun" to 91
            ).forEach { item ->
                TrendRow(
                    day = item.first,
                    value = item.second,
                    colors = colors
                )

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun TrendRow(
    day: String,
    value: Int,
    colors: ProgressColors
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = day,
            color = colors.textPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.size(width = 38.dp, height = 20.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(9.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(colors.track)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(value.coerceIn(0, 100) / 100f)
                    .height(9.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(colors.primary, colors.accent)
                        )
                    )
            )
        }

        Text(
            text = "$value%",
            color = colors.textSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.size(width = 42.dp, height = 20.dp)
        )
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
    onOpenProfile: () -> Unit,
    onOpenPairCode: () -> Unit
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
                subtitle = "Open screens related to student progress",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            SecondaryProgressButton("Home Dashboard", "Return to main app dashboard", "⌂", colors, onOpenHome)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryProgressButton("Materials Library", "Open uploaded study materials", "📚", colors, onOpenMaterials)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryProgressButton("AI Quizzes", "Open generated quiz list", "🧠", colors, onOpenQuizzes)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryProgressButton("Assignments", "Open homework tracking", "📝", colors, onOpenAssignments)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryProgressButton("Focus Shield", "Open focus mode", "🛡", colors, onOpenFocus)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryProgressButton("Profile", "Open account information", "👤", colors, onOpenProfile)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryProgressButton("Pair Code", "Open parent-student linking code", "🔗", colors, onOpenPairCode)
        }
    }
}

@Composable
private fun ProgressExplanationCard(
    colors: ProgressColors
) {
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
                    text = "Checkpoint explanation",
                    color = colors.success,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "This screen shows how Rakizz measures progress from quizzes, assignments, and focus activity. It supports parent monitoring and student self-review.",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
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
                    fontSize = 20.sp
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
                text = "›",
                color = colors.textSecondary,
                fontSize = 28.sp,
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
            fontSize = 21.sp,
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

private enum class ProgressStatus {
    GOOD,
    WARNING,
    DANGER
}

private data class ProgressItem(
    val title: String,
    val subtitle: String,
    val progress: Int,
    val status: ProgressStatus
)

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
