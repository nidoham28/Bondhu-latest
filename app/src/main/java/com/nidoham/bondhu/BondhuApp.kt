package com.nidoham.bondhu

import android.app.Application
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import timber.log.Timber

class BondhuApp : Application() {

    // Singleton client – accessible globally
    lateinit var supabase: SupabaseClient
        private set

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Initialize Supabase
        supabase = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,      // you'll define these
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
            install(Storage)
            install(Functions)
            // Optional: configure each module here
        }

        Timber.d("Supabase client initialized")
    }
}