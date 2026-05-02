package com.rakizz.student.presentation.auth.role

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rakizz.student.presentation.theme.RakizzColors

@Composable
fun ChooseRoleScreen(
    onContinueAsStudent: () -> Unit,
    onContinueAsParent: () -> Unit
) {
    Scaffold(
        containerColor = RakizzColors.Background
    ) { paddingValues ->
        LazyColumn(
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
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(
                start = 22.dp,
                end = 22.dp,
                top = 18.dp,
                bottom = 30.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                RoleHero()
            }

            item {
                RoleOptionCard(
                    letter = "S",
                    title = "Continue as Student",
                    subtitle = "Upload study materials, generate AI quizzes, track assignments, and unlock apps by learning.",
                    smallLabel = "Learning dashboard",
                    isPrimary = true,
                    onClick = onContinueAsStudent
                )
            }

            item {
                RoleOptionCard(
                    letter = "P",
                    title = "Continue as Parent",
                    subtitle = "Link a student account, choose blocked apps, and set focus time rules.",
                    smallLabel = "Parent controls",
                    isPrimary = false,
                    onClick = onContinueAsParent
                )
            }

            item {
                InfoCard()
            }
        }
    }
}

@Composable
private fun RoleHero() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = RakizzColors.Primary,
        shadowElevation = 3.dp
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(76.dp),
                shape = CircleShape,
                color = RakizzColors.White.copy(alpha = 0.16f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "R",
                        color = RakizzColors.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Choose your role",
                color = RakizzColors.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Rakizz separates student learning tools from parent focus controls.",
                color = RakizzColors.White.copy(alpha = 0.86f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )
        }
    }
}

@Composable
private fun RoleOptionCard(
    letter: String,
    title: String,
    subtitle: String,
    smallLabel: String,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    val accent = if (isPrimary) {
        RakizzColors.Primary
    } else {
        RakizzColors.Accent
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(28.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isPrimary) {
                RakizzColors.Primary.copy(alpha = 0.35f)
            } else {
                RakizzColors.CardBorder
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(58.dp),
                    shape = CircleShape,
                    color = accent.copy(alpha = 0.13f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter,
                            color = accent,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        color = RakizzColors.TextMain,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = smallLabel,
                        color = accent,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = subtitle,
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPrimary) {
                        RakizzColors.Primary
                    } else {
                        RakizzColors.PrimaryDark
                    },
                    contentColor = RakizzColors.White
                )
            ) {
                Text(
                    text = if (isPrimary) {
                        "Use Student Portal"
                    } else {
                        "Use Parent Portal"
                    },
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun InfoCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = RakizzColors.PrimarySoft,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "How roles work",
                color = RakizzColors.PrimaryDark,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "The student account handles materials, quizzes, assignments, and focus mode. The parent account creates focus rules for the linked student.",
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}