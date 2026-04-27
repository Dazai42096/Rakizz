package com.rakizz.student.presentation.profile

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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

private val ScreenTop = Color(0xFF03112A)
private val ScreenBottom = Color(0xFF000000)
private val WhiteText = Color(0xFFF7F8FA)
private val SecondaryText = Color(0xFF8A92A3)
private val PrimaryBlue = Color(0xFF2457D6)
private val CardSurface = Color(0xFF171717)
private val CardBorder = Color(0xFF2A2F3A)
private val BadgeSurface = Color(0xFF1E1E22)
private val RedAccent = Color(0xFFFF2D2D)
private val GreenAccent = Color(0xFF22C55E)

@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onGeneratePairCodeClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
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
                top = 18.dp,
                bottom = 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                ProfileTopBar(
                    onBackClick = onBackClick,
                    onSettingsClick = onSettingsClick
                )
            }

            item {
                ProfileHero()
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        value = "12",
                        label = "LEVEL",
                        valueColor = WhiteText
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        value = "85%",
                        label = "FOCUS",
                        valueColor = PrimaryBlue
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        value = "24",
                        label = "STREAK",
                        valueColor = WhiteText
                    )
                }
            }

            item {
                Text(
                    text = "ACCOUNT & SETTINGS",
                    color = SecondaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )
            }

            item {
                SettingsItemCard(
                    icon = Icons.Rounded.QrCode2,
                    iconBackground = Color(0xFF132C63),
                    iconTint = PrimaryBlue,
                    title = "Generate Pair Code",
                    subtitle = "Connect with parent app",
                    onClick = onGeneratePairCodeClick
                )
            }

            item {
                SettingsItemCard(
                    icon = Icons.Rounded.Notifications,
                    iconBackground = Color(0xFF233149),
                    iconTint = Color(0xFFD7DCE6),
                    title = "Notifications",
                    subtitle = "Study reminders & alerts",
                    onClick = onNotificationsClick
                )
            }

            item {
                SettingsItemCard(
                    icon = Icons.Rounded.Security,
                    iconBackground = Color(0xFF233149),
                    iconTint = Color(0xFFD7DCE6),
                    title = "Privacy",
                    subtitle = "Password & data settings",
                    onClick = onPrivacyClick
                )
            }

            item {
                LogoutCard(
                    onClick = onLogoutClick
                )
            }

            item {
                Text(
                    text = "Rakizz App v2.4.1 (Build 890)",
                    color = Color(0xFF566074),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ProfileTopBar(
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = WhiteText,
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = "Student Profile",
            color = WhiteText,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = "Settings",
                tint = WhiteText,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun ProfileHero() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(184.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(176.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PrimaryBlue.copy(alpha = 0.28f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .size(156.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD9D2C8))
                    .border(4.dp, Color(0xFF101319), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6E726F))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .width(54.dp)
                            .height(64.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF5C615E))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Student Profile",
                        color = Color(0xFF444444),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 18.dp, bottom = 16.dp)
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF111111))
                    .border(1.dp, CardBorder, CircleShape)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Edit profile",
                    tint = WhiteText,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Alex Johnson",
            color = WhiteText,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SmallBadge(
                text = "GRADE 10",
                background = BadgeSurface,
                textColor = Color(0xFFD9DDE6)
            )
            SmallBadge(
                text = "STUDENT",
                background = Color(0xFF0E254F),
                textColor = Color(0xFF7DB2FF)
            )
        }
    }
}

@Composable
private fun SmallBadge(
    text: String,
    background: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    valueColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = CardSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = valueColor,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = label,
                color = SecondaryText,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )
        }
    }
}

@Composable
private fun SettingsItemCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(CardSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(26.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(iconBackground)
                .border(
                    width = 1.dp,
                    color = iconTint.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(20.dp)
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

        Spacer(modifier = Modifier.width(18.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = WhiteText,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                color = SecondaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = SecondaryText,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun LogoutCard(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(CardSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(26.dp))
            .clickable { onClick() }
            .padding(horizontal = 22.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
            contentDescription = "Log out",
            tint = RedAccent,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Log Out",
            color = RedAccent,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}