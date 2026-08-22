package com.stark.podtrail.data

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val podcasts: List<Podcast>,
    val episodes: List<Episode>
)

@Serializable
data class MinimalBackupData(
    val version: Int = 2,
    val timestamp: Long = System.currentTimeMillis(),
    val podcasts: List<MinimalPodcast>? = null,
    val episodes: List<MinimalEpisode>? = null
)

@Serializable
data class MinimalPodcast(
    val feedUrl: String,
    val title: String,
    val isFavorite: Boolean
)

@Serializable
data class MinimalEpisode(
    val feedUrl: String,
    val guid: String,
    val listened: Boolean,
    val playbackPosition: Long,
    val lastPlayedTimestamp: Long
)
