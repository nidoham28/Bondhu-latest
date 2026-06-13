package com.nidoham.bondhu.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nidoham.bondhu.AuthEvent
import com.nidoham.bondhu.AuthTab
import com.nidoham.bondhu.LoginViewModel
import com.nidoham.bondhu.R
import com.nidoham.bondhu.ui.theme.AccentError
import com.nidoham.bondhu.ui.theme.AccentGreen
import com.nidoham.bondhu.ui.theme.AccentPrimary
import com.nidoham.bondhu.ui.theme.AccentSecondary
import com.nidoham.bondhu.ui.theme.BgDeep
import com.nidoham.bondhu.ui.theme.BgInput
import com.nidoham.bondhu.ui.theme.BgSurface
import com.nidoham.bondhu.ui.theme.BorderDefault
import com.nidoham.bondhu.ui.theme.BorderFocused
import com.nidoham.bondhu.ui.theme.TextHint
import com.nidoham.bondhu.ui.theme.TextPrimary
import com.nidoham.bondhu.ui.theme.TextSecondary

// ─────────────────────────────────────────────────────────────────────────────
// Root screen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> viewModel.onProfilePicSelected(uri) }

    /* Collect one-shot events */
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthEvent.NavigateToHome     -> onNavigateToHome()
                is AuthEvent.ShowSuccessMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(BgDeep, Color(0xFF0D1B2A), Color(0xFF0F1923)))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            /* Global API error banner */
            uiState.errorMessage?.let { error ->
                ErrorBanner(message = error)
                Spacer(Modifier.height(12.dp))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape  = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BgSurface),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedContent(
                        targetState = uiState.activeTab to uiState.signupStep,
                        transitionSpec = {
                            val forward =
                                (targetState.first == AuthTab.SIGNUP && initialState.first == AuthTab.LOGIN) ||
                                        targetState.second > initialState.second
                            val enter = if (forward) slideInHorizontally { it } + fadeIn()
                            else slideInHorizontally { -it } + fadeIn()
                            val exit  = if (forward) slideOutHorizontally { -it } + fadeOut()
                            else slideOutHorizontally { it } + fadeOut()
                            enter.togetherWith(exit)
                        },
                        label = "auth_pane"
                    ) { (tab, step) ->
                        when {
                            tab == AuthTab.LOGIN -> LoginPane(
                                email           = uiState.loginEmail,
                                emailError      = uiState.loginEmailError,
                                onEmailChange   = viewModel::onLoginEmailChange,
                                password        = uiState.loginPassword,
                                passwordError   = uiState.loginPasswordError,
                                onPasswordChange= viewModel::onLoginPasswordChange,
                                passwordVisible = uiState.loginPasswordVisible,
                                onTogglePw      = viewModel::toggleLoginPasswordVisibility,
                                isLoading       = uiState.isLoading,
                                onLogin         = viewModel::login,
                                onGoSignup      = { viewModel.switchTab(AuthTab.SIGNUP) }
                            )
                            step == 1 -> SignupStep1Pane(
                                email                = uiState.signupEmail,
                                emailError           = uiState.signupEmailError,
                                onEmailChange        = viewModel::onSignupEmailChange,
                                password             = uiState.signupPassword,
                                passwordError        = uiState.signupPasswordError,
                                onPasswordChange     = viewModel::onSignupPasswordChange,
                                confirmPassword      = uiState.signupConfirmPassword,
                                confirmPasswordError = uiState.signupConfirmPasswordError,
                                onConfirmChange      = viewModel::onSignupConfirmPasswordChange,
                                passwordVisible      = uiState.signupPasswordVisible,
                                onTogglePw           = viewModel::toggleSignupPasswordVisibility,
                                confirmVisible       = uiState.signupConfirmPasswordVisible,
                                onToggleConfirm      = viewModel::toggleSignupConfirmPasswordVisibility,
                                onNext               = viewModel::goToSignupStep2,
                                onGoLogin            = { viewModel.switchTab(AuthTab.LOGIN) }
                            )
                            else -> SignupStep2Pane(
                                username        = uiState.username,
                                usernameError   = uiState.usernameError,
                                onUsernameChange= viewModel::onUsernameChange,
                                profilePicUri   = uiState.profilePicUri,
                                onPickImage     = { imagePicker.launch("image/*") },
                                isLoading       = uiState.isLoading,
                                onSignup        = viewModel::signUp,
                                onBack          = viewModel::goBackToSignupStep1
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier  = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AccentError.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .border(1.dp, AccentError.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(message, color = AccentError, fontSize = 13.sp)
    }
}

@Composable
private fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(totalSteps) { index ->
            val step       = index + 1
            val isActive   = step == currentStep
            val isComplete = step < currentStep

            Box(
                modifier = Modifier
                    .size(if (isActive) 34.dp else 30.dp)
                    .background(
                        when {
                            isActive   -> AccentPrimary
                            isComplete -> AccentGreen
                            else       -> BorderDefault
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isComplete)
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(15.dp))
                else
                    Text("$step",
                        color = if (isActive) Color.White else TextHint,
                        fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            if (index < totalSteps - 1) {
                Spacer(Modifier.width(6.dp))
                Box(Modifier.width(44.dp).height(2.dp)
                    .background(if (isComplete) AccentGreen else BorderDefault))
                Spacer(Modifier.width(6.dp))
            }
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    error: String? = null,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onToggleVisibility: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = {
                Icon(leadingIcon, null, tint = if (error != null) AccentError else AccentSecondary)
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { onToggleVisibility?.invoke() }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide" else "Show",
                            tint = TextSecondary
                        )
                    }
                }
            } else null,
            isError = error != null,
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = if (error != null) AccentError else BorderFocused,
                unfocusedBorderColor    = if (error != null) AccentError else BorderDefault,
                focusedLabelColor       = if (error != null) AccentError else AccentSecondary,
                unfocusedLabelColor     = TextHint,
                errorBorderColor        = AccentError,
                errorLabelColor         = AccentError,
                cursorColor             = AccentPrimary,
                focusedTextColor        = TextPrimary,
                unfocusedTextColor      = TextPrimary,
                focusedContainerColor   = BgInput,
                unfocusedContainerColor = BgInput,
                errorContainerColor     = BgInput
            )
        )
        if (error != null) {
            Text(error, color = AccentError, fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp))
        }
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    trailingIcon: ImageVector? = null
) {
    Button(
        onClick  = onClick,
        enabled  = !isLoading,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape    = RoundedCornerShape(14.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor         = AccentPrimary,
            disabledContainerColor = AccentPrimary.copy(alpha = 0.5f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp))
        } else {
            Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            if (trailingIcon != null) {
                Spacer(Modifier.width(8.dp))
                Icon(trailingIcon, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun SwitchAuthRow(message: String, action: String, onClick: () -> Unit) {
    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Text("$message ", color = TextSecondary, fontSize = 14.sp)
        TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
            Text(action, color = AccentSecondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Login pane
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LoginPane(
    email: String, emailError: String?, onEmailChange: (String) -> Unit,
    password: String, passwordError: String?, onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean, onTogglePw: () -> Unit,
    isLoading: Boolean,
    onLogin: () -> Unit, onGoSignup: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Welcome back", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("Sign in to your account", fontSize = 13.sp, color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))

        AuthTextField(email, onEmailChange, "Email address", Icons.Outlined.Email,
            error = emailError, keyboardType = KeyboardType.Email)
        Spacer(Modifier.height(14.dp))
        AuthTextField(password, onPasswordChange, "Password", Icons.Outlined.Lock,
            error = passwordError, isPassword = true,
            passwordVisible = passwordVisible, onToggleVisibility = onTogglePw)

        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = { /* TODO */ }) {
                Text("Forgot password?", color = AccentSecondary, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(4.dp))
        PrimaryButton("Login", onLogin, isLoading)
        Spacer(Modifier.height(20.dp))
        SwitchAuthRow("Don't have an account?", "Sign Up", onGoSignup)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sign-up step 1 pane
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SignupStep1Pane(
    email: String, emailError: String?, onEmailChange: (String) -> Unit,
    password: String, passwordError: String?, onPasswordChange: (String) -> Unit,
    confirmPassword: String, confirmPasswordError: String?, onConfirmChange: (String) -> Unit,
    passwordVisible: Boolean, onTogglePw: () -> Unit,
    confirmVisible: Boolean, onToggleConfirm: () -> Unit,
    onNext: () -> Unit, onGoLogin: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        StepIndicator(currentStep = 1, totalSteps = 2)
        Spacer(Modifier.height(16.dp))
        Text("Create account", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("Step 1 of 2 — account details", fontSize = 13.sp, color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))

        AuthTextField(email, onEmailChange, "Email address", Icons.Outlined.Email,
            error = emailError, keyboardType = KeyboardType.Email)
        Spacer(Modifier.height(14.dp))
        AuthTextField(password, onPasswordChange, "Password", Icons.Outlined.Lock,
            error = passwordError, isPassword = true,
            passwordVisible = passwordVisible, onToggleVisibility = onTogglePw)
        Spacer(Modifier.height(14.dp))
        AuthTextField(confirmPassword, onConfirmChange, "Confirm password", Icons.Outlined.Lock,
            error = confirmPasswordError, isPassword = true,
            passwordVisible = confirmVisible, onToggleVisibility = onToggleConfirm)

        Spacer(Modifier.height(24.dp))
        PrimaryButton("Continue", onNext, trailingIcon = Icons.AutoMirrored.Filled.ArrowForward)
        Spacer(Modifier.height(20.dp))
        SwitchAuthRow("Already have an account?", "Login", onGoLogin)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sign-up step 2 pane  ← FIXED: StepIndicator is now truly centered
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SignupStep2Pane(
    username: String, usernameError: String?, onUsernameChange: (String) -> Unit,
    profilePicUri: Uri?,
    onPickImage: () -> Unit,
    isLoading: Boolean,
    onSignup: () -> Unit,
    onBack: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextSecondary)
            }
            StepIndicator(currentStep = 2, totalSteps = 2)
        }

        Spacer(Modifier.height(12.dp))
        Text("Set up your profile", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("Step 2 of 2 — add a photo & username", fontSize = 13.sp, color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))

        Box(Modifier.size(108.dp), contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .clip(CircleShape)
                    .background(BgInput)
                    .border(
                        2.dp,
                        Brush.linearGradient(listOf(AccentPrimary, AccentSecondary)),
                        CircleShape
                    )
                    .clickable { onPickImage() },
                contentAlignment = Alignment.Center
            ) {
                if (profilePicUri != null) {
                    AsyncImage(
                        model = profilePicUri,
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Icon(Icons.Outlined.Person, null,
                        tint = AccentSecondary, modifier = Modifier.size(52.dp))
                }
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(AccentPrimary, CircleShape)
                    .border(2.dp, BgSurface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CameraAlt, "Pick image",
                    tint = Color.White, modifier = Modifier.size(15.dp))
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            if (profilePicUri != null) "Tap to change photo" else "Tap to add a photo",
            fontSize = 12.sp, color = TextHint
        )
        Spacer(Modifier.height(24.dp))

        AuthTextField(username, onUsernameChange, "Username", Icons.Outlined.Person,
            error = usernameError)

        Spacer(Modifier.height(24.dp))
        PrimaryButton("Create account", onSignup, isLoading)
        Spacer(Modifier.height(20.dp))
    }
}