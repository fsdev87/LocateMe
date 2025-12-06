package com.mustafafaraz.locateme.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class TokenManager(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("auth_prefs")
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    }

    // Save token and user info after login/signup
    suspend fun saveAuthData(token: String, userId: String, userName: String, userEmail: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_ID_KEY] = userId
            prefs[USER_NAME_KEY] = userName
            prefs[USER_EMAIL_KEY] = userEmail
        }
    }

    // Get token (suspend function - can be called directly in coroutines)
    suspend fun getToken(): String? {
        return context.dataStore.data.first()[TOKEN_KEY]
    }

    // Get token as Flow (for observing changes)
    fun getTokenFlow(): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[TOKEN_KEY]
        }
    }

    // Check if user is logged in (CRITICAL for SplashActivity)
    suspend fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    // Get authorization header for API calls
    suspend fun getAuthHeader(): String {
        val token = getToken()
        return "Bearer $token"
    }

    // Clear all data (logout)
    suspend fun clearAllData() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    // Get user info
    suspend fun getUserId(): String? {
        return context.dataStore.data.first()[USER_ID_KEY]
    }

    suspend fun getUserName(): String? {
        return context.dataStore.data.first()[USER_NAME_KEY]
    }

    suspend fun getUserEmail(): String? {
        return context.dataStore.data.first()[USER_EMAIL_KEY]
    }
}