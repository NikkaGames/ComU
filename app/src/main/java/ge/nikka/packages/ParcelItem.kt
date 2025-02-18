package ge.nikka.packages

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator

// MyObject.java
class ParcelItem : Parcelable {
    // Getter methods
    var name: String?
        private set
    var age = 0
        private set
    private var icon: Bitmap? = null

    // Constructor
    constructor(iconn: Bitmap?, name: String?) {
        icon = iconn
        this.name = name
    }

    fun bitmap(): Bitmap? {
        return icon
    }

    // Parcelable implementation
    protected constructor(`in`: Parcel) {
        name = `in`.readString()
        age = `in`.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeInt(age)
        //dest.writeTypedObject(Bitmap, i)
    }

    companion object {
        @JvmField
        val CREATOR: Creator<ParcelItem?> = object : Creator<ParcelItem?> {
            override fun createFromParcel(`in`: Parcel): ParcelItem? {
                return ParcelItem(`in`)
            }

            override fun newArray(size: Int): Array<ParcelItem?> {
                return arrayOfNulls(size)
            }
        }
    }
}
