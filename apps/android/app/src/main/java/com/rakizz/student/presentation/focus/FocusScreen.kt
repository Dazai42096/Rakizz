package com.rakizz.student.presentation.focus

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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

@Suppress("UNUSED_PARAMETER")
@Composable
fun FocusScreen(
    navController: NavController? = null,
    viewModel: FocusViewModel? = null,
    onBackClick: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onStartFocusClick: () -> Unit = {},
    onStartFocus: () -> Unit = {},
    onStopFocusClick: () -> Unit = {},
    onStopFocus: () -> Unit = {},
    onOpenUnlockQuizClick: () -> Unit = {},
    onUnlockQuizClick: () -> Unit = {},
    onOpenUnlockQuiz: () -> Unit = {},
    onNavigateToUnlockQuiz: () -> Unit = {},
    onTryBlockedAppClick: (String) -> Unit = {},
    onManageAppsClick: () -> Unit = {},
    onBlockedAppsClick: () -> Unit = {},
    onOpenBlockedAppsClick: () -> Unit = {},
    onParentFocusClick: () -> Unit = {},
    onOpenParentFocusClick: () -> Unit = {},
    onViewProgressClick: () -> Unit = {},
    onGoToMaterialsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onRefreshClick: () -> Unit = {}
) {
    val colors = focusScreenColors()

    var focusActive by remember { mutableStateOf(false) }
    var selectedDuration by remember { mutableStateOf("45 min") }
    var message by remember { mutableStateOf("") }

    val blockedApps = remember {
        mutableStateListOf(
            FocusAppItem("1", "TikTok", "Short videos", "🎵", true),
            FocusAppItem("2", "Instagram", "Social media", "📸", true),
            FocusAppItem("3", "YouTube", "Video streaming", "▶", true),
            FocusAppItem("4", "Games", "Mobile games", "🎮", false),
            FocusAppItem("5", "Browser", "Web distractions", "🌐", false)
        )
    }

    val selectedCount = blockedApps.count { it.isBlocked }
    val totalCount = blockedApps.size

    val infiniteTransition = rememberInfiniteTransition(label = "focus_animation")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "focus_pulse"
    )

    fun goBack() {
        if (navController != null) {
            navController.popBackStack()
        } else {
            onBackClick()
            onNavigateBack()
        }
    }

    fun startFocusSession() {
        focusActive = true
        message = "Focus mode started. Distracting apps are now protected."
        onStartFocusClick()
        onStartFocus()
    }

    fun stopFocusSession() {
        focusActive = false
        message = "Focus mode stopped for demo."
        onStopFocusClick()
        onStopFocus()
    }

    fun openUnlockQuiz(appName: String = "TikTok") {
        onTryBlockedAppClick(appName)
        onOpenUnlockQuizClick()
        onUnlockQuizClick()
        onOpenUnlockQuiz()
        onNavigateToUnlockQuiz()
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
        // Animated shield glow.
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-130).dp)
                .graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                    alpha = 0.85f
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

            FocusTopBar(
                colors = colors,
                onBackClick = { goBack() },
                onSettingsClick = {
                    onSettingsClick()
                    onRefreshClick()
                }
            )

            Spacer(modifier = Modifier.height(22.dp))

            FocusHeroCard(
                focusActive = focusActive,
                selectedDuration = selectedDuration,
                selectedAppsCount = selectedCount,
                totalAppsCount = totalCount,
                pulse = pulse,
                colors = colors
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = message.isNotEmpty()) {
                FocusMessageCard(
                    message = message,
                    success = focusActive,
                    colors = colors
                )
            }

            if (message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
            }

            DurationCard(
                selectedDuration = selectedDuration,
                colors = colors,
                onDurationSelected = { selectedDuration = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            FocusControlCard(
                focusActive = focusActive,
                colors = colors,
                onStartClick = { startFocusSession() },
                onStopClick = { stopFocusSession() },
                onUnlockQuizClick = { openUnlockQuiz("TikTok") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            QuickNavigationCard(
                colors = colors,
                onViewProgressClick = onViewProgressClick,
                onGoToMaterialsClick = onGoToMaterialsClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            BlockedAppsCard(
                apps = blockedApps,
                colors = colors,
                onManageAppsClick = {
                    onManageAppsClick()
                    onBlockedAppsClick()
                    onOpenBlockedAppsClick()
                },
                onToggleApp = { app ->
                    val index = blockedApps.indexOfFirst { it.id == app.id }
                    if (index != -1) {
                        blockedApps[index] = app.copy(isBlocked = !app.isBlocked)
                    }
                },
                onTryBlockedAppClick = { appName ->
                    openUnlockQuiz(appName)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            FocusRulesCard(colors = colors)

            Spacer(modifier = Modifier.height(16.dp))

            ParentLinkCard(
                colors = colors,
                onClick = {
                    onParentFocusClick()
                    onOpenParentFocusClick()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            CheckpointFocusCard(colors = colors)

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun FocusTopBar(
    colors: FocusScreenColors,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit
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
                text = "Focus Shield",
                color = colors.textPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Block distractions and protect study time",
                color = colors.textSecondary,
                fontSize = 13.sp
            )
        }

        CircleIconButton(
            text = "⚙",
            colors = colors,
            onClick = onSettingsClick
        )
    }
}

@Composable
private fun FocusHeroCard(
    focusActive: Boolean,
    selectedDuration: String,
    selectedAppsCount: Int,
    totalAppsCount: Int,
    pulse: Float,
    colors: FocusScreenColors
) {
    val statusColor = if (focusActive) colors.success else colors.warning
    val statusText = if (focusActive) "Focus mode is active" else "Focus mode is ready"

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
                        .size(112.dp)
                        .graphicsLayer {
                            scaleX = if (focusActive) pulse else 1f
                            scaleY = if (focusActive) pulse else 1f
                        }
                        .clip(RoundedCornerShape(34.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    statusColor,
                                    colors.primary
                                )
                            )
                        )
                        .border(3.dp, colors.glassBorder, RoundedCornerShape(34.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🛡",
                        fontSize = 48.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = statusColor.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.34f))
                ) {
                    Text(
                        text = "● $statusText",
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Protect your study session from distracting apps.",
                    color = colors.textPrimary,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 31.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Selected duration: $selectedDuration • $selectedAppsCount of $totalAppsCount apps protected",
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

@Composable
private fun FocusMessageCard(
    message: String,
    success: Boolean,
    colors: FocusScreenColors
) {
    val messageColor = if (success) colors.success else colors.warning

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
private fun DurationCard(
    selectedDuration: String,
    colors: FocusScreenColors,
    onDurationSelected: (String) -> Unit
) {
    val durations = listOf("25 min", "45 min", "60 min", "90 min")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "Session Duration",
                subtitle = "Choose how long the focus protection should run",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                durations.forEach { duration ->
                    DurationChip(
                        text = duration,
                        selected = selectedDuration == duration,
                        colors = colors,
                        modifier = Modifier.weight(1f),
                        onClick = { onDurationSelected(duration) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DurationChip(
    text: String,
    selected: Boolean,
    colors: FocusScreenColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(46.dp)
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
                text = text,
                color = if (selected) colors.primary else colors.textPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FocusControlCard(
    focusActive: Boolean,
    colors: FocusScreenColors,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onUnlockQuizClick: () -> Unit
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
                subtitle = "Start protection or test the quiz unlock flow",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (focusActive) {
                MainGradientButton(
                    text = "Stop Focus Mode",
                    colors = colors,
                    danger = true,
                    onClick = onStopClick
                )
            } else {
                MainGradientButton(
                    text = "Start Focus Mode",
                    colors = colors,
                    danger = false,
                    onClick = onStartClick
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryFocusButton(
                text = "Open Unlock Quiz Demo",
                subtitle = "Simulate what happens when a blocked app is opened",
                iconText = "🧩",
                colors = colors,
                onClick = onUnlockQuizClick
            )
        }
    }
}

@Composable
private fun QuickNavigationCard(
    colors: FocusScreenColors,
    onViewProgressClick: () -> Unit,
    onGoToMaterialsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "Quick Actions",
                subtitle = "Move between focus, progress, and learning materials",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            SecondaryFocusButton(
                text = "View Progress",
                subtitle = "Open student progress and focus performance",
                iconText = "📊",
                colors = colors,
                onClick = onViewProgressClick
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryFocusButton(
                text = "Go To Materials",
                subtitle = "Open study materials used for quiz unlock",
                iconText = "📚",
                colors = colors,
                onClick = onGoToMaterialsClick
            )
        }
    }
}

@Composable
private fun MainGradientButton(
    text: String,
    colors: FocusScreenColors,
    danger: Boolean,
    onClick: () -> Unit
) {
    val firstColor = if (danger) colors.danger else colors.primary
    val secondColor = if (danger) Color(0xFFFF6B6B) else colors.accent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        firstColor,
                        secondColor
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
private fun SecondaryFocusButton(
    text: String,
    subtitle: String,
    iconText: String,
    colors: FocusScreenColors,
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
                Text(text = iconText, fontSize = 20.sp)
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
private fun BlockedAppsCard(
    apps: List<FocusAppItem>,
    colors: FocusScreenColors,
    onManageAppsClick: () -> Unit,
    onToggleApp: (FocusAppItem) -> Unit,
    onTryBlockedAppClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                SectionTitle(
                    title = "Protected Apps",
                    subtitle = "Choose apps that should be blocked during focus mode",
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = colors.primary.copy(alpha = 0.13f),
                    border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.28f)),
                    modifier = Modifier.clickable(onClick = onManageAppsClick)
                ) {
                    Text(
                        text = "Manage",
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            apps.forEachIndexed { index, app ->
                FocusAppRow(
                    app = app,
                    colors = colors,
                    onToggleClick = { onToggleApp(app) },
                    onTryBlockedAppClick = { onTryBlockedAppClick(app.name) }
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
private fun FocusAppRow(
    app: FocusAppItem,
    colors: FocusScreenColors,
    onToggleClick: () -> Unit,
    onTryBlockedAppClick: () -> Unit
) {
    val statusColor = if (app.isBlocked) colors.danger else colors.success
    val statusText = if (app.isBlocked) "Blocked" else "Allowed"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(statusColor.copy(alpha = 0.13f))
                .border(1.dp, statusColor.copy(alpha = 0.30f), RoundedCornerShape(17.dp))
                .clickable(onClick = onToggleClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = app.iconText,
                fontSize = 22.sp
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 13.dp)
        ) {
            Text(
                text = app.name,
                color = colors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = app.description,
                color = colors.textSecondary,
                fontSize = 12.sp
            )
        }

        Surface(
            shape = RoundedCornerShape(100.dp),
            color = statusColor.copy(alpha = 0.13f),
            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.28f)),
            modifier = Modifier.clickable(onClick = onTryBlockedAppClick)
        ) {
            Text(
                text = statusText,
                color = statusColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun FocusRulesCard(colors: FocusScreenColors) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "How Focus Unlock Works",
                subtitle = "The core Rakizz flow explained simply",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            FocusRuleStep(
                number = "1",
                title = "Parent selects distracting apps",
                subtitle = "Apps like TikTok, Instagram, YouTube, and games can be protected.",
                colors = colors
            )

            RuleDivider(colors)

            FocusRuleStep(
                number = "2",
                title = "Student tries to open blocked app",
                subtitle = "Rakizz checks if focus mode or parent rules are active.",
                colors = colors
            )

            RuleDivider(colors)

            FocusRuleStep(
                number = "3",
                title = "Quiz unlock is required",
                subtitle = "The student must pass a quiz generated from study materials to unlock temporary access.",
                colors = colors
            )
        }
    }
}

@Composable
private fun FocusRuleStep(
    number: String,
    title: String,
    subtitle: String,
    colors: FocusScreenColors
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(colors.primary.copy(alpha = 0.16f))
                .border(1.dp, colors.primary.copy(alpha = 0.34f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = colors.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )

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
private fun RuleDivider(colors: FocusScreenColors) {
    HorizontalDivider(
        color = colors.border,
        thickness = 1.dp,
        modifier = Modifier.padding(start = 17.dp, top = 12.dp, bottom = 12.dp)
    )
}

@Composable
private fun ParentLinkCard(
    colors: FocusScreenColors,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = colors.primary.copy(alpha = 0.11f),
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "👨‍👩‍👧",
                fontSize = 27.sp
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = "Parent Focus Control",
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "Parent can configure schedules, blocked apps, and progress visibility.",
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
private fun CheckpointFocusCard(colors: FocusScreenColors) {
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
                    text = "This screen demonstrates Rakizz's main focus-control idea: selected apps are blocked during study time, and quiz unlock can give temporary access.",
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
    colors: FocusScreenColors,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
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
    colors: FocusScreenColors,
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
private fun focusScreenColors(): FocusScreenColors {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    return if (isDark) {
        FocusScreenColors(
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
        FocusScreenColors(
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

private data class FocusAppItem(
    val id: String,
    val name: String,
    val description: String,
    val iconText: String,
    val isBlocked: Boolean
)

private data class FocusScreenColors(
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