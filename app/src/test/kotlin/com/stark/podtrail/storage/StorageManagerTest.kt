package com.stark.podtrail.storage

import androidx.room3.Room
import com.stark.podtrail.data.Episode
import com.stark.podtrail.data.Podcast
import com.stark.podtrail.data.PodcastDao
import com.stark.podtrail.data.PodcastDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class StorageManagerTest {

    private lateinit var database: PodcastDatabase
    private lateinit var dao: PodcastDao
    private lateinit var storageManager: StorageManager

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        database = Room.inMemoryDatabaseBuilder(context, PodcastDatabase::class.java).build()
        dao = database.podcastDao()
        storageManager = StorageManager(dao, database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testGetStorageStats_calculatesAccurately() {
        runBlocking {
            val podId = dao.insertPodcast(Podcast(title = "Tech Podcast", feedUrl = "https://tech.com", lastUpdated = 1700000000000L))
            
            val sevenMonthsAgo = System.currentTimeMillis() - 210L * 24 * 3600 * 1000
            dao.insertEpisode(Episode(podcastId = podId, guid = "e1", title = "E1", pubDate = sevenMonthsAgo, listened = false, description = null))
            dao.insertEpisode(Episode(podcastId = podId, guid = "e2", title = "E2", pubDate = System.currentTimeMillis(), listened = true, description = "A".repeat(100)))

            val stats = storageManager.getStorageStats()
            assertEquals(2, stats.totalEpisodes)
            assertTrue(stats.totalSizeMB > 0)
            assertEquals(1, stats.episodesWithoutDescription)
            assertEquals(1, stats.oldUnlistenedEpisodes)
            assertEquals(1700000000000L, stats.podcastsLastUpdated["Tech Podcast"])
        }
    }

    @Test
    fun testPerformCleanup_oldUnlistenedEpisodes() {
        runBlocking {
            val podId = dao.insertPodcast(Podcast(title = "Show", feedUrl = "https://show.com"))
            val eightMonthsAgo = System.currentTimeMillis() - 240L * 24 * 3600 * 1000
            dao.insertEpisode(Episode(podcastId = podId, guid = "old-1", title = "Old Ep", pubDate = eightMonthsAgo, listened = false))
            dao.insertEpisode(Episode(podcastId = podId, guid = "new-1", title = "New Ep", pubDate = System.currentTimeMillis(), listened = false))

            val result = storageManager.performCleanup(CleanupOption.OLD_UNLISTENED_EPISODES)
            assertEquals(1, result.itemsAffected)
            assertEquals(CleanupOption.OLD_UNLISTENED_EPISODES, result.option)

            val remaining = dao.getEpisodesByPodcastIdSync(podId)
            assertEquals(1, remaining.size)
            assertEquals("New Ep", remaining[0].title)
        }
    }

    @Test
    fun testPerformCleanup_truncateDescriptions() {
        runBlocking {
            val podId = dao.insertPodcast(Podcast(title = "Show", feedUrl = "https://show.com"))
            dao.insertEpisode(Episode(podcastId = podId, guid = "long-1", title = "Long Ep", pubDate = 100L, description = "X".repeat(400)))

            val result = storageManager.performCleanup(CleanupOption.TRUNCATE_DESCRIPTIONS)
            assertEquals(1, result.itemsAffected)

            val ep = dao.getEpisodesByPodcastIdSync(podId)[0]
            assertEquals(200, ep.description?.length)
        }
    }

    @Test
    fun testPerformCleanup_compactDatabase() {
        runBlocking {
            val result = storageManager.performCleanup(CleanupOption.COMPACT_DATABASE)
            assertEquals(CleanupOption.COMPACT_DATABASE, result.option)
            assertEquals(1, result.itemsAffected)
        }
    }
}
