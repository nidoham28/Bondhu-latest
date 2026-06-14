package com.nidoham.bondhu.supabase.util

import com.nidoham.bondhu.supabase.models.AuthResult

interface AuthCallback {
    fun onResult(result: AuthResult)
}