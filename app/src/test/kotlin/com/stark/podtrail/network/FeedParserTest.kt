package com.stark.podtrail.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class FeedParserTest {
    private val parser = FeedParser()

    @Test
    fun `test parsing simple RSS feed`() = runBlocking {
        val xml = """
            <rss version="2.0">
                <channel>
                    <title>Test Podcast</title>
                    <description>A test podcast description</description>
                    <image>
                        <url>http://example.com/image.jpg</url>
                    </image>
                    <item>
                        <title>Episode 1</title>
                        <guid>ep1</guid>
                        <enclosure url="http://example.com/ep1.mp3" type="audio/mpeg" />
                        <pubDate>Mon, 01 Jan 2024 12:00:00 +0000</pubDate>
                        <description>First episode</description>
                    </item>
                </channel>
            </rss>
        """.trimIndent()

        val (podcast, episodes) = parser.parseFeed(xml)

        assertNotNull(podcast)
        assertEquals("Test Podcast", podcast?.title)
        assertEquals("http://example.com/image.jpg", podcast?.imageUrl)

        assertEquals(1, episodes.size)
        assertEquals("Episode 1", episodes[0].title)
        assertEquals("ep1", episodes[0].guid)
        assertEquals("http://example.com/ep1.mp3", episodes[0].audioUrl)
    }

    @Test
    fun `test duration parsing`() {
        // Since parseDurationToMillis is private, I'll test it via parseFeed or make it internal too.
        // For now, I'll just check if it's correctly used in parseFeed.
        val xml = """
            <rss version="2.0">
                <channel>
                    <item>
                        <title>Ep 1</title>
                        <enclosure url="url" type="audio/mpeg" />
                        <itunes:duration>01:00:00</itunes:duration>
                    </item>
                </channel>
            </rss>
        """.trimIndent()
        val (_, episodes) = parser.parseFeed(xml)
        assertEquals(3600000L, episodes[0].durationMillis)
    }

    @Test
    fun `test duration parsing formats`() = runBlocking {
        val xml = """
            <rss version="2.0" xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd">
                <channel>
                    <title>Duration Show</title>
                    <item>
                        <title>Ep MMSS</title>
                        <guid>ep-mmss</guid>
                        <enclosure url="url1" type="audio/mpeg" />
                        <itunes:duration>45:30</itunes:duration>
                    </item>
                    <item>
                        <title>Ep Seconds</title>
                        <guid>ep-sec</guid>
                        <enclosure url="url2" type="audio/mpeg" />
                        <itunes:duration>180</itunes:duration>
                    </item>
                </channel>
            </rss>
        """.trimIndent()
        val (_, episodes) = parser.parseFeed(xml)
        assertEquals(2, episodes.size)
        assertEquals((45 * 60 + 30) * 1000L, episodes[0].durationMillis)
        assertEquals(180 * 1000L, episodes[1].durationMillis)
    }

    @Test
    fun `test missing guid generates deterministic fallback guid`() = runBlocking {
        val xml = """
            <rss version="2.0">
                <channel>
                    <title>No GUID Show</title>
                    <item>
                        <title>Episode Without Guid</title>
                        <pubDate>Mon, 01 Jan 2024 12:00:00 +0000</pubDate>
                        <enclosure url="http://example.com/ep.mp3" type="audio/mpeg" />
                    </item>
                </channel>
            </rss>
        """.trimIndent()
        val (_, episodes) = parser.parseFeed(xml)
        assertEquals(1, episodes.size)
        assertEquals("http://example.com/ep.mp3", episodes[0].guid)
    }

    @Test
    fun `test missing pubDate defaults to zero`() = runBlocking {
        val xml = """
            <rss version="2.0">
                <channel>
                    <title>No Date Show</title>
                    <item>
                        <title>Undated Episode</title>
                        <guid>undated-1</guid>
                        <enclosure url="http://example.com/ep.mp3" type="audio/mpeg" />
                    </item>
                </channel>
            </rss>
        """.trimIndent()
        val (_, episodes) = parser.parseFeed(xml)
        assertEquals(1, episodes.size)
        assertEquals(0L, episodes[0].pubDateMillis)
    }

    @Test
    fun `test namespaced tags with itunes episode and image`() = runBlocking {
        val xml = """
            <rss version="2.0" xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd" xmlns:content="http://purl.org/rss/1.0/modules/content/">
                <channel>
                    <title>Namespaced Show</title>
                    <itunes:image href="https://example.com/itunes-cover.jpg" />
                    <item>
                        <title>Episode With Content</title>
                        <guid>ns-1</guid>
                        <enclosure url="http://example.com/audio.mp3" type="audio/mpeg" />
                        <itunes:episode>42</itunes:episode>
                        <itunes:image href="https://example.com/ep-cover.jpg" />
                        <content:encoded><![CDATA[<p>Rich HTML formatted show notes</p>]]></content:encoded>
                    </item>
                </channel>
            </rss>
        """.trimIndent()
        val (podcast, episodes) = parser.parseFeed(xml)
        assertEquals("https://example.com/itunes-cover.jpg", podcast?.imageUrl)
        assertEquals(1, episodes.size)
        assertEquals(42, episodes[0].episodeNumber)
        assertEquals("https://example.com/ep-cover.jpg", episodes[0].imageUrl)
        assertTrue(episodes[0].description?.contains("Rich HTML") == true)
    }
}
