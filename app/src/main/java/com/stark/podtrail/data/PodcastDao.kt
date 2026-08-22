package com.stark.podtrail.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.RawQuery
import androidx.room3.RoomRawQuery
import androidx.room3.Transaction
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PodcastDao {
    @Transaction
    @Query("""
        SELECT 
            p.*, 
            COUNT(e.id) as totalEpisodes,
            SUM(CASE WHEN e.listened = 1 THEN 1 ELSE 0 END) as listenedEpisodes,
            SUM(CASE WHEN e.listened = 1 THEN e.durationMillis ELSE 0 END) as timeListened
        FROM podcasts p
        LEFT JOIN episodes e ON p.id = e.podcastId
        GROUP BY p.id
        ORDER BY p.title
    """)
    abstract fun getAllPodcasts(): Flow<List<PodcastWithStats>>

    @Query("SELECT * FROM podcasts WHERE isFavorite = 1")
    abstract fun getFavoritePodcasts(): Flow<List<Podcast>>

    @Query("UPDATE podcasts SET isFavorite = :isFavorite WHERE id = :id")
    abstract suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    @Query("UPDATE episodes SET listened = :listened, listenedAt = :timestamp WHERE podcastId = :podcastId")
    abstract suspend fun markPodcastEpisodesListened(podcastId: Long, listened: Boolean, timestamp: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertPodcast(podcast: Podcast): Long

    @Query("SELECT * FROM podcasts WHERE feedUrl = :feedUrl LIMIT 1")
    abstract suspend fun getPodcastByFeedUrl(feedUrl: String): Podcast?

    @Query("SELECT * FROM podcasts WHERE id = :id LIMIT 1")
    abstract suspend fun getPodcastById(id: Long): Podcast?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertEpisodes(episodes: List<Episode>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertEpisode(episode: Episode): Long

    @Query("SELECT * FROM episodes WHERE podcastId = :podcastId ORDER BY pubDate DESC")
    abstract fun getEpisodesForPodcast(podcastId: Long): Flow<List<Episode>>

    @Query("SELECT id, podcastId, title, pubDate, imageUrl, episodeNumber, durationMillis, listened, listenedAt, playbackPosition, lastPlayedTimestamp, userRating, userNotes FROM episodes WHERE podcastId = :podcastId ORDER BY pubDate DESC")
    abstract fun getEpisodesForPodcastLite(podcastId: Long): Flow<List<EpisodeListItem>>

    @Query("SELECT * FROM episodes WHERE podcastId = :podcastId ORDER BY pubDate ASC")
    abstract fun getEpisodesForPodcastAsc(podcastId: Long): Flow<List<Episode>>
    
    @Query("SELECT id, podcastId, title, pubDate, imageUrl, episodeNumber, durationMillis, listened, listenedAt, playbackPosition, lastPlayedTimestamp, userRating, userNotes FROM episodes WHERE podcastId = :podcastId ORDER BY pubDate ASC")
    abstract fun getEpisodesForPodcastLiteAsc(podcastId: Long): Flow<List<EpisodeListItem>>

    @Query("SELECT id, podcastId, title, pubDate, imageUrl, episodeNumber, durationMillis, listened, listenedAt, playbackPosition, lastPlayedTimestamp, userRating, userNotes FROM episodes WHERE podcastId = :podcastId ORDER BY durationMillis ASC")
    abstract fun getEpisodesForPodcastLiteDurationAsc(podcastId: Long): Flow<List<EpisodeListItem>>

    @Query("SELECT id, podcastId, title, pubDate, imageUrl, episodeNumber, durationMillis, listened, listenedAt, playbackPosition, lastPlayedTimestamp, userRating, userNotes FROM episodes WHERE podcastId = :podcastId ORDER BY durationMillis DESC")
    abstract fun getEpisodesForPodcastLiteDurationDesc(podcastId: Long): Flow<List<EpisodeListItem>>

    @Query("SELECT * FROM episodes WHERE listened = 1 ORDER BY listenedAt DESC")
    abstract fun getHistory(): Flow<List<Episode>>

    @Query("SELECT * FROM episodes WHERE id = :episodeId LIMIT 1")
    abstract suspend fun getEpisodeById(episodeId: Long): Episode?

    @Query("SELECT * FROM episodes WHERE id = :episodeId LIMIT 1")
    abstract fun getEpisodeByIdFlow(episodeId: Long): Flow<Episode?>

    @Query("UPDATE episodes SET description = :description WHERE id = :episodeId")
    abstract suspend fun updateEpisodeDescription(episodeId: Long, description: String)

    @Update
    abstract suspend fun updateEpisode(episode: Episode)

    @Update
    abstract suspend fun updatePodcast(podcast: Podcast)

    @Query("DELETE FROM podcasts")
    abstract suspend fun deleteAllPodcasts()

    @Query("DELETE FROM episodes")
    abstract suspend fun deleteAllEpisodes()

    @Query("DELETE FROM podcasts WHERE id = :podcastId")
    abstract suspend fun deletePodcastById(podcastId: Long)

    @Query("DELETE FROM episodes WHERE podcastId = :podcastId")
    abstract suspend fun deleteEpisodesByPodcastId(podcastId: Long)

    @Transaction
    open suspend fun deletePodcast(podcastId: Long) {
        deleteEpisodesByPodcastId(podcastId)
        deletePodcastById(podcastId)
    }

    @Query("SELECT SUM(durationMillis) FROM episodes WHERE listened = 1")
    abstract fun getTotalDurationListened(): Flow<Long?>

    @androidx.room3.Query("SELECT id, podcastId, title, pubDate, imageUrl, episodeNumber, durationMillis, listened, listenedAt, playbackPosition, lastPlayedTimestamp, userRating, userNotes FROM episodes ORDER BY pubDate DESC")
    abstract fun getAllEpisodesLite(): Flow<List<EpisodeListItem>>

    @Query("SELECT lastPlayedTimestamp FROM episodes WHERE lastPlayedTimestamp > 0 ORDER BY lastPlayedTimestamp DESC")
    abstract fun getAllLastPlayedTimestamps(): Flow<List<Long>>

    // --- Bulk Export/Import ---

    @Query("SELECT * FROM podcasts")
    abstract suspend fun getAllPodcastsSync(): List<Podcast>

    @Query("SELECT * FROM episodes")
    abstract suspend fun getAllEpisodesSync(): List<Episode>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPodcasts(podcasts: List<Podcast>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAllEpisodes(episodes: List<Episode>)

    @Transaction
    open suspend fun importMinimalBackup(
        podcasts: List<MinimalPodcast>,
        episodes: List<MinimalEpisode>
    ) {
        deleteAllEpisodes()
        deleteAllPodcasts()
        val urlToIdMap = mutableMapOf<String, Long>()
        podcasts.forEach { mp ->
            val p = Podcast(
                title = mp.title,
                feedUrl = mp.feedUrl,
                isFavorite = mp.isFavorite,
                imageUrl = null,
                description = null,
                primaryGenre = null,
                lastUpdated = System.currentTimeMillis()
            )
            val id = insertPodcast(p)
            urlToIdMap[mp.feedUrl] = id
        }
        val episodeStubs = episodes.mapNotNull { me ->
            val pid = urlToIdMap[me.feedUrl]
            if (pid != null) {
                Episode(
                    podcastId = pid,
                    guid = me.guid,
                    title = "Restoring...",
                    listened = me.listened,
                    playbackPosition = me.playbackPosition,
                    lastPlayedTimestamp = me.lastPlayedTimestamp,
                    pubDate = 0,
                    audioUrl = null,
                    imageUrl = null,
                    description = null
                )
            } else null
        }
        insertAllEpisodes(episodeStubs)
    }

    @Transaction
    open suspend fun importLegacyBackup(
        podcasts: List<Podcast>,
        episodes: List<Episode>
    ) {
        deleteAllEpisodes()
        deleteAllPodcasts()
        insertPodcasts(podcasts)
        insertAllEpisodes(episodes)
    }

    @Query("SELECT COUNT(*) FROM episodes WHERE title = 'Restoring...'")
    abstract suspend fun getRestoringCount(): Int

    @Query("SELECT * FROM episodes WHERE podcastId = :podcastId")
    abstract suspend fun getEpisodesByPodcastIdSync(podcastId: Long): List<Episode>

    @Transaction
    open suspend fun upsertEpisodesMetadata(newEpisodes: List<Episode>) {
        if (newEpisodes.isEmpty()) return
        val podcastId = newEpisodes.first().podcastId
        val existingEpisodes = getEpisodesByPodcastIdSync(podcastId).associateBy { it.guid }
        
        val toUpdate = mutableListOf<Episode>()
        val toInsert = mutableListOf<Episode>()

        for (newEp in newEpisodes) {
            val existing = existingEpisodes[newEp.guid]
            if (existing != null) {
                val updated = existing.copy(
                    title = newEp.title,
                    pubDate = newEp.pubDate,
                    audioUrl = newEp.audioUrl,
                    imageUrl = newEp.imageUrl,
                    episodeNumber = newEp.episodeNumber,
                    durationMillis = newEp.durationMillis,
                    description = newEp.description
                )
                if (updated != existing) {
                    toUpdate.add(updated)
                }
            } else {
                toInsert.add(newEp)
            }
        }
        
        if (toUpdate.isNotEmpty()) updateEpisodes(toUpdate)
        if (toInsert.isNotEmpty()) insertEpisodes(toInsert)
    }

    @Update
    abstract suspend fun updateEpisodes(episodes: List<Episode>)

    @Query("""
        SELECT 
            p.*, 
            COUNT(e.id) as totalEpisodes,
            SUM(CASE WHEN e.listened = 1 THEN 1 ELSE 0 END) as listenedEpisodes,
            SUM(CASE WHEN e.listened = 1 THEN e.durationMillis ELSE 0 END) as timeListened
        FROM podcasts p
        LEFT JOIN episodes e ON p.id = e.podcastId
        GROUP BY p.id
        ORDER BY timeListened DESC
        LIMIT 5
    """)
    abstract fun getTopPodcastsByDuration(): Flow<List<PodcastWithStats>>

    @Query("SELECT lastPlayedTimestamp, durationMillis FROM episodes WHERE listened = 1 AND lastPlayedTimestamp > :since")
    abstract fun getListenedEpisodesSince(since: Long): Flow<List<EpisodeActivityData>>

    @Query("""
        SELECT e.* FROM episodes e
        INNER JOIN (
            SELECT podcastId, MIN(pubDate) as minPubDate
            FROM episodes
            WHERE listened = 0
            GROUP BY podcastId
        ) latest ON e.podcastId = latest.podcastId AND e.pubDate = latest.minPubDate
        WHERE e.listened = 0
        ORDER BY e.pubDate ASC
    """)
    abstract suspend fun getUpNextEpisodes(): List<Episode>

    // Playlist operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPlaylist(playlist: Playlist): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPlaylistCollection(collection: PlaylistCollection): Long

    @Query("SELECT * FROM playlist_collections ORDER BY position ASC")
    abstract fun getAllPlaylistCollections(): Flow<List<PlaylistCollection>>

    @Query("""
        SELECT e.* FROM playlists p 
        INNER JOIN episodes e ON p.episodeId = e.id 
        WHERE p.name = :collectionName 
        ORDER BY p.position ASC
    """)
    abstract fun getEpisodesInPlaylist(collectionName: String): Flow<List<Episode>>

    @Query("DELETE FROM playlists WHERE episodeId = :episodeId")
    abstract suspend fun removeFromPlaylist(episodeId: Long)

    @Query("DELETE FROM playlist_collections WHERE id = :collectionId")
    abstract suspend fun deletePlaylistCollection(collectionId: Long)

    @Query("UPDATE playlists SET position = :position WHERE id = :id")
    abstract suspend fun updatePlaylistPosition(id: Long, position: Int)

    @Query("UPDATE playlist_collections SET position = :position WHERE id = :id")
    abstract suspend fun updatePlaylistCollectionPosition(id: Long, position: Int)

    @Query("""
        SELECT id, podcastId, title, pubDate, imageUrl, episodeNumber, durationMillis, listened, listenedAt, playbackPosition, lastPlayedTimestamp, userRating, userNotes 
        FROM episodes 
        WHERE pubDate >= :startTime AND pubDate < :endTime
        ORDER BY pubDate DESC
    """)
    abstract fun getEpisodesForMonth(startTime: Long, endTime: Long): Flow<List<EpisodeListItem>>

    @Query("""
        SELECT id, podcastId, title, pubDate, imageUrl, episodeNumber, durationMillis, listened, listenedAt, playbackPosition, lastPlayedTimestamp, userRating, userNotes 
        FROM episodes 
        WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'
        ORDER BY pubDate DESC
    """)
    abstract fun searchEpisodes(query: String): Flow<List<EpisodeListItem>>

    @Query("""
        SELECT id, podcastId, title, pubDate, imageUrl, episodeNumber, durationMillis, listened, listenedAt, playbackPosition, lastPlayedTimestamp, userRating, userNotes 
        FROM episodes 
        WHERE podcastId = :podcastId AND (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        ORDER BY pubDate DESC
    """)
    abstract fun searchEpisodesInPodcast(podcastId: Long, query: String): Flow<List<EpisodeListItem>>

    @Query("SELECT * FROM podcasts WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    abstract fun searchPodcasts(query: String): Flow<List<Podcast>>

    // Storage Maintenance Queries
    @Query("SELECT COUNT(*) FROM episodes WHERE listened = 0 AND pubDate < :cutoff")
    abstract suspend fun countOldUnlistenedEpisodes(cutoff: Long): Int

    @Query("DELETE FROM episodes WHERE listened = 0 AND pubDate < :cutoff")
    abstract suspend fun deleteOldUnlistenedEpisodes(cutoff: Long): Int

    @Query("SELECT COUNT(*) FROM episodes WHERE description IS NULL OR LENGTH(description) < 50")
    abstract suspend fun countShortDescriptionEpisodes(): Int

    @Query("UPDATE episodes SET description = SUBSTR(description, 1, 200) WHERE LENGTH(description) > 200")
    abstract suspend fun truncateLongDescriptions(): Int

    @Query("DELETE FROM podcasts WHERE (lastUpdated IS NOT NULL AND lastUpdated > 0 AND lastUpdated < :cutoff)")
    abstract suspend fun deleteInactivePodcasts(cutoff: Long): Int

    @Query("""
        UPDATE episodes 
        SET listened = :listened, 
            listenedAt = CASE WHEN :listened = 1 THEN :timestamp ELSE NULL END, 
            lastPlayedTimestamp = CASE WHEN :listened = 1 THEN :timestamp ELSE lastPlayedTimestamp END, 
            playbackPosition = CASE WHEN :listened = 1 THEN 0 ELSE playbackPosition END 
        WHERE id IN (:episodeIds)
    """)
    abstract suspend fun markEpisodesListenedBulk(episodeIds: List<Long>, listened: Boolean, timestamp: Long)

    @Query("""
        UPDATE episodes 
        SET listened = 0, 
            listenedAt = NULL, 
            playbackPosition = 0, 
            userRating = NULL, 
            userNotes = NULL 
        WHERE id IN (:episodeIds)
    """)
    abstract suspend fun clearEpisodesTrackingBulk(episodeIds: List<Long>)

    @RawQuery
    abstract suspend fun vacuum(query: RoomRawQuery): Int
}

data class EpisodeActivityData(
    val lastPlayedTimestamp: Long,
    val durationMillis: Long?
)
