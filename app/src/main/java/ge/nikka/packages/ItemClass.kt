package ge.nikka.packages

import android.graphics.Bitmap
import java.io.File

class ItemClass {
    constructor(uname: String, msg: String, uuid: String) {
        name = uname
        pname = msg
        path = uuid
    }

    constructor(uname: String, msg: String) {
        name = uname
        pname = msg
    }

    constructor()

    @JvmField
    var name = ""
    @JvmField
    var pname = ""
    @JvmField
    var path = ""
    @JvmField
    var time: Long = 31536000
    @JvmField
    var icon: Bitmap? = null
    var lsize: Long = -1
    var file: File? = null
    @JvmField
    var isme = false
    @JvmField
    var isread = true
}
