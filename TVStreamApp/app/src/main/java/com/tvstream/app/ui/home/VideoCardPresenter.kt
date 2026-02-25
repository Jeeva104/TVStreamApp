package com.tvstream.app.ui.home

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.tvstream.app.R
import com.tvstream.app.data.model.VideoItem

/**
 * VideoCardPresenter – Leanback [Presenter] that binds [VideoItem] to [ImageCardView].
 *
 * ImageCardView is the standard Leanback card component providing:
 *  - Focus-based scaling (handled internally by Leanback)
 *  - Shadow elevation on focus
 *  - Info panel for title / subtitle
 *
 * Glide handles image loading with:
 *  - AUTOMATIC memory + disk cache
 *  - Placeholder while loading
 *  - Error fallback
 */
class VideoCardPresenter : Presenter() {

    companion object {
        // Card dimensions optimised for 1080p TV screen (10-foot UI rule)
        private const val CARD_WIDTH_DP = 320
        private const val CARD_HEIGHT_DP = 180
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val context = parent.context

        val cardView = ImageCardView(context).apply {
            // Leanback handles focus state & scale animation internally
            isFocusable = true
            isFocusableInTouchMode = true
            // Use the Leanback-recommended info style for readable text at distance
            setMainImageDimensions(
                dpToPx(context, CARD_WIDTH_DP),
                dpToPx(context, CARD_HEIGHT_DP)
            )
        }

        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val video = item as VideoItem
        val cardView = viewHolder.view as ImageCardView
        val context = cardView.context

        // Set title and subtitle in the card info panel
        cardView.titleText = video.title
        cardView.contentText = video.category

        // Load thumbnail via Glide into a CustomTarget to avoid leaking ImageView references
        Glide.with(context)
            .load(video.thumbnailUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)    // cache original + resized
            .placeholder(R.drawable.bg_card_placeholder)
            .error(R.drawable.bg_card_placeholder)
            .into(object : CustomTarget<Drawable>(
                dpToPx(context, CARD_WIDTH_DP),
                dpToPx(context, CARD_HEIGHT_DP)
            ) {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    cardView.mainImage = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    cardView.mainImage = placeholder
                }
            })
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        // Cancel any pending Glide requests to prevent memory leaks
        Glide.with(cardView.context).clear(cardView.mainImageView)
        cardView.mainImage = null
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}
