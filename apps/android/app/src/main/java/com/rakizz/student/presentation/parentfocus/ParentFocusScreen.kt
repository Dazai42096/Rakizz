package com.rakizz.student.presentation.parentfocus

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
fun ParentFocusScreen(
    navController: NavController? = null,
    viewModel: Any? = null,

    // Back / refresh callbacks
    onBackClick: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onRefreshClick: () -> Unit = {},

    // Student callbacks
    onStudentClick: (String) -> Unit = {},
    onStudentSelected: (String) -> Unit = {},
    onSelectStudent: (String) -> Unit = {},

    // App callbacks
    onAppClick: (String) -> Unit = {},
    onAppSelected: (String) -> Unit = {},
    onAppToggle: (String) -> Unit = {},
    onToggleApp: (String) -> Unit = {},
    onBlockedAppClick: (String) -> Unit = {},

    // Rule / schedule callbacks
    onSaveRulesClick: (String) -> Unit = {},
    onSavePolicyClick: (String) -> Unit = {},
    onCreateScheduleClick: (String) -> Unit = {},
    onScheduleClick: (String) -> Unit = {},
    onActivateFocusClick: (String) -> Unit = {},
    onDeactivateFocusClick: (String) -> Unit = {},

    // Navigation callbacks
    onOpenHome: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenPairCode: () -> Unit = {},
    onOpenProgress: (String) -> Unit = {},
    onOpenStudentProgress: (String) -> Unit = {},
    onOpenMaterials: () -> Unit = {},
    onOpenQuizzes: () -> Unit = {},
    onOpenAssignments: () -> Unit = {}
) {
    val colors = parentFocusColors()

    val students = remember {
        listOf(
            ParentStudentItem(
                id = "student-001",
                name = "Azmi Student",
                grade = "Computer Science",
                status = "Linked",
                focusScore = 78
            ),
            ParentStudentItem(
                id = "student-002",
                name = "Linked Student",
                grade = "Grade 10",
                status = "Pending Review",
                focusScore = 64
            )
        )
    }

    val apps = remember {
        listOf(
            ParentAppItem("app-youtube", "YouTube", "Video distraction", "High"),
            ParentAppItem("app-tiktok", "TikTok", "Short videos", "High"),
            ParentAppItem("app-instagram", "Instagram", "Social media", "Medium"),
            ParentAppItem("app-games", "Mobile Games", "Gaming apps", "High"),
            ParentAppItem("app-browser", "Browser", "Web access", "Low")
        )
    }

    var selectedStudent by remember { mutableStateOf(students.first()) }
    var blockedApps by remember { mutableStateOf(setOf("app-youtube", "app-tiktok", "app-games")) }
    var focusEnabled by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "parent_focus_animation")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "parent_focus_glow"
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

    fun selectStudent(student: ParentStudentItem) {
        selectedStudent = student
        message = "${student.name} selected."

        onStudentClick(student.id)
        onStudentSelected(student.id)
        onSelectStudent(student.id)
    }

    fun toggleApp(app: ParentAppItem) {
        blockedApps = if (blockedApps.contains(app.id)) {
            blockedApps - app.id
        } else {
            blockedApps + app.id
        }

        message = "${app.name} rule updated."

        onAppClick(app.id)
        onAppSelected(app.id)
        onAppToggle(app.id)
        onToggleApp(app.id)
        onBlockedAppClick(app.id)
    }

    fun saveRules() {
        message = "Parent focus rules saved."

        onSaveRulesClick(selectedStudent.id)
        onSavePolicyClick(selectedStudent.id)
    }

    fun createSchedule() {
        message = "Focus schedule created."

        onCreateScheduleClick(selectedStudent.id)
        onScheduleClick(selectedStudent.id)
    }

    fun toggleFocusStatus() {
        focusEnabled = !focusEnabled

        if (focusEnabled) {
            message = "Focus shield enabled."
            onActivateFocusClick(selectedStudent.id)
        } else {
            message = "Focus shield paused."
            onDeactivateFocusClick(selectedStudent.id)
        }
    }

    fun openProgress() {
        onOpenProgress(selectedStudent.id)
        onOpenStudentProgress(selectedStudent.id)
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
                .size(310.dp)
                .align(Alignment.TopEnd)
                .offset(x = 110.dp, y = (-140).dp)
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

            ParentFocusTopBar(
                colors = colors,
                onBackClick = { goBack() },
                onRefreshClick = {
                    message = "Parent focus data refreshed."
                    onRefreshClick()
                }
            )

            Spacer(modifier = Modifier.height(22.dp))

            ParentFocusHeroCard(
                selectedStudent = selectedStudent,
                focusEnabled = focusEnabled,
                blockedCount = blockedApps.size,
                glowScale = glowScale,
                colors = colors
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = message.isNotEmpty()) {
                ParentFocusMessageCard(
                    message = message,
                    colors = colors
                )
            }

            if (message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
            }

            StudentSelectorCard(
                students = students,
                selectedStudent = selectedStudent,
                colors = colors,
                onStudentClick = { selectStudent(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            FocusControlCard(
                selectedStudent = selectedStudent,
                focusEnabled = focusEnabled,
                blockedCount = blockedApps.size,
                colors = colors,
                onToggleFocus = { toggleFocusStatus() },
                onSaveRules = { saveRules() },
                onCreateSchedule = { createSchedule() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppsSelectionCard(
                apps = apps,
                blockedApps = blockedApps,
                colors = colors,
                onAppClick = { toggleApp(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ScheduleCard(
                colors = colors,
                onCreateSchedule = { createSchedule() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ParentMonitoringCard(
                selectedStudent = selectedStudent,
                colors = colors,
                onOpenProgress = { openProgress() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ParentFocusNavigationCard(
                colors = colors,
                onOpenHome = onOpenHome,
                onOpenProfile = onOpenProfile,
                onOpenPairCode = onOpenPairCode,
                onOpenProgress = { openProgress() },
                onOpenMaterials = onOpenMaterials,
                onOpenQuizzes = onOpenQuizzes,
                onOpenAssignments = onOpenAssignments
            )

            Spacer(modifier = Modifier.height(16.dp))

            ParentFocusExplanationCard(colors = colors)

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ParentFocusTopBar(
    colors: ParentFocusColors,
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
                text = "Parent Focus",
                color = colors.textPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Manage student focus rules and blocked apps",
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
private fun ParentFocusHeroCard(
    selectedStudent: ParentStudentItem,
    focusEnabled: Boolean,
    blockedCount: Int,
    glowScale: Float,
    colors: ParentFocusColors
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .graphicsLayer {
                            scaleX = glowScale
                            scaleY = glowScale
                        }
                        .clip(RoundedCornerShape(34.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    colors.primary,
                                    colors.accent
                                )
                            )
                        )
                        .border(3.dp, colors.glassBorder, RoundedCornerShape(34.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🛡",
                        fontSize = 46.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = if (focusEnabled) {
                        colors.success.copy(alpha = 0.14f)
                    } else {
                        colors.warning.copy(alpha = 0.14f)
                    },
                    border = BorderStroke(
                        1.dp,
                        if (focusEnabled) {
                            colors.success.copy(alpha = 0.34f)
                        } else {
                            colors.warning.copy(alpha = 0.34f)
                        }
                    )
                ) {
                    Text(
                        text = if (focusEnabled) "● Focus shield active" else "● Focus shield paused",
                        color = if (focusEnabled) colors.success else colors.warning,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = selectedStudent.name,
                    color = colors.textPrimary,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "$blockedCount apps blocked • ${selectedStudent.focusScore}% focus score",
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ParentFocusMessageCard(
    message: String,
    colors: ParentFocusColors
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
private fun StudentSelectorCard(
    students: List<ParentStudentItem>,
    selectedStudent: ParentStudentItem,
    colors: ParentFocusColors,
    onStudentClick: (ParentStudentItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "Linked Students",
                subtitle = "Choose which student to manage",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            students.forEachIndexed { index, student ->
                StudentRow(
                    student = student,
                    selected = student.id == selectedStudent.id,
                    colors = colors,
                    onClick = { onStudentClick(student) }
                )

                if (index != students.lastIndex) {
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
private fun StudentRow(
    student: ParentStudentItem,
    selected: Boolean,
    colors: ParentFocusColors,
    onClick: () -> Unit
) {
    val mainColor = if (selected) colors.primary else colors.textSecondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(mainColor.copy(alpha = 0.14f))
                .border(1.dp, mainColor.copy(alpha = 0.30f), RoundedCornerShape(17.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = student.name.firstOrNull()?.uppercase() ?: "S",
                color = mainColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = student.name,
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "${student.grade} • ${student.status}",
                color = colors.textSecondary,
                fontSize = 12.sp
            )
        }

        Text(
            text = "${student.focusScore}%",
            color = mainColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun FocusControlCard(
    selectedStudent: ParentStudentItem,
    focusEnabled: Boolean,
    blockedCount: Int,
    colors: ParentFocusColors,
    onToggleFocus: () -> Unit,
    onSaveRules: () -> Unit,
    onCreateSchedule: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "Focus Controls",
                subtitle = "Main parent controls for ${selectedStudent.name}",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MiniStatCard(
                    title = "Blocked",
                    value = blockedCount.toString(),
                    subtitle = "apps",
                    iconText = "⛔",
                    mainColor = colors.danger,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                MiniStatCard(
                    title = "Score",
                    value = "${selectedStudent.focusScore}%",
                    subtitle = "focus",
                    iconText = "🎯",
                    mainColor = colors.success,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                MiniStatCard(
                    title = "Mode",
                    value = if (focusEnabled) "On" else "Off",
                    subtitle = "shield",
                    iconText = "🛡",
                    mainColor = if (focusEnabled) colors.primary else colors.warning,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            MainButton(
                text = if (focusEnabled) "Pause Focus Shield" else "Activate Focus Shield",
                colors = colors,
                onClick = onToggleFocus
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryButton(
                text = "Save Parent Rules",
                subtitle = "Apply selected blocked apps and quiz unlock rules",
                iconText = "✓",
                colors = colors,
                onClick = onSaveRules
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryButton(
                text = "Create Focus Schedule",
                subtitle = "Set study time and restricted app periods",
                iconText = "⏰",
                colors = colors,
                onClick = onCreateSchedule
            )
        }
    }
}

@Composable
private fun AppsSelectionCard(
    apps: List<ParentAppItem>,
    blockedApps: Set<String>,
    colors: ParentFocusColors,
    onAppClick: (ParentAppItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "Distracting Apps",
                subtitle = "Select apps that should be blocked during focus time",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            apps.forEachIndexed { index, app ->
                AppRuleRow(
                    app = app,
                    blocked = blockedApps.contains(app.id),
                    colors = colors,
                    onClick = { onAppClick(app) }
                )

                if (index != apps.lastIndex) {
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
private fun AppRuleRow(
    app: ParentAppItem,
    blocked: Boolean,
    colors: ParentFocusColors,
    onClick: () -> Unit
) {
    val mainColor = if (blocked) colors.danger else colors.success

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(mainColor.copy(alpha = 0.14f))
                .border(1.dp, mainColor.copy(alpha = 0.30f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (blocked) "⛔" else "✓",
                color = mainColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = app.name,
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "${app.category} • Risk: ${app.risk}",
                color = colors.textSecondary,
                fontSize = 12.sp
            )
        }

        Surface(
            shape = RoundedCornerShape(100.dp),
            color = mainColor.copy(alpha = 0.13f),
            border = BorderStroke(1.dp, mainColor.copy(alpha = 0.28f))
        ) {
            Text(
                text = if (blocked) "Blocked" else "Allowed",
                color = mainColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
            )
        }
    }
}

@Composable
private fun ScheduleCard(
    colors: ParentFocusColors,
    onCreateSchedule: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "Focus Schedule",
                subtitle = "Schedule for parent-defined focus periods",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            ScheduleRow(
                title = "Study Time",
                time = "05:00 PM - 07:00 PM",
                status = "Active",
                color = colors.success,
                colors = colors
            )

            HorizontalDivider(
                color = colors.border,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            ScheduleRow(
                title = "Night Restriction",
                time = "10:00 PM - 07:00 AM",
                status = "Scheduled",
                color = colors.primary,
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            SecondaryButton(
                text = "Create New Schedule",
                subtitle = "Add another focus period",
                iconText = "＋",
                colors = colors,
                onClick = onCreateSchedule
            )
        }
    }
}

@Composable
private fun ScheduleRow(
    title: String,
    time: String,
    status: String,
    color: Color,
    colors: ParentFocusColors
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(color.copy(alpha = 0.14f))
                .border(1.dp, color.copy(alpha = 0.30f), RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⏰",
                fontSize = 20.sp
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = time,
                color = colors.textSecondary,
                fontSize = 12.sp
            )
        }

        Text(
            text = status,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun ParentMonitoringCard(
    selectedStudent: ParentStudentItem,
    colors: ParentFocusColors,
    onOpenProgress: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onOpenProgress),
        shape = RoundedCornerShape(24.dp),
        color = colors.primary.copy(alpha = 0.11f),
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📊",
                fontSize = 27.sp
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = "View ${selectedStudent.name}'s Progress",
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "Open learning progress, quiz activity, assignments, and focus reports.",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }

            Text(
                text = "›",
                color = colors.primary,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun ParentFocusNavigationCard(
    colors: ParentFocusColors,
    onOpenHome: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenPairCode: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenMaterials: () -> Unit,
    onOpenQuizzes: () -> Unit,
    onOpenAssignments: () -> Unit
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
                subtitle = "Move to related Rakizz screens",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            SecondaryButton("Home Dashboard", "Return to main app dashboard", "⌂", colors, onOpenHome)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryButton("Student Progress", "Open progress monitoring", "📊", colors, onOpenProgress)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryButton("Pair Code", "Open parent-student linking code", "🔗", colors, onOpenPairCode)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryButton("Materials", "Open uploaded study materials", "📚", colors, onOpenMaterials)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryButton("AI Quizzes", "Open generated quizzes", "🧠", colors, onOpenQuizzes)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryButton("Assignments", "Open homework tracking", "📝", colors, onOpenAssignments)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryButton("Profile", "Open parent account settings", "👤", colors, onOpenProfile)
        }
    }
}

@Composable
private fun ParentFocusExplanationCard(colors: ParentFocusColors) {
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
                    text = "This screen shows the parent control side of Rakizz. Parents can select distracting apps, create focus schedules, and monitor student progress.",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun MiniStatCard(
    title: String,
    value: String,
    subtitle: String,
    iconText: String,
    mainColor: Color,
    colors: ParentFocusColors,
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
                fontSize = 21.sp
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
private fun MainButton(
    text: String,
    colors: ParentFocusColors,
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
            text = text,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun SecondaryButton(
    text: String,
    subtitle: String,
    iconText: String,
    colors: ParentFocusColors,
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
            Text(
                text = iconText,
                fontSize = 22.sp,
                modifier = Modifier.padding(end = 12.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
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
    colors: ParentFocusColors
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
    colors: ParentFocusColors,
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
private fun parentFocusColors(): ParentFocusColors {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    return if (isDark) {
        ParentFocusColors(
            backgroundTop = Color(0xFF020617),
            backgroundMiddle = Color(0xFF07111F),
            backgroundBottom = Color(0xFF000000),
            card = Color(0xE60B1220),
            cardAlt = Color(0xCC111A2E),
            border = Color(0x334D9DFF),
            primary = Color(0xFF2F80FF),
            accent = Color(0xFF00D4FF),
            success = Color(0xFF22C55E),
            warning = Color(0xFFF59E0B),
            danger = Color(0xFFEF4444),
            textPrimary = Color.White,
            textSecondary = Color(0xFF94A3B8),
            glassBorder = Color(0x66FFFFFF)
        )
    } else {
        ParentFocusColors(
            backgroundTop = Color(0xFFF8FBFF),
            backgroundMiddle = Color(0xFFEAF4FF),
            backgroundBottom = Color(0xFFFFFFFF),
            card = Color(0xFFFFFFFF),
            cardAlt = Color(0xFFF1F7FF),
            border = Color(0x263B82F6),
            primary = Color(0xFF2563EB),
            accent = Color(0xFF06B6D4),
            success = Color(0xFF16A34A),
            warning = Color(0xFFD97706),
            danger = Color(0xFFDC2626),
            textPrimary = Color(0xFF0F172A),
            textSecondary = Color(0xFF64748B),
            glassBorder = Color(0xFFFFFFFF)
        )
    }
}

private data class ParentStudentItem(
    val id: String,
    val name: String,
    val grade: String,
    val status: String,
    val focusScore: Int
)

private data class ParentAppItem(
    val id: String,
    val name: String,
    val category: String,
    val risk: String
)

private data class ParentFocusColors(
    val backgroundTop: Color,
    val backgroundMiddle: Color,
    val backgroundBottom: Color,
    val card: Color,
    val cardAlt: Color,
    val border: Color,
    val primary: Color,
    val accent: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val glassBorder: Color
)