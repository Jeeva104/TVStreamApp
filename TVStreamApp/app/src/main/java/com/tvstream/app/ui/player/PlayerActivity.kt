package com.tvstream.app.ui.player

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.tvstream.app.databinding.ActivityPlayerBinding
import com.tvstream.app.utils.Constants

/**
 * PlayerActivity – full-screen video playback using Media3 ExoPlayer.
 *
 * ───── Lifecycle Contract ─────────────────────────────────────────────────
 *  onStart  → initializePlayer()   (allocate ExoPlayer, begin buffering)
 *  onPause  → player?.pause()      (stop decoding when app goes background)
 *  onStop   → releasePlayer()      (free all resources, prevent memory leaks)
 *
 * This pattern is recommended by the official ExoPlayer / Media3 documentation
 * and ensures the player is released even on unexpected process death paths.
 *
 * ───── Supported formats ─────────────────────────────────────────────────
 *  - MP4  (progressive HTTP)
 *  - HLS  (.m3u8) via media3-exoplayer-hls dependency
 *
 * ───── No Deprecated APIs ────────────────────────────────────────────────
 *  Uses only androidx.media3.* — the successor to com.google.android.exoplayer2.*
 */
class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding

    // ExoPlayer instance – nullable; only valid between onStart and onStop
    private var player: ExoPlayer? = null

    // Playback position & state saved across lifecycle events
    private var playbackPosition: Long = 0L
    private var playWhenReady: Boolean = true

    // Video metadata received via Intent extras
    private var videoUrl: String = ""
    private var videoTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Extract intent extras (passed from HomeBrowseFragment / HomePhoneFragment)
        videoUrl = intent.getStringExtra(Constants.EXTRA_VIDEO_URL) ?: ""
        videoTitle = intent.getStringExtra(Constants.EXTRA_VIDEO_TITLE) ?: ""

        if (videoUrl.isEmpty()) {
            Toast.makeText(this, "Invalid video URL", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Set window flags for truly immersive fullscreen experience
        hideSystemUI()
    }

    override fun onStart() {
        super.onStart()
        // Initialize player here (not onCreate) to support multi-window / picture-in-picture
        initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    override fun onPause() {
        super.onPause()
        // Pause playback when the activity loses focus (e.g. notification shade)
        player?.pause()
        // Save position so we can resume from exact spot
        playbackPosition = player?.currentPosition ?: 0L
        playWhenReady = player?.playWhenReady ?: true
    }

    override fun onStop() {
        super.onStop()
        // Release ALL resources – critical to prevent memory leaks and audio focus issues
        releasePlayer()
    }

    /**
     * Creates and configures the [ExoPlayer] instance.
     *
     * [MediaItem.fromUri] auto-detects MP4 (progressive) vs HLS (.m3u8) based on URL,
     * when combined with the media3-exoplayer-hls dependency.
     */
    private fun initializePlayer() {
        player = ExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                // Bind ExoPlayer to the StyledPlayerView in the layout
                binding.playerView.player = exoPlayer

                // Build MediaItem – supports both MP4 and HLS automatically
                val mediaItem = MediaItem.fromUri(videoUrl)
                exoPlayer.setMediaItem(mediaItem)

                // Restore position if returning from background
                exoPlayer.seekTo(playbackPosition)
                exoPlayer.playWhenReady = playWhenReady

                // Attach listener for buffering indicator & error handling
                exoPlayer.addListener(playerListener)

                exoPlayer.prepare()
            }
    }

    /**
     * Releases the player and clears the PlayerView binding.
     * Called from onStop to ensure resources are freed even when the activity
     * is sent to background (swipe away, home button etc.)
     */
    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.removeListener(playerListener)
            exoPlayer.release()
        }
        player = null
    }

    /**
     * Player event listener – handles buffering indicator visibility and errors.
     */
    private val playerListener = object : Player.Listener {

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    // Show spinner when data is being fetched
                    binding.progressBuffering.visibility = View.VISIBLE
                }
                Player.STATE_READY, Player.STATE_ENDED, Player.STATE_IDLE -> {
                    // Hide spinner once buffering is done
                    binding.progressBuffering.visibility = View.GONE
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            binding.progressBuffering.visibility = View.GONE
            val errorMsg = when (error.errorCode) {
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
                    "Network connection failed. Check your internet."
                PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND ->
                    "Video not found."
                PlaybackException.ERROR_CODE_DECODER_INIT_FAILED ->
                    "Decoder initialization failed."
                else -> "Playback error: ${error.message}"
            }
            Toast.makeText(this@PlayerActivity, errorMsg, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Handle D-pad BACK key on TV – finish the activity cleanly.
     * On phone the system back gesture/button handles this automatically.
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * Enable true immersive fullscreen mode hiding status/nav bars.
     * Uses WindowInsetsController on API 30+ (modern API) with a legacy fallback.
     * Re-applied in onResume to handle cases where the system UI shows temporarily.
     */
    private fun hideSystemUI() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // API 30+ — WindowInsetsController (modern, non-deprecated)
            window.insetsController?.let { controller ->
                controller.hide(
                    android.view.WindowInsets.Type.statusBars()
                        or android.view.WindowInsets.Type.navigationBars()
                )
                controller.systemBarsBehavior =
                    android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // API 21–29 – legacy flags (still valid, only deprecated on API 30+)
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
        }
    }
}
