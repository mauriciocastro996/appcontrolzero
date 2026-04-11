package com.example.loginbiometrico.utils

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "controlzero_prefs"
        private const val KEY_SESSION_TOKEN = "session_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_ROLE = "user_role"
    }
    
    fun saveTokens(sessionToken: String, refreshToken: String) {
        prefs.edit().apply {
            putString(KEY_SESSION_TOKEN, sessionToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            apply()
        }
    }
    
    fun getSessionToken(): String? {
        return prefs.getString(KEY_SESSION_TOKEN, null)
    }
    
    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }
    
    fun saveUserInfo(email: String, userId: Long, role: String) {
        prefs.edit().apply {
            putString(KEY_USER_EMAIL, email)
            putLong(KEY_USER_ID, userId)
            putString(KEY_USER_ROLE, role)
            apply()
        }
    }
    
    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }
    
    fun getUserId(): Long {
        return prefs.getLong(KEY_USER_ID, -1)
    }
    
    fun getUserRole(): String? {
        return prefs.getString(KEY_USER_ROLE, null)
    }
    
    fun clearTokens() {
        prefs.edit().apply {
            remove(KEY_SESSION_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_ID)
            remove(KEY_USER_ROLE)
            apply()
        }
    }
    
    fun isLoggedIn(): Boolean {
        return getSessionToken() != null
    }
}
