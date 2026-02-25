package com.tvstream.app.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.tvstream.app.R
import com.tvstream.app.data.model.VideoItem
import com.tvstream.app.databinding.FragmentHomePhoneBinding
import com.tvstream.app.ui.player.PlayerActivity
import com.tvstream.app.utils.Constants
import com.tvstream.app.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

/**
 * HomePhoneFragment – shown on non-TV devices (phones/tablets).
 *
 * Displays videos in a responsive grid using RecyclerView + GridLayoutManager.
 * Observes the same [HomeViewModel] as the TV fragment ensuring data consistency.
 *
 * On small phones: 2 columns | Tablets (sw600dp): 3 columns (handled by grid resource)
 */
class HomePhoneFragment : Fragment() {

    private var _binding: FragmentHomePhoneBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels()
    private lateinit var videoAdapter: PhoneVideoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomePhoneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        val spanCount = resources.getInteger(R.integer.grid_span_count)
        videoAdapter = PhoneVideoAdapter { video -> openPlayer(video) }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), spanCount)
            adapter = videoAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allVideos.collect { videos ->
                    videoAdapter.submitList(videos)
                }
            }
        }
    }

    private fun openPlayer(video: VideoItem) {
        val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
            putExtra(Constants.EXTRA_VIDEO_ID, video.id)
            putExtra(Constants.EXTRA_VIDEO_URL, video.videoUrl)
            putExtra(Constants.EXTRA_VIDEO_TITLE, video.title)
            putExtra(Constants.EXTRA_THUMBNAIL_URL, video.thumbnailUrl)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Null out binding to prevent memory leaks in fragment backstack
        _binding = null
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PhoneVideoAdapter – DiffUtil-backed RecyclerView adapter for the phone grid
// ─────────────────────────────────────────────────────────────────────────────

class PhoneVideoAdapter(
    private val onVideoClick: (VideoItem) -> Unit
) : RecyclerView.Adapter<PhoneVideoAdapter.VideoViewHolder>() {

    private var videos: List<VideoItem> = emptyList()

    fun submitList(newList: List<VideoItem>) {
        videos = newList
        notifyDataSetChanged()
    }

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.iv_thumbnail)
        val title: TextView = itemView.findViewById(R.id.tv_title)
        val category: TextView = itemView.findViewById(R.id.tv_category)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video_phone, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videos[position]
        holder.title.text = video.title
        holder.category.text = video.category

        Glide.with(holder.itemView.context)
            .load(video.thumbnailUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.bg_card_placeholder)
            .centerCrop()
            .into(holder.thumbnail)

        holder.itemView.setOnClickListener { onVideoClick(video) }
    }

    override fun getItemCount(): Int = videos.size
}
