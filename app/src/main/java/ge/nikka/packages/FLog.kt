package ge.nikka.packages

import android.content.Context
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FLog(private val context: Context, private val fpath: String, autoSaveEnabled: Boolean) {
    private val cache: StringBuilder

    val addressStr: String
    private var autoSave = false

    init {
        autoSave = autoSaveEnabled
        addressStr = Integer.toHexString(System.identityHashCode(this))

        cache = StringBuilder()
        cache.append("/--- New FLog Instance / MemPtr : ").append(addressStr).append(" ---/")
            .append("\n/--- Path : ").append(fpath).append(" ---/")
            .append("\n/--- Creation Date : ").append(currentDateTime()).append(" ---/\n\n")
    }

    fun fileExists(name: String?): Boolean {
        val file = File(context.getExternalFilesDir(null), name)
        return file.exists()
    }

    fun getFileSize(filename: String?): Long {
        val file = File(context.getExternalFilesDir(null), filename)
        return if (file.exists()) file.length() else -1
    }

    private fun currentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd | HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    fun append(str: String?) {
        cache.append("\n").append(currentDateTime()).append(" : ").append(str).append("\n")
        if (autoSave) {
            save(NEW_LOG)
        }
    }

    fun append(value: Int) {
        append(value.toString())
    }

    fun appendArg(format: String?, vararg args: Any?) {
        cache.append("\n").append(currentDateTime()).append(" : ")
            .append(String.format(format!!, *args)).append("\n")
        if (autoSave) {
            save(NEW_LOG)
        }
    }

    fun appendArgMode(format: String?, mode: Int, vararg args: Any?) {
        cache.append("\n").append(currentDateTime()).append(" : ")
            .append(String.format(format!!, *args)).append("\n")
        if (autoSave) {
            save(mode)
        }
    }

    fun setAutoSave(state: Boolean) {
        autoSave = state
    }

    fun clear() {
        cache.setLength(0)
    }

    fun erase() {
        val file = File(fpath)
        if (fileExists(fpath) && getFileSize(fpath) > 0) {
            file.delete()
        }
    }

    fun save(mode: Int) {
        val file = File(fpath)
        try {
            if (mode == NEW_LOG) {
                FileWriter(file, false).use { writer -> writer.write(cache.toString()) }
            } else if (mode == APPEND_LOG) {
                FileWriter(file, true).use { writer ->
                    writer.append("\n").append(cache.toString())
                }
            } else {
                throw IllegalArgumentException("Unknown save mode!")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    val addressPtr: Int
        get() = System.identityHashCode(this)

    companion object {
        private const val NEW_LOG = 0
        private const val APPEND_LOG = 1
        private const val MAX_LEN = 524288
    }
}