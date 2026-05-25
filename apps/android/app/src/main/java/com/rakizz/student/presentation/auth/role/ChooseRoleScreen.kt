package com.rakizz.student.presentation.auth.role

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
fun ChooseRoleScreen(
    navController: NavController? = null,

    // Generic role callback.
    onRoleSelected: (String) -> Unit = {},

    // These two names are used by your RakizzNavHost.kt.
    onContinueAsStudent: () -> Unit = {},
    onContinueAsParent: () -> Unit = {},

    // Extra student callbacks for compatibility.
    onStudentClick: () -> Unit = {},
    onStudentSelected: () -> Unit = {},
    onChooseStudentClick: () -> Unit = {},
    onNavigateToStudentSignUp: () -> Unit = {},
    onStudentSignUpClick: () -> Unit = {},

    // Extra parent callbacks for compatibility.
    onParentClick: () -> Unit = {},
    onParentSelected: () -> Unit = {},
    onChooseParentClick: () -> Unit = {},
    onNavigateToParentSignUp: () -> Unit = {},
    onParentSignUpClick: () -> Unit = {},

    // Back/login callbacks.
    onBackClick: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onBackToLogin: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    val colors = chooseRoleColors()

    var selectedRole by remember { mutableStateOf("student") }

    val infiniteTransition = rememberInfiniteTransition(label = "choose_role_animation")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "choose_role_glow"
    )

    fun goBack() {
        if (navController != null) {
            navController.popBackStack()
        } else {
            onBackClick()
            onNavigateBack()
            onBackToLogin()
        }
    }

    fun continueWithRole() {
        if (selectedRole == "student") {
            onRoleSelected("student")
            onContinueAsStudent()
            onStudentClick()
            onStudentSelected()
            onChooseStudentClick()
            onNavigateToStudentSignUp()
            onStudentSignUpClick()
        } else {
            onRoleSelected("parent")
            onContinueAsParent()
            onParentClick()
            onParentSelected()
            onChooseParentClick()
            onNavigateToParentSignUp()
            onParentSignUpClick()
        }
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
        // Soft futuristic background glow.
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
                .padding(horizontal = 22.dp)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            ChooseRoleTopBar(
                colors = colors,
                onBackClick = { goBack() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            RoleLogoSection(
                colors = colors,
                glowScale = glowScale
            )

            Spacer(modifier = Modifier.height(24.dp))

            RoleHeroCard(colors = colors)

            Spacer(modifier = Modifier.height(16.dp))

            RoleOptionCard(
                title = "Student",
                subtitle = "Upload materials, solve AI quizzes, track assignments, and use focus mode.",
                iconText = "🎓",
                selected = selectedRole == "student",
                mainColor = colors.primary,
                colors = colors,
                onClick = { selectedRole = "student" }
            )

            Spacer(modifier = Modifier.height(12.dp))

            RoleOptionCard(
                title = "Parent / Guardian",
                subtitle = "Link with a student, monitor progress, select distracting apps, and set focus rules.",
                iconText = "👨‍👩‍👧",
                selected = selectedRole == "parent",
                mainColor = colors.accent,
                colors = colors,
                onClick = { selectedRole = "parent" }
            )

            Spacer(modifier = Modifier.height(18.dp))

            ContinueButton(
                selectedRole = selectedRole,
                colors = colors,
                onClick = { continueWithRole() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            LoginRedirectButton(
                colors = colors,
                onClick = {
                    onLoginClick()
                    goBack()
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            RoleExplanationCard(colors = colors)

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ChooseRoleTopBar(
    colors: ChooseRoleColors,
    onBackClick: () -> Unit
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
                text = "Choose Role",
                color = colors.textPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Select how you will use Rakizz",
                color = colors.textSecondary,
                fontSize = 13.sp
            )
        }

        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(colors.primary.copy(alpha = 0.14f))
                .border(1.dp, colors.primary.copy(alpha = 0.32f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "R",
                color = colors.primary,
                fontSize = 21.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun RoleLogoSection(
    colors: ChooseRoleColors,
    glowScale: Float
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .graphicsLayer {
                    scaleX = glowScale
                    scaleY = glowScale
                }
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            colors.primary,
                            colors.accent
                        )
                    )
                )
                .border(3.dp, colors.glassBorder, RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "R",
                color = Color.White,
                fontSize = 50.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Rakizz",
            color = colors.textPrimary,
            fontSize = 33.sp,
            fontWeight = FontWeight.Black
        )

        Text(
            text = "One system. Two experiences.",
            color = colors.textSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun RoleHeroCard(colors: ChooseRoleColors) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            colors.primary.copy(alpha = 0.25f),
                            colors.card,
                            colors.cardAlt
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = colors.primary.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.32f))
                ) {
                    Text(
                        text = "● Personalized setup",
                        color = colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Start with the right experience for your account.",
                    color = colors.textPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 30.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Students focus on learning and quizzes. Parents manage focus rules, app blocking, and progress monitoring.",
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun RoleOptionCard(
    title: String,
    subtitle: String,
    iconText: String,
    selected: Boolean,
    mainColor: Color,
    colors: ChooseRoleColors,
    onClick: () -> Unit
) {
    val background = if (selected) {
        mainColor.copy(alpha = 0.15f)
    } else {
        colors.card
    }

    val border = if (selected) {
        mainColor.copy(alpha = 0.45f)
    } else {
        colors.border
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        border = BorderStroke(1.dp, border)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(mainColor.copy(alpha = 0.16f))
                    .border(1.dp, mainColor.copy(alpha = 0.32f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iconText,
                    fontSize = 28.sp
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        color = colors.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.weight(1f)
                    )

                    if (selected) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = mainColor.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, mainColor.copy(alpha = 0.34f))
                        ) {
                            Text(
                                text = "Selected",
                                color = mainColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = subtitle,
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }

            Text(
                text = if (selected) "✓" else "○",
                color = mainColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun ContinueButton(
    selectedRole: String,
    colors: ChooseRoleColors,
    onClick: () -> Unit
) {
    val label = if (selectedRole == "student") {
        "Continue as Student"
    } else {
        "Continue as Parent"
    }

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
            text = label,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun LoginRedirectButton(
    colors: ChooseRoleColors,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = colors.card,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "Already have an account? Login",
                color = colors.textSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RoleExplanationCard(colors: ChooseRoleColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.primary.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Checkpoint explanation",
                color = colors.primary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "This screen separates the app into two user flows: student learning features and parent monitoring/focus-control features.",
                color = colors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = colors.border)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "It supports the Rakizz requirement for standard user management and different account roles.",
                color = colors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun CircleIconButton(
    text: String,
    colors: ChooseRoleColors,
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
private fun chooseRoleColors(): ChooseRoleColors {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    return if (isDark) {
        ChooseRoleColors(
            backgroundTop = Color(0xFF020617),
            backgroundMiddle = Color(0xFF07111F),
            backgroundBottom = Color(0xFF000000),
            card = Color(0xE60B1220),
            cardAlt = Color(0xCC111A2E),
            border = Color(0x334D9DFF),
            primary = Color(0xFF2F80FF),
            accent = Color(0xFF00D4FF),
            textPrimary = Color.White,
            textSecondary = Color(0xFF94A3B8),
            glassBorder = Color(0x66FFFFFF)
        )
    } else {
        ChooseRoleColors(
            backgroundTop = Color(0xFFF8FBFF),
            backgroundMiddle = Color(0xFFEAF4FF),
            backgroundBottom = Color(0xFFFFFFFF),
            card = Color(0xFFFFFFFF),
            cardAlt = Color(0xFFF1F7FF),
            border = Color(0x263B82F6),
            primary = Color(0xFF2563EB),
            accent = Color(0xFF06B6D4),
            textPrimary = Color(0xFF0F172A),
            textSecondary = Color(0xFF64748B),
            glassBorder = Color(0xFFFFFFFF)
        )
    }
}

private data class ChooseRoleColors(
    val backgroundTop: Color,
    val backgroundMiddle: Color,
    val backgroundBottom: Color,
    val card: Color,
    val cardAlt: Color,
    val border: Color,
    val primary: Color,
    val accent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val glassBorder: Color
)