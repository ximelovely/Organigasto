package com.app.organigasto.data.local

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("organigasto_prefs", Context.MODE_PRIVATE)

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean("biometric_enabled", false)
        set(value) = prefs.edit().putBoolean("biometric_enabled", value).apply()

    var hasLoggedInOnce: Boolean
        get() = prefs.getBoolean("has_logged_in", false)
        set(value) = prefs.edit().putBoolean("has_logged_in", value).apply()

    var isLoggedInSession: Boolean
        get() = prefs.getBoolean("is_logged_in_session", false)
        set(value) = prefs.edit().putBoolean("is_logged_in_session", value).apply()

    var userEmail: String?
        get() = prefs.getString("user_email", null)
        set(value) = prefs.edit().putString("user_email", value).apply()

    var userName: String?
        get() = prefs.getString("user_name", null)
        set(value) = prefs.edit().putString("user_name", value).apply()

    var currentUserId: Int
        get() {
            val id = prefs.getInt("current_user_id", 1)
            return if (id > 0) id else 1
        }
        set(value) {
            val validId = if (value > 0) value else 1
            prefs.edit().putInt("current_user_id", validId).apply()
        }

    fun isTutorialCompleted(userId: Int): Boolean {
        return prefs.getBoolean("tutorial_completed_$userId", false)
    }

    fun setTutorialCompleted(userId: Int, completed: Boolean) {
        prefs.edit().putBoolean("tutorial_completed_$userId", completed).apply()
    }

    var themeMode: Int // 0: System, 1: Light, 2: Dark
        get() = prefs.getInt("theme_mode", 0)
        set(value) = prefs.edit().putInt("theme_mode", value).apply()
}
