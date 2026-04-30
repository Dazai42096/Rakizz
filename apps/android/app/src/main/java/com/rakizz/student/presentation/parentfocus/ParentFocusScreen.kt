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
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.rakizz.student.data.remote.dto.InstalledAppResponseDto
import com.rakizz.student.data.remote.dto.PolicyDto
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
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
                        viewModel.checkAccount()
                    }
                )
            }

            item {
                HeaderCard(
                    parentEmail = uiState.parentEmail
                )
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

            if (uiState.isParentAccount == false) {
                item {
                    ParentOnlyCard()
                }
            } else {
                item {
                    StudentPickerCard(
                        uiState = uiState,
                        onStudentIdChange = viewModel::onStudentIdChange,
                        onStartTimeChange = viewModel::onStartTimeChange,
                        onEndTimeChange = viewModel::onEndTimeChange,
                        onLoadAppsClick = viewModel::loadStudentApps
                    )
                }

                item {
                    AppsActionRow(
                        uiState = uiState,
                        onSelectAllClick = viewModel::selectAllApps,
                        onClearClick = viewModel::clearSelectedApps
                    )
                }

                if (uiState.isLoading) {
                    item {
                        LoadingCard()
                    }
                } else if (uiState.studentApps.isEmpty()) {
                    item {
                        EmptyAppsCard()
                    }
                } else {
                    items(uiState.studentApps) { app ->
                        StudentAppRow(
                            app = app,
                            isChecked = uiState.selectedPackages.contains(app.packageName),
                            onClick = {
                                viewModel.toggleApp(app.packageName)
                            }
                        )
                    }

                    item {
                        Button(
                            onClick = {
                                viewModel.createFocusRule()
                            },
                            enabled = !uiState.isSaving,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                contentColor = WhiteText
                            )
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    color = WhiteText,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Save Focus Rule",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
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

                if (uiState.policies.isEmpty()) {
                    item {
                        EmptyRulesCard()
                    }
                } else {
                    items(uiState.policies) { policy ->
                        PolicyCard(policy = policy)
                    }
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
private fun HeaderCard(
    parentEmail: String
) {
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
                text = "Choose apps for focus time",
                color = WhiteText,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // student phone sends apps, parent chooses what to block
            Text(
                text = "The student phone syncs installed apps. The parent chooses which apps should be blocked during focus time.",
                color = SecondaryText,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            if (parentEmail.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Signed in: $parentEmail",
                    color = SecondaryText,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun StudentPickerCard(
    uiState: ParentFocusUiState,
    onStudentIdChange: (String) -> Unit,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit,
    onLoadAppsClick: () -> Unit
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
                text = "Student and focus time",
                color = WhiteText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            DarkTextField(
                value = uiState.studentId,
                label = "Student ID",
                onValueChange = onStudentIdChange
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DarkTextField(
                    value = uiState.startTime,
                    label = "Start time",
                    modifier = Modifier.weight(1f),
                    onValueChange = onStartTimeChange
                )

                DarkTextField(
                    value = uiState.endTime,
                    label = "End time",
                    modifier = Modifier.weight(1f),
                    onValueChange = onEndTimeChange
                )
            }

            Button(
                onClick = onLoadAppsClick,
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
                Icon(
                    imageVector = Icons.Rounded.Apps,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.padding(4.dp))

                Text(
                    text = "Load Student Apps",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AppsActionRow(
    uiState: ParentFocusUiState,
    onSelectAllClick: () -> Unit,
    onClearClick: () -> Unit
) {
    if (uiState.studentApps.isEmpty()) {
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(
            onClick = onSelectAllClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Select All",
                color = WhiteText
            )
        }

        OutlinedButton(
            onClick = onClearClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Clear",
                color = WhiteText
            )
        }
    }
}

@Composable
private fun StudentAppRow(
    app: InstalledAppResponseDto,
    isChecked: Boolean,
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
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = {
                    onClick()
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = PrimaryBlue,
                    uncheckedColor = SecondaryText,
                    checkmarkColor = WhiteText
                )
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = app.appName,
                    color = WhiteText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = app.packageName,
                    color = SecondaryText,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun PolicyCard(
    policy: PolicyDto
) {
    val start = policy.configJson["start_time"]?.jsonPrimitive?.contentOrNull
    val end = policy.configJson["end_time"]?.jsonPrimitive?.contentOrNull

    val apps = policy.configJson["blocked_apps"]?.jsonArray?.mapNotNull { item ->
        item.toAppName()
    }.orEmpty()

    val subtitle = if (start != null && end != null) {
        "$start - $end"
    } else {
        policy.ruleType
    }

    val appText = if (apps.isEmpty()) {
        "No apps listed"
    } else {
        apps.joinToString(", ")
    }

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
                text = "Focus time rule",
                color = WhiteText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                color = SecondaryText,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = appText,
                color = WhiteText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun JsonElement.toAppName(): String? {
    return try {
        val obj = this.jsonObject

        obj["app_name"]?.jsonPrimitive?.contentOrNull
            ?: obj["package_name"]?.jsonPrimitive?.contentOrNull
    } catch (_: Exception) {
        null
    }
}

@Composable
private fun DarkTextField(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(text = label)
        },
        modifier = modifier.fillMaxWidth(),
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
private fun LoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                color = PrimaryBlue,
                modifier = Modifier.padding(end = 14.dp)
            )

            Text(
                text = "Loading...",
                color = WhiteText,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun EmptyAppsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Apps,
                contentDescription = null,
                tint = SecondaryText
            )

            Spacer(modifier = Modifier.padding(6.dp))

            Text(
                text = "No apps loaded yet. Student must press Sync Phone Apps first.",
                color = SecondaryText,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun EmptyRulesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        )
    ) {
        Text(
            text = "No focus rules saved yet.",
            color = SecondaryText,
            fontSize = 15.sp,
            modifier = Modifier.padding(18.dp)
        )
    }
}

@Composable
private fun ParentOnlyCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Block,
                contentDescription = null,
                tint = ErrorOrange
            )

            Spacer(modifier = Modifier.padding(6.dp))

            Text(
                text = "This page is only for parent accounts.",
                color = WhiteText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}