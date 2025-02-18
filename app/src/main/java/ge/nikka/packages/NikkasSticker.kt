package ge.nikka.packages

import com.aghajari.emojiview.sticker.Sticker
import java.io.Serializable

@Suppress("UNCHECKED_CAST", "UNCHECKED_CAST")
class NikkasSticker(drawableId: Int, var title: String) : Sticker<Int>(drawableId), Serializable {

    companion object {
        private const val serialVersionUID = 3L
    }

    private var imageResourceId: Int = drawableId

    fun getImageResourceId(): Int {
        return imageResourceId
    }
}
