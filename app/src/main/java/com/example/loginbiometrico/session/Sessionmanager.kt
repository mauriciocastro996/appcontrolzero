package com.example.loginbiometrico.session

import android.content.Context
import android.content.SharedPreferences

/**
 * Guarda y recupera los tokens JWT en SharedPreferences.
 * Uso:
 *   SessionManager.init(context)
 *   SessionManager.saveTokens(sessionToken, refreshToken)
 *   val token = SessionManager.sessionToken
 */
object SessionManager {

    private const val PREFS_NAME = "controlzero_session"
    private const val KEY_SESSION_TOKEN = "session_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveTokens(sessionToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_SESSION_TOKEN, sessionToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    val sessionToken: String?
        get() = prefs.getString(KEY_SESSION_TOKEN, null)

    val refreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)

    val isLoggedIn: Boolean
        get() = sessionToken != null

    fun clear() {
        prefs.edit().clear().apply()
    }
}