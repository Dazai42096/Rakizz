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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

private val ScreenTop = Color(0xFF020B1F)
private val ScreenBottom = Color(0xFF000000)
private val PrimaryBlue = Color(0xFF1F5BDE)
private val WhiteText = Color(0xFFF5F7FB)
private val SecondaryText = Color(0xFF8A92A3)
private val CardBg = Color(0xFF121317)
private val CardBorder = Color(0xFF242832)
private val WarningSurface = Color(0xFF13213E)
private val WarningBorder = Color(0xFF264A8A)

@Composable
fun HomeScreen(
    onOpenMaterials: () -> Unit,
    onOpenAssignments: () -> Unit,
    onOpenQuizzes: () -> Unit,
    onOpenParentFocus: () -> Unit
) {
    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            HomeBottomBar(
                onOpenMaterials = onOpenMaterials,
                onOpenAssignments = onOpenAssignments,
                onOpenQuizzes = onOpenQuizzes
            )
        }
    ) { paddingValues ->

        // main scroll for home page
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
            HomeTopBar()

            Spacer(modifier = Modifier.height(18.dp))

            HomeSummaryCard()

            Spacer(modifier = Modifier.height(28.dp))

            SectionHeader(title = "Student Portal")

            Spacer(modifier = Modifier.height(16.dp))

            FeatureCard(
                title = "Study Materials",
                description = "Open your library, preview stored files, download them again, and generate quizzes from selected materials.",
                buttonText = "Open Materials",
                accentColor = PrimaryBlue,
                onClick = onOpenMaterials
            )

            Spacer(modifier = Modifier.height(14.dp))

            FeatureCard(
                title = "Assignments",
                description = "View stored assignments and add real deadlines that stay available later in the app.",
                buttonText = "Open Assignments",
                accentColor = Color(0xFF3B82F6),
                onClick = onOpenAssignments
            )

            Spacer(modifier = Modifier.height(14.dp))

            FeatureCard(
                title = "Quizzes",
                description = "Review the stored quizzes already generated from your materials and reopen them later.",
                buttonText = "Open Quizzes",
                accentColor = Color(0xFF8B5CF6),
                onClick = onOpenQuizzes
            )

            Spacer(modifier = Modifier.height(28.dp))

            SectionHeader(title = "Module Status")

            Spacer(modifier = Modifier.height(16.dp))

            FocusModuleStatusCard()

            Spacer(modifier = Modifier.height(14.dp))

            FeatureCard(
                title = "Parent Focus Tools",
                description = "Create a daily app limit rule for a linked student. This shows that the parent controls blocking rules.",
                buttonText = "Open Parent Tools",
                accentColor = Color(0xFF10B981),
                onClick = onOpenParentFocus
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HomeTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RakizzBrand()

        Spacer(modifier = Modifier.weight(1f))

        ProfileAvatar()
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
private fun ProfileAvatar() {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFB77850)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0D3BF))
                    .align(Alignment.TopCenter)
            )

            Box(
                modifier = Modifier
                    .width(14.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(Color(0xFFF0D3BF))
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun HomeSummaryCard() {
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
                text = "Student dashboard",
                color = WhiteText,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Home screen: main place for the student features.",
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
private fun FocusModuleStatusCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(WarningSurface)
            .border(1.dp, WarningBorder, RoundedCornerShape(22.dp))
            .padding(18.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Focus mode active",
                color = WhiteText,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // focus rules and usage reports are connected now
            // full blocking will be added after this
            Text(
                text = "Reading parent focus rules.",
                color = Color(0xFFD3DDF6),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
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