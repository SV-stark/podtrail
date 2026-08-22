package com.stark.podtrail.data

import android.content.Context
import androidx.room3.Room
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SettingsRepositoryTest {

    private lateinit var context: Context
    private lateinit var database: PodcastDatabase
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        database = Room.inMemoryDatabaseBuilder(context, PodcastDatabase::class.java).build()
        settingsRepository = SettingsRepository(context, database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testDefaultSettings() {
        runBlocking {
            val settings = settingsRepository.settings.first()
            assertEquals(ThemeMode.SYSTEM, settings.themeMode)
            assertTrue(settings.useDynamicColor)
            assertFalse(settings.useAmoled)
        }
    }

    @Test
    fun testUpdateThemeMode() {
        runBlocking {
            settingsRepository.setThemeMode(ThemeMode.DARK)
            val settings = settingsRepository.settings.first()
            assertEquals(ThemeMode.DARK, settings.themeMode)
        }
    }

    @Test
    fun testUpdateDynamicColorAndAmoled() {
        runBlocking {
            settingsRepository.setDynamicColor(false)
            settingsRepository.setAmoled(true)
            settingsRepository.setCustomColor(0xFF00FF00.toInt())

            val settings = settingsRepository.settings.first()
            assertFalse(settings.useDynamicColor)
            assertTrue(settings.useAmoled)
            assertEquals(0xFF00FF00.toInt(), settings.customColor)
        }
    }

    @Test
    fun testUpdateProfileInfo() {
        runBlocking {
            settingsRepository.setUserName("Stark")
            settingsRepository.setProfileImage("content://media/profile.jpg")
            settingsRepository.setProfileBg("content://media/bg.jpg")

            val settings = settingsRepository.settings.first()
            assertEquals("Stark", settings.userName)
            assertEquals("content://media/profile.jpg", settings.profileImageUri)
            assertEquals("content://media/bg.jpg", settings.profileBgUri)
        }
    }
}
