package com.rakizz.student.presentation.paircode

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.presentation.theme.RakizzColors

@Composable
fun PairCodeScreen(
    onBackClick: () -> Unit,
    onCopyClick: (String) -> Unit = {},
    onShareClick: (String) -> Unit = {},
    onRegenerateClick: (String) -> Unit = {},
    viewModel: PairCodeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val pairCodeText = when {
        uiState.isLoading && uiState.pairCode.isBlank() -> "Loading..."
        uiState.error != null && uiState.pairCode.isBlank() -> "No Code"
        else -> uiState.pairCode
    }

    val hasValidCode = uiState.pairCode.isNotBlank() && !uiState.isLoading

    Scaffold(
        containerColor = RakizzColors.Background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            RakizzColors.Background,
                            RakizzColors.BackgroundSoft
                        )
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
                PairCodeTopBar(
                    onBackClick = onBackClick
                )
            }

            item {
                PairCodeHero()
            }

            item {
                CodeCard(
                    code = pairCodeText,
                    isLoading = uiState.isLoading,
                    error = uiState.error
                )
            }

            item {
                ActionButtonsRow(
                    enabled = hasValidCode,
                    onCopyClick = {
                        onCopyClick(uiState.pairCode)
                    },
                    onShareClick = {
                        onShareClick(uiState.pairCode)
                    }
                )
            }

            item {
                InstructionCard()
            }

            item {
                Button(
                    onClick = {
                        viewModel.loadPairCode()
                        onRegenerateClick(uiState.pairCode)
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(66.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RakizzColors.Primary,
                        contentColor = RakizzColors.White,
                        disabledContainerColor = RakizzColors.Primary.copy(alpha = 0.45f),
                        disabledContentColor = RakizzColors.White.copy(alpha = 0.7f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = if (uiState.isLoading) "Loading Code..." else "Refresh Code",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            item {
                SecurityNoticeCard()
            }
        }
    }
}

@Composable
private fun PairCodeTopBar(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = RakizzColors.TextMain,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = "Pair Code",
            color = RakizzColors.TextMain,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PairCodeHero() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(148.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(148.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                RakizzColors.Primary.copy(alpha = 0.25f),
                                RakizzColors.Background.copy(alpha = 0f)
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(RakizzColors.Card)
                    .border(1.dp, RakizzColors.CardBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.QrCode2,
                    contentDescription = null,
                    tint = RakizzColors.Accent,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Link with a Parent",
            color = RakizzColors.TextMain,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Share this code with your parent so they can connect to your student account.",
            color = RakizzColors.TextSecond,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun CodeCard(
    code: String,
    isLoading: Boolean,
    error: String?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = RakizzColors.Card,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = RakizzColors.CardBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "YOUR ACTIVE PAIR CODE",
                color = RakizzColors.TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.1.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(RakizzColors.PrimarySoft)
                    .border(
                        width = 1.dp,
                        color = RakizzColors.Primary.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(26.dp)
                    )
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = code,
                    color = RakizzColors.TextMain,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = when {
                    isLoading -> "Loading your pair code..."
                    error != null -> "Could not load pair code. Make sure you are logged in and the backend is running."
                    else -> "Use this code in the parent app"
                },
                color = if (error != null) {
                    RakizzColors.Error
                } else {
                    RakizzColors.Success
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ActionButtonsRow(
    enabled: Boolean,
    onCopyClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SecondaryActionButton(
            modifier = Modifier.weight(1f),
            text = "Copy",
            icon = Icons.Rounded.ContentCopy,
            enabled = enabled,
            onClick = onCopyClick
        )

        SecondaryActionButton(
            modifier = Modifier.weight(1f),
            text = "Share",
            icon = Icons.Rounded.Share,
            enabled = enabled,
            onClick = onShareClick
        )
    }
}

@Composable
private fun SecondaryActionButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = RakizzColors.Card,
            contentColor = RakizzColors.TextMain,
            disabledContainerColor = RakizzColors.Card.copy(alpha = 0.45f),
            disabledContentColor = RakizzColors.TextMain.copy(alpha = 0.45f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = RakizzColors.CardBorder
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun InstructionCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = RakizzColors.Card,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = RakizzColors.CardBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "HOW TO USE",
                color = RakizzColors.TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.1.sp
            )

            StepRow(number = "1", text = "Open the parent app or parent registration flow.")
            StepRow(number = "2", text = "Enter this pair code when asked to link a student.")
            StepRow(number = "3", text = "Once approved, your account will be connected.")
        }
    }
}

@Composable
private fun StepRow(
    number: String,
    text: String
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(RakizzColors.PrimarySoft),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = RakizzColors.Primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            color = RakizzColors.TextMain,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 22.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SecurityNoticeCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = RakizzColors.CardSoft,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = RakizzColors.CardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Rounded.VerifiedUser,
                contentDescription = null,
                tint = RakizzColors.Success,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Secure pairing",
                    color = RakizzColors.TextMain,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "This code should only be shared with your parent or guardian.",
                    color = RakizzColors.TextSecond,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 21.sp
                )
            }
        }
    }
}