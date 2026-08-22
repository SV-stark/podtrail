package com.stark.podtrail.ui

import androidx.room3.Room
import com.stark.podtrail.data.Episode
import com.stark.podtrail.data.Podcast
import com.stark.podtrail.data.PodcastDao
import com.stark.podtrail.data.PodcastDatabase
import com.stark.podtrail.data.PodcastRepository
import com.stark.podtrail.data.SettingsRepository
import com.stark.podtrail.data.SortOption
import com.stark.podtrail.network.FeedParser
import com.stark.podtrail.network.ItunesPodcastSearcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
class PodcastViewModelTest {

    private lateinit var database: PodcastDatabase
    private lateinit var dao: PodcastDao
    private lateinit var repo: PodcastRepository
    private lateinit var settingsRepo: SettingsRepository
    private lateinit var viewModel: PodcastViewModel

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        database = Room.inMemoryDatabaseBuilder(context, PodcastDatabase::class.java).build()
        dao = database.podcastDao()
        repo = PodcastRepository(dao, FeedParser())
        settingsRepo = SettingsRepository(context, database)
        val searcher = ItunesPodcastSearcher()
        viewModel = PodcastViewModel(repo, settingsRepo, database, searcher)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testToggleFavorite() {
        runBlocking {
            val podId = dao.insertPodcast(Podcast(title = "Fav Show", feedUrl = "https://fav.com", isFavorite = false))

            repo.toggleFavorite(podId, false)
            val updated = dao.getPodcastById(podId)
            assertTrue(updated?.isFavorite == true)

            repo.toggleFavorite(podId, true)
            val updatedAgain = dao.getPodcastById(podId)
            assertFalse(updatedAgain?.isFavorite == true)
        }
    }

    @Test
    fun testSearchQueryUpdate() {
        viewModel.search("Science")
        assertEquals("Science", viewModel.searchQuery.value)

        viewModel.search("")
        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun testSetSortOption() {
        viewModel.setSortOption(SortOption.DURATION_LONGEST)
        assertEquals(SortOption.DURATION_LONGEST, viewModel.sortOption.value)

        viewModel.setSortOption(SortOption.DATE_OLDEST)
        assertEquals(SortOption.DATE_OLDEST, viewModel.sortOption.value)
    }

    @Test
    fun testUpdateEpisodeRatingAndNotes() {
        runBlocking {
            val podId = dao.insertPodcast(Podcast(title = "Show", feedUrl = "https://show.com"))
            val epId = dao.insertEpisode(Episode(podcastId = podId, guid = "e1", title = "Ep 1", pubDate = 100L))

            repo.updateEpisodeRatingAndNotes(epId, 5, "Remarkable episode!")

            val ep = dao.getEpisodeById(epId)
            assertEquals(5, ep?.userRating)
            assertEquals("Remarkable episode!", ep?.userNotes)
        }
    }

    @Test
    fun testClearError() {
        viewModel.clearError()
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun testPlaylistCollectionFlow() {
        runBlocking {
            repo.createPlaylistCollection("Roadtrip", "Long episodes")
            val collections = repo.getAllPlaylistCollections().first()
            assertEquals(1, collections.size)
            assertEquals("Roadtrip", collections[0].name)

            repo.deletePlaylistCollection(collections[0].id)
            val after = repo.getAllPlaylistCollections().first()
            assertTrue(after.isEmpty())
        }
    }
}
