package com.rakizz.student.presentation.focus

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.domain.model.FocusPolicy
import com.rakizz.student.presentation.theme.RakizzColors

@Composable
fun FocusScreen(
    onBackClick: () -> Unit,
    onTryBlockedAppClick: (String) -> Unit,
    onViewProgressClick: () -> Unit = {},
    onGoToMaterialsClick: () -> Unit = {},
    viewModel: FocusViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                start = 20.dp,
                end = 20.dp,
                top = 14.dp,
                bottom = 30.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TopBar(
                    onBackClick = onBackClick,
                    onRefreshClick = viewModel::loadFocusData
                )
            }

            item {
                FocusMainCard()
            }

            uiState.actionMessage?.let { message ->
                item {
                    MessageCard(
                        title = "Done",
                        message = message,
                        color = RakizzColors.Success,
                        onClick = viewModel::clearMessage
                    )
                }
            }

            uiState.errorMessage?.let { message ->
                item {
                    MessageCard(
                        title = "Error",
                        message = message,
                        color = RakizzColors.Error,
                        onClick = viewModel::clearMessage
                    )
                }
            }

            item {
                SyncAppsCard(
                    isLoading = uiState.isLoading,
                    onSyncAppsClick = viewModel::syncInstalledApps
                )
            }

            item {
                QuizUnlockInfoCard()
            }

            item {
                SectionTitle(
                    title = "Parent focus rules",
                    subtitle = if (uiState.policies.isEmpty()) {
                        "No active rules loaded yet"
                    } else {
                        "${uiState.policies.size} rule(s) from parent account"
                    }
                )
            }

            if (uiState.isLoading) {
                item {
                    LoadingCard()
                }
            } else if (uiState.policies.isEmpty()) {
                item {
                    EmptyRulesCard()
                }
            } else {
                items(
                    items = uiState.policies,
                    key = { rule -> rule.id }
                ) { rule ->
                    FocusRuleCard(
                        rule = rule,
                        onTryBlockedAppClick = onTryBlockedAppClick
                    )
                }
            }

            item {
                CommitteeNoteCard()
            }
        }
    }
}

@Composable
private fun TopBar(
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledIconButton(
            onClick = onBackClick,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = RakizzColors.Card,
                contentColor = RakizzColors.Primary
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Focus Mode",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Blocked apps are unlocked through learning.",
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        FilledIconButton(
            onClick = onRefreshClick,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = RakizzColors.PrimarySoft,
                contentColor = RakizzColors.Primary
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Refresh"
            )
        }
    }
}

@Composable
private fun FocusMainCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
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
                .padding(22.dp)
        ) {
            Text(
                text = "Study first, unlock later",
                color = RakizzColors.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "When a parent blocks an app during focus time, the student must pass a mixed AI quiz with at least 70% to unlock it.",
                color = RakizzColors.White.copy(alpha = 0.88f),
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WhitePill("Parent rule")
                WhitePill("AI quiz")
                WhitePill("70% pass")
            }
        }
    }
}

@Composable
private fun SyncAppsCard(
    isLoading: Boolean,
    onSyncAppsClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBubble(
                    icon = Icons.Filled.PhoneAndroid,
                    color = RakizzColors.Primary
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Sync phone apps",
                        color = RakizzColors.TextMain,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "Send this phone's installed apps to the backend so the parent can choose which apps to block.",
                        color = RakizzColors.TextSecond,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSyncAppsClick,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RakizzColors.Primary,
                    contentColor = RakizzColors.White,
                    disabledContainerColor = RakizzColors.Primary.copy(alpha = 0.55f),
                    disabledContentColor = RakizzColors.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = RakizzColors.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))
                }

                Text(
                    text = if (isLoading) "Syncing..." else "Sync Phone Apps",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun QuizUnlockInfoCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Text(
                text = "How unlocking works",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            StepRow(
                icon = Icons.Filled.Block,
                title = "Blocked app detected",
                subtitle = "Rakizz detects the restricted app during focus time."
            )

            StepRow(
                icon = Icons.Filled.AutoAwesome,
                title = "Mixed AI quiz appears",
                subtitle = "The quiz is generated from the student's uploaded materials."
            )

            StepRow(
                icon = Icons.Filled.CheckCircle,
                title = "70% required",
                subtitle = "If the student passes, the app is unlocked for a short time."
            )

            StepRow(
                icon = Icons.Filled.LockOpen,
                title = "Failed quiz keeps app blocked",
                subtitle = "If the student does not pass, Rakizz asks for another quiz."
            )
        }
    }
}

@Composable
private fun FocusRuleCard(
    rule: FocusPolicy,
    onTryBlockedAppClick: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBubble(
                    icon = Icons.Filled.Timer,
                    color = RakizzColors.Warning
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = rule.title,
                        color = RakizzColors.TextMain,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = rule.timeText,
                        color = RakizzColors.Primary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            InfoBox(
                label = "Blocked apps",
                value = rule.appsText
            )

            rule.note?.takeIf { it.isNotBlank() }?.let { note ->
                Spacer(modifier = Modifier.height(10.dp))

                InfoBox(
                    label = "Note",
                    value = note
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // this is still useful as a safe test from inside the app
                    onTryBlockedAppClick(rule.packageName)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RakizzColors.Primary,
                    contentColor = RakizzColors.White
                )
            ) {
                Text(
                    text = "Open Unlock Quiz",
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "For the real demo, open the blocked app normally from the phone launcher. This button is a backup test.",
                color = RakizzColors.TextMuted,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun InfoBox(
    label: String,
    value: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = RakizzColors.CardSoft,
        border = androidx.compose.foundation.BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = label.uppercase(),
                color = RakizzColors.TextMuted,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = value,
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

@Composable
private fun StepRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        IconBubble(
            icon = icon,
            color = RakizzColors.Primary
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = subtitle,
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
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
private fun MessageCard(
    title: String,
    message: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(22.dp),
        color = RakizzColors.Card,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                color = color,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = message,
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun LoadingCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                color = RakizzColors.Primary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(30.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = "Loading focus rules...",
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EmptyRulesCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconBubble(
                icon = Icons.Filled.Block,
                color = RakizzColors.TextMuted
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "No parent rules yet",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ask the parent account to link this student and create a focus rule.",
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CommitteeNoteCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = RakizzColors.AccentSoft,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Checkpoint explanation",
                color = RakizzColors.PrimaryDark,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Parent focus rules define blocked apps and focus time. During an active rule, opening a blocked app sends the student to an AI quiz unlock flow. Passing score is 70%.",
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

@Composable
private fun IconBubble(
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Surface(
        modifier = Modifier.size(42.dp),
        shape = CircleShape,
        color = color.copy(alpha = 0.13f)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun WhitePill(
    text: String
) {
    Box(
        modifier = Modifier
            .background(
                color = RakizzColors.White.copy(alpha = 0.16f),
                shape = RoundedCornerShape(11.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = RakizzColors.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold
        )
    }
}