package com.rakizz.student.presentation.auth.parentsignup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.presentation.common.UiState

private val ScreenTop = Color(0xFF03112A)
private val ScreenBottom = Color(0xFF000000)
private val WhiteText = Color(0xFFF7F8FA)
private val SecondaryText = Color(0xFF9EA6B8)
private val PrimaryBlue = Color(0xFF2457D6)
private val CardSurface = Color(0xFF171717)
private val ErrorText = Color(0xFFFF6B6B)

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

    Scaffold(
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(ScreenTop, ScreenBottom)
                    )
                )
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            IconButton(
                onClick = onBackClick
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = WhiteText
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Create Parent Account",
                color = WhiteText,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Parents can create focus rules and check student activity.",
                color = SecondaryText,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardSurface
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ParentTextField(
                        value = fullName,
                        label = "Parent name",
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words,
                        onValueChange = {
                            fullName = it
                            viewModel.clearError()
                        }
                    )

                    ParentTextField(
                        value = email,
                        label = "Email",
                        keyboardType = KeyboardType.Email,
                        onValueChange = {
                            email = it
                            viewModel.clearError()
                        }
                    )

                    ParentTextField(
                        value = password,
                        label = "Password",
                        keyboardType = KeyboardType.Password,
                        visualTransformation = if (showPassword) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    showPassword = !showPassword
                                }
                            ) {
                                Icon(
                                    imageVector = if (showPassword) {
                                        Icons.Rounded.VisibilityOff
                                    } else {
                                        Icons.Rounded.Visibility
                                    },
                                    contentDescription = "Toggle password",
                                    tint = SecondaryText
                                )
                            }
                        },
                        onValueChange = {
                            password = it
                            viewModel.clearError()
                        }
                    )

                    if (!errorMessage.isNullOrBlank()) {
                        Text(
                            text = errorMessage,
                            color = ErrorText,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.registerParent(
                                fullName = fullName,
                                email = email,
                                pass = password
                            )
                        },
                        enabled = !isLoading,
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
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = WhiteText,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Create Parent Account",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Already have an account? Sign in",
                color = SecondaryText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    onAlreadyHaveAccountClick()
                }
            )
        }
    }
}

@Composable
private fun ParentTextField(
    value: String,
    label: String,
    keyboardType: KeyboardType,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
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
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            capitalization = capitalization
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