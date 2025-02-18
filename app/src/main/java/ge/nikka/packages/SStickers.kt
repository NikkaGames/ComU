package ge.nikka.packages

import android.view.View
import android.view.ViewGroup
import com.aghajari.emojiview.sticker.Sticker
import com.aghajari.emojiview.sticker.StickerCategory

class SStickers : StickerCategory<Int> {

    private val stickers: Array<NikkasSticker> = arrayOf(
        NikkasSticker(R.drawable.troll, "[sticker:troll1]"),
        NikkasSticker(R.drawable.troll2, "[sticker:troll2]"),
        NikkasSticker(R.drawable.smile, "[sticker:smile]"),
        NikkasSticker(R.drawable.spidy, "[sticker:spidy]"),
        NikkasSticker(R.drawable.stevy, "[sticker:stevy]"),
        NikkasSticker(R.drawable.skeleton, "[sticker:skeleton]"),
        NikkasSticker(R.drawable.notsigma, "[sticker:notsigma]"),
        NikkasSticker(R.drawable.jonkler, "[sticker:jonkler]"),
        NikkasSticker(R.drawable.baby, "[sticker:baby]"),
        NikkasSticker(R.drawable.fine, "[sticker:fine]"),
        NikkasSticker(R.drawable.stare, "[sticker:stare]"),
        NikkasSticker(R.drawable.thumbsup, "[sticker:thumbsup]"),
        NikkasSticker(R.drawable.clap, "[sticker:clap]"),
        NikkasSticker(R.drawable.what, "[sticker:what]")
    )

    override fun getStickers(): Array<Sticker<*>> {
        return stickers as Array<Sticker<*>>
    }

    override fun getCategoryData(): Int {
        return R.drawable.ic_msg_panel_stickers
    }

    override fun useCustomView(): Boolean {
        return false
    }

    override fun getView(viewGroup: ViewGroup?): View? {
        return null
    }

    override fun bindView(view: View?) {
    }

    override fun getEmptyView(viewGroup: ViewGroup?): View? {
        return null
    }
    
    private fun getStickerPosition(view: View): Int {
        return (view.parent as ViewGroup).indexOfChild(view)
    }
}
