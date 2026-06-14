package com.nidoham.bondhu.supabase.models

data class AuthResult(
    val isSuccess: Boolean,
    val exception: String? = null
)