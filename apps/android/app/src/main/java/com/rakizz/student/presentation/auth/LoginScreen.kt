package com.rakizz.student.presentation.auth

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.presentation.common.UiState
import com.rakizz.student.presentation.theme.RakizzColors

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onForgotPasswordClick: () -> Unit = {},
    onCreateAccountClick: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    var email by rememberSaveable {
        mutableStateOf("")
    }

    var password by rememberSaveable {
        mutableStateOf("")
    }

    val isLoading = state is UiState.Loading
    val errorMessage = (state as? UiState.Error)?.message

    LaunchedEffect(state) {
        val successState = state as? UiState.Success

        if (successState != null) {
            // send the logged-in role to navigation
            onLoginSuccess(successState.data.role)
        }
    }

    LoginScreenContent(
        email = email,
        password = password,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onEmailChange = {
            email = it
            viewModel.clearError()
        },
        onPasswordChange = {
            password = it
            viewModel.clearError()
        },
        onLoginClick = {
            // backend login uses email as username
            viewModel.login(
                email = email.trim(),
                pass = password
            )
        },
        onCreateAccountClick = onCreateAccountClick
    )
}

@Composable
private fun LoginScreenContent(
    email: String,
    password: String,
    isLoading: Boolean,
    errorMessage: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onCreateAccountClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    // false = password starts hidden
    // true = password starts visible
    var passwordVisible by rememberSaveable {
        mutableStateOf(false)
    }

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
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BrandHeader()

        Spacer(modifier = Modifier.height(30.dp))

        WelcomeCard()

        Spacer(modifier = Modifier.height(18.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = RakizzColors.Card,
            shadowElevation = 3.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, RakizzColors.CardBorder)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Sign in",
                    color = RakizzColors.TextMain,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Use your saved Rakizz account.",
                    color = RakizzColors.TextSecond,
                    style = MaterialTheme.typography.bodyMedium
                )

                AuthTextField(
                    value = email,
                    label = "Email",
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    onValueChange = onEmailChange,
                    onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                )

                AuthTextField(
                    value = password,
                    label = "Password",
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordVisibilityClick = {
                        passwordVisible = !passwordVisible
                        
                    },
                    onValueChange = onPasswordChange,
                    onDone = {
                        keyboardController?.hide()
                        focusManager.clearFocus()

                        if (email.isNotBlank() && password.isNotBlank()) {
                            onLoginClick()
                        }
                    }
                )

                if (!errorMessage.isNullOrBlank()) {
                    MessageText(
                        text = errorMessage,
                        color = RakizzColors.Error
                    )
                }

                Button(
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onLoginClick()
                    },
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
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
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = RakizzColors.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Text(
                        text = if (isLoading) "Signing in..." else "Sign In",
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Text(
                    text = "Forgot password?",
                    color = RakizzColors.TextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable {
                            Toast
                                .makeText(
                                    context,
                                    "We wanted to implement this with a whatsapp+Email API Service, but we couldnt at this stage because it takes a subscription and we are on a tight budget, so maybe in the future updates, and compicated procsess, but for now, you can contact us on our social media or email to reset your password :) so we left it at the end on the full implmentation list, but we will try to implement it as soon as we can, thank you for understanding, when we are ready to deploy.",
                                    Toast.LENGTH_LONG
                                )
                                .show()
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = RakizzColors.PrimarySoft,
            shadowElevation = 1.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, RakizzColors.CardBorder)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "New to Rakizz?",
                        color = RakizzColors.PrimaryDark,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "Create student or parent account.",
                        color = RakizzColors.TextSecond,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = "Create",
                    color = RakizzColors.Primary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.clickable {
                        onCreateAccountClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun BrandHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(46.dp),
            shape = CircleShape,
            color = RakizzColors.Primary
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "R",
                    color = RakizzColors.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = "Rakizz",
                color = RakizzColors.PrimaryDark,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Learning-first focus control",
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun WelcomeCard() {
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
            Text(
                text = "Welcome back",
                color = RakizzColors.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Continue studying, managing materials, and staying focused with Rakizz.",
                color = RakizzColors.White.copy(alpha = 0.86f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    label: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityClick: () -> Unit = {},
    onValueChange: (String) -> Unit,
    onNext: () -> Unit = {},
    onDone: () -> Unit = {}
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(label)
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
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
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = if (isPassword) {
            {
                IconButton(
                    onClick = onPasswordVisibilityClick
                ) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                        contentDescription = if (passwordVisible) {
                            "Hide password"
                        } else {
                            "Show password"
                        },
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
private fun MessageText(
    text: String,
    color: androidx.compose.ui.graphics.Color
) {
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth()
    )
}