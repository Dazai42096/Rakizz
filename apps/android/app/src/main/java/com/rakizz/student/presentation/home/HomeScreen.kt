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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakizz.student.presentation.theme.RakizzColors

@Composable
fun HomeScreen(
    onOpenMaterials: () -> Unit,
    onOpenAssignments: () -> Unit,
    onOpenQuizzes: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenParentFocus: () -> Unit,
    onOpenProfile: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        containerColor = RakizzColors.Background,
        bottomBar = {
            StudentBottomBar(
                onOpenHome = {},
                onOpenLibrary = onOpenMaterials,
                onOpenQuizzes = onOpenQuizzes,
                onOpenFocus = onOpenFocus,
                onOpenProfile = onOpenProfile
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            RakizzColors.Background,
                            RakizzColors.BackgroundSoft
                        )
                    )
                )
                .padding(padding)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            HomeTopBar(
                onProfileClick = onOpenProfile,
                onLogout = onLogout
            )

            Spacer(modifier = Modifier.height(18.dp))

            MainWelcomeCard(
                onContinueStudying = onOpenMaterials
            )

            Spacer(modifier = Modifier.height(18.dp))

            FocusStatusCard(
                onOpenFocus = onOpenFocus
            )

            Spacer(modifier = Modifier.height(22.dp))

            SectionTitle(
                title = "Student tools",
                subtitle = "Use these modules Note preview."
            )

            Spacer(modifier = Modifier.height(12.dp))

            FeatureGrid(
                onOpenMaterials = onOpenMaterials,
                onOpenQuizzes = onOpenQuizzes,
                onOpenAssignments = onOpenAssignments,
                onOpenFocus = onOpenFocus
            )

            Spacer(modifier = Modifier.height(22.dp))

            ParentPortalCard(
                onOpenParentFocus = onOpenParentFocus
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HomeTopBar(
    onProfileClick: () -> Unit,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Rakizz",
                color = RakizzColors.PrimaryDark,
                fontSize = 27.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Study, focus, and unlock through learning.",
                color = RakizzColors.TextSecond,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Surface(
            modifier = Modifier
                .size(42.dp)
                .clickable {
                    onProfileClick()
                },
            shape = CircleShape,
            color = RakizzColors.Primary,
            shadowElevation = 2.dp
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "S",
                    color = RakizzColors.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // small logout link, not a big scary button
    Text(
        text = "Sign out",
        color = RakizzColors.TextMuted,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clickable {
            onLogout()
        }
    )
}

@Composable
private fun MainWelcomeCard(
    onContinueStudying: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = RakizzColors.Primary,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            RakizzColors.Primary,
                            RakizzColors.PrimaryDark
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Text(
                text = "Welcome back",
                color = RakizzColors.White,
                fontSize = 29.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your materials, AI quizzes, assignments, and focus rules are grouped here in one simple dashboard.",
                color = Color.White.copy(alpha = 0.86f),
                fontSize = 15.sp,
                lineHeight = 21.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onContinueStudying,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RakizzColors.White,
                    contentColor = RakizzColors.PrimaryDark
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 13.dp)
            ) {
                Text(
                    text = "Continue studying",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun FocusStatusCard(
    onOpenFocus: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .border(1.dp, RakizzColors.CardBorder, RoundedCornerShape(24.dp))
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(RakizzColors.PrimarySoft),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "F",
                    color = RakizzColors.Primary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Focus mode",
                    color = RakizzColors.TextMain,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Parent rules and quiz unlock are managed here.",
                    color = RakizzColors.TextSecond,
                    fontSize = 14.sp,
                    lineHeight = 19.sp
                )
            }

            Text(
                text = "Open",
                color = RakizzColors.Primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.clickable {
                    onOpenFocus()
                }
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String
) {
    Column {
        Text(
            text = title,
            color = RakizzColors.TextMain,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            color = RakizzColors.TextSecond,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun FeatureGrid(
    onOpenMaterials: () -> Unit,
    onOpenQuizzes: () -> Unit,
    onOpenAssignments: () -> Unit,
    onOpenFocus: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SmallFeatureCard(
                modifier = Modifier.weight(1f),
                title = "Library",
                shortText = "Upload and view materials.",
                badge = "PDF",
                onClick = onOpenMaterials
            )

            SmallFeatureCard(
                modifier = Modifier.weight(1f),
                title = "AI Quizzes",
                shortText = "Mixed quiz from material.",
                badge = "AI",
                onClick = onOpenQuizzes
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SmallFeatureCard(
                modifier = Modifier.weight(1f),
                title = "Assignments",
                shortText = "Tasks and reminders.",
                badge = "Due",
                onClick = onOpenAssignments
            )

            SmallFeatureCard(
                modifier = Modifier.weight(1f),
                title = "Focus",
                shortText = "Blocked apps + unlock quiz.",
                badge = "70%",
                onClick = onOpenFocus
            )
        }
    }
}

@Composable
private fun SmallFeatureCard(
    modifier: Modifier,
    title: String,
    shortText: String,
    badge: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(156.dp)
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(24.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, RakizzColors.CardBorder, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(RakizzColors.AccentSoft)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = badge,
                    color = RakizzColors.PrimaryDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                color = RakizzColors.TextMain,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = shortText,
                color = RakizzColors.TextSecond,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun ParentPortalCard(
    onOpenParentFocus: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = RakizzColors.CardSoft,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, RakizzColors.CardBorder, RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Text(
                text = "Parent portal",
                color = RakizzColors.PrimaryDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Use this only when previewnstrating parent controls: link student, load apps, select blocked apps, and save focus time.",
                color = RakizzColors.TextSecond,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onOpenParentFocus,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RakizzColors.Primary,
                    contentColor = RakizzColors.White
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Open parent controls",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun StudentBottomBar(
    onOpenHome: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenQuizzes: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Surface(
        color = RakizzColors.Card,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .border(1.dp, RakizzColors.CardBorder)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomItem(
                label = "Home",
                selected = true,
                onClick = onOpenHome
            )

            BottomItem(
                label = "Library",
                selected = false,
                onClick = onOpenLibrary
            )

            BottomItem(
                label = "Quizzes",
                selected = false,
                onClick = onOpenQuizzes
            )

            BottomItem(
                label = "Focus",
                selected = false,
                onClick = onOpenFocus
            )

            BottomItem(
                label = "Profile",
                selected = false,
                onClick = onOpenProfile
            )
        }
    }
}

@Composable
private fun BottomItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val textColor = if (selected) {
        RakizzColors.Primary
    } else {
        RakizzColors.TextMuted
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable {
                onClick()
            }
            .padding(horizontal = 9.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(if (selected) 8.dp else 6.dp)
                .clip(CircleShape)
                .background(textColor)
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold
        )
    }
}