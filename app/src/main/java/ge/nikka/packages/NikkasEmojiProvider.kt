package ge.nikka.packages

import android.graphics.drawable.Drawable
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.DrawableCompat
import com.aghajari.emojiview.AXEmojiManager
import com.aghajari.emojiview.sticker.Sticker
import com.aghajari.emojiview.sticker.StickerCategory
import com.aghajari.emojiview.sticker.StickerLoader
import com.aghajari.emojiview.sticker.StickerProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class NikkasEmojiProvider : StickerProvider {

    override fun getCategories(): Array<StickerCategory<*>> {
        return arrayOf(SStickers())
    }

    override fun getLoader(): StickerLoader {
        return object : StickerLoader {
            override fun onLoadSticker(view: View, sticker: Sticker<*>) {
                val data = sticker.getData()
                val imageView = view as? AppCompatImageView ?: return
                when (data) {
                    is Int -> {
                        Glide.with(view).load(data).apply(RequestOptions.fitCenterTransform()).into(imageView)
                    }
                    is String -> {
                        Glide.with(view).load(data).apply(RequestOptions.fitCenterTransform()).into(imageView)
                    }
                }
            }

            override fun onLoadStickerCategory(view: View, stickerCategory: StickerCategory<*>, selected: Boolean) {
                try {
                    if (stickerCategory is SStickers) {
                        val dr0 = AppCompatResources.getDrawable(view.context, stickerCategory.categoryData as Int)
                        val dr = dr0?.constantState?.newDrawable()
                        if (dr != null) {
                            if (selected) {
                                DrawableCompat.setTint(DrawableCompat.wrap(dr), AXEmojiManager.getStickerViewTheme().selectedColor)
                            } else {
                                DrawableCompat.setTint(DrawableCompat.wrap(dr), AXEmojiManager.getStickerViewTheme().defaultColor)
                            }
                            (view as AppCompatImageView).setImageDrawable(dr)
                        }
                    } else {
                        Glide.with(view)
                            .load(stickerCategory.categoryData.toString().toInt())
                            .apply(RequestOptions.fitCenterTransform())
                            .into(view as AppCompatImageView)
                    }
                } catch (_: Exception) {
                }
            }
        }
    }

    override fun isRecentEnabled(): Boolean {
        return true
    }
}