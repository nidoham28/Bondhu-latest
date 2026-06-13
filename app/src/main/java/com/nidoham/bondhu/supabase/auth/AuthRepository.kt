package com.nidoham.bondhu.supabase.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Result wrapper for one‑shot operations (sign‑in, sign‑up, sign‑out)
sealed class AuthOperationResult<out T> {
    data class Success<T>(val data: T) : AuthOperationResult<T>()
    data class Error(val message: String, val exception: Exception? = null) : AuthOperationResult<Nothing>()
}

class AuthRepository(private val supabase: SupabaseClient) {

    /**
     * A cold Flow that emits `true` when the user is authenticated with a valid session,
     * and `false` otherwise. Collect this in a lifecycle‑aware component (e.g. ViewModel).
     */
    val sessionState: Flow<Boolean> = supabase.auth.sessionStatus.map { status ->
        if (status is SessionStatus.Authenticated) {
            // Check that the session hasn't expired
            status.session.expiresAt > kotlin.time.Clock.System.now()
        } else {
            false
        }
    }

    /**
     * One‑shot check for current login status. Prefer collecting [sessionState] for UI.
     */
    suspend fun isLoggedIn(): Boolean {
        val session = supabase.auth.currentSessionOrNull() ?: return false
        return session.expiresAt > kotlin.time.Clock.System.now()
    }

    /**
     * Sign up with email and password.
     */
    suspend fun signUp(email: String, password: String): AuthOperationResult<Boolean> {
        if (email.isBlank() || password.isBlank()) {
            return AuthOperationResult.Error("Email and password cannot be empty")
        }
        if (password.length < 6) {
            return AuthOperationResult.Error("Password must be at least 6 characters")
        }

        return try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            AuthOperationResult.Success(true)
        } catch (e: Exception) {
            val message = when {
                e.message?.contains("User already registered", ignoreCase = true) == true ->
                    "User already exists"
                e.message?.contains("Invalid email", ignoreCase = true) == true ->
                    "Invalid email format"
                else -> "Sign up failed: ${e.message}"
            }
            AuthOperationResult.Error(message, e)
        }
    }

    /**
     * Sign in with email and password.
     */
    suspend fun signIn(email: String, password: String): AuthOperationResult<Boolean> {
        if (email.isBlank() || password.isBlank()) {
            return AuthOperationResult.Error("Email and password cannot be empty")
        }

        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            AuthOperationResult.Success(true)
        } catch (e: Exception) {
            val message = when {
                e.message?.contains("Invalid login credentials", ignoreCase = true) == true ->
                    "Invalid email or password"
                e.message?.contains("Email not confirmed", ignoreCase = true) == true ->
                    "Please verify your email first"
                else -> "Sign in failed: ${e.message}"
            }
            AuthOperationResult.Error(message, e)
        }
    }

    /**
     * Sign out the current user.
     */
    suspend fun signOut(): AuthOperationResult<Boolean> {
        return try {
            supabase.auth.signOut()
            AuthOperationResult.Success(true)
        } catch (e: Exception) {
            AuthOperationResult.Error("Sign out failed: ${e.message}", e)
        }
    }

    /**
     * Returns the current user's email, or null if not logged in.
     */
    suspend fun getCurrentUserEmail(): String? {
        return supabase.auth.currentUserOrNull()?.email
    }
}