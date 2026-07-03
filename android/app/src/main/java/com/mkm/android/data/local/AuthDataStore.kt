package com.mkm.android.data.local

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.authDataStore by preferencesDataStore(name = "auth_prefs")
val TOKEN_KEY = stringPreferencesKey("jwt_token")
