package com.tvstream.app.utils

/**
 * App-wide constants used across multiple layers to avoid magic strings.
 */
object Constants {

    /** Intent extra key for passing the video item ID to PlayerActivity */
    const val EXTRA_VIDEO_ID = "extra_video_id"

    /** Intent extra key for passing the video URL directly to PlayerActivity */
    const val EXTRA_VIDEO_URL = "extra_video_url"

    /** Intent extra key for passing the video title to PlayerActivity */
    const val EXTRA_VIDEO_TITLE = "extra_video_title"

    /** Intent extra key for passing the thumbnail URL to PlayerActivity */
    const val EXTRA_THUMBNAIL_URL = "extra_thumbnail_url"

    /** Intent extra key for the full VideoItem parcelable (if needed in future) */
    const val EXTRA_VIDEO_ITEM = "extra_video_item"
}
