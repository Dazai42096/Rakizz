package com.rakizz.student.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

private val ScreenTop = Color(0xFF020B1F)
private val ScreenBottom = Color(0xFF000000)
private val PrimaryBlue = Color(0xFF1F5BDE)
private val ParentGreen = Color(0xFF10B981)
private val WhiteText = Color(0xFFF5F7FB)
private val SecondaryText = Color(0xFF8A92A3)
private val CardBg = Color(0xFF121317)
private val CardBorder = Color(0xFF242832)

@Composable
fun HomeScreen(
    onOpenMaterials: () -> Unit,
    onOpenAssignments: () -> Unit,
    onOpenQuizzes: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenParentFocus: () -> Unit,
    onOpenProfile: () -> Unit,
    onLogout: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            if (uiState.role == "student") {
                HomeBottomBar(
                    onOpenMaterials = onOpenMaterials,
                    onOpenAssignments = onOpenAssignments,
                    onOpenQuizzes = onOpenQuizzes
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(ScreenTop, ScreenBottom)
                    )
                )
                .padding(paddingValues)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            HomeTopBar(
                email = uiState.email,
                onLogout = onLogout
            )

            Spacer(modifier = Modifier.height(18.dp))

            when {
                uiState.isLoading -> {
                    LoadingCard()
                }

                uiState.error != null -> {
                    ErrorCard(
                        message = uiState.error ?: "Something went wrong",
                        onRetry = viewModel::loadAccount
                    )
                }

                uiState.role == "parent" -> {
                    ParentHomeContent(
                        onOpenParentFocus = onOpenParentFocus,
                        onOpenProfile = onOpenProfile
                    )
                }

                else -> {
                    StudentHomeContent(
                        onOpenMaterials = onOpenMaterials,
                        onOpenAssignments = onOpenAssignments,
                        onOpenQuizzes = onOpenQuizzes,
                        onOpenFocus = onOpenFocus,
                        onOpenProfile = onOpenProfile
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StudentHomeContent(
    onOpenMaterials: () -> Unit,
    onOpenAssignments: () -> Unit,
    onOpenQuizzes: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenProfile: () -> Unit
) {
    HomeSummaryCard(
        title = "Student Dashboard",
        text = "Use your materials, quizzes, assignments, focus rules, and account page from one place."
    )

    Spacer(modifier = Modifier.height(28.dp))

    SectionHeader(title = "Checkpoint Features")

    Spacer(modifier = Modifier.height(16.dp))

    FeatureCard(
        title = "Study Materials",
        description = "View and manage saved study materials.",
        buttonText = "Open Materials",
        accentColor = PrimaryBlue,
        onClick = onOpenMaterials
    )

    Spacer(modifier = Modifier.height(14.dp))

    FeatureCard(
        title = "AI Quizzes",
        description = "Generate and solve quizzes from uploaded materials.",
        buttonText = "Open Quizzes",
        accentColor = Color(0xFF8B5CF6),
        onClick = onOpenQuizzes
    )

    Spacer(modifier = Modifier.height(14.dp))

    FeatureCard(
        title = "Assignments",
        description = "Track assignments and reminders.",
        buttonText = "Open Assignments",
        accentColor = Color(0xFF3B82F6),
        onClick = onOpenAssignments
    )

    Spacer(modifier = Modifier.height(14.dp))

    FeatureCard(
        title = "Focus Mode",
        description = "Sync phone apps and view focus rules created by the parent.",
        buttonText = "Open Focus",
        accentColor = Color(0xFFF59E0B),
        onClick = onOpenFocus
    )

    Spacer(modifier = Modifier.height(14.dp))

    FeatureCard(
        title = "Profile",
        description = "Open the account page for profile and account options.",
        buttonText = "Open Profile",
        accentColor = Color(0xFF64748B),
        onClick = onOpenProfile
    )
}

@Composable
private fun ParentHomeContent(
    onOpenParentFocus: () -> Unit,
    onOpenProfile: () -> Unit
) {
    HomeSummaryCard(
        title = "Parent Dashboard",
        text = "Create focus-time rules and manage the parent account from one place."
    )

    Spacer(modifier = Modifier.height(28.dp))

    SectionHeader(title = "Parent Tools")

    Spacer(modifier = Modifier.height(16.dp))

    FeatureCard(
        title = "Focus Rules",
        description = "Load the student's installed apps, choose apps to block, and set focus time.",
        buttonText = "Open Focus Rules",
        accentColor = ParentGreen,
        onClick = onOpenParentFocus
    )

    Spacer(modifier = Modifier.height(14.dp))

    FeatureCard(
        title = "Profile",
        description = "Open the account page for profile and account options.",
        buttonText = "Open Profile",
        accentColor = Color(0xFF64748B),
        onClick = onOpenProfile
    )
}

@Composable
private fun HomeTopBar(
    email: String,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RakizzBrand()

        Spacer(modifier = Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.End
        ) {
            if (email.isNotBlank()) {
                Text(
                    text = email,
                    color = SecondaryText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "Sign out",
                color = PrimaryBlue,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    onLogout()
                }
            )
        }
    }
}

@Composable
private fun RakizzBrand() {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .rotate(45f)
                .background(
                    color = PrimaryBlue,
                    shape = RoundedCornerShape(5.dp)
                )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = buildAnnotatedString {
                append("Rakizz")
                withStyle(SpanStyle(color = PrimaryBlue)) {
                    append("!!")
                }
            },
            color = WhiteText,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun HomeSummaryCard(
    title: String,
    text: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(26.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF2B3F6F),
                        Color(0xFF1A2232),
                        Color(0xFF10131A)
                    )
                )
            )
            .padding(22.dp)
    ) {
        Column {
            Text(
                text = title,
                color = WhiteText,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = text,
                color = SecondaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String
) {
    Text(
        text = title,
        color = WhiteText,
        fontSize = 22.sp,
        fontWeight = FontWeight.ExtraBold
    )
}

@Composable
private fun FeatureCard(
    title: String,
    description: String,
    buttonText: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.18f))
                    .border(
                        width = 1.dp,
                        color = accentColor.copy(alpha = 0.28f),
                        shape = RoundedCornerShape(14.dp)
                    )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                color = WhiteText,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                color = SecondaryText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onClick,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = WhiteText
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Text(
                    text = buttonText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LoadingCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
            .padding(22.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = PrimaryBlue)
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        Text(
            text = message,
            color = Color(0xFFFF9F0A),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlue,
                contentColor = WhiteText
            )
        ) {
            Text("Retry")
        }
    }
}

