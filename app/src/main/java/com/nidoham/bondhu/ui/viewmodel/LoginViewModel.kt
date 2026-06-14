package com.nidoham.bondhu.ui.viewmodel

import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nidoham.bondhu.supabase.auth.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// UI state models
// ─────────────────────────────────────────────────────────────────────────────

/** Which card the user is looking at. */
enum class AuthTab { LOGIN, SIGNUP }

/**
 * Complete snapshot of everything the LoginScreen needs to render.
 * The screen has NO business logic — it only reads this state and
 * forwards events back to the ViewModel.
 */
data class LoginUiState(
    // ── tab / step ──────────────────────────────────────────────────────────
    val activeTab: AuthTab = AuthTab.LOGIN,
    val signupStep: Int = 1,               // 1 = credentials  |  2 = profile

    // ── login fields ────────────────────────────────────────────────────────
    val loginEmail: String = "",
    val loginPassword: String = "",
    val loginPasswordVisible: Boolean = false,

    // ── signup step-1 fields ─────────────────────────────────────────────────
    val signupEmail: String = "",
    val signupPassword: String = "",
    val signupConfirmPassword: String = "",
    val signupPasswordVisible: Boolean = false,
    val signupConfirmPasswordVisible: Boolean = false,

    // ── signup step-2 fields ─────────────────────────────────────────────────
    val username: String = "",
    val profilePicUri: Uri? = null,

    // ── transient UI ─────────────────────────────────────────────────────────
    val isLoading: Boolean = false,
    val errorMessage: String? = null,       // shown as a snackbar / inline error

    // ── field-level validation ───────────────────────────────────────────────
    val loginEmailError: String? = null,
    val loginPasswordError: String? = null,
    val signupEmailError: String? = null,
    val signupPasswordError: String? = null,
    val signupConfirmPasswordError: String? = null,
    val usernameError: String? = null,
)

/** One-shot side effects that the screen should act on once, then forget. */
sealed class AuthEvent {
    /** Navigate away from the auth screen — the user is now signed in. */
    data object NavigateToHome : AuthEvent()

    /** Show a short success toast / snackbar. */
    data class ShowSuccessMessage(val message: String) : AuthEvent()
}

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /** Collect in the screen with `repeatOnLifecycle(STARTED)`. */
    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // ── Tab / step navigation ────────────────────────────────────────────────

    fun switchTab(tab: AuthTab) {
        _uiState.update {
            it.copy(
                activeTab = tab,
                signupStep = 1,
                errorMessage = null
            )
        }
    }

    fun goToSignupStep2() {
        if (!validateSignupStep1()) return
        _uiState.update { it.copy(signupStep = 2, errorMessage = null) }
    }

    fun goBackToSignupStep1() {
        _uiState.update { it.copy(signupStep = 1, errorMessage = null) }
    }

    // ── Login field updates ──────────────────────────────────────────────────

    fun onLoginEmailChange(value: String) =
        _uiState.update { it.copy(loginEmail = value, loginEmailError = null, errorMessage = null) }

    fun onLoginPasswordChange(value: String) =
        _uiState.update { it.copy(loginPassword = value, loginPasswordError = null, errorMessage = null) }

    fun toggleLoginPasswordVisibility() =
        _uiState.update { it.copy(loginPasswordVisible = !it.loginPasswordVisible) }

    // ── Signup step-1 field updates ──────────────────────────────────────────

    fun onSignupEmailChange(value: String) =
        _uiState.update { it.copy(signupEmail = value, signupEmailError = null, errorMessage = null) }

    fun onSignupPasswordChange(value: String) =
        _uiState.update { it.copy(signupPassword = value, signupPasswordError = null, errorMessage = null) }

    fun onSignupConfirmPasswordChange(value: String) =
        _uiState.update { it.copy(signupConfirmPassword = value, signupConfirmPasswordError = null, errorMessage = null) }

    fun toggleSignupPasswordVisibility() =
        _uiState.update { it.copy(signupPasswordVisible = !it.signupPasswordVisible) }

    fun toggleSignupConfirmPasswordVisibility() =
        _uiState.update { it.copy(signupConfirmPasswordVisible = !it.signupConfirmPasswordVisible) }

    // ── Signup step-2 field updates ──────────────────────────────────────────

    fun onUsernameChange(value: String) =
        _uiState.update { it.copy(username = value, usernameError = null, errorMessage = null) }

    fun onProfilePicSelected(uri: Uri?) =
        _uiState.update { it.copy(profilePicUri = uri) }

    // ── Actions ──────────────────────────────────────────────────────────────

    /** Called when the user taps "Login". */
    fun login() {
        if (!validateLoginFields()) return

        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // ✅ Use AuthResult with isSuccess / exception
            val result = authRepository.signIn(state.loginEmail.trim(), state.loginPassword)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false) }
                _events.send(AuthEvent.NavigateToHome)
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.exception ?: "Sign-in failed")
                }
            }
        }
    }

    /**
     * Called when the user taps "Create account" on step 2.
     * Signs up with Supabase using the credentials from step 1.
     * Username and profile pic are stored separately (e.g. in your own user
     * profile table) — add that call inside the Success branch.
     */
    fun signUp() {
        if (!validateSignupStep2()) return

        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = authRepository.signUp(state.signupEmail.trim(), state.signupPassword)
            if (result.isSuccess) {
                // TODO: persist username + profilePicUri to your profile table here
                _uiState.update { it.copy(isLoading = false) }
                _events.send(
                    AuthEvent.ShowSuccessMessage("Account created! Check your email to verify.")
                )
                // Switch back to login so the user can sign in after confirming email
                _uiState.update { it.copy(activeTab = AuthTab.LOGIN, signupStep = 1) }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.exception ?: "Sign-up failed")
                }
            }
        }
    }

    // ── Validation helpers ───────────────────────────────────────────────────

    private fun validateLoginFields(): Boolean {
        val state = _uiState.value
        var valid = true

        val emailError = when {
            state.loginEmail.isBlank()            -> "Email is required"
            !state.loginEmail.isValidEmail()      -> "Enter a valid email"
            else                                  -> null
        }
        val passwordError = when {
            state.loginPassword.isBlank()         -> "Password is required"
            else                                  -> null
        }

        _uiState.update {
            it.copy(loginEmailError = emailError, loginPasswordError = passwordError)
        }

        if (emailError != null || passwordError != null) valid = false
        return valid
    }

    private fun validateSignupStep1(): Boolean {
        val state = _uiState.value
        var valid = true

        val emailError = when {
            state.signupEmail.isBlank()                          -> "Email is required"
            !state.signupEmail.isValidEmail()                    -> "Enter a valid email"
            else                                                 -> null
        }
        val passwordError = when {
            state.signupPassword.isBlank()                       -> "Password is required"
            state.signupPassword.length < 6                      -> "Minimum 6 characters"
            else                                                 -> null
        }
        val confirmError = when {
            state.signupConfirmPassword.isBlank()                -> "Please confirm your password"
            state.signupConfirmPassword != state.signupPassword  -> "Passwords do not match"
            else                                                 -> null
        }

        _uiState.update {
            it.copy(
                signupEmailError           = emailError,
                signupPasswordError        = passwordError,
                signupConfirmPasswordError = confirmError
            )
        }

        if (emailError != null || passwordError != null || confirmError != null) valid = false
        return valid
    }

    private fun validateSignupStep2(): Boolean {
        val state = _uiState.value

        val usernameError = when {
            state.username.isBlank()      -> "Username is required"
            state.username.length < 3     -> "Minimum 3 characters"
            state.username.length > 20    -> "Maximum 20 characters"
            !state.username.isValidUsername() -> "Letters, numbers, and underscores only"
            else                          -> null
        }

        _uiState.update { it.copy(usernameError = usernameError) }
        return usernameError == null
    }

    // ── Factory ─────────────────────────────────────────────────────────────

    companion object {
        fun factory(authRepository: AuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    LoginViewModel(authRepository) as T
            }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Extension helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun String.isValidEmail(): Boolean =
    Patterns.EMAIL_ADDRESS.matcher(this).matches()

private fun String.isValidUsername(): Boolean =
    matches(Regex("^[a-zA-Z0-9_]+$"))