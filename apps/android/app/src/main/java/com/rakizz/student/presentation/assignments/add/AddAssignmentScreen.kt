package com.rakizz.student.presentation.assignments.add

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rakizz.student.notifications.assignment.AssignmentReminderScheduler
import com.rakizz.student.presentation.theme.RakizzColors
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import androidx.compose.foundation.clickable

@Composable
fun AddAssignmentScreen(
    navController: NavController,
    viewModel: AddAssignmentViewModel = hiltViewModel()
) {
    AddAssignmentScreen(
        onBackClick = {
            navController.popBackStack()
        },
        onAssignmentSaved = { reminderWarning ->
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("assignment_created", true)

            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set(
                    key = "assignment_save_message",
                    value = reminderWarning
                        ?.takeIf { it.isNotBlank() }
                        ?.let { warning -> "Assignment saved. Reminder warning: $warning" }
                        ?: "Assignment saved."
                )

            navController.popBackStack()
        },
        viewModel = viewModel
    )
}

@Composable
private fun AddAssignmentScreen(
    onBackClick: () -> Unit,
    onAssignmentSaved: (String?) -> Unit,
    viewModel: AddAssignmentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )

    LaunchedEffect(Unit) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Android 13+ needs this for reminders
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AddAssignmentEvent.Saved -> {
                    val localReminderWarning = AssignmentReminderScheduler.scheduleReminder(
                        context = context,
                        assignmentId = event.assignmentId,
                        title = event.title,
                        description = event.description,
                        dueAtRaw = event.dueAtRaw
                    )

                    onAssignmentSaved(
                        mergeWarnings(
                            event.reminderWarning,
                            localReminderWarning
                        )
                    )
                }
            }
        }
    }

    Scaffold(
        containerColor = RakizzColors.Background
    ) { paddingValues ->
        Column(
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
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TopBar(
                onBackClick = onBackClick
            )

            MainCard()

            AssignmentFormCard(
                uiState = uiState,
                onTitleChanged = viewModel::onTitleChanged,
                onDescriptionChanged = viewModel::onDescriptionChanged,
                onPickDate = {
                    val initialDate = parseSelectedDate(uiState.dueDate) ?: LocalDate.now()

                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            viewModel.onDueDateChanged(
                                String.format(
                                    Locale.US,
                                    "%04d-%02d-%02d",
                                    year,
                                    month + 1,
                                    dayOfMonth
                                )
                            )
                        },
                        initialDate.year,
                        initialDate.monthValue - 1,
                        initialDate.dayOfMonth
                    ).show()
                },
                onPickTime = {
                    val initialTime = parseSelectedTime(uiState.dueTime)
                        ?: LocalTime.now().withSecond(0).withNano(0)

                    TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            viewModel.onDueTimeChanged(
                                String.format(
                                    Locale.US,
                                    "%02d:%02d",
                                    hourOfDay,
                                    minute
                                )
                            )
                        },
                        initialTime.hour,
                        initialTime.minute,
                        true
                    ).show()
                },
                onSaveClick = viewModel::saveAssignment,
                onCancelClick = onBackClick
            )
        }
    }
}

@Composable
private fun TopBar(
    onBackClick: () -> Unit
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

        Column {
            Text(
                text = "Add Assignment",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Save homework with a deadline.",
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun MainCard() {
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
                text = "Homework reminder",
                color = RakizzColors.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter the task, choose the due date and time, then Rakizz saves it and schedules a local reminder.",
                color = RakizzColors.White.copy(alpha = 0.88f),
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )
        }
    }
}

@Composable
private fun AssignmentFormCard(
    uiState: AddAssignmentUiState,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onPickDate: () -> Unit,
    onPickTime: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Assignment details",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            AcademicTextField(
                value = uiState.title,
                label = "Title",
                enabled = !uiState.isSaving,
                capitalization = KeyboardCapitalization.Sentences,
                onValueChange = onTitleChanged
            )

            AcademicTextField(
                value = uiState.description,
                label = "Description",
                enabled = !uiState.isSaving,
                minLines = 4,
                capitalization = KeyboardCapitalization.Sentences,
                onValueChange = onDescriptionChanged
            )

            PickerButton(
                icon = Icons.Filled.CalendarMonth,
                label = "Due date",
                value = uiState.dueDate.ifBlank { "Select due date" },
                enabled = !uiState.isSaving,
                onClick = onPickDate
            )

            PickerButton(
                icon = Icons.Filled.Timer,
                label = "Due time",
                value = uiState.dueTime.ifBlank { "Select due time" },
                enabled = !uiState.isSaving,
                onClick = onPickTime
            )

            ReminderNote()

            uiState.inputError?.let { error ->
                ErrorText(error)
            }

            uiState.submitError?.let { error ->
                ErrorText(error)
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
                } else {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))
                }

                Text(
                    text = if (uiState.isSaving) "Saving..." else "Save Assignment",
                    fontWeight = FontWeight.ExtraBold
                )
            }

            OutlinedButton(
                onClick = onCancelClick,
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, RakizzColors.Primary),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = RakizzColors.Primary
                )
            ) {
                Text(
                    text = "Cancel",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun AcademicTextField(
    value: String,
    label: String,
    enabled: Boolean,
    minLines: Int = 1,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(text = label)
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        minLines = minLines,
        singleLine = minLines == 1,
        shape = RoundedCornerShape(18.dp),
        keyboardOptions = KeyboardOptions(
            capitalization = capitalization,
            keyboardType = KeyboardType.Text
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = RakizzColors.TextMain,
            unfocusedTextColor = RakizzColors.TextMain,
            disabledTextColor = RakizzColors.TextMuted,
            focusedContainerColor = RakizzColors.Card,
            unfocusedContainerColor = RakizzColors.Card,
            disabledContainerColor = RakizzColors.CardSoft,
            focusedLabelColor = RakizzColors.Primary,
            unfocusedLabelColor = RakizzColors.TextSecond,
            disabledLabelColor = RakizzColors.TextMuted,
            focusedBorderColor = RakizzColors.Primary,
            unfocusedBorderColor = RakizzColors.CardBorder,
            disabledBorderColor = RakizzColors.CardBorder,
            cursorColor = RakizzColors.Primary
        )
    )
}

@Composable
private fun PickerButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = RakizzColors.CardSoft,
        border = BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Row(
            modifier = Modifier
                .clickable(enabled = enabled) {
                    onClick()
                }
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = RakizzColors.PrimarySoft,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RakizzColors.Primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label.uppercase(),
                    color = RakizzColors.TextMuted,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = value,
                    color = RakizzColors.TextMain,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ReminderNote() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = RakizzColors.PrimarySoft,
        border = BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.NotificationsActive,
                contentDescription = null,
                tint = RakizzColors.Primary,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Rakizz will try to schedule a local reminder for this assignment on the device.",
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

@Composable
private fun ErrorText(
    message: String
) {
    Text(
        text = message,
        color = RakizzColors.Error,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth()
    )
}

private fun parseSelectedDate(
    value: String
): LocalDate? {
    return try {
        if (value.isBlank()) {
            null
        } else {
            LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
        }
    } catch (_: DateTimeParseException) {
        null
    }
}

private fun parseSelectedTime(
    value: String
): LocalTime? {
    return try {
        if (value.isBlank()) {
            null
        } else {
            LocalTime.parse(
                value,
                DateTimeFormatter.ofPattern("HH:mm")
            )
        }
    } catch (_: DateTimeParseException) {
        null
    }
}

private fun mergeWarnings(
    vararg warnings: String?
): String? {
    val merged = warnings
        .mapNotNull { warning ->
            warning
                ?.trim()
                ?.takeIf { value -> value.isNotEmpty() }
        }
        .distinct()

    return if (merged.isEmpty()) {
        null
    } else {
        merged.joinToString(" ")
    }
}