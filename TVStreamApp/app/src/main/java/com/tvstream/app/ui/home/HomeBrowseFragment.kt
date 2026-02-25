package com.tvstream.app.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.OnItemViewSelectedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.tvstream.app.R
import com.tvstream.app.data.model.VideoItem
import com.tvstream.app.ui.player.PlayerActivity
import com.tvstream.app.utils.Constants
import com.tvstream.app.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

/**
 * HomeBrowseFragment – the main TV home screen.
 *
 * Extends [BrowseSupportFragment] which provides Netflix-style category rows with:
 *  - Left-side navigation drawer (category headers)
 *  - Horizontal scrollable rows per category
 *  - Built-in D-pad focus management
 *  - Smooth focus-based scaling animations
 *
 * Architecture note: observes [HomeViewModel] StateFlow via repeatOnLifecycle
 * so the coroutine is automatically paused/resumed matching the fragment lifecycle,
 * preventing leaks and unnecessary work in the background.
 */
class HomeBrowseFragment : BrowseSupportFragment() {

    // Shared ViewModel with Activity scope – survives fragment recreation
    private val viewModel: HomeViewModel by activityViewModels()

    // Root adapter for all category rows
    private lateinit var rowsAdapter: ArrayObjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUIElements()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the rows adapter with Leanback ListRowPresenter
        rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        adapter = rowsAdapter

        setupEventListeners()
        observeViewModel()
    }

    /**
     * Configure the Leanback browse header / toolbar area.
     */
    private fun setupUIElements() {
        title = getString(R.string.app_name)
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        // Brand colour for the header / sidebar
        brandColor = ContextCompat.getColor(
            requireContext(), R.color.brand_color
        )
    }

    /**
     * Set click and selection listeners.
     *
     * [onItemViewClickedListener]: navigates to [PlayerActivity] passing video info.
     * [onItemViewSelectedListener]: could trigger auto-preview (bonus feature hook).
     */
    private fun setupEventListeners() {
        onItemViewClickedListener = OnItemViewClickedListener {
                _: Presenter.ViewHolder,
                item: Any,
                _: RowPresenter.ViewHolder,
                _: Row ->
            if (item is VideoItem) {
                openPlayer(item)
            }
        }

        onItemViewSelectedListener = OnItemViewSelectedListener {
                _: Presenter.ViewHolder?,
                item: Any?,
                _: RowPresenter.ViewHolder,
                _: Row ->
            // Hook point for auto-preview playback (bonus feature)
            // Currently a no-op; see PlayerActivity for preview ExoPlayer setup
        }
    }

    /**
     * Observe [HomeViewModel.videosByCategory] StateFlow.
     *
     * repeatOnLifecycle(STARTED) ensures:
     *  - Collection starts when fragment is visible (STARTED)
     *  - Collection pauses when fragment goes to background (< STARTED)
     *  - Avoids UI updates while the fragment is not visible → no crashes
     */
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.videosByCategory.collect { categoryMap ->
                    if (categoryMap.isNotEmpty()) {
                        buildRows(categoryMap)
                    }
                }
            }
        }
    }

    /**
     * Builds one [ListRow] per category and populates the root [rowsAdapter].
     *
     * Each row has:
     *  - [HeaderItem] with the category name
     *  - [ArrayObjectAdapter] backed by [VideoCardPresenter]
     */
    private fun buildRows(categoryMap: Map<String, List<VideoItem>>) {
        rowsAdapter.clear()

        val cardPresenter = VideoCardPresenter()

        categoryMap.entries.forEachIndexed { index, (category, videos) ->
            val header = HeaderItem(index.toLong(), category)
            val listRowAdapter = ArrayObjectAdapter(cardPresenter)
            videos.forEach { listRowAdapter.add(it) }
            rowsAdapter.add(ListRow(header, listRowAdapter))
        }
    }

    /**
     * Launch [PlayerActivity] with all video metadata needed for playback.
     * Using explicit extras (no Parcelable) for clarity across process boundaries.
     */
    private fun openPlayer(video: VideoItem) {
        val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
            putExtra(Constants.EXTRA_VIDEO_ID, video.id)
            putExtra(Constants.EXTRA_VIDEO_URL, video.videoUrl)
            putExtra(Constants.EXTRA_VIDEO_TITLE, video.title)
            putExtra(Constants.EXTRA_THUMBNAIL_URL, video.thumbnailUrl)
        }
        startActivity(intent)
    }
}
