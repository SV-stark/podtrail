package com.stark.podtrail.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: PodcastDatabase
) {
    private val dataStore = context.dataStore
    private val dao = database.podcastDao()
    private val json = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true
    }

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val USE_DYNAMIC = booleanPreferencesKey("use_dynamic")
        val USE_AMOLED = booleanPreferencesKey("use_amoled")
        val CUSTOM_COLOR = intPreferencesKey("custom_color")
        val PROFILE_IMAGE = stringPreferencesKey("profile_image")
        val PROFILE_BG = stringPreferencesKey("profile_bg")
        val USER_NAME = stringPreferencesKey("user_name")
    }

    val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            themeMode = ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name),
            useDynamicColor = prefs[Keys.USE_DYNAMIC] ?: true,
            useAmoled = prefs[Keys.USE_AMOLED] ?: false,
            customColor = prefs[Keys.CUSTOM_COLOR] ?: 0xFF0F5A56.toInt(),
            profileImageUri = prefs[Keys.PROFILE_IMAGE],
            profileBgUri = prefs[Keys.PROFILE_BG],
            userName = prefs[Keys.USER_NAME]
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setDynamicColor(enable: Boolean) {
        dataStore.edit { it[Keys.USE_DYNAMIC] = enable }
    }

    suspend fun setAmoled(enable: Boolean) {
        dataStore.edit { it[Keys.USE_AMOLED] = enable }
    }

    suspend fun setCustomColor(color: Int) {
        dataStore.edit { it[Keys.CUSTOM_COLOR] = color }
    }

    suspend fun setProfileImage(uri: String?) {
        dataStore.edit { 
            if (uri != null) it[Keys.PROFILE_IMAGE] = uri else it.remove(Keys.PROFILE_IMAGE)
        }
    }

    suspend fun setProfileBg(uri: String?) {
        dataStore.edit {
            if (uri != null) it[Keys.PROFILE_BG] = uri else it.remove(Keys.PROFILE_BG)
        }
    }

    suspend fun setUserName(name: String) {
        dataStore.edit { it[Keys.USER_NAME] = name }
    }

    fun getDatabasePath(): java.io.File {
        return context.getDatabasePath("podtrail.db")
    }

    suspend fun importDatabase(uri: android.net.Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) return@withContext false

                val jsonString = GZIPInputStream(inputStream).bufferedReader().use { it.readText() }

                // Try parsing as MinimalBackupData first (version 2)
                try {
                    val backupData = json.decodeFromString<MinimalBackupData>(jsonString)
                    if (!backupData.podcasts.isNullOrEmpty() || !backupData.episodes.isNullOrEmpty()) {
                        dao.importMinimalBackup(
                            podcasts = backupData.podcasts ?: emptyList(),
                            episodes = backupData.episodes ?: emptyList()
                        )
                        return@withContext true
                    }
                } catch (e: Exception) {
                    // Fallback to legacy version
                }

                val legacyBackup = json.decodeFromString<BackupData>(jsonString)
                dao.importLegacyBackup(
                    podcasts = legacyBackup.podcasts,
                    episodes = legacyBackup.episodes
                )
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun exportDatabase(uri: android.net.Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val podcasts = dao.getAllPodcastsSync()
                val episodes = dao.getAllEpisodesSync()
                
                val minimalPodcasts = podcasts.map { p ->
                    MinimalPodcast(
                        feedUrl = p.feedUrl,
                        title = p.title,
                        isFavorite = p.isFavorite
                    )
                }
                
                val podcastIdToUrl = podcasts.associate { it.id to it.feedUrl }
                
                val minimalEpisodes = episodes.filter { it.listened || it.playbackPosition > 0 }
                    .mapNotNull { ep ->
                        val url = podcastIdToUrl[ep.podcastId]
                        if (url != null) {
                            MinimalEpisode(
                                feedUrl = url,
                                guid = ep.guid,
                                listened = ep.listened,
                                playbackPosition = ep.playbackPosition,
                                lastPlayedTimestamp = ep.lastPlayedTimestamp
                            )
                        } else null
                    }

                val backupData = MinimalBackupData(
                    podcasts = minimalPodcasts,
                    episodes = minimalEpisodes
                )
                
                val jsonString = json.encodeToString(backupData)
                
                val outputStream = context.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    GZIPOutputStream(outputStream).bufferedWriter().use { it.write(jsonString) }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun exportOpml(uri: android.net.Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val podcasts = dao.getAllPodcastsSync()
                val opmlManager = OpmlManager()
                val opmlString = opmlManager.generateOpml(podcasts)
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(opmlString) }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun importOpml(uri: android.net.Uri, podcastRepository: PodcastRepository): Int {
        return withContext(Dispatchers.IO) {
            var count = 0
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext 0
                val opmlManager = OpmlManager()
                val outlines = opmlManager.parseOpml(inputStream)
                
                val existingPodcasts = podcastRepository.allPodcastsDirect()
                val existingUrls = existingPodcasts.map { it.feedUrl }.toSet()
                
                outlines.forEach { outline ->
                    if (!existingUrls.contains(outline.xmlUrl)) {
                        val result = podcastRepository.addPodcast(outline.xmlUrl)
                        if (result.isSuccess) {
                            count++
                        }
                    }
                }
                count
            } catch (e: Exception) {
                e.printStackTrace()
                0
            }
        }
    }
}
