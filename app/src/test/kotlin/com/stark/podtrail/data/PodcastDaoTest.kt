package com.stark.podtrail.data

import androidx.room3.Room
import androidx.room3.RoomRawQuery
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
class PodcastDaoTest {

    private lateinit var database: PodcastDatabase
    private lateinit var dao: PodcastDao

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        database = Room.inMemoryDatabaseBuilder(context, PodcastDatabase::class.java).build()
        dao = database.podcastDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInsertPodcastAndEpisodes() {
        runBlocking {
            val podcast = Podcast(
                title = "Test Show",
                feedUrl = "https://example.com/rss",
                description = "A great podcast",
                primaryGenre = "Technology"
            )
            val podcastId = dao.insertPodcast(podcast)
            assertTrue(podcastId > 0)

            val episode1 = Episode(
                podcastId = podcastId,
                guid = "ep-1",
                title = "Episode 1",
                pubDate = 1700000000000L,
                durationMillis = 1800000L
            )
            val episode2 = Episode(
                podcastId = podcastId,
                guid = "ep-2",
                title = "Episode 2",
                pubDate = 1700100000000L,
                durationMillis = 2400000L
            )
            dao.insertEpisodes(listOf(episode1, episode2))

            val episodes = dao.getEpisodesByPodcastIdSync(podcastId)
            assertEquals(2, episodes.size)
            assertEquals("Episode 1", episodes[0].title)
            assertEquals("Episode 2", episodes[1].title)
        }
    }

    @Test
    fun testCompositeGuidIndex_allowsIdenticalGuidInDifferentPodcasts() {
        runBlocking {
            val pod1Id = dao.insertPodcast(Podcast(title = "Show A", feedUrl = "https://a.com/rss"))
            val pod2Id = dao.insertPodcast(Podcast(title = "Show B", feedUrl = "https://b.com/rss"))

            val epA = Episode(podcastId = pod1Id, guid = "shared-guid-1", title = "Show A Ep 1", pubDate = 1000L)
            val epB = Episode(podcastId = pod2Id, guid = "shared-guid-1", title = "Show B Ep 1", pubDate = 2000L)

            dao.insertEpisodes(listOf(epA, epB))

            val episodesA = dao.getEpisodesByPodcastIdSync(pod1Id)
            val episodesB = dao.getEpisodesByPodcastIdSync(pod2Id)

            assertEquals(1, episodesA.size)
            assertEquals("Show A Ep 1", episodesA[0].title)
            assertEquals(1, episodesB.size)
            assertEquals("Show B Ep 1", episodesB[0].title)

            val epADuplicate = Episode(podcastId = pod1Id, guid = "shared-guid-1", title = "Show A Duplicate", pubDate = 3000L)
            dao.insertEpisodes(listOf(epADuplicate))
            val episodesAAfter = dao.getEpisodesByPodcastIdSync(pod1Id)
            assertEquals(1, episodesAAfter.size)
            assertEquals("Show A Ep 1", episodesAAfter[0].title)
        }
    }

    @Test
    fun testUpdateEpisodeDescription_doesNotCorruptHistoryOrProgress() {
        runBlocking {
            val podId = dao.insertPodcast(Podcast(title = "Show", feedUrl = "https://show.com/rss"))
            val epId = dao.insertEpisode(
                Episode(
                    podcastId = podId,
                    guid = "ep-detail-test",
                    title = "Episode with short description",
                    description = "Short desc",
                    listened = true,
                    listenedAt = 1600000000000L,
                    playbackPosition = 450000L,
                    lastPlayedTimestamp = 1600000000000L,
                    pubDate = 1500000000000L
                )
            )

            val fullDescription = "<p>Full elaborate HTML episode description with show notes</p>"
            dao.updateEpisodeDescription(epId, fullDescription)

            val updatedEp = dao.getEpisodeById(epId)
            assertNotNull(updatedEp)
            assertEquals(fullDescription, updatedEp?.description)
            assertTrue(updatedEp?.listened == true)
            assertEquals(1600000000000L, updatedEp?.listenedAt)
            assertEquals(450000L, updatedEp?.playbackPosition)
            assertEquals(1600000000000L, updatedEp?.lastPlayedTimestamp)
        }
    }

    @Test
    fun testGetUpNextEpisodes_singleQueryPartitioning() {
        runBlocking {
            val pod1Id = dao.insertPodcast(Podcast(title = "Podcast 1", feedUrl = "https://p1.com"))
            val pod2Id = dao.insertPodcast(Podcast(title = "Podcast 2", feedUrl = "https://p2.com"))

            dao.insertEpisode(Episode(podcastId = pod1Id, guid = "p1-1", title = "P1 E1", pubDate = 100L, listened = true))
            dao.insertEpisode(Episode(podcastId = pod1Id, guid = "p1-2", title = "P1 E2", pubDate = 200L, listened = false))
            dao.insertEpisode(Episode(podcastId = pod1Id, guid = "p1-3", title = "P1 E3", pubDate = 300L, listened = false))

            dao.insertEpisode(Episode(podcastId = pod2Id, guid = "p2-1", title = "P2 E1", pubDate = 50L, listened = false))

            val upNext = dao.getUpNextEpisodes()
            assertEquals(2, upNext.size)

            val titles = upNext.map { it.title }.toSet()
            assertTrue(titles.contains("P1 E2"))
            assertTrue(titles.contains("P2 E1"))
        }
    }

    @Test
    fun testMarkPodcastEpisodesListened() {
        runBlocking {
            val podId = dao.insertPodcast(Podcast(title = "Podcast", feedUrl = "https://p.com"))
            dao.insertEpisode(Episode(podcastId = podId, guid = "1", title = "E1", pubDate = 100L, listened = false))
            dao.insertEpisode(Episode(podcastId = podId, guid = "2", title = "E2", pubDate = 200L, listened = false))

            val now = 1710000000000L
            dao.markPodcastEpisodesListened(podId, true, now)

            val episodes = dao.getEpisodesByPodcastIdSync(podId)
            assertTrue(episodes.all { it.listened })
            assertTrue(episodes.all { it.listenedAt == now })
        }
    }

    @Test
    fun testDeleteInactivePodcasts() {
        runBlocking {
            val now = System.currentTimeMillis()
            val oneYearAgo = now - 365L * 24 * 3600 * 1000
            val sixMonthsAgo = now - 180L * 24 * 3600 * 1000
            val twoYearsAgo = now - 730L * 24 * 3600 * 1000

            val activeId = dao.insertPodcast(Podcast(title = "Active", feedUrl = "https://active.com", lastUpdated = sixMonthsAgo))
            val inactiveId = dao.insertPodcast(Podcast(title = "Inactive", feedUrl = "https://inactive.com", lastUpdated = twoYearsAgo))
            val newId = dao.insertPodcast(Podcast(title = "New", feedUrl = "https://new.com", lastUpdated = 0L))

            val deleted = dao.deleteInactivePodcasts(oneYearAgo)
            assertEquals(1, deleted)

            assertNotNull(dao.getPodcastById(activeId))
            assertNull(dao.getPodcastById(inactiveId))
            assertNotNull(dao.getPodcastById(newId))
        }
    }

    @Test
    fun testTruncateLongDescriptions() {
        runBlocking {
            val podId = dao.insertPodcast(Podcast(title = "Show", feedUrl = "https://show.com"))
            val longText = "A".repeat(500)
            val epId = dao.insertEpisode(Episode(podcastId = podId, guid = "long-ep", title = "Long", description = longText, pubDate = 100L))

            val truncatedCount = dao.truncateLongDescriptions()
            assertEquals(1, truncatedCount)

            val ep = dao.getEpisodeById(epId)
            assertEquals(200, ep?.description?.length)
        }
    }

    @Test
    fun testAtomicImportMinimalBackup() {
        runBlocking {
            val minimalPodcasts = listOf(
                MinimalPodcast(title = "Imported Show", feedUrl = "https://imported.com/rss", isFavorite = true)
            )
            val minimalEpisodes = listOf(
                MinimalEpisode(feedUrl = "https://imported.com/rss", guid = "imp-1", listened = true, playbackPosition = 12000L, lastPlayedTimestamp = 1700000000000L)
            )

            dao.importMinimalBackup(minimalPodcasts, minimalEpisodes)

            val podcasts = dao.getAllPodcastsSync()
            assertEquals(1, podcasts.size)
            assertEquals("Imported Show", podcasts[0].title)

            val episodes = dao.getEpisodesByPodcastIdSync(podcasts[0].id)
            assertEquals(1, episodes.size)
            assertEquals("imp-1", episodes[0].guid)
            assertTrue(episodes[0].listened)
            assertEquals(12000L, episodes[0].playbackPosition)
        }
    }

    @Test
    fun testPlaylistOperations() {
        runBlocking {
            val podId = dao.insertPodcast(Podcast(title = "Show", feedUrl = "https://show.com"))
            val epId = dao.insertEpisode(Episode(podcastId = podId, guid = "ep-pl", title = "Playlist Ep", pubDate = 100L))

            val playlist = Playlist(name = "Favorites", episodeId = epId)
            val playlistId = dao.insertPlaylist(playlist)
            assertTrue(playlistId > 0)

            val itemsBefore = dao.getEpisodesInPlaylist("Favorites").first()
            assertEquals(1, itemsBefore.size)
            assertEquals("Playlist Ep", itemsBefore[0].title)

            dao.removeFromPlaylist(epId)
            val itemsAfter = dao.getEpisodesInPlaylist("Favorites").first()
            assertTrue(itemsAfter.isEmpty())
        }
    }

    @Test
    fun testDatabaseVacuum() {
        runBlocking {
            dao.vacuum(RoomRawQuery("VACUUM"))
        }
    }
}
