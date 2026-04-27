package com.rakizz.student.presentation.auth.role

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ScreenTop = Color(0xFF03112A)
private val ScreenBottom = Color(0xFF000000)
private val WhiteText = Color(0xFFF7F8FA)
private val SecondaryText = Color(0xFF8A92A3)
private val CardSurface = Color(0xFF171717)
private val CardBorder = Color(0xFF2A2F3A)
private val PrimaryBlue = Color(0xFF2457D6)
private val BlueSoft = Color(0xFF6EA7FF)
private val PurpleSoft = Color(0xFFC59BFF)

@Composable
fun ChooseRoleScreen(
    onContinueAsStudent: () -> Unit,
    onContinueAsParent: () -> Unit
) {
    Scaffold(
        containerColor = Color.Black
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(ScreenTop, ScreenBottom)
                    )
                )
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                top = 28.dp,
                bottom = 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                RoleHero()
            }

            item {
                RoleOptionCard(
                    title = "Continue as Student",
                    subtitle = "Access study materials, focus mode, assignments, and AI quizzes.",
                    icon = Icons.Rounded.School,
                    iconTint = BlueSoft,
                    iconBackground = Color(0xFF112B5E),
                    accent = PrimaryBlue,
                    primary = true,
                    onClick = onContinueAsStudent
                )
            }

            item {
                RoleOptionCard(
                    title = "Continue as Parent",
                    subtitle = "Monitor progress, manage focus rules, and review student activity.",
                    icon = Icons.Rounded.Shield,
                    iconTint = PurpleSoft,
                    iconBackground = Color(0xFF2E2145),
                    accent = Color(0xFF7B5CFF),
                    primary = false,
                    onClick = onContinueAsParent
                )
            }

            item {
                ConnectedInfoCard()
            }
        }
    }
}

@Composable
private fun RoleHero() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(146.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(146.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PrimaryBlue.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF111723))
                    .border(1.dp, CardBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChildCare,
                        contentDescription = null,
                        tint = BlueSoft,
                        modifier = Modifier.size(28.dp)
                    )
                    Icon(
                        imageVector = Icons.Rounded.Groups,
                        contentDescription = null,
                        tint = PurpleSoft,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Choose Your Role",
            color = WhiteText,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Select how you want to use Rakizz today.",
            color = SecondaryText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun RoleOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    accent: Color,
    primary: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = CardSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (primary) {
                        Modifier
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        accent.copy(alpha = 0.10f),
                                        Color.Transparent
                                    )
                                )
                            )
                    } else {
                        Modifier
                    }
                )
                .border(
                    width = 1.dp,
                    color = if (primary) accent.copy(alpha = 0.26f) else CardBorder,
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(22.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(iconBackground)
                        .border(
                            1.dp,
                            iconTint.copy(alpha = 0.22f),
                            RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        color = WhiteText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (primary) "Recommended for this app" else "Parent control access",
                        color = accent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = subtitle,
                color = SecondaryText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (primary) PrimaryBlue else Color(0xFF24262E),
                    contentColor = WhiteText
                )
            ) {
                Text(
                    text = if (primary) "Continue as Student" else "Continue as Parent",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.width(10.dp))

                Icon(
                    imageVector = Icons.Rounded.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ConnectedInfoCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF101319)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Text(
                text = "ALREADY CONNECTED?",
                color = SecondaryText,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "You can switch role-specific screens later without losing your current session.",
                color = WhiteText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 22.sp
            )
        }
    }
}