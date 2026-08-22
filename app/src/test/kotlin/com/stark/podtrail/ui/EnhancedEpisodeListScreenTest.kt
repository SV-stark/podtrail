package com.stark.podtrail.ui

import android.content.Context
import androidx.room3.Room
import com.stark.podtrail.data.PodcastDatabase
import com.stark.podtrail.data.PodcastRepository
import com.stark.podtrail.data.SettingsRepository
import com.stark.podtrail.data.Podcast
import com.stark.podtrail.data.Episode
import com.stark.podtrail.network.FeedParser
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class EnhancedEpisodeListScreenTest {
    private lateinit var db: PodcastDatabase
    private lateinit var repo: PodcastRepository
    private lateinit var settingsRepo: SettingsRepository
    private lateinit var vm: PodcastViewModel

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, PodcastDatabase::class.java).build()
        repo = PodcastRepository(db.podcastDao(), FeedParser())
        settingsRepo = SettingsRepository(context, db)
        vm = PodcastViewModel(repo, settingsRepo, db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testEpisodesForPodcastFlow() = runBlocking {
        // Insert a dummy podcast
        val p = Podcast(id = 1, title = "Tech Talk", feedUrl = "https://example.com/feed")
        db.podcastDao().insertPodcast(p)

        // Insert a dummy episode
        val ep = Episode(
            podcastId = 1,
            title = "Episode 1",
            guid = "ep1",
            pubDate = System.currentTimeMillis()
        )
        db.podcastDao().insertEpisodes(listOf(ep))

        // Collect episodesFor
        val result = vm.episodesFor(1L).first()
        assertEquals(1, result.size)
        assertEquals("Episode 1", result[0].title)
    }
}
