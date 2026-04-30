package com.rakizz.student.presentation.profile

import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

private val ScreenTop = Color(0xFF03112A)
private val ScreenBottom = Color(0xFF000000)
private val WhiteText = Color(0xFFF7F8FA)
private val SecondaryText = Color(0xFF9EA6B8)
private val PrimaryBlue = Color(0xFF2457D6)
private val CardSurface = Color(0xFF171717)
private val CardBorder = Color(0xFF2A2F3A)
private val ErrorOrange = Color(0xFFFF9F0A)
private val GreenAccent = Color(0xFF30D158)

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
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(ScreenTop, ScreenBottom)
                    )
                )
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 16.dp)
        ) {
            ProfileTopBar(
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            when {
                uiState.isLoading -> {
                    LoadingCard()
                }

                uiState.error != null -> {
                    MessageCard(
                        title = "Error",
                        message = uiState.error ?: "Could not load profile",
                        color = ErrorOrange,
                        onClick = viewModel::clearMessage
                    )
                }

                else -> {
                    ProfileContent(
                        uiState = uiState,
                        onEditClick = viewModel::startEditing,
                        onCancelClick = viewModel::cancelEditing,
                        onSaveClick = viewModel::saveProfile,
                        onShowPairCodeClick = viewModel::togglePairCode,
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
            text = "Profile",
            color = WhiteText,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    onEditClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    onShowPairCodeClick: () -> Unit,
    onFullNameChange: (String) -> Unit,
    onSchoolChange: (String) -> Unit,
    onGradeChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onImageChange: (String) -> Unit,
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var showImageMenu by remember { mutableStateOf(false) }
    var showImagePreview by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                // keep permission so image can open again later
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
                // emulator may ignore this sometimes
            }

            onImageChange(uri.toString())
            onEditClick()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileImage(
            role = uiState.role,
            name = uiState.fullName,
            imageUri = uiState.profileImageUrl,
            onClick = {
                showImageMenu = true
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = uiState.fullName.ifBlank { roleTitle(uiState.role) },
            color = WhiteText,
            fontSize = 25.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = uiState.email,
            color = SecondaryText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(20.dp))

        uiState.message?.let {
            MessageCard(
                title = "Done",
                message = it,
                color = GreenAccent,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        PairCodeCard(
            userId = uiState.userId,
            showPairCode = uiState.showPairCode,
            onShowClick = onShowPairCodeClick,
            onCopyClick = {
                clipboard.setText(AnnotatedString(uiState.userId))
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isEditing) {
            EditProfileCard(
                uiState = uiState,
                onFullNameChange = onFullNameChange,
                onSchoolChange = onSchoolChange,
                onGradeChange = onGradeChange,
                onPhoneChange = onPhoneChange,
                onImageChange = onImageChange
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onSaveClick,
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = WhiteText
                ),
                contentPadding = PaddingValues(horizontal = 18.dp)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        color = WhiteText,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Save Profile",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onCancelClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = "Cancel",
                    color = WhiteText,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Button(
                onClick = onEditClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = WhiteText
                )
            ) {
                Text(
                    text = "Edit Profile",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ReadProfileCard(uiState = uiState)
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(
                text = "Sign Out",
                color = WhiteText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (showImageMenu) {
        ImageMenuDialog(
            hasImage = uiState.profileImageUrl.isNotBlank(),
            onDismiss = {
                showImageMenu = false
            },
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
            onDismiss = {
                showImagePreview = false
            }
        )
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
            .size(112.dp)
            .clip(CircleShape)
            .background(Color(0xFFE9E1D2))
            .border(2.dp, PrimaryBlue.copy(alpha = 0.45f), CircleShape)
            .clickable {
                onClick()
            },
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
                color = Color(0xFF333333),
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold
            )
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
        title = {
            Text(
                text = "Profile Picture",
                color = WhiteText
            )
        },
        text = {
            Column {
                Text(
                    text = "Choose what you want to do.",
                    color = SecondaryText
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (hasImage) {
                    Button(
                        onClick = onViewImage,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            contentColor = WhiteText
                        )
                    ) {
                        Text("View Image")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = onChangeImage,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = WhiteText
                    )
                ) {
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
                    color = PrimaryBlue
                )
            }
        },
        containerColor = CardSurface
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
        title = {
            Text(
                text = "Profile Image",
                color = WhiteText
            )
        },
        text = {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Profile image preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = "No image selected yet.",
                    color = SecondaryText
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Close",
                    color = PrimaryBlue
                )
            }
        },
        containerColor = CardSurface
    )
}

@Composable
private fun PairCodeCard(
    userId: String,
    showPairCode: Boolean,
    onShowClick: () -> Unit,
    onCopyClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardSurface
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Text(
                text = "Pair Code",
                color = WhiteText,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Use this ID to link the student account with the parent account.",
                color = SecondaryText,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (showPairCode) {
                Text(
                    text = userId,
                    color = PrimaryBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onCopyClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = WhiteText
                    )
                ) {
                    Text("Copy Pair Code")
                }
            } else {
                Button(
                    onClick = onShowClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = WhiteText
                    )
                ) {
                    Text("Generate Pair Code")
                }
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
        shape = RoundedCornerShape(20.dp),
        color = CardSurface
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoLine("Role", uiState.role.uppercase())
            InfoLine("Name", uiState.fullName.ifBlank { "Not added yet" })
            InfoLine("School", uiState.school.ifBlank { "Not added yet" })
            InfoLine("Grade", uiState.gradeLevel.ifBlank { "Not added yet" })
            InfoLine("Phone", uiState.phoneNumber.ifBlank { "Not added yet" })
            InfoLine("Picture", if (uiState.profileImageUrl.isBlank()) "Not added yet" else "Image selected")
        }
    }
}

@Composable
private fun EditProfileCard(
    uiState: ProfileUiState,
    onFullNameChange: (String) -> Unit,
    onSchoolChange: (String) -> Unit,
    onGradeChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onImageChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardSurface
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                label = "Phone Number",
                keyboardType = KeyboardType.Phone,
                onValueChange = onPhoneChange
            )

            ProfileTextField(
                value = uiState.profileImageUrl,
                label = "Picture URL / path",
                onValueChange = onImageChange
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
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
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
private fun InfoLine(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            color = SecondaryText,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            color = WhiteText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
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
        color = CardSurface
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(CardSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
            .padding(22.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = PrimaryBlue)
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
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
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