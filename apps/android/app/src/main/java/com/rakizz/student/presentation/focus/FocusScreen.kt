package com.rakizz.student.presentation.focus

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LockClock
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.domain.model.FocusPolicy
import com.rakizz.student.domain.model.UsagePackageSummary

private val ScreenTop = Color(0xFF03112A)
private val ScreenBottom = Color(0xFF000000)
private val WhiteText = Color(0xFFF7F8FA)
private val SecondaryText = Color(0xFF8A92A3)
private val PrimaryBlue = Color(0xFF2457D6)
private val CardSurface = Color(0xFF171717)
private val CardBorder = Color(0xFF2A2F3A)
private val WarningSurface = Color(0xFF13213E)
private val GreenAccent = Color(0xFF30D158)
private val OrangeAccent = Color(0xFFFF9F0A)

@Composable
fun FocusScreen(
    onBackClick: () -> Unit,
    onTakeUnlockQuizClick: () -> Unit = {},
    onViewProgressClick: () -> Unit = {},
    onGoToMaterialsClick: () -> Unit = {},
    viewModel: FocusViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    DisposableEffect(Unit) {
        onDispose {
            viewModel.refreshPermissionState()
        }
    }

    Scaffold(
        containerColor = Color.Black
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
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
                FocusTopBar(
                    onBackClick = onBackClick,
                    onRefreshClick = {
                        viewModel.loadFocusData()
                    }
                )
            }

            item {
                FocusStatusCard(uiState = uiState)
            }

            item {
                PermissionCard(
                    usageAccessGranted = uiState.usageAccessGranted,
                    onOpenSettingsClick = {
                        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    },
                    onRefreshClick = {
                        viewModel.refreshPermissionState()
                    }
                )
            }

            uiState.actionMessage?.let { message ->
                item {
                    MessageCard(
                        icon = Icons.Rounded.Info,
                        iconTint = GreenAccent,
                        title = "Done",
                        message = message,
                        onClick = { viewModel.clearMessage() }
                    )
                }
            }

            uiState.errorMessage?.let { message ->
                item {
                    MessageCard(
                        icon = Icons.Rounded.Warning,
                        iconTint = OrangeAccent,
                        title = "Something went wrong",
                        message = message,
                        onClick = { viewModel.clearMessage() }
                    )
                }
            }

            item {
                SectionLabel("PARENT FOCUS RULES")
            }

            if (uiState.policies.isEmpty()) {
                item {
                    EmptyCard(
                        title = "No rules yet",
                        message = "When a parent creates a focus rule, it will appear here."
                    )
                }
            } else {
                items(uiState.policies) { policy ->
                    PolicyCard(policy = policy)
                }
            }

            item {
                SectionLabel("USAGE SUMMARY")
            }

            item {
                UsageTotalCard(uiState = uiState)
            }

            val packages = uiState.usageSummary?.packages.orEmpty()
            if (packages.isEmpty()) {
                item {
                    EmptyCard(
                        title = "No usage data yet",
                        message = "Press sync test usage to send sample usage data to the backend."
                    )
                }
            } else {
                items(packages) { packageSummary ->
                    UsagePackageCard(packageSummary = packageSummary)
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.syncDemoUsage() },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            contentColor = WhiteText
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Upload,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Sync Test Usage",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onTakeUnlockQuizClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Quiz,
                            contentDescription = null,
                            tint = WhiteText,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Open Quizzes",
                            color = WhiteText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                SectionLabel("AVAILABLE STUDY TOOLS")
            }

            item {
                AvailableNowCard()
            }

            item {
                BottomActionsRow(
                    onViewProgressClick = onViewProgressClick,
                    onGoToMaterialsClick = onGoToMaterialsClick
                )
            }
        }
    }
}

@Composable
private fun FocusTopBar(
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = WhiteText,
                modifier = Modifier.size(28.dp)
            )
        }

        Text(
            text = "Focus Mode",
            color = WhiteText,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.align(Alignment.Center)
        )

        IconButton(
            onClick = onRefreshClick,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = "Refresh",
                tint = WhiteText,
                modifier = Modifier.size(25.dp)
            )
        }
    }
}

@Composable
private fun FocusStatusCard(
    uiState: FocusUiState
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = CardSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(116.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(116.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    PrimaryBlue.copy(alpha = 0.22f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF161616))
                        .border(1.dp, CardBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = PrimaryBlue,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.LockClock,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Focus backend is connected",
                color = WhiteText,
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "This screen now reads parent rules and usage reports from the backend.",
                color = SecondaryText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PermissionCard(
    usageAccessGranted: Boolean,
    onOpenSettingsClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    val title = if (usageAccessGranted) {
        "Usage access is enabled"
    } else {
        "Usage access is not enabled"
    }

    val message = if (usageAccessGranted) {
        "Rakizz can check app usage on this device."
    } else {
        "Enable usage access so Rakizz can read app usage later."
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = WarningSurface
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (usageAccessGranted) Icons.Rounded.Info else Icons.Rounded.Settings,
                        contentDescription = null,
                        tint = if (usageAccessGranted) GreenAccent else OrangeAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        color = WhiteText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = message,
                        color = Color(0xFFD3DDF6),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenSettingsClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = WhiteText
                    )
                ) {
                    Text("Open Settings")
                }

                OutlinedButton(
                    onClick = onRefreshClick,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Refresh",
                        color = WhiteText
                    )
                }
            }
        }
    }
}

@Composable
private fun PolicyCard(
    policy: FocusPolicy
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CardSurface
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            FeatureHeader(
                icon = Icons.Rounded.Schedule,
                iconTint = PrimaryBlue,
                title = policy.title,
                subtitle = policy.packageName
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Limit: ${policy.limitText}",
                color = WhiteText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            policy.note?.takeIf { it.isNotBlank() }?.let { note ->
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = note,
                    color = SecondaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun UsageTotalCard(
    uiState: FocusUiState
) {
    val summary = uiState.usageSummary

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CardSurface
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            FeatureHeader(
                icon = Icons.Rounded.ShowChart,
                iconTint = GreenAccent,
                title = "Last ${summary?.days ?: 7} days",
                subtitle = "Total tracked app usage"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = summary?.totalText ?: "0s",
                color = WhiteText,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun UsagePackageCard(
    packageSummary: UsagePackageSummary
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF202020)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF132C63)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.LockClock,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = packageSummary.packageName,
                    color = WhiteText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Used for ${packageSummary.durationText}",
                    color = SecondaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun MessageCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    message: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF202020)
    ) {
        FeatureHeader(
            modifier = Modifier.padding(16.dp),
            icon = icon,
            iconTint = iconTint,
            title = title,
            subtitle = message
        )
    }
}

@Composable
private fun EmptyCard(
    title: String,
    message: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF202020)
    ) {
        FeatureHeader(
            modifier = Modifier.padding(16.dp),
            icon = Icons.Rounded.Info,
            iconTint = SecondaryText,
            title = title,
            subtitle = message
        )
    }
}

@Composable
private fun AvailableNowCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = CardSurface
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            LiveFeatureRow(
                icon = Icons.Rounded.School,
                title = "Study materials",
                subtitle = "Upload, preview, and download stored materials."
            )

            DividerLine()

            LiveFeatureRow(
                icon = Icons.Rounded.Quiz,
                title = "AI quizzes",
                subtitle = "Generate quizzes from selected materials."
            )

            DividerLine()

            LiveFeatureRow(
                icon = Icons.Rounded.ShowChart,
                title = "Assignments",
                subtitle = "Create assignments and schedule local reminders."
            )
        }
    }
}

@Composable
private fun LiveFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    FeatureHeader(
        icon = icon,
        iconTint = GreenAccent,
        title = title,
        subtitle = subtitle
    )
}

@Composable
private fun FeatureHeader(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF132C63))
                .border(1.dp, iconTint.copy(alpha = 0.20f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = WhiteText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                color = SecondaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BottomActionsRow(
    onViewProgressClick: () -> Unit,
    onGoToMaterialsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "View Progress",
            color = SecondaryText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onViewProgressClick() }
        )

        Spacer(modifier = Modifier.width(22.dp))

        Text(
            text = "•",
            color = SecondaryText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(22.dp))

        Text(
            text = "Go to Materials",
            color = SecondaryText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onGoToMaterialsClick() }
        )
    }
}

@Composable
private fun SectionLabel(
    text: String
) {
    Text(
        text = text,
        color = SecondaryText,
        fontSize = 16.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.2.sp
    )
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(CardBorder.copy(alpha = 0.55f))
    )
}