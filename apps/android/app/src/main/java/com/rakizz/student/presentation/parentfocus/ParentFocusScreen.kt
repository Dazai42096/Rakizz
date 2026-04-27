package com.rakizz.student.presentation.parentfocus

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.rakizz.student.data.remote.dto.PolicyDto
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

private val ScreenTop = Color(0xFF03112A)
private val ScreenBottom = Color(0xFF000000)
private val WhiteText = Color(0xFFF7F8FA)
private val SecondaryText = Color(0xFF9EA6B8)
private val PrimaryBlue = Color(0xFF2457D6)
private val CardSurface = Color(0xFF171717)
private val SuccessGreen = Color(0xFF30D158)
private val ErrorOrange = Color(0xFFFF9F0A)

@Composable
fun ParentFocusScreen(
    onBackClick: () -> Unit,
    viewModel: ParentFocusViewModel = hiltViewModel()
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
                        listOf(ScreenTop, ScreenBottom)
                    )
                )
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TopBar(
                    onBackClick = onBackClick,
                    onRefreshClick = {
                        viewModel.loadPolicies()
                    }
                )
            }

            item {
                HeaderCard()
            }

            uiState.message?.let { message ->
                item {
                    MessageCard(
                        title = "Done",
                        message = message,
                        color = SuccessGreen,
                        onClick = {
                            viewModel.clearMessages()
                        }
                    )
                }
            }

            uiState.error?.let { error ->
                item {
                    MessageCard(
                        title = "Error",
                        message = error,
                        color = ErrorOrange,
                        onClick = {
                            viewModel.clearMessages()
                        }
                    )
                }
            }

            item {
                CreateRuleCard(
                    uiState = uiState,
                    onStudentIdChange = viewModel::onStudentIdChange,
                    onPackageNameChange = viewModel::onPackageNameChange,
                    onDailyLimitChange = viewModel::onDailyLimitChange,
                    onNoteChange = viewModel::onNoteChange,
                    onCreateClick = viewModel::createRule
                )
            }

            item {
                Text(
                    text = "CREATED RULES",
                    color = SecondaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )
            }

            if (uiState.isLoading) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
            } else if (uiState.policies.isEmpty()) {
                item {
                    EmptyCard()
                }
            } else {
                items(uiState.policies) { policy ->
                    PolicyCard(policy = policy)
                }
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
        IconButton(
            onClick = onBackClick
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = WhiteText
            )
        }

        Text(
            text = "Parent Focus",
            color = WhiteText,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onRefreshClick
        ) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = "Refresh",
                tint = WhiteText
            )
        }
    }
}

@Composable
private fun HeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Create blocking rules",
                color = WhiteText,
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // parent makes the rule, student app reads it
            Text(
                text = "The parent creates a rule here. The student app gets the rule from the backend.",
                color = SecondaryText,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun CreateRuleCard(
    uiState: ParentFocusUiState,
    onStudentIdChange: (String) -> Unit,
    onPackageNameChange: (String) -> Unit,
    onDailyLimitChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onCreateClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "New app limit rule",
                color = WhiteText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            DarkTextField(
                value = uiState.studentId,
                label = "Student ID",
                onValueChange = onStudentIdChange
            )

            DarkTextField(
                value = uiState.packageName,
                label = "Package name",
                onValueChange = onPackageNameChange
            )

            DarkTextField(
                value = uiState.dailyLimitMinutes,
                label = "Daily limit minutes",
                onValueChange = onDailyLimitChange
            )

            DarkTextField(
                value = uiState.note,
                label = "Note",
                onValueChange = onNoteChange
            )

            Button(
                onClick = onCreateClick,
                enabled = !uiState.isLoading,
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
                    text = if (uiState.isLoading) "Saving..." else "Create Rule",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DarkTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(text = label)
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = WhiteText,
            unfocusedTextColor = WhiteText,
            focusedLabelColor = PrimaryBlue,
            unfocusedLabelColor = SecondaryText,
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = Color(0xFF343A46),
            cursorColor = PrimaryBlue
        )
    )
}

@Composable
private fun PolicyCard(
    policy: PolicyDto
) {
    val packageName = policy.configJson["package_name"]?.jsonPrimitive?.contentOrNull ?: "unknown app"
    val limit = policy.configJson["daily_limit_minutes"]?.jsonPrimitive?.intOrNull ?: 0
    val note = policy.configJson["note"]?.jsonPrimitive?.contentOrNull.orEmpty()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Daily app limit",
                color = WhiteText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = packageName,
                color = SecondaryText,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Limit: $limit minutes/day",
                color = WhiteText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            if (note.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))

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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        )
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
private fun EmptyCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        )
    ) {
        Text(
            text = "No rules created yet.",
            color = SecondaryText,
            fontSize = 15.sp,
            modifier = Modifier.padding(18.dp)
        )
    }
}