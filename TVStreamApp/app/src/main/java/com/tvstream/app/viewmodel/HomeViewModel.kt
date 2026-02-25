package com.tvstream.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tvstream.app.data.model.VideoItem
import com.tvstream.app.data.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * HomeViewModel owns the UI state for the home screen.
 *
 * Exposes:
 *  - [videosByCategory] – StateFlow consumed by HomeBrowseFragment (TV) and
 *    HomePhoneFragment (phone) to render rows / grid.
 *
 * Uses coroutines so data loading can be made async (e.g. network) without
 * changing the public API.
 */
class HomeViewModel : ViewModel() {

    // Backing mutable state – private so only ViewModel mutates it
    private val _videosByCategory = MutableStateFlow<Map<String, List<VideoItem>>>(emptyMap())

    /** Immutable state exposed to UI – safe to observe from any lifecycle-aware observer */
    val videosByCategory: StateFlow<Map<String, List<VideoItem>>> = _videosByCategory.asStateFlow()

    // Flat list convenience flow for phone grid
    private val _allVideos = MutableStateFlow<List<VideoItem>>(emptyList())
    val allVideos: StateFlow<List<VideoItem>> = _allVideos.asStateFlow()

    init {
        loadVideos()
    }

    /**
     * Loads video data from the repository.
     * Runs inside viewModelScope so it's automatically cancelled when the
     * ViewModel is cleared – no manual lifecycle management needed.
     *
     * Currently synchronous (hardcoded list), but the coroutine wrapper makes
     * it trivial to swap in a suspend network call in the future.
     */
    private fun loadVideos() {
        viewModelScope.launch {
            _videosByCategory.value = VideoRepository.getVideosByCategory()
            _allVideos.value = VideoRepository.getVideos()
        }
    }

    /** Called externally to force a data refresh (e.g. pull-to-refresh on phone) */
    fun refresh() = loadVideos()
}
