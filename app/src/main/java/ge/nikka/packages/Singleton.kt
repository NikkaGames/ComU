package ge.nikka.packages

import android.graphics.Bitmap

class Singleton private constructor() {

    @JvmField
    var icon: Bitmap? = null
    @JvmField
    var uid = "0"
    @JvmField
    var myUid = "0"
    @JvmField
    var myName = "0"
    @JvmField
    var listener: SwipeListener? = null
    @JvmField
    var name = "0"
    @JvmField
    var isnotif = false
    @JvmField
    var puid = "0"
    @JvmField
    var myimg: Bitmap? = null
    companion object {
        @JvmField
        var rpk = ""
        private var instance: Singleton? = null

        @JvmStatic
        fun getInstance(): Singleton {
            if (instance == null) {
                synchronized(Singleton::class.java) {
                    if (instance == null) {
                        instance = Singleton()
                    }
                }
            }
            return instance!!
        }
    }

    fun getNotif(): Boolean {
        return this.isnotif
    }

    fun setNotif(stat: Boolean) {
        this.isnotif = stat
    }

    fun setMyPic(img: Bitmap) {
        this.myimg = img
    }

    fun setPuid(vall: String) {
        this.puid = vall
    }

    fun getPuid(): String {
        return this.puid
    }

    fun getMyName(): String {
        return this.myName
    }

    fun setIcon(bit: Bitmap) {
        this.icon = bit
    }

    fun setUid(uuid: String) {
        this.uid = uuid
    }

    fun setMyName(myname: String) {
        this.myName = myname
    }

    fun setReplyTo(rpl: SwipeListener?) {
        this.listener = rpl
    }

    fun getReplyTo(): SwipeListener? {
        return this.listener
    }

    fun setMyUid(uuid: String) {
        this.myUid = uuid
    }

    fun setName(nname: String) {
        this.name = nname
    }

    fun cleanUp() {
        uid = "0"
        name = "0"
        icon = null
        //myimg = null
    }

    fun getIcon(): Bitmap? {
        return this.icon
    }

    fun getMyPic(): Bitmap? {
        return this.myimg
    }

    fun getUid(): String {
        return this.uid
    }

    fun getMyUid(): String {
        return this.myUid
    }

    fun getName(): String {
        return this.name
    }
}
