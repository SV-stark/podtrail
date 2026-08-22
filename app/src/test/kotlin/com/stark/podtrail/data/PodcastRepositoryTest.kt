package com.stark.podtrail.data

import androidx.room3.Room
import com.stark.podtrail.network.FeedParser
import com.stark.podtrail.network.ParsedEpisode
import com.stark.podtrail.network.ParsedPodcast
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PodcastRepositoryTest {

    private lateinit var database: PodcastDatabase
    private lateinit var dao: PodcastDao
    private lateinit var repository: PodcastRepository

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        database = Room.inMemoryDatabaseBuilder(context, PodcastDatabase::class.java).build()
        dao = database.podcastDao()
        repository = PodcastRepository(dao, FeedParser())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testStreakCalculation_contiguousDays() {
        runBlocking {
            val podId = dao.insertPodcast(Podcast(title = "Streak Show", feedUrl = "https://streak.com"))
            val now = System.currentTimeMillis()
            val zone = java.util.TimeZone.getDefault()
            val offset = zone.getOffset(now)
            val todayEpochDay = (now + offset) / 86400000L

            for (i in 0..2) {
                val dayTimestamp = (todayEpochDay - i) * 86400000L - offset + 3600000L
                dao.insertEpisode(
                    Episode(
                        podcastId = podId,
                        guid = "streak-$i",
                        title = "Ep $i",
                        pubDate = 100L,
                        listened = true,
                        listenedAt = dayTimestamp,
                        lastPlayedTimestamp = dayTimestamp
                    )
                )
            }

            val streak = repository.getCurrentStreak().first()
            assertEquals(3, streak)
        }
    }

    @Test
    fun testStreakCalculation_brokenStreak() {
        runBlocking {
            val podId = dao.insertPodcast(Podcast(title = "Streak Show", feedUrl = "https://streak.com"))
            val now = System.currentTimeMillis()
            val zone = java.util.TimeZone.getDefault()
            val offset = zone.getOffset(now)
            val todayEpochDay = (now + offset) / 86400000L

            val todayTimestamp = todayEpochDay * 86400000L - offset + 3600000L
            val twoDaysAgoTimestamp = (todayEpochDay - 2) * 86400000L - offset + 3600000L

            dao.insertEpisode(Episode(podcastId = podId, guid = "s-0", title = "Today", pubDate = 100L, listened = true, listenedAt = todayTimestamp, lastPlayedTimestamp = todayTimestamp))
            dao.insertEpisode(Episode(podcastId = podId, guid = "s-2", title = "2 Days Ago", pubDate = 100L, listened = true, listenedAt = twoDaysAgoTimestamp, lastPlayedTimestamp = twoDaysAgoTimestamp))

            val streak = repository.getCurrentStreak().first()
            assertEquals(1, streak)
        }
    }

    @Test
    fun testStreakCalculation_noActivity() {
        runBlocking {
            val streak = repository.getCurrentStreak().first()
            assertEquals(0, streak)
        }
    }

    @Test
    fun testLast7DaysActivity() {
        runBlocking {
            val podId = dao.insertPodcast(Podcast(title = "Activity Show", feedUrl = "https://act.com"))
            val now = System.currentTimeMillis()
            dao.insertEpisode(
                Episode(
                    podcastId = podId,
                    guid = "act-1",
                    title = "Act 1",
                    pubDate = 100L,
                    listened = true,
                    durationMillis = 1800000L,
                    lastPlayedTimestamp = now
                )
            )

            val activity = repository.getLast7DaysActivity().first()
            assertEquals(7, activity.size)
            val totalRecordedDuration = activity.values.sum()
            assertEquals(1800000L, totalRecordedDuration)
        }
    }

    @Test
    fun testMarkEpisodeListened_resetsPositionAndSetsTimestamp() {
        runBlocking {
            val podId = dao.insertPodcast(Podcast(title = "Show", feedUrl = "https://show.com"))
            val epId = dao.insertEpisode(
                Episode(
                    podcastId = podId,
                    guid = "pos-test",
                    title = "Position Test",
                    pubDate = 100L,
                    playbackPosition = 30000L,
                    listened = false
                )
            )

            val ep = dao.getEpisodeById(epId)!!
            repository.markEpisodeListened(ep, true)

            val updated = dao.getEpisodeById(epId)!!
            assertTrue(updated.listened)
            assertEquals(0L, updated.playbackPosition)
            assertNotNull(updated.listenedAt)
        }
    }

    @Test
    fun testBatchOperations() {
        runBlocking {
            val podId = dao.insertPodcast(Podcast(title = "Batch Show", feedUrl = "https://batch.com"))
            val ep1Id = dao.insertEpisode(Episode(podcastId = podId, guid = "b1", title = "B1", pubDate = 100L, listened = false))
            val ep2Id = dao.insertEpisode(Episode(podcastId = podId, guid = "b2", title = "B2", pubDate = 200L, listened = false))

            repository.markEpisodesListenedBatch(listOf(ep1Id, ep2Id), true)
            val episodes = dao.getEpisodesByPodcastIdSync(podId)
            assertTrue(episodes.all { it.listened })

            repository.clearEpisodesTrackingBatch(listOf(ep1Id, ep2Id))
            val clearedEpisodes = dao.getEpisodesByPodcastIdSync(podId)
            assertTrue(clearedEpisodes.none { it.listened })
            assertTrue(clearedEpisodes.all { it.playbackPosition == 0L })
        }
    }

    @Test
    fun testPlaylistCollectionManagement() {
        runBlocking {
            repository.createPlaylistCollection("Workout Mix", "High energy podcasts")
            val collections = repository.getAllPlaylistCollections().first()
            assertEquals(1, collections.size)
            assertEquals("Workout Mix", collections[0].name)

            repository.deletePlaylistCollection(collections[0].id)
            val collectionsAfter = repository.getAllPlaylistCollections().first()
            assertTrue(collectionsAfter.isEmpty())
        }
    }
}
