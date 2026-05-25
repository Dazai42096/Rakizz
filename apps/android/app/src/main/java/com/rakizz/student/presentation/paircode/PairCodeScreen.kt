package com.rakizz.student.presentation.paircode

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
import androidx.compose.runtime.mutableIntStateOf
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
fun PairCodeScreen(
    navController: NavController? = null,
    viewModel: Any? = null,

    // Back callbacks
    onBackClick: () -> Unit = {},
    onNavigateBack: () -> Unit = {},

    // NavHost sends the code value into these callbacks
    onCopyCodeClick: (String) -> Unit = {},
    onShareCodeClick: (String) -> Unit = {},

    // These are also String callbacks because your NavHost uses { code -> ... }
    onCopyClick: (String) -> Unit = {},
    onShareClick: (String) -> Unit = {},

    // Generate / refresh callbacks
    onGenerateCodeClick: () -> Unit = {},
    onGenerateNewCodeClick: () -> Unit = {},
    onRegenerateClick: () -> Unit = {},
    onRefreshClick: () -> Unit = {},

    // Navigation callbacks
    onOpenProfile: () -> Unit = {},
    onOpenProfileClick: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onOpenHome: () -> Unit = {},
    onOpenParentFocus: () -> Unit = {},
    onOpenParentFocusClick: () -> Unit = {},
    onNavigateToParentFocus: () -> Unit = {},
    onOpenFocus: () -> Unit = {},
    onOpenMaterials: () -> Unit = {},
    onOpenLibrary: () -> Unit = {}
) {
    val colors = pairCodeColors()

    var pairCode by remember { mutableStateOf("RKZ-428-961") }
    var generatedCount by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pair_code_animation")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pair_code_glow"
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

    fun copyCode() {
        isError = false
        message = "Pair code copied for demo."

        onCopyCodeClick(pairCode)
        onCopyClick(pairCode)
    }

    fun shareCode() {
        isError = false
        message = "Pair code ready to share with parent."

        onShareCodeClick(pairCode)
        onShareClick(pairCode)
    }

    fun generateNewCode() {
        generatedCount += 1

        pairCode = when (generatedCount % 4) {
            0 -> "RKZ-428-961"
            1 -> "RKZ-726-314"
            2 -> "RKZ-539-802"
            else -> "RKZ-184-670"
        }

        isError = false
        message = "New pair code generated."

        onGenerateCodeClick()
        onGenerateNewCodeClick()
        onRegenerateClick()
        onRefreshClick()
    }

    fun openProfile() {
        onOpenProfile()
        onOpenProfileClick()
        onNavigateToProfile()
    }

    fun openParentFocus() {
        onOpenParentFocus()
        onOpenParentFocusClick()
        onNavigateToParentFocus()
    }

    fun openMaterials() {
        onOpenMaterials()
        onOpenLibrary()
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
                .align(Alignment.TopCenter)
                .offset(y = (-135).dp)
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

            PairCodeTopBar(
                colors = colors,
                onBackClick = { goBack() },
                onRefreshClick = { generateNewCode() }
            )

            Spacer(modifier = Modifier.height(22.dp))

            PairCodeHeroCard(
                pairCode = pairCode,
                glowScale = glowScale,
                colors = colors
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = message.isNotEmpty()) {
                PairCodeMessageCard(
                    message = message,
                    isError = isError,
                    colors = colors
                )
            }

            if (message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
            }

            PairCodeActionsCard(
                colors = colors,
                onCopyClick = { copyCode() },
                onShareClick = { shareCode() },
                onGenerateClick = { generateNewCode() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            LinkStatusCard(colors = colors)

            Spacer(modifier = Modifier.height(16.dp))

            PairingStepsCard(colors = colors)

            Spacer(modifier = Modifier.height(16.dp))

            ParentAccessCard(
                colors = colors,
                onOpenParentFocus = { openParentFocus() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PairCodeNavigationCard(
                colors = colors,
                onOpenProfile = { openProfile() },
                onOpenHome = onOpenHome,
                onOpenFocus = onOpenFocus,
                onOpenMaterials = { openMaterials() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PairCodeExplanationCard(colors = colors)

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun PairCodeTopBar(
    colors: PairCodeColors,
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
                text = "Pair Code",
                color = colors.textPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Link parent and student accounts",
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
private fun PairCodeHeroCard(
    pairCode: String,
    glowScale: Float,
    colors: PairCodeColors
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
                        text = "🔗",
                        fontSize = 46.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = colors.success.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, colors.success.copy(alpha = 0.34f))
                ) {
                    Text(
                        text = "● Student code is active",
                        color = colors.success,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = pairCode,
                    color = colors.primary,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Give this code to the parent / guardian so they can connect their account to this student.",
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun PairCodeMessageCard(
    message: String,
    isError: Boolean,
    colors: PairCodeColors
) {
    val messageColor = if (isError) colors.danger else colors.success

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
private fun PairCodeActionsCard(
    colors: PairCodeColors,
    onCopyClick: () -> Unit,
    onShareClick: () -> Unit,
    onGenerateClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "Code Actions",
                subtitle = "Copy, share, or refresh the linking code",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            MainGradientButton(
                text = "Copy Pair Code",
                colors = colors,
                onClick = onCopyClick
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryPairButton(
                text = "Share with Parent",
                subtitle = "Prepare the code to send to parent / guardian",
                iconText = "📤",
                colors = colors,
                onClick = onShareClick
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryPairButton(
                text = "Generate New Code",
                subtitle = "Replace the old pair code for security",
                iconText = "↻",
                colors = colors,
                onClick = onGenerateClick
            )
        }
    }
}

@Composable
private fun LinkStatusCard(
    colors: PairCodeColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PairStatusCard(
            title = "Status",
            value = "Active",
            subtitle = "ready",
            iconText = "✓",
            mainColor = colors.success,
            colors = colors,
            modifier = Modifier.weight(1f)
        )

        PairStatusCard(
            title = "Role",
            value = "Student",
            subtitle = "owner",
            iconText = "🎓",
            mainColor = colors.primary,
            colors = colors,
            modifier = Modifier.weight(1f)
        )

        PairStatusCard(
            title = "Parent",
            value = "0",
            subtitle = "linked",
            iconText = "👨‍👩‍👧",
            mainColor = colors.accent,
            colors = colors,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PairStatusCard(
    title: String,
    value: String,
    subtitle: String,
    iconText: String,
    mainColor: Color,
    colors: PairCodeColors,
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
                fontSize = 19.sp,
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
private fun PairingStepsCard(
    colors: PairCodeColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "How Pairing Works",
                subtitle = "Simple parent-student linking flow",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            PairStep(
                number = "1",
                title = "Student opens Pair Code",
                subtitle = "The student shows this generated code from the profile area.",
                colors = colors
            )

            StepDivider(colors)

            PairStep(
                number = "2",
                title = "Parent enters the code",
                subtitle = "The parent uses the code during sign up or parent linking.",
                colors = colors
            )

            StepDivider(colors)

            PairStep(
                number = "3",
                title = "Rakizz connects both accounts",
                subtitle = "After linking, the parent can view progress and manage focus rules.",
                colors = colors
            )
        }
    }
}

@Composable
private fun PairStep(
    number: String,
    title: String,
    subtitle: String,
    colors: PairCodeColors
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
private fun StepDivider(colors: PairCodeColors) {
    HorizontalDivider(
        color = colors.border,
        thickness = 1.dp,
        modifier = Modifier.padding(start = 17.dp, top = 12.dp, bottom = 12.dp)
    )
}

@Composable
private fun ParentAccessCard(
    colors: PairCodeColors,
    onOpenParentFocus: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onOpenParentFocus),
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
                    text = "After Linking",
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "Parent can manage blocked apps, schedules, quiz unlock, and progress monitoring.",
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
private fun PairCodeNavigationCard(
    colors: PairCodeColors,
    onOpenProfile: () -> Unit,
    onOpenHome: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenMaterials: () -> Unit
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
                subtitle = "Move between linking and core student screens",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            SecondaryPairButton(
                text = "Back to Profile",
                subtitle = "Return to account information",
                iconText = "👤",
                colors = colors,
                onClick = onOpenProfile
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryPairButton(
                text = "Home Dashboard",
                subtitle = "Open the main Rakizz dashboard",
                iconText = "⌂",
                colors = colors,
                onClick = onOpenHome
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryPairButton(
                text = "Focus Shield",
                subtitle = "Open focus mode and quiz unlock",
                iconText = "🛡",
                colors = colors,
                onClick = onOpenFocus
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryPairButton(
                text = "Materials Library",
                subtitle = "Open uploaded study materials",
                iconText = "📚",
                colors = colors,
                onClick = onOpenMaterials
            )
        }
    }
}

@Composable
private fun PairCodeExplanationCard(
    colors: PairCodeColors
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
                    text = "This screen explains the parent-student linking feature. The pair code lets a parent connect to a student without exposing internal database IDs.",
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
    colors: PairCodeColors,
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
private fun SecondaryPairButton(
    text: String,
    subtitle: String,
    iconText: String,
    colors: PairCodeColors,
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
    colors: PairCodeColors
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
    colors: PairCodeColors,
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
private fun pairCodeColors(): PairCodeColors {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    return if (isDark) {
        PairCodeColors(
            backgroundTop = Color(0xFF020617),
            backgroundMiddle = Color(0xFF07111F),
            backgroundBottom = Color(0xFF000000),
            card = Color(0xE60B1220),
            cardAlt = Color(0xCC111A2E),
            border = Color(0x334D9DFF),
            primary = Color(0xFF2F80FF),
            accent = Color(0xFF00D4FF),
            success = Color(0xFF22C55E),
            danger = Color(0xFFEF4444),
            textPrimary = Color.White,
            textSecondary = Color(0xFF94A3B8),
            glassBorder = Color(0x66FFFFFF)
        )
    } else {
        PairCodeColors(
            backgroundTop = Color(0xFFF8FBFF),
            backgroundMiddle = Color(0xFFEAF4FF),
            backgroundBottom = Color(0xFFFFFFFF),
            card = Color(0xFFFFFFFF),
            cardAlt = Color(0xFFF1F7FF),
            border = Color(0x263B82F6),
            primary = Color(0xFF2563EB),
            accent = Color(0xFF06B6D4),
            success = Color(0xFF16A34A),
            danger = Color(0xFFDC2626),
            textPrimary = Color(0xFF0F172A),
            textSecondary = Color(0xFF64748B),
            glassBorder = Color(0xFFFFFFFF)
        )
    }
}

private data class PairCodeColors(
    val backgroundTop: Color,
    val backgroundMiddle: Color,
    val backgroundBottom: Color,
    val card: Color,
    val cardAlt: Color,
    val border: Color,
    val primary: Color,
    val accent: Color,
    val success: Color,
    val danger: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val glassBorder: Color
)