package com.stark.podtrail.data

import com.stark.podtrail.network.FeedParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PodcastRepository @Inject constructor(
    private val dao: PodcastDao,
    private val parser: FeedParser
) {

    fun allPodcasts() = dao.getAllPodcasts()
    
    suspend fun allPodcastsDirect() = dao.getAllPodcasts().first().map { it.podcast }

    fun favoritePodcasts() = dao.getFavoritePodcasts()

    suspend fun toggleFavorite(id: Long, currentStatus: Boolean) {
        dao.updateFavoriteStatus(id, !currentStatus)
    }

    suspend fun addPodcast(feedUrl: String, genre: String? = null): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val (mappedPodcast, episodes) = parser.fetchFeed(feedUrl)
            val podcastTitle = mappedPodcast?.title ?: feedUrl
            val podcastImage = mappedPodcast?.imageUrl
            val podcastDesc = mappedPodcast?.description
            val podcastGenre = genre ?: mappedPodcast?.genre ?: "Uncategorized"
            
            // If podcast exists, reuse id; otherwise insert
            val existing = dao.getPodcastByFeedUrl(feedUrl)
            val podcastId = existing?.id ?: dao.insertPodcast(Podcast(
                title = podcastTitle, 
                feedUrl = feedUrl, 
                imageUrl = podcastImage,
                description = podcastDesc,
                primaryGenre = podcastGenre
            )).let { id ->
                if (id <= 0 && existing != null) existing.id else id
            }

            if (podcastId <= 0) return@withContext Result.failure<Long>(Exception("Failed to insert podcast"))

            val eps = episodes.map {
                val safeDesc = it.description?.take(200) // Store only a snippet initially to save space
                Episode(
                    podcastId = podcastId,
                    title = it.title,
                    guid = it.guid,
                    pubDate = it.pubDateMillis,
                    audioUrl = it.audioUrl,
                    imageUrl = it.imageUrl ?: podcastImage, // fallback to podcast image if episode image missing
                    episodeNumber = it.episodeNumber,
                    durationMillis = it.durationMillis,
                    description = safeDesc
                )
            }
            dao.upsertEpisodesMetadata(eps)
            Result.success(podcastId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshAllPodcasts() = withContext(Dispatchers.IO) {
        val podcasts = dao.getAllPodcasts().first().map { it.podcast }
        podcasts.forEach { podcast ->
            try {
                val (mappedPodcast, episodes) = parser.fetchFeed(podcast.feedUrl)
                
                // Update podcast details if changed (e.g. image, title, description)
                 if (mappedPodcast != null) {
                     val updatedPodcast = podcast.copy(
                        title = mappedPodcast.title?.takeIf { it.isNotBlank() } ?: podcast.title,
                        imageUrl = mappedPodcast.imageUrl?.takeIf { it.isNotBlank() } ?: podcast.imageUrl,
                        description = mappedPodcast.description?.takeIf { it.isNotBlank() } ?: podcast.description,
                        primaryGenre = mappedPodcast.genre ?: podcast.primaryGenre ?: "Uncategorized",
                        lastUpdated = System.currentTimeMillis()
                    )
                    // Only update if something relevant successfully parsed and is different
                    if (updatedPodcast != podcast) {
                        dao.updatePodcast(updatedPodcast)
                    }
                }

                val eps = episodes.map {
                     val safeDesc = it.description?.take(200)
                     Episode(
                        podcastId = podcast.id,
                        title = it.title,
                        guid = it.guid,
                        pubDate = it.pubDateMillis,
                        audioUrl = it.audioUrl,
                        imageUrl = it.imageUrl ?: mappedPodcast?.imageUrl ?: podcast.imageUrl,
                        episodeNumber = it.episodeNumber,
                        durationMillis = it.durationMillis,
                        description = safeDesc
                    )
                }
                dao.upsertEpisodesMetadata(eps)
            } catch (e: Exception) {
                // Ignore failure for individual podcast refresh
                e.printStackTrace()
            }
        }
    }

    fun episodesForPodcast(podcastId: Long, sortOption: SortOption = SortOption.DATE_NEWEST) = 
        when (sortOption) {
            SortOption.DATE_NEWEST -> dao.getEpisodesForPodcastLite(podcastId)
            SortOption.DATE_OLDEST -> dao.getEpisodesForPodcastLiteAsc(podcastId)
            SortOption.DURATION_SHORTEST -> dao.getEpisodesForPodcastLiteDurationAsc(podcastId)
            SortOption.DURATION_LONGEST -> dao.getEpisodesForPodcastLiteDurationDesc(podcastId)
        }



    suspend fun getEpisode(id: Long) = dao.getEpisodeById(id)
    
    fun getEpisodeFlow(id: Long) = dao.getEpisodeByIdFlow(id)

    suspend fun updateEpisodeDescription(episodeId: Long, description: String) {
        dao.updateEpisodeDescription(episodeId, description)
    }

    fun getHistory() = dao.getHistory()
    
    fun getAllEpisodesLite() = dao.getAllEpisodesLite()

    fun getEpisodesForMonth(year: Int, month: Int): Flow<List<EpisodeListItem>> {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startTime = cal.timeInMillis
        
        cal.add(Calendar.MONTH, 1)
        val endTime = cal.timeInMillis
        
        return dao.getEpisodesForMonth(startTime, endTime)
    }

    fun searchEpisodes(query: String): Flow<List<EpisodeListItem>> {
        return dao.searchEpisodes(query)
    }

    fun searchEpisodesInPodcast(podcastId: Long, query: String): Flow<List<EpisodeListItem>> {
        return dao.searchEpisodesInPodcast(podcastId, query)
    }

    fun searchPodcasts(query: String): Flow<List<Podcast>> {
        return dao.searchPodcasts(query)
    }

    fun getTotalTimeListened() = dao.getTotalDurationListened().map { it ?: 0L }

    fun getCurrentStreak() = dao.getAllLastPlayedTimestamps().map { timestamps ->
        if (timestamps.isEmpty()) return@map 0
        
        val zone = java.util.TimeZone.getDefault()
        fun toEpochDay(ts: Long): Long {
            val offset = zone.getOffset(ts)
            return (ts + offset) / 86400000L
        }

        val uniqueDays = timestamps.map { toEpochDay(it) }.toSortedSet()
        val today = toEpochDay(System.currentTimeMillis())

        var streak = 0
        var currentDay = if (uniqueDays.contains(today)) today else today - 1

        while (uniqueDays.contains(currentDay)) {
            streak++
            currentDay--
        }
        streak
    }

    fun getTopPodcastsByDuration() = dao.getTopPodcastsByDuration()

    fun getLast7DaysActivity(): Flow<Map<Int, Long>> {
        val zone = java.util.TimeZone.getDefault()
        val now = System.currentTimeMillis()
        val offset = zone.getOffset(now)
        val todayEpochDay = (now + offset) / 86400000L
        val startOf7DaysAgo = (todayEpochDay - 6) * 86400000L - offset

        return dao.getListenedEpisodesSince(startOf7DaysAgo).map { activityData ->
            val dailyMap = mutableMapOf<Int, Long>()
            val cal = Calendar.getInstance()
            for (i in 0..6) {
                cal.timeInMillis = (todayEpochDay - i) * 86400000L - offset
                val dayKey = cal.get(Calendar.DAY_OF_YEAR)
                dailyMap[dayKey] = 0L
            }

            activityData.forEach { data ->
                cal.timeInMillis = data.lastPlayedTimestamp
                val dayKey = cal.get(Calendar.DAY_OF_YEAR)
                if (dailyMap.containsKey(dayKey)) {
                    dailyMap[dayKey] = (dailyMap[dayKey] ?: 0L) + (data.durationMillis ?: 0L)
                }
            }
            dailyMap
        }
    }
    
    suspend fun getUpNext(): List<Episode> = withContext(Dispatchers.IO) {
        dao.getUpNextEpisodes()
    }

    suspend fun markEpisodeListened(episode: EpisodeListItem, listened: Boolean) {
        val fullEpisode = dao.getEpisodeById(episode.id) ?: return
        val now = System.currentTimeMillis()
        dao.updateEpisode(fullEpisode.copy(
            listened = listened,
            listenedAt = if (listened) now else null,
            lastPlayedTimestamp = if (listened) now else fullEpisode.lastPlayedTimestamp,
            playbackPosition = if (listened) 0 else fullEpisode.playbackPosition
        ))
    }
    
    suspend fun markEpisodeListened(episode: Episode, listened: Boolean) {
        val now = System.currentTimeMillis()
        dao.updateEpisode(episode.copy(
            listened = listened,
            listenedAt = if (listened) now else null,
            lastPlayedTimestamp = if (listened) now else episode.lastPlayedTimestamp,
            playbackPosition = if (listened) 0 else episode.playbackPosition
        ))
    }
    
    suspend fun updateEpisodeRatingAndNotes(episodeId: Long, rating: Int?, notes: String?) {
        val fullEpisode = dao.getEpisodeById(episodeId) ?: return
        dao.updateEpisode(fullEpisode.copy(
            userRating = rating,
            userNotes = notes
        ))
    }

    suspend fun deletePodcast(podcastId: Long) {
        dao.deletePodcast(podcastId)
    }

    suspend fun markPodcastListened(podcastId: Long, listened: Boolean) {
        val timestamp = if (listened) System.currentTimeMillis() else 0L
        dao.markPodcastEpisodesListened(podcastId, listened, timestamp)
    }

    suspend fun fetchRemoteEpisodeDescription(podcastId: Long, episodeGuid: String): String? = withContext(Dispatchers.IO) {
        val podcast = dao.getPodcastById(podcastId) ?: return@withContext null
        try {
            val (_, episodes) = parser.fetchFeed(podcast.feedUrl)
            val match = episodes.find { (it.guid == episodeGuid) || (it.title == episodeGuid) }
            match?.description
        } catch (e: Exception) {
            null
        }
    }

    suspend fun checkAndFixRestoringEpisodes() = withContext(Dispatchers.IO) {
        val count = dao.getRestoringCount()
        if (count > 0) {
            refreshAllPodcasts()
        }
    }

    suspend fun markEpisodesListenedBatch(episodeIds: Collection<Long>, listened: Boolean) = withContext(Dispatchers.IO) {
        if (episodeIds.isEmpty()) return@withContext
        val now = System.currentTimeMillis()
        dao.markEpisodesListenedBulk(episodeIds.toList(), listened, now)
    }

    suspend fun clearEpisodesTrackingBatch(episodeIds: Collection<Long>) = withContext(Dispatchers.IO) {
        if (episodeIds.isEmpty()) return@withContext
        dao.clearEpisodesTrackingBulk(episodeIds.toList())
    }

    fun getAllPlaylistCollections(): Flow<List<PlaylistCollection>> = dao.getAllPlaylistCollections()

    fun getEpisodesInPlaylist(collectionName: String): Flow<List<Episode>> = dao.getEpisodesInPlaylist(collectionName)

    suspend fun createPlaylistCollection(name: String, description: String? = null) = withContext(Dispatchers.IO) {
        dao.insertPlaylistCollection(PlaylistCollection(name = name, description = description))
    }

    suspend fun deletePlaylistCollection(collectionId: Long) = withContext(Dispatchers.IO) {
        dao.deletePlaylistCollection(collectionId)
    }

    suspend fun addEpisodeToPlaylist(episodeId: Long, playlistName: String) = withContext(Dispatchers.IO) {
        dao.insertPlaylist(Playlist(name = playlistName, episodeId = episodeId))
    }

    suspend fun addEpisodesToPlaylistBatch(episodeIds: Collection<Long>, playlistName: String) = withContext(Dispatchers.IO) {
        episodeIds.forEach { id ->
            dao.insertPlaylist(Playlist(name = playlistName, episodeId = id))
        }
    }

    suspend fun removeEpisodeFromPlaylist(episodeId: Long) = withContext(Dispatchers.IO) {
        dao.removeFromPlaylist(episodeId)
    }
}
