package com.rakizz.student.presentation.parentfocus

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.data.remote.dto.InstalledAppResponseDto
import com.rakizz.student.data.remote.dto.PolicyDto
import com.rakizz.student.presentation.theme.RakizzColors
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun ParentFocusScreen(
    onBackClick: () -> Unit,
    viewModel: ParentFocusViewModel = hiltViewModel()
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
                    onRefreshClick = {
                        viewModel.loadPolicies()
                    }
                )
            }

            item {
                ParentMainCard()
            }

            uiState.message?.let { message ->
                item {
                    MessageCard(
                        title = "Done",
                        message = message,
                        color = RakizzColors.Success,
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
                        color = RakizzColors.Error,
                        onClick = {
                            viewModel.clearMessages()
                        }
                    )
                }
            }

            item {
                StepTitle(
                    number = "1",
                    title = "Link student",
                    subtitle = "Use the pair code from the student profile."
                )
            }

            item {
                PairCodeCard(
                    uiState = uiState,
                    onPairCodeChange = viewModel::onPairCodeChange,
                    onLinkStudentClick = viewModel::linkStudent
                )
            }

            if (uiState.linkedStudentId.isNotBlank()) {
                item {
                    LinkedStudentCard(
                        email = uiState.linkedStudentEmail,
                        studentId = uiState.linkedStudentId
                    )
                }

                item {
                    StepTitle(
                        number = "2",
                        title = "Load apps",
                        subtitle = "Get the synced apps from the student's phone."
                    )
                }

                item {
                    LoadAppsCard(
                        appCount = uiState.studentApps.size,
                        isLoading = uiState.isLoading,
                        onLoadAppsClick = viewModel::loadStudentApps
                    )
                }
            }

            if (uiState.studentApps.isNotEmpty()) {
                item {
                    StepTitle(
                        number = "3",
                        title = "Choose blocked apps",
                        subtitle = "Select the apps that should be blocked during focus time."
                    )
                }

                item {
                    AppsActionRow(
                        selectedCount = uiState.selectedPackages.size,
                        totalCount = uiState.studentApps.size,
                        onSelectAllClick = viewModel::selectAllApps,
                        onClearClick = viewModel::clearSelectedApps
                    )
                }

                items(
                    items = uiState.studentApps,
                    key = { app -> app.packageName }
                ) { app ->
                    StudentAppRow(
                        app = app,
                        isChecked = uiState.selectedPackages.contains(app.packageName),
                        onClick = {
                            viewModel.toggleApp(app.packageName)
                        }
                    )
                }

                item {
                    StepTitle(
                        number = "4",
                        title = "Set focus time",
                        subtitle = "The app will be blocked only during this time window."
                    )
                }

                item {
                    FocusTimeCard(
                        uiState = uiState,
                        onStartTimeChange = viewModel::onStartTimeChange,
                        onEndTimeChange = viewModel::onEndTimeChange,
                        onSaveClick = viewModel::createFocusRule
                    )
                }
            } else {
                item {
                    WaitingCard(
                        linked = uiState.linkedStudentId.isNotBlank()
                    )
                }
            }

            item {
                SectionTitle(
                    title = "Saved focus rules",
                    subtitle = if (uiState.policies.isEmpty()) {
                        "No rules saved yet"
                    } else {
                        "${uiState.policies.size} saved rule(s)"
                    }
                )
            }

            if (uiState.policies.isEmpty()) {
                item {
                    EmptyRulesCard()
                }
            } else {
                items(
                    items = uiState.policies,
                    key = { policy -> policy.id }
                ) { policy ->
                    PolicyCard(policy = policy)
                }
            }

            item {
                reviewExplanationCard()
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
                text = "Parent Focus",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Create rules for the student focus time.",
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Text(
            text = "Refresh",
            color = RakizzColors.Primary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.clickable {
                onRefreshClick()
            }
        )
    }
}

@Composable
private fun ParentMainCard() {
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
                text = "Control focus time",
                color = RakizzColors.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Parents link the student account, load the student's phone apps, choose blocked apps, and save a focus rule.",
                color = RakizzColors.White.copy(alpha = 0.88f),
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WhitePill("Link")
                WhitePill("Choose apps")
                WhitePill("Save rule")
            }
        }
    }
}

