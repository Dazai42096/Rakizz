package com.rakizz.student.presentation.assignments.add

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rakizz.student.notifications.assignment.AssignmentReminderScheduler
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

@Composable
fun AddAssignmentScreen(
    navController: NavController,
    viewModel: AddAssignmentViewModel = hiltViewModel()
) {
    AddAssignmentScreen(
        onBackClick = { navController.popBackStack() },
        onAssignmentSaved = { reminderWarning ->
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("assignment_created", true)

            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set(
                    "assignment_save_message",
                    reminderWarning?.takeIf { it.isNotBlank() }?.let {
                        "Assignment saved. Reminder warning: $it"
                    } ?: "Assignment saved."
                )

            navController.popBackStack()
        },
        viewModel = viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssignmentScreen(
    onBackClick: () -> Unit = {},
    onAssignmentSaved: (String?) -> Unit = {},
    viewModel: AddAssignmentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

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
        topBar = {
            TopAppBar(
                title = { Text("Add Assignment") }
            )
        }
    ) { padding ->
        AddAssignmentContent(
            padding = padding,
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
                            String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
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

@Composable
private fun AddAssignmentContent(
    padding: PaddingValues,
    uiState: AddAssignmentUiState,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onPickDate: () -> Unit,
    onPickTime: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Create a real stored assignment with a due date and time.",
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = uiState.title,
            onValueChange = onTitleChanged,
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !uiState.isSaving
        )

        OutlinedTextField(
            value = uiState.description,
            onValueChange = onDescriptionChanged,
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            enabled = !uiState.isSaving
        )

        Text(
            text = "Due date",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedButton(
            onClick = onPickDate,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving
        ) {
            Text(
                text = uiState.dueDate.ifBlank { "Select due date" }
            )
        }

        Text(
            text = "Due time",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedButton(
            onClick = onPickTime,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving
        ) {
            Text(
                text = uiState.dueTime.ifBlank { "Select due time" }
            )
        }

        Text(
            text = "Pick the due date and time instead of typing them manually.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        uiState.inputError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        uiState.submitError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 8.dp),
                    strokeWidth = 2.dp
                )
                Text("Saving...")
            } else {
                Text("Save Assignment")
            }
        }

        TextButton(
            onClick = onCancelClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving
        ) {
            Text("Cancel")
        }
    }
}

private fun parseSelectedDate(value: String): LocalDate? {
    return try {
        if (value.isBlank()) null else LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (_: DateTimeParseException) {
        null
    }
}

private fun parseSelectedTime(value: String): LocalTime? {
    return try {
        if (value.isBlank()) null else LocalTime.parse(value, DateTimeFormatter.ofPattern("HH:mm"))
    } catch (_: DateTimeParseException) {
        null
    }
}

private fun mergeWarnings(vararg warnings: String?): String? {
    val merged = warnings
        .mapNotNull { it?.trim()?.takeIf { value -> value.isNotEmpty() } }
        .distinct()

    return if (merged.isEmpty()) null else merged.joinToString(" ")
}