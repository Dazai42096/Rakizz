package com.rakizz.student.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakizz.student.presentation.common.components.RakizzGlassCard
import com.rakizz.student.presentation.common.components.RakizzLogoIcon
import com.rakizz.student.presentation.common.components.RakizzMetricCard
import com.rakizz.student.presentation.common.components.RakizzPrimaryButton
import com.rakizz.student.presentation.common.components.RakizzStatusChip
import com.rakizz.student.presentation.theme.RakizzColors
import com.rakizz.student.presentation.theme.RakizzThemeController

@Composable
fun HomeScreen(
    onOpenMaterials: () -> Unit,
    onOpenAssignments: () -> Unit,
    onOpenQuizzes: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenProgress: () -> Unit = {},
    onLogout: () -> Unit
) {
    val isDarkMode = RakizzThemeController.isCurrentlyDark()

    var showContent by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        showContent = true
    }

    Scaffold(
        containerColor = RakizzColors.Background,
        bottomBar = {
            FuturisticBottomBar(
                onOpenHome = {},
                onOpenLibrary = onOpenMaterials,
                onOpenQuizzes = onOpenQuizzes,
                onOpenFocus = onOpenFocus,
                onOpenProfile = onOpenProfile
            )
        }
    ) { paddingValues ->
        Box(
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
                .padding(paddingValues)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = 450)
                ) + slideInVertically(
                    animationSpec = tween(durationMillis = 450),
                    initialOffsetY = {
                        it / 5
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FuturisticHomeTopBar(
                        isDarkMode = isDarkMode,
                        onToggleTheme = {
                            RakizzThemeController.toggleTheme()
                        },
                        onProfileClick = onOpenProfile,
                        onLogout = onLogout
                    )

                    TodayMissionCard(
                        onContinueStudying = onOpenMaterials
                    )

                    HomeStatsGrid(
                        onOpenMaterials = onOpenMaterials,
                        onOpenQuizzes = onOpenQuizzes,
                        onOpenAssignments = onOpenAssignments,
                        onOpenProgress = onOpenProgress
                    )

                    AiRecommendationCard(
                        onOpenQuizzes = onOpenQuizzes
                    )

                    SectionHeader(
                        title = "Command center",
                        subtitle = "Fast access to the main Rakizz systems."
                    )

                    QuickActionsGrid(
                        onOpenMaterials = onOpenMaterials,
                        onOpenQuizzes = onOpenQuizzes,
                        onOpenAssignments = onOpenAssignments,
                        onOpenFocus = onOpenFocus,
                        onOpenProgress = onOpenProgress,
                        onOpenProfile = onOpenProfile
                    )

                    FocusShieldPreview(
                        onOpenFocus = onOpenFocus
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun FuturisticHomeTopBar(
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RakizzLogoIcon(
            modifier = Modifier.size(50.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Rakizz",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "AI learning + focus control",
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }

        ThemeSwitch(
            isDarkMode = isDarkMode,
            onClick = onToggleTheme
        )

        Spacer(modifier = Modifier.width(10.dp))

        Surface(
            modifier = Modifier
                .size(44.dp)
                .clickable {
                    onProfileClick()
                },
            shape = CircleShape,
            color = RakizzColors.PrimarySoft,
            border = BorderStroke(
                width = 1.dp,
                color = RakizzColors.Primary.copy(alpha = 0.35f)
            ),
            shadowElevation = 2.dp
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "S",
                    color = RakizzColors.Primary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }

    Text(
        text = "Sign out",
        color = RakizzColors.TextMuted,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clickable {
            onLogout()
        }
    )
}

@Composable
private fun ThemeSwitch(
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    val knobOffset by animateDpAsState(
        targetValue = if (isDarkMode) {
            40.dp
        } else {
            0.dp
        },
        animationSpec = tween(durationMillis = 220),
        label = "home_theme_switch_offset"
    )

    Surface(
        modifier = Modifier
            .width(82.dp)
            .height(42.dp)
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(50),
        color = RakizzColors.Card,
        border = BorderStroke(
            width = 1.dp,
            color = RakizzColors.CardBorder
        ),
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .offset(x = knobOffset)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(RakizzColors.Primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isDarkMode) "D" else "L",
                    color = RakizzColors.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun TodayMissionCard(
    onContinueStudying: () -> Unit
) {
    RakizzGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 32.dp,
        glow = true,
        contentPadding = PaddingValues(22.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                RakizzStatusChip(
                    text = "TODAY MODE",
                    icon = Icons.Rounded.AutoAwesome
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Today’s Focus Mission",
                    color = RakizzColors.TextMain,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Complete 1 AI quiz and finish 45 minutes of focused study time.",
                    color = RakizzColors.TextSecond,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            MissionProgressRing(
                progress = 0.68f
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        RakizzPrimaryButton(
            text = "Continue studying",
            icon = Icons.Rounded.AutoAwesome,
            onClick = onContinueStudying
        )
    }
}

@Composable
private fun MissionProgressRing(
    progress: Float
) {
    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 500),
        label = "mission_ring_scale"
    )

    Surface(
        modifier = Modifier
            .size(88.dp)
            .graphicsLayer(
                scaleX = animatedScale,
                scaleY = animatedScale
            ),
        shape = CircleShape,
        color = RakizzColors.PrimarySoft,
        border = BorderStroke(
            width = 1.dp,
            color = RakizzColors.Primary.copy(alpha = 0.35f)
        )
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                RakizzColors.Primary.copy(alpha = 0.42f),
                                RakizzColors.PrimarySoft
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = RakizzColors.TextMain,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun HomeStatsGrid(
    onOpenMaterials: () -> Unit,
    onOpenQuizzes: () -> Unit,
    onOpenAssignments: () -> Unit,
    onOpenProgress: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RakizzMetricCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onOpenMaterials()
                    },
                title = "MATERIALS",
                value = "12",
                subtitle = "Study files",
                accentColor = RakizzColors.Primary
            )

            RakizzMetricCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onOpenQuizzes()
                    },
                title = "QUIZZES",
                value = "8",
                subtitle = "AI practice",
                accentColor = RakizzColors.Accent
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RakizzMetricCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onOpenAssignments()
                    },
                title = "TASKS",
                value = "4",
                subtitle = "Pending",
                accentColor = RakizzColors.Warning
            )

            RakizzMetricCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onOpenProgress()
                    },
                title = "FOCUS",
                value = "70%",
                subtitle = "Today score",
                accentColor = RakizzColors.Success
            )
        }
    }
}

@Composable
private fun AiRecommendationCard(
    onOpenQuizzes: () -> Unit
) {
    RakizzGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 28.dp,
        onClick = onOpenQuizzes,
        glow = false
    ) {
        Row(
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = RakizzColors.Primary,
                shadowElevation = 4.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AI",
                        color = RakizzColors.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "AI Tutor recommendation",
                    color = RakizzColors.TextMain,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Generate a mixed quiz from your latest material to unlock stronger focus progress today.",
                    color = RakizzColors.TextSecond,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )

                Spacer(modifier = Modifier.height(12.dp))

                RakizzStatusChip(
                    text = "Open AI quizzes",
                    color = RakizzColors.Primary,
                    softColor = RakizzColors.PrimarySoft
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String
) {
    Column {
        Text(
            text = title,
            color = RakizzColors.TextMain,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            color = RakizzColors.TextSecond,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun QuickActionsGrid(
    onOpenMaterials: () -> Unit,
    onOpenQuizzes: () -> Unit,
    onOpenAssignments: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                label = "Library",
                description = "Materials",
                badge = "PDF",
                accentColor = RakizzColors.Primary,
                onClick = onOpenMaterials
            )

            QuickActionCard(
                modifier = Modifier.weight(1f),
                label = "AI Quiz",
                description = "Practice",
                badge = "AI",
                accentColor = RakizzColors.Accent,
                onClick = onOpenQuizzes
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                label = "Tasks",
                description = "Assignments",
                badge = "DUE",
                accentColor = RakizzColors.Warning,
                onClick = onOpenAssignments
            )

            QuickActionCard(
                modifier = Modifier.weight(1f),
                label = "Shield",
                description = "Focus",
                badge = "LOCK",
                accentColor = RakizzColors.Success,
                onClick = onOpenFocus
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                label = "Progress",
                description = "Reports",
                badge = "STATS",
                accentColor = RakizzColors.Primary,
                onClick = onOpenProgress
            )

            QuickActionCard(
                modifier = Modifier.weight(1f),
                label = "Profile",
                description = "Account",
                badge = "USER",
                accentColor = RakizzColors.Accent,
                onClick = onOpenProfile
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier,
    label: String,
    description: String,
    badge: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    RakizzGlassCard(
        modifier = modifier.height(150.dp),
        cornerRadius = 26.dp,
        onClick = onClick,
        contentPadding = PaddingValues(16.dp)
    ) {
        RakizzStatusChip(
            text = badge,
            color = accentColor,
            softColor = accentColor.copy(alpha = 0.13f)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = label,
            color = RakizzColors.TextMain,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = description,
            color = RakizzColors.TextSecond,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun FocusShieldPreview(
    onOpenFocus: () -> Unit
) {
    RakizzGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 30.dp,
        glow = true,
        onClick = onOpenFocus
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(22.dp),
                color = RakizzColors.PrimarySoft,
                border = BorderStroke(
                    width = 1.dp,
                    color = RakizzColors.Primary.copy(alpha = 0.35f)
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🛡",
                        fontSize = 28.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Focus Shield",
                        color = RakizzColors.TextMain,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    RakizzStatusChip(
                        text = "ACTIVE",
                        color = RakizzColors.Success,
                        softColor = RakizzColors.Success.copy(alpha = 0.12f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Distracting apps stay locked until learning goals are completed.",
                    color = RakizzColors.TextSecond,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
            }
        }
    }
}

@Composable
private fun FuturisticBottomBar(
    onOpenHome: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenQuizzes: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Surface(
        color = Color.Transparent,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = RakizzColors.Card.copy(alpha = 0.96f),
                border = BorderStroke(
                    width = 1.dp,
                    color = RakizzColors.CardBorder
                ),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem(
                        label = "Home",
                        selected = true,
                        onClick = onOpenHome
                    )

                    BottomNavItem(
                        label = "Library",
                        selected = false,
                        onClick = onOpenLibrary
                    )

                    BottomNavItem(
                        label = "Quiz",
                        selected = false,
                        onClick = onOpenQuizzes
                    )

                    BottomNavItem(
                        label = "Focus",
                        selected = false,
                        onClick = onOpenFocus
                    )

                    BottomNavItem(
                        label = "Profile",
                        selected = false,
                        onClick = onOpenProfile
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val itemScale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "bottom_nav_item_scale"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable {
                onClick()
            }
            .graphicsLayer(
                scaleX = itemScale,
                scaleY = itemScale
            )
            .background(
                if (selected) {
                    RakizzColors.PrimarySoft
                } else {
                    Color.Transparent
                }
            )
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) {
                    RakizzColors.Primary.copy(alpha = 0.22f)
                } else {
                    Color.Transparent
                },
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(if (selected) 8.dp else 6.dp)
                .clip(CircleShape)
                .background(
                    if (selected) {
                        RakizzColors.Primary
                    } else {
                        RakizzColors.TextMuted
                    }
                )
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = label,
            color = if (selected) {
                RakizzColors.Primary
            } else {
                RakizzColors.TextMuted
            },
            fontSize = 11.sp,
            fontWeight = if (selected) {
                FontWeight.ExtraBold
            } else {
                FontWeight.Bold
            },
            textAlign = TextAlign.Center
        )
    }
}