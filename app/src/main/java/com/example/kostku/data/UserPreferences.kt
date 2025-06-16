package com.example.kostku.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class UserPreferences(
    val email: String = "",
    val name: String = ""
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesManager(private val context: Context) {
    companion object {
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val NAME_KEY = stringPreferencesKey("name")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        UserPreferences(
            email = preferences[EMAIL_KEY] ?: "",
            name = preferences[NAME_KEY] ?: ""
        )
    }

    suspend fun saveUserPreferences(email: String, name: String) {
        context.dataStore.edit { preferences ->
            preferences[EMAIL_KEY] = email
            preferences[NAME_KEY] = name
        }
    }

    suspend fun clearUserPreferences() {
        context.dataStore.edit { preferences ->
            preferences.remove(EMAIL_KEY)
            preferences.remove(NAME_KEY)
        }
    }
} 