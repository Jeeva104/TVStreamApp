package com.tvstream.app.data.model

/**
 * VideoItem represents a single streamable video in the app.
 *
 * This is a pure data class – no Android framework dependencies,
 * making it easy to serialize/deserialize from JSON in the future.
 *
 * @param id          Unique identifier (used as stable key in adapters)
 * @param title       Display title shown on cards and player
 * @param thumbnailUrl HTTPS URL to a thumbnail image (loaded via Glide)
 * @param videoUrl    Direct video URL – supports MP4 and HLS (.m3u8)
 * @param category    Row grouping key (e.g. "Movies", "Sports", "News")
 */
data class VideoItem(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val category: String
)