@Composable
private fun StepTitle(
    number: String,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(
                    color = RakizzColors.Primary,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = RakizzColors.White,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = subtitle,
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun PairCodeCard(
    uiState: ParentFocusUiState,
    onPairCodeChange: (String) -> Unit,
    onLinkStudentClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Student pair code",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Example: RKZ-123456",
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium
            )

            AcademicTextField(
                value = uiState.pairCode,
                label = "Pair code",
                onValueChange = onPairCodeChange,
                capitalization = KeyboardCapitalization.Characters
            )

            Button(
                onClick = onLinkStudentClick,
                enabled = !uiState.isLoading,
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
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = RakizzColors.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))
                }

                Text(
                    text = if (uiState.isLoading) "Linking..." else "Link Student",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun LinkedStudentCard(
    email: String,
    studentId: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = RakizzColors.PrimarySoft,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Student linked",
                color = RakizzColors.Success,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = email.ifBlank { "Student account linked" },
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = studentId,
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LoadAppsCard(
    appCount: Int,
    isLoading: Boolean,
    onLoadAppsClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Student phone apps",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (appCount > 0) {
                    "$appCount apps loaded from the student account."
                } else {
                    "Load apps after the student presses Sync Phone Apps."
                },
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onLoadAppsClick,
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
                Text(
                    text = if (isLoading) "Loading..." else "Load Student Apps",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun AppsActionRow(
    selectedCount: Int,
    totalCount: Int,
    onSelectAllClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = RakizzColors.Card,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "$selectedCount / $totalCount selected",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onSelectAllClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, RakizzColors.Primary)
                ) {
                    Text(
                        text = "Select all",
                        color = RakizzColors.Primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                OutlinedButton(
                    onClick = onClearClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, RakizzColors.TextMuted)
                ) {
                    Text(
                        text = "Clear",
                        color = RakizzColors.TextSecond,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun StudentAppRow(
    app: InstalledAppResponseDto,
    isChecked: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(22.dp),
        color = if (isChecked) {
            RakizzColors.PrimarySoft
        } else {
            RakizzColors.Card
        },
        shadowElevation = 1.dp,
        border = BorderStroke(
            width = 1.dp,
            color = if (isChecked) RakizzColors.Primary else RakizzColors.CardBorder
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
                    checkedColor = RakizzColors.Primary,
                    uncheckedColor = RakizzColors.TextMuted,
                    checkmarkColor = RakizzColors.White
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = app.appName,
                    color = RakizzColors.TextMain,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = app.packageName,
                    color = RakizzColors.TextSecond,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun FocusTimeCard(
    uiState: ParentFocusUiState,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Focus time window",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Use 24-hour format, for example 16:00 to 18:00.",
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AcademicTextField(
                    value = uiState.startTime,
                    label = "Start",
                    modifier = Modifier.weight(1f),
                    onValueChange = onStartTimeChange
                )

                AcademicTextField(
                    value = uiState.endTime,
                    label = "End",
                    modifier = Modifier.weight(1f),
                    onValueChange = onEndTimeChange
                )
            }

            Button(
                onClick = onSaveClick,
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RakizzColors.Primary,
                    contentColor = RakizzColors.White,
                    disabledContainerColor = RakizzColors.Primary.copy(alpha = 0.55f),
                    disabledContentColor = RakizzColors.White
                )
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        color = RakizzColors.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))
                }

                Text(
                    text = if (uiState.isSaving) "Saving..." else "Save Focus Rule",
                    fontWeight = FontWeight.ExtraBold
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
        try {
            val obj = item.jsonObject
            obj["app_name"]?.jsonPrimitive?.contentOrNull
                ?: obj["package_name"]?.jsonPrimitive?.contentOrNull
        } catch (_: Exception) {
            null
        }
    }.orEmpty()

    val timeText = if (start != null && end != null) {
        "$start - $end"
    } else {
        "No time"
    }

    val appsText = if (apps.isEmpty()) {
        "No apps listed"
    } else {
        apps.joinToString(", ")
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Focus time rule",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = timeText,
                color = RakizzColors.Primary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = appsText,
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

@Composable
private fun WaitingCard(
    linked: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = RakizzColors.AccentSoft,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Text(
            text = if (linked) {
                "Student is linked. Load the student apps to continue."
            } else {
                "Start by linking the student using the pair code."
            },
            color = RakizzColors.PrimaryDark,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(18.dp)
        )
    }
}

@Composable
private fun EmptyRulesCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = RakizzColors.Card,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Text(
            text = "No focus rules saved yet.",
            color = RakizzColors.TextSecond,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(18.dp)
        )
    }
}

@Composable
private fun reviewExplanationCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = RakizzColors.PrimarySoft,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "How this works",
                color = RakizzColors.PrimaryDark,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "This screen shows the parent side of focus control. The parent links the student, chooses apps from the student phone, sets a time window, and saves the rule. The student must pass a mixed AI quiz to unlock a blocked app.",
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
private fun AcademicTextField(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
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
        shape = RoundedCornerShape(18.dp),
        keyboardOptions = KeyboardOptions(
            capitalization = capitalization
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = RakizzColors.TextMain,
            unfocusedTextColor = RakizzColors.TextMain,
            focusedContainerColor = RakizzColors.Card,
            unfocusedContainerColor = RakizzColors.Card,
            focusedLabelColor = RakizzColors.Primary,
            unfocusedLabelColor = RakizzColors.TextSecond,
            focusedBorderColor = RakizzColors.Primary,
            unfocusedBorderColor = RakizzColors.CardBorder,
            cursorColor = RakizzColors.Primary
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(22.dp),
        color = RakizzColors.Card,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
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