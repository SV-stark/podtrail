package com.stark.podtrail.network

import androidx.annotation.VisibleForTesting
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class ParsedPodcast(
    val title: String?,
    val imageUrl: String?,
    val description: String?,
    val genre: String? = null
)

data class ParsedEpisode(
    val title: String,
    val guid: String,
    val audioUrl: String,
    val pubDateMillis: Long,
    val imageUrl: String? = null,
    val durationMillis: Long? = null,
    val description: String? = null,
    val episodeNumber: Int? = null
)

@Singleton
class FeedParser @Inject constructor(
    private val client: OkHttpClient
) {
    constructor() : this(OkHttpClient())

    suspend fun fetchFeed(url: String): Pair<ParsedPodcast?, List<ParsedEpisode>> {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP error: ${response.code}")
            val body = response.body?.string() ?: throw Exception("Empty body")
            return parseFeed(body)
        }
    }

    @VisibleForTesting
    internal fun parseFeed(xml: String): Pair<ParsedPodcast?, List<ParsedEpisode>> {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        var eventType = parser.eventType
        var podcast: ParsedPodcast? = null
        val episodes = mutableListOf<ParsedEpisode>()

        var currentTag: String? = null
        var inItem = false
        var inImage = false

        // Podcast level
        var pTitle: String? = null
        var pImage: String? = null
        var pDesc: String? = null
        var pGenre: String? = null

        // Episode level
        var eTitle: String? = null
        var eGuid: String? = null
        var eAudio: String? = null
        var ePubDate: String? = null
        var eImage: String? = null
        var eDuration: String? = null
        var eDesc: String? = null
        var eNumber: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    val rawTag = parser.name ?: ""
                    currentTag = rawTag
                    val localTag = rawTag.substringAfterLast(':').lowercase()

                    if (localTag == "item") {
                        inItem = true
                    } else if (localTag == "image") {
                        val href = parser.getAttributeValue(null, "href") ?: ""
                        if (href.isNotEmpty()) {
                            if (inItem) eImage = href else pImage = href
                        } else if (!inItem) {
                            inImage = true
                        }
                    } else if (localTag == "enclosure" && inItem) {
                        eAudio = parser.getAttributeValue(null, "url")
                    } else if (localTag == "category" && !inItem) {
                        pGenre = parser.getAttributeValue(null, "text")
                    }
                }
                XmlPullParser.TEXT -> {
                    val text = parser.text.trim()
                    if (text.isNotEmpty() && currentTag != null) {
                        val localTag = currentTag.substringAfterLast(':').lowercase()
                        if (inItem) {
                            when (localTag) {
                                "title" -> eTitle = text
                                "guid" -> eGuid = text
                                "pubdate" -> ePubDate = text
                                "duration" -> eDuration = text
                                "description", "summary", "encoded" -> {
                                    if (eDesc == null || localTag == "encoded" || text.length > (eDesc?.length ?: 0)) {
                                        eDesc = text
                                    }
                                }
                                "episode" -> eNumber = text
                            }
                        } else {
                            when (localTag) {
                                "title" -> pTitle = text
                                "description", "summary" -> pDesc = text
                                "url" -> if (inImage) pImage = text
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    val localTag = (parser.name ?: "").substringAfterLast(':').lowercase()
                    if (localTag == "item") {
                        if (eTitle != null && eAudio != null) {
                            val computedGuid = eGuid?.takeIf { it.isNotBlank() }
                                ?: eAudio?.takeIf { it.isNotBlank() }
                                ?: "${eTitle}_${ePubDate ?: ""}"
                            
                            episodes.add(ParsedEpisode(
                                title = eTitle ?: "Untitled",
                                guid = computedGuid,
                                audioUrl = eAudio ?: "",
                                pubDateMillis = parseDateToMillis(ePubDate),
                                imageUrl = eImage,
                                durationMillis = parseDurationToMillis(eDuration),
                                description = eDesc,
                                episodeNumber = eNumber?.toIntOrNull()
                            ))
                        }
                        // Reset episode vars
                        eTitle = null; eGuid = null; eAudio = null; ePubDate = null
                        eImage = null; eDuration = null; eDesc = null; eNumber = null
                        inItem = false
                    } else if (localTag == "image") {
                        inImage = false
                    } else if (localTag == "channel") {
                        podcast = ParsedPodcast(pTitle, pImage, pDesc, pGenre)
                    }
                    currentTag = null
                }
            }
            eventType = parser.next()
        }

        return Pair(podcast, episodes)
    }

    private fun parseDateToMillis(dateStr: String?): Long {
        if (dateStr == null) return 0L
        val formats = arrayOf(
            "EEE, dd MMM yyyy HH:mm:ss Z",
            "EEE, dd MMM yyyy HH:mm:ss z",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ssZ"
        )
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                return sdf.parse(dateStr)?.time ?: 0L
            } catch (e: Exception) { /* continue */ }
        }
        return 0L
    }

    private fun parseDurationToMillis(duration: String?): Long? {
        if (duration == null) return null
        try {
            if (duration.contains(":")) {
                val parts = duration.split(":").map { it.trim().toLongOrNull() ?: 0L }
                return when (parts.size) {
                    1 -> parts[0] * 1000
                    2 -> (parts[0] * 60 + parts[1]) * 1000
                    3 -> (parts[0] * 3600 + parts[1] * 60 + parts[2]) * 1000
                    else -> null
                }
            } else {
                return duration.toLongOrNull()?.let { it * 1000 }
            }
        } catch (e: Exception) {
            return null
        }
    }
}
