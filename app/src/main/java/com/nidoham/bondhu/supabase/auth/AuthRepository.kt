package com.nidoham.bondhu.supabase.auth

import com.nidoham.bondhu.supabase.models.AuthResult
import com.nidoham.bondhu.supabase.util.AuthCallback
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.logging.Logger
import kotlin.time.Clock

@Suppress("unused", "DEPRECATION")
class AuthRepository(
    private val supabase: SupabaseClient,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val logger = Logger.getLogger("AuthRepository")

    // ═══ Session State for Jetpack Compose (Flow) ═══
    val sessionState = supabase.auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> status.session.expiresAt > Clock.System.now()
            else -> false
        }
    }

    // ═══ Check if User is Logged In ═══
    suspend fun isLoggedIn(): Boolean {
        val session = supabase.auth.currentSessionOrNull() ?: return false
        return session.expiresAt > Clock.System.now()
    }

    // ════════════════════════════ Suspend Methods (Return AuthResult) ════════════════════════════

    // ─────────────────────────── Sign Up ───────────────────────────
    suspend fun signUp(email: String, password: String): AuthResult {
        if (email.isBlank() || password.isBlank()) {
            return AuthResult(isSuccess = false, exception = "Email and password cannot be empty")
        }
        if (password.length < 6) {
            return AuthResult(
                isSuccess = false,
                exception = "Password must be at least 6 characters"
            )
        }

        return try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            logger.info("Sign-up successful for $email")
            AuthResult(isSuccess = true, exception = null)
        } catch (e: Exception) {
            logger.warning("Sign-up failed: ${e.message}")
            val message = when {
                e.message?.contains("User already registered", ignoreCase = true) == true ->
                    "User already exists"
                e.message?.contains("Invalid email", ignoreCase = true) == true ->
                    "Invalid email format"
                else -> "Sign-up failed: ${e.message}"
            }
            AuthResult(isSuccess = false, exception = message)
        }
    }

    // ─────────────────────────── Sign In ───────────────────────────
    suspend fun signIn(email: String, password: String): AuthResult {
        if (email.isBlank() || password.isBlank()) {
            return AuthResult(isSuccess = false, exception = "Email and password cannot be empty")
        }

        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            logger.info("Sign-in successful for $email")
            AuthResult(isSuccess = true, exception = null)
        } catch (e: Exception) {
            logger.warning("Sign-in failed: ${e.message}")
            val message = when {
                e.message?.contains("Invalid login credentials", ignoreCase = true) == true ->
                    "Invalid email or password"
                e.message?.contains("Email not confirmed", ignoreCase = true) == true ->
                    "Please verify your email address"
                else -> "Sign-in failed: ${e.message}"
            }
            AuthResult(isSuccess = false, exception = message)
        }
    }

    // ─────────────────────────── Sign Out ──────────────────────────
    suspend fun signOut(): AuthResult {
        return try {
            supabase.auth.signOut()
            logger.info("User signed out")
            AuthResult(isSuccess = true, exception = null)
        } catch (e: Exception) {
            logger.warning("Sign-out failed: ${e.message}")
            AuthResult(isSuccess = false, exception = "Sign-out failed: ${e.message}")
        }
    }

    // ─────────────────────────── Password Reset ────────────────────
    suspend fun resetPassword(email: String): AuthResult {
        return try {
            supabase.auth.resetPasswordForEmail(email)
            AuthResult(isSuccess = true, exception = null)
        } catch (e: Exception) {
            AuthResult(isSuccess = false, exception = "Failed to send reset email: ${e.message}")
        }
    }

    // ════════════════════════════ User Info ════════════════════════════
    fun getCurrentUserEmail(): String? = supabase.auth.currentUserOrNull()?.email
    fun getCurrentUserId(): String? = supabase.auth.currentUserOrNull()?.id

    // ════════════════════════════ Callback Methods (FirebaseAuth-like) ════════════════════════════

    // Sign Up with callback
    fun signUp(
        email: String,
        password: String,
        callback: AuthCallback
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            val result = signUp(email, password)
            coroutineScope.launch {
                callback.onResult(result)
            }
        }
    }

    // Sign In with callback
    fun signIn(
        email: String,
        password: String,
        callback: AuthCallback
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            val result = signIn(email, password)
            coroutineScope.launch {
                callback.onResult(result)
            }
        }
    }

    // Sign Out with callback
    fun signOut(
        callback: AuthCallback
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            val result = signOut()
            coroutineScope.launch {
                callback.onResult(result)
            }
        }
    }

    // Reset Password with callback
    fun resetPassword(
        email: String,
        callback: AuthCallback
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            val result = resetPassword(email)
            coroutineScope.launch {
                callback.onResult(result)
            }
        }
    }
}