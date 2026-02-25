package com.tvstream.app.data.repository

import com.tvstream.app.data.model.VideoItem

/**
 * VideoRepository is the single source of truth for video data.
 *
 * Currently backed by a hardcoded list (suitable for offline demo / evaluation).
 * The structure supports easy extension to:
 *  - Local JSON asset (via Gson / Moshi)
 *  - Remote REST API (via Retrofit / Ktor)
 *
 * Using an `object` (singleton) avoids repeated allocations; in a DI-based
 * project (Hilt/Koin) this would be a class injected with @Singleton scope.
 */
object VideoRepository {

    /**
     * Sample video catalogue with 3 categories and 6+ items each.
     * Thumbnails use picsum.photos (stable, no-auth image CDN).
     * Videos use publicly available MP4 / HLS streams for demo purposes.
     */
    private val sampleVideos: List<VideoItem> = listOf(

        // ── Movies ──────────────────────────────────────────────────────────
        VideoItem(
            id = "m1",
            title = "Big Buck Bunny",
            thumbnailUrl = "https://picsum.photos/seed/bunny/320/180",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            category = "Movies"
        ),
        VideoItem(
            id = "m2",
            title = "Elephant Dream",
            thumbnailUrl = "https://picsum.photos/seed/elephant/320/180",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            category = "Movies"
        ),
        VideoItem(
            id = "m3",
            title = "For Bigger Blazes",
            thumbnailUrl = "https://picsum.photos/seed/blazes/320/180",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            category = "Movies"
        ),
        VideoItem(
            id = "m4",
            title = "Subaru Outback",
            thumbnailUrl = "https://picsum.photos/seed/subaru/320/180",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4",
            category = "Movies"
        ),

        // ── Sports ──────────────────────────────────────────────────────────
        VideoItem(
            id = "s1",
            title = "Weeknd Blinding Lights (HLS)",
            thumbnailUrl = "https://picsum.photos/seed/sports1/320/180",
            videoUrl = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8",
            category = "Sports"
        ),
        VideoItem(
            id = "s2",
            title = "For Bigger Escapes",
            thumbnailUrl = "https://picsum.photos/seed/escapes/320/180",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            category = "Sports"
        ),
        VideoItem(
            id = "s3",
            title = "For Bigger Fun",
            thumbnailUrl = "https://picsum.photos/seed/fun/320/180",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            category = "Sports"
        ),
        VideoItem(
            id = "s4",
            title = "Apple HLS 4K Stream",
            thumbnailUrl = "https://picsum.photos/seed/hls4k/320/180",
            videoUrl = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8",
            category = "Sports"
        ),

        // ── News ─────────────────────────────────────────────────────────────
        VideoItem(
            id = "n1",
            title = "Tears of Steel",
            thumbnailUrl = "https://picsum.photos/seed/steel/320/180",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            category = "News"
        ),
        VideoItem(
            id = "n2",
            title = "Volkswagen GTI",
            thumbnailUrl = "https://picsum.photos/seed/vw/320/180",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/VolkswagenGTIReview.mp4",
            category = "News"
        ),
        VideoItem(
            id = "n3",
            title = "We Are Going On Bullrun",
            thumbnailUrl = "https://picsum.photos/seed/bullrun/320/180",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
            category = "News"
        ),
        VideoItem(
            id = "n4",
            title = "What Care Can You Get",
            thumbnailUrl = "https://picsum.photos/seed/carget/320/180",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4",
            category = "News"
        )
    )

    /**
     * Returns the complete flat list of videos.
     * Useful for phone grid or search results.
     */
    fun getVideos(): List<VideoItem> = sampleVideos

    /**
     * Returns videos grouped by category, preserving insertion order.
     * Consumed by HomeViewModel to build Leanback category rows.
     */
    fun getVideosByCategory(): Map<String, List<VideoItem>> =
        sampleVideos.groupBy { it.category }

    /**
     * Lookup a single video by ID – used when navigating to player from deep-link.
     */
    fun getVideoById(id: String): VideoItem? = sampleVideos.find { it.id == id }
}
