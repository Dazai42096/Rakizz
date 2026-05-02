package com.rakizz.student.presentation.auth.parentsignup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.presentation.common.UiState
import com.rakizz.student.presentation.theme.RakizzColors

@Composable
fun ParentSignUpScreen(
    onBackClick: () -> Unit,
    onCreateAccountDone: () -> Unit,
    onAlreadyHaveAccountClick: () -> Unit,
    viewModel: ParentSignUpViewModel = hiltViewModel()
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    val state by viewModel.uiState.collectAsState()

    val isLoading = state is UiState.Loading
    val errorMessage = (state as? UiState.Error)?.message

    LaunchedEffect(state) {
        if (state is UiState.Success) {
            onCreateAccountDone()
        }
    }

    ParentSignupContent(
        fullName = fullName,
        email = email,
        password = password,
        showPassword = showPassword,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onFullNameChange = {
            fullName = it
            viewModel.clearError()
        },
        onEmailChange = {
            email = it
            viewModel.clearError()
        },
        onPasswordChange = {
            password = it
            viewModel.clearError()
        },
        onTogglePassword = {
            showPassword = !showPassword
        },
        onCreateClick = {
            // parent signup uses parent role in the viewmodel
            viewModel.registerParent(
                fullName = fullName,
                email = email,
                pass = password
            )
        },
        onBackClick = onBackClick,
        onAlreadyHaveAccountClick = onAlreadyHaveAccountClick
    )
}

@Composable
private fun ParentSignupContent(
    fullName: String,
    email: String,
    password: String,
    showPassword: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onCreateClick: () -> Unit,
    onBackClick: () -> Unit,
    onAlreadyHaveAccountClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

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
                .padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ParentTopBar(
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            ParentHeroCard()

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = RakizzColors.Card,
                shadowElevation = 3.dp,
                border = BorderStroke(1.dp, RakizzColors.CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Create parent account",
                        color = RakizzColors.TextMain,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "This account manages focus rules for a linked student.",
                        color = RakizzColors.TextSecond,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                    )

                    ParentTextField(
                        value = fullName,
                        label = "Parent name",
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next,
                        onValueChange = onFullNameChange,
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    )

                    ParentTextField(
                        value = email,
                        label = "Email",
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                        onValueChange = onEmailChange,
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    )

                    ParentTextField(
                        value = password,
                        label = "Password",
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        isPassword = true,
                        showPassword = showPassword,
                        onTogglePassword = onTogglePassword,
                        onValueChange = onPasswordChange,
                        onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            onCreateClick()
                        }
                    )

                    if (!errorMessage.isNullOrBlank()) {
                        Text(
                            text = errorMessage,
                            color = RakizzColors.Error,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            onCreateClick()
                        },
                        enabled = !isLoading,
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
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = RakizzColors.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))
                        }

                        Text(
                            text = if (isLoading) {
                                "Creating..."
                            } else {
                                "Create Parent Account"
                            },
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ParentInfoCard()

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account?",
                    color = RakizzColors.TextSecond,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Sign in",
                    color = RakizzColors.Primary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.clickable {
                        onAlreadyHaveAccountClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun ParentTopBar(
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
                text = "Parent Account",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Create focus-control access.",
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ParentHeroCard() {
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(76.dp),
                shape = CircleShape,
                color = RakizzColors.White.copy(alpha = 0.16f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FamilyRestroom,
                        contentDescription = null,
                        tint = RakizzColors.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Guide focus time",
                color = RakizzColors.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Link a student account, choose apps, and set study-time restrictions.",
                color = RakizzColors.White.copy(alpha = 0.86f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )
        }
    }
}

@Composable
private fun ParentTextField(
    value: String,
    label: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onTogglePassword: () -> Unit = {},
    onValueChange: (String) -> Unit,
    onNext: () -> Unit = {},
    onDone: () -> Unit = {}
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
            keyboardType = keyboardType,
            capitalization = capitalization,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                onNext()
            },
            onDone = {
                onDone()
            }
        ),
        visualTransformation = if (isPassword && !showPassword) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = if (isPassword) {
            {
                IconButton(
                    onClick = onTogglePassword
                ) {
                    Icon(
                        imageVector = if (showPassword) {
                            Icons.Rounded.VisibilityOff
                        } else {
                            Icons.Rounded.Visibility
                        },
                        contentDescription = "Toggle password",
                        tint = RakizzColors.TextSecond
                    )
                }
            }
        } else {
            null
        },
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
private fun ParentInfoCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = RakizzColors.PrimarySoft,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Rounded.Security,
                contentDescription = null,
                tint = RakizzColors.Primary,
                modifier = Modifier.size(23.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Student linking comes next",
                    color = RakizzColors.PrimaryDark,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "After signup, use the Parent Focus page to enter the student pair code and create focus rules.",
                    color = RakizzColors.TextSecond,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
            }
        }
    }
}