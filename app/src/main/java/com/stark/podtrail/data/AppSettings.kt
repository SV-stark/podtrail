package com.stark.podtrail.data

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = true,
    val useAmoled: Boolean = false,
    val customColor: Int = 0xFF0F5A56.toInt(),
    val profileImageUri: String? = null,
    val profileBgUri: String? = null,
    val userName: String? = null
)