@Composable
private fun HomeBottomBar(
    onOpenMaterials: () -> Unit,
    onOpenAssignments: () -> Unit,
    onOpenQuizzes: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .border(1.dp, Color.White.copy(alpha = 0.06f))
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomItem(
            label = "Home",
            selected = true,
            icon = {
                HomeGlyph(selected = true)
            },
            onClick = {}
        )

        BottomItem(
            label = "Materials",
            selected = false,
            icon = {
                BookGlyph(selected = false)
            },
            onClick = onOpenMaterials
        )

        BottomItem(
            label = "Quiz",
            selected = false,
            icon = {
                QuizGlyph(selected = false)
            },
            onClick = onOpenQuizzes
        )

        BottomItem(
            label = "Tasks",
            selected = false,
            icon = {
                ClipboardGlyph(selected = false)
            },
            onClick = onOpenAssignments
        )
    }
}

@Composable
private fun BottomItem(
    label: String,
    selected: Boolean,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val textColor = if (selected) {
        PrimaryBlue
    } else {
        SecondaryText
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                onClick()
            }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon()

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun HomeGlyph(
    selected: Boolean
) {
    val color = if (selected) {
        PrimaryBlue
    } else {
        SecondaryText
    }

    Box(
        modifier = Modifier.size(18.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .width(15.dp)
                .height(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 2.dp)
                .rotate(45f)
                .size(10.dp)
                .background(color)
        )
    }
}

@Composable
private fun BookGlyph(
    selected: Boolean
) {
    val color = if (selected) {
        PrimaryBlue
    } else {
        SecondaryText
    }

    Box(
        modifier = Modifier
            .width(18.dp)
            .height(16.dp)
            .clip(RoundedCornerShape(3.dp))
            .border(2.dp, color, RoundedCornerShape(3.dp))
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(2.dp)
                .height(12.dp)
                .background(color)
        )
    }
}

@Composable
private fun QuizGlyph(
    selected: Boolean
) {
    val color = if (selected) {
        PrimaryBlue
    } else {
        SecondaryText
    }

    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(RoundedCornerShape(3.dp))
            .border(2.dp, color, RoundedCornerShape(3.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "?",
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun ClipboardGlyph(
    selected: Boolean
) {
    val color = if (selected) {
        PrimaryBlue
    } else {
        SecondaryText
    }

    Box(
        modifier = Modifier
            .width(17.dp)
            .height(18.dp)
            .clip(RoundedCornerShape(3.dp))
            .border(2.dp, color, RoundedCornerShape(3.dp))
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-2).dp)
                .width(8.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
    }
}