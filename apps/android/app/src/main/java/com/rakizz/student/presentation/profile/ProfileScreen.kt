package com.rakizz.student.presentation.profile

import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.presentation.theme.RakizzColors

@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onGeneratePairCodeClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onLogoutClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            ProfileTopBar(
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(18.dp))

            when {
                uiState.isLoading -> {
                    LoadingCard()
                }

                uiState.error != null -> {
                    MessageCard(
                        title = "Error",
                        message = uiState.error ?: "Could not load profile",
                        color = RakizzColors.Error,
                        onClick = viewModel::clearMessage
                    )
                }

                else -> {
                    ProfileContent(
                        uiState = uiState,
                        onEditClick = viewModel::startEditing,
                        onCancelClick = viewModel::cancelEditing,
                        onSaveClick = viewModel::saveProfile,
                        onOpenPairCodeClick = onGeneratePairCodeClick,
                        onFullNameChange = viewModel::onFullNameChange,
                        onSchoolChange = viewModel::onSchoolChange,
                        onGradeChange = viewModel::onGradeChange,
                        onPhoneChange = viewModel::onPhoneChange,
                        onImageChange = viewModel::onImageChange,
                        onLogoutClick = onLogoutClick
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun ProfileTopBar(
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
                text = "Profile",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Account information and pairing.",
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    onEditClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    onOpenPairCodeClick: () -> Unit,
    onFullNameChange: (String) -> Unit,
    onSchoolChange: (String) -> Unit,
    onGradeChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onImageChange: (String) -> Unit,
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current

    var showImageMenu by remember { mutableStateOf(false) }
    var showImagePreview by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
                // Some emulators ignore persistent URI permission.
            }

            onImageChange(uri.toString())
            onEditClick()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileHeroCard(
            uiState = uiState,
            onImageClick = { showImageMenu = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        uiState.message?.let { message ->
            MessageCard(
                title = "Done",
                message = message,
                color = RakizzColors.Success,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(14.dp))
        }

        if (uiState.role.lowercase() == "student") {
            PairCodeCard(
                onOpenClick = onOpenPairCodeClick
            )

            Spacer(modifier = Modifier.height(14.dp))
        }

        if (uiState.isEditing) {
            EditProfileCard(
                uiState = uiState,
                onFullNameChange = onFullNameChange,
                onSchoolChange = onSchoolChange,
                onGradeChange = onGradeChange,
                onPhoneChange = onPhoneChange
            )

            Spacer(modifier = Modifier.height(14.dp))

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
                ),
                contentPadding = PaddingValues(horizontal = 18.dp)
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
                    text = if (uiState.isSaving) "Saving..." else "Save Profile",
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onCancelClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, RakizzColors.Primary)
            ) {
                Text(
                    text = "Cancel",
                    color = RakizzColors.Primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        } else {
            ReadProfileCard(
                uiState = uiState
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onEditClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RakizzColors.Primary,
                    contentColor = RakizzColors.White
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Edit Profile",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LogoutCard(
            onLogoutClick = onLogoutClick
        )
    }

    if (showImageMenu) {
        ImageMenuDialog(
            hasImage = uiState.profileImageUrl.isNotBlank(),
            onDismiss = { showImageMenu = false },
            onViewImage = {
                showImageMenu = false
                showImagePreview = true
            },
            onChangeImage = {
                showImageMenu = false
                imagePicker.launch(arrayOf("image/*"))
            }
        )
    }

    if (showImagePreview) {
        ImagePreviewDialog(
            imageUri = uiState.profileImageUrl,
            onDismiss = { showImagePreview = false }
        )
    }
}

@Composable
private fun ProfileHeroCard(
    uiState: ProfileUiState,
    onImageClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
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
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileImage(
                role = uiState.role,
                name = uiState.fullName,
                imageUri = uiState.profileImageUrl,
                onClick = onImageClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = uiState.fullName.ifBlank { roleTitle(uiState.role) },
                color = RakizzColors.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = uiState.email,
                color = RakizzColors.White.copy(alpha = 0.82f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            WhitePill(
                text = uiState.role.uppercase().ifBlank { "ACCOUNT" }
            )
        }
    }
}

@Composable
private fun ProfileImage(
    role: String,
    name: String,
    imageUri: String,
    onClick: () -> Unit
) {
    val imageBitmap = rememberImageBitmap(imageUri)

    val letter = when {
        name.isNotBlank() -> name.first().uppercase()
        role.lowercase() == "parent" -> "P"
        else -> "S"
    }

    Box(
        modifier = Modifier
            .size(116.dp)
            .clip(CircleShape)
            .background(RakizzColors.Card)
            .border(3.dp, RakizzColors.White.copy(alpha = 0.55f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = "Profile image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = letter,
                color = RakizzColors.Primary,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun PairCodeCard(
    onOpenClick: () -> Unit
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBubble(
                    icon = Icons.Filled.Badge,
                    color = RakizzColors.Primary
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Student pair code",
                        color = RakizzColors.TextMain,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "Open the secure pair-code screen to link this student account with a parent.",
                        color = RakizzColors.TextSecond,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onOpenClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RakizzColors.Primary,
                    contentColor = RakizzColors.White
                )
            ) {
                Text(
                    text = "Open Pair Code",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun ReadProfileCard(
    uiState: ProfileUiState
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Account details",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            InfoLine(
                icon = Icons.Filled.Person,
                label = "Name",
                value = uiState.fullName.ifBlank { "Not added yet" }
            )

            InfoLine(
                icon = Icons.Filled.School,
                label = "School",
                value = uiState.school.ifBlank { "Not added yet" }
            )

            InfoLine(
                icon = Icons.Filled.Badge,
                label = "Grade",
                value = uiState.gradeLevel.ifBlank { "Not added yet" }
            )

            InfoLine(
                icon = Icons.Filled.Person,
                label = "Phone",
                value = uiState.phoneNumber.ifBlank { "Not added yet" }
            )

            InfoLine(
                icon = Icons.Filled.Image,
                label = "Picture",
                value = if (uiState.profileImageUrl.isBlank()) {
                    "Not added yet"
                } else {
                    "Image selected"
                }
            )
        }
    }
}

@Composable
private fun EditProfileCard(
    uiState: ProfileUiState,
    onFullNameChange: (String) -> Unit,
    onSchoolChange: (String) -> Unit,
    onGradeChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit
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
                text = "Edit information",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            ProfileTextField(
                value = uiState.fullName,
                label = "Name",
                onValueChange = onFullNameChange
            )

            ProfileTextField(
                value = uiState.school,
                label = "School",
                onValueChange = onSchoolChange
            )

            ProfileTextField(
                value = uiState.gradeLevel,
                label = "Grade",
                onValueChange = onGradeChange
            )

            ProfileTextField(
                value = uiState.phoneNumber,
                label = "Phone number",
                keyboardType = KeyboardType.Phone,
                onValueChange = onPhoneChange
            )

            Text(
                text = "To change the profile picture, press the image at the top.",
                color = RakizzColors.TextMuted,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProfileTextField(
    value: String,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
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
        shape = RoundedCornerShape(18.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
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
private fun InfoLine(
    icon: ImageVector,
    label: String,
    value: String
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

@Composable
private fun LogoutCard(
    onLogoutClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = RakizzColors.Card,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Account",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, RakizzColors.Error),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = RakizzColors.Error
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )

                Spacer(modifier = Modifier.width(9.dp))

                Text(
                    text = "Sign Out",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun ImageMenuDialog(
    hasImage: Boolean,
    onDismiss: () -> Unit,
    onViewImage: () -> Unit,
    onChangeImage: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RakizzColors.Card,
        title = {
            Text(
                text = "Profile picture",
                color = RakizzColors.TextMain,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column {
                Text(
                    text = "Choose what you want to do.",
                    color = RakizzColors.TextSecond
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (hasImage) {
                    Button(
                        onClick = onViewImage,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RakizzColors.Primary,
                            contentColor = RakizzColors.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text("View Image")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = onChangeImage,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RakizzColors.Primary,
                        contentColor = RakizzColors.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Image,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text("Change Image")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Close",
                    color = RakizzColors.Primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    )
}

@Composable
private fun ImagePreviewDialog(
    imageUri: String,
    onDismiss: () -> Unit
) {
    val imageBitmap = rememberImageBitmap(imageUri)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RakizzColors.Card,
        title = {
            Text(
                text = "Profile image",
                color = RakizzColors.TextMain,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Profile image preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(270.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = "No image selected yet.",
                    color = RakizzColors.TextSecond
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Close",
                    color = RakizzColors.Primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
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
            .clickable { onClick() },
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
private fun LoadingCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Box(
            modifier = Modifier.padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = RakizzColors.Primary
            )
        }
    }
}

@Composable
private fun IconBubble(
    icon: ImageVector,
    color: Color
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

@Composable
private fun rememberImageBitmap(
    uriString: String
): ImageBitmap? {
    val context = LocalContext.current

    return remember(uriString) {
        if (uriString.isBlank()) {
            null
        } else {
            try {
                val uri = Uri.parse(uriString)

                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(
                        context.contentResolver,
                        uri
                    )

                    ImageDecoder.decodeBitmap(source)
                } else {
                    MediaStore.Images.Media.getBitmap(
                        context.contentResolver,
                        uri
                    )
                }

                bitmap.asImageBitmap()
            } catch (_: Exception) {
                null
            }
        }
    }
}

private fun roleTitle(
    role: String
): String {
    return when (role.lowercase()) {
        "parent" -> "Parent Account"
        "student" -> "Student Account"
        else -> "Rakizz Account"
    }
}