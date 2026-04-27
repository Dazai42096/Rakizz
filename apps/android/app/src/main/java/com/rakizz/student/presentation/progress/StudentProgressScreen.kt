package com.rakizz.student.presentation.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.AutoAwesome
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
private val GreenAccent = Color(0xFF22C55E)
private val RedAccent = Color(0xFFFF3B30)

@Composable
fun StudentProgressScreen(
    onBackClick: () -> Unit,
    onDownloadClick: () -> Unit = {}
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
                ProgressTopBar(
                    onBackClick = onBackClick,
                    onDownloadClick = onDownloadClick
                )
            }

            item {
                InsightCard()
            }

            item {
                WeeklyFocusCard()
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "PASS RATE",
                        value = "92%",
                        subtitle = "↗ Top 5% of class",
                        valueColor = WhiteText,
                        subtitleColor = GreenAccent
                    )

                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "BLOCKED",
                        value = "142",
                        subtitle = "Apps stopped today",
                        valueColor = WhiteText,
                        subtitleColor = SecondaryText
                    )
                }
            }

            item {
                Text(
                    text = "TOP DISTRACTIONS BLOCKED",
                    color = SecondaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )
            }

            item {
                DistractionRow(
                    appName = "Instagram",
                    category = "Social Media",
                    savedTime = "45m",
                    accent = Color(0xFFE1306C),
                    shortLabel = "IG"
                )
            }

            item {
                DistractionRow(
                    appName = "TikTok",
                    category = "Entertainment",
                    savedTime = "32m",
                    accent = Color(0xFF25F4EE),
                    shortLabel = "TT"
                )
            }

            item {
                DistractionRow(
                    appName = "YouTube",
                    category = "Video",
                    savedTime = "15m",
                    accent = Color(0xFFFF0000),
                    shortLabel = "YT"
                )
            }
        }
    }
}

@Composable
private fun ProgressTopBar(
    onBackClick: () -> Unit,
    onDownloadClick: () -> Unit
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
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = "Student Progress",
            color = WhiteText,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onDownloadClick) {
            Icon(
                imageVector = Icons.Rounded.Download,
                contentDescription = "Download",
                tint = WhiteText,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun InsightCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = CardSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF132C63)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "AI INSIGHT",
                    color = Color(0xFFAFC6FF),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Your best focus days are Tue–Wed. You tend to study 25% longer when starting before 10 AM.",
                    color = WhiteText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 28.sp
                )
            }
        }
    }
}

@Composable
private fun WeeklyFocusCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = CardSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WEEKLY FOCUS",
                    color = SecondaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0E254F))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Total: 14h 20m",
                        color = PrimaryBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    WeeklyBar(day = "M", height = 56.dp, showBubble = false, value = "")
                    WeeklyBar(day = "T", height = 92.dp, showBubble = false, value = "")
                    WeeklyBar(day = "W", height = 128.dp, showBubble = true, value = "3.5h")
                    WeeklyBar(day = "T", height = 84.dp, showBubble = false, value = "")
                    WeeklyBar(day = "F", height = 72.dp, showBubble = false, value = "")
                    WeeklyBar(day = "S", height = 40.dp, showBubble = false, value = "")
                    WeeklyBar(day = "S", height = 24.dp, showBubble = false, value = "")
                }
            }
        }
    }
}

@Composable
private fun WeeklyBar(
    day: String,
    height: androidx.compose.ui.unit.Dp,
    showBubble: Boolean,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        if (showBubble) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = value,
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        } else {
            Spacer(modifier = Modifier.height(34.dp))
        }

        Box(
            modifier = Modifier
                .width(26.dp)
                .height(height)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PrimaryBlue,
                            Color(0xFF0E254F)
                        )
                    )
                )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = day,
            color = WhiteText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    valueColor: Color,
    subtitleColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = CardSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp)
        ) {
            Text(
                text = title,
                color = SecondaryText,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.1.sp
            )

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = value,
                color = valueColor,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = subtitle,
                color = subtitleColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DistractionRow(
    appName: String,
    category: String,
    savedTime: String,
    accent: Color,
    shortLabel: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CardSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(accent.copy(alpha = 0.16f))
                    .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = shortLabel,
                    color = accent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = appName,
                    color = WhiteText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = category,
                    color = SecondaryText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = savedTime,
                    color = RedAccent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Saved",
                    color = SecondaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}