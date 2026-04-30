package com.rakizz.student.presentation.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.domain.model.FocusPolicy

private val ScreenTop = Color(0xFF03112A)
private val ScreenBottom = Color(0xFF000000)
private val WhiteText = Color(0xFFF7F8FA)
private val SecondaryText = Color(0xFF8A92A3)
private val PrimaryBlue = Color(0xFF2457D6)
private val CardSurface = Color(0xFF171717)
private val CardBorder = Color(0xFF2A2F3A)
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
                    onRefreshClick = viewModel::loadFocusData
                )
            }

            item {
                FocusHeaderCard(
                    isLoading = uiState.isLoading
                )
            }

            uiState.actionMessage?.let { message ->
                item {
                    MessageCard(
                        title = "Done",
                        message = message,
                        color = GreenAccent,
                        onClick = viewModel::clearMessage
                    )
                }
            }

            uiState.errorMessage?.let { message ->
                item {
                    MessageCard(
                        title = "Error",
                        message = message,
                        color = OrangeAccent,
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
                SectionLabel("PARENT FOCUS RULES")
            }

            if (uiState.isLoading) {
                item {
                    LoadingCard()
                }
            } else if (uiState.policies.isEmpty()) {
                item {
                    EmptyCard()
                }
            } else {
                items(uiState.policies) { rule ->
                    RuleCard(rule = rule)
                }
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
        Text(
            text = "Back",
            color = SecondaryText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clickable {
                    onBackClick()
                }
        )

        Text(
            text = "Focus Mode",
            color = WhiteText,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.align(Alignment.Center)
        )

        Text(
            text = "Refresh",
            color = PrimaryBlue,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clickable {
                    onRefreshClick()
                }
        )
    }
}

@Composable
private fun FocusHeaderCard(
    isLoading: Boolean
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
            if (isLoading) {
                CircularProgressIndicator(
                    color = PrimaryBlue
                )
            } else {
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .fillMaxWidth()
                        .background(
                            color = PrimaryBlue,
                            shape = RoundedCornerShape(20.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Student focus rules",
                color = WhiteText,
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "The student only syncs phone apps and views rules created by the parent.",
                color = SecondaryText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
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
        shape = RoundedCornerShape(24.dp),
        color = CardSurface
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Sync phone apps",
                color = WhiteText,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "This sends the phone app list to the backend so the parent can choose apps for focus time.",
                color = SecondaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onSyncAppsClick,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = WhiteText
                )
            ) {
                Text(
                    text = if (isLoading) "Please wait..." else "Sync Phone Apps",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RuleCard(
    rule: FocusPolicy
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CardSurface
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = rule.title,
                color = WhiteText,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = rule.timeText,
                color = PrimaryBlue,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Apps: ${rule.appsText}",
                color = WhiteText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            rule.note?.takeIf { it.isNotBlank() }?.let { note ->
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = note,
                    color = SecondaryText,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun MessageCard(
    title: String,
    message: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF202020)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                color = color,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = message,
                color = SecondaryText,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun LoadingCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = CardSurface
    ) {
        Box(
            modifier = Modifier.padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
    }
}

@Composable
private fun EmptyCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF202020)
    ) {
        Text(
            text = "No parent rules yet.",
            color = SecondaryText,
            fontSize = 15.sp,
            modifier = Modifier.padding(18.dp)
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