package com.stark.podtrail.storage

import com.stark.podtrail.data.PodcastDao
import com.stark.podtrail.data.PodcastDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus

data class StorageStats(
    val totalEpisodes: Int,
    val totalSizeMB: Double,
    val episodesWithoutDescription: Int,
    val oldUnlistenedEpisodes: Int,
    val podcastsLastUpdated: Map<String, Long>
)

enum class CleanupOption {
    OLD_UNLISTENED_EPISODES,
    TRUNCATE_DESCRIPTIONS,
    REMOVE_INACTIVE_PODCASTS,
    COMPACT_DATABASE
}

data class CleanupResult(
    val option: CleanupOption,
    val itemsAffected: Int,
    val spaceSavedMB: Double
)

class StorageManager(
    private val dao: PodcastDao,
    private val database: PodcastDatabase? = null
) {
    
    suspend fun getStorageStats(): StorageStats = withContext(Dispatchers.IO) {
        val allEpisodes = dao.getAllEpisodesSync()
        val totalEpisodes = allEpisodes.size
        
        // Estimate size (average record size ~1.5KB)
        val totalSizeMB = (totalEpisodes * 1536.0) / (1024 * 1024)
        
        val episodesWithoutDescription = dao.countShortDescriptionEpisodes()
        
        val sixMonthsAgo = Clock.System.now().minus(180, DateTimeUnit.DAY, TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val oldUnlistenedEpisodes = dao.countOldUnlistenedEpisodes(sixMonthsAgo)
        
        val podcastsLastUpdated = dao.getAllPodcastsSync().associate { 
            podcast -> podcast.title to (podcast.lastUpdated ?: 0L)
        }
        
        StorageStats(
            totalEpisodes = totalEpisodes,
            totalSizeMB = totalSizeMB,
            episodesWithoutDescription = episodesWithoutDescription,
            oldUnlistenedEpisodes = oldUnlistenedEpisodes,
            podcastsLastUpdated = podcastsLastUpdated
        )
    }
    
    suspend fun performCleanup(option: CleanupOption): CleanupResult = withContext(Dispatchers.IO) {
        when (option) {
            CleanupOption.OLD_UNLISTENED_EPISODES -> {
                val sixMonthsAgo = Clock.System.now().minus(180, DateTimeUnit.DAY, TimeZone.currentSystemDefault()).toEpochMilliseconds()
                val deletedCount = dao.deleteOldUnlistenedEpisodes(sixMonthsAgo)
                val estimatedSaved = (deletedCount * 1536.0) / (1024 * 1024)
                
                CleanupResult(
                    option = option,
                    itemsAffected = deletedCount,
                    spaceSavedMB = estimatedSaved
                )
            }
            
            CleanupOption.TRUNCATE_DESCRIPTIONS -> {
                val updatedCount = dao.truncateLongDescriptions()
                val estimatedSaved = (updatedCount * 512.0) / (1024 * 1024)
                
                CleanupResult(
                    option = option,
                    itemsAffected = updatedCount,
                    spaceSavedMB = estimatedSaved
                )
            }
            
            CleanupOption.REMOVE_INACTIVE_PODCASTS -> {
                val oneYearAgo = Clock.System.now().minus(365, DateTimeUnit.DAY, TimeZone.currentSystemDefault()).toEpochMilliseconds()
                val deletedCount = dao.deleteInactivePodcasts(oneYearAgo)
                val estimatedSaved = (deletedCount * 10240.0) / (1024 * 1024)
                
                CleanupResult(
                    option = option,
                    itemsAffected = deletedCount,
                    spaceSavedMB = estimatedSaved
                )
            }
            
            CleanupOption.COMPACT_DATABASE -> {
                try {
                    dao.vacuum(androidx.room3.RoomRawQuery("VACUUM"))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                CleanupResult(
                    option = option,
                    itemsAffected = 1,
                    spaceSavedMB = 0.1
                )
            }
        }
    }
    
    suspend fun autoCleanup(): List<CleanupResult> = withContext(Dispatchers.IO) {
        val stats = getStorageStats()
        val cleanupResults = mutableListOf<CleanupResult>()
        
        if (stats.oldUnlistenedEpisodes > 100) {
            cleanupResults.add(performCleanup(CleanupOption.OLD_UNLISTENED_EPISODES))
        }
        
        if (stats.episodesWithoutDescription > 50) {
            cleanupResults.add(performCleanup(CleanupOption.TRUNCATE_DESCRIPTIONS))
        }
        
        if (stats.totalSizeMB > 100) {
            cleanupResults.add(performCleanup(CleanupOption.COMPACT_DATABASE))
        }
        
        cleanupResults
    }
}