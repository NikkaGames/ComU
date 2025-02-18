package ge.nikka.packages

import android.app.Application
import android.content.Context
import android.graphics.Color
import com.aghajari.emojiview.AXEmojiManager
import com.aghajari.emojiview.googleprovider.AXGoogleEmojiProvider
import dalvik.system.DexClassLoader
import org.acra.ACRA.init
import org.acra.BuildConfig
import org.acra.config.CoreConfigurationBuilder
import org.acra.config.MailSenderConfigurationBuilder
import org.acra.data.StringFormat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.file.Files
import dalvik.system.InMemoryDexClassLoader

class Comu : Application() {
    override fun attachBaseContext(arg0: Context) {
        super.attachBaseContext(arg0)
    }

    private fun copyAssetToCache(context: Context, assetFileName: String): File? {
        val cacheFile = File(context.codeCacheDir, "classes.dex")
        try {
            context.assets.open(assetFileName).use { inputStream ->
                FileOutputStream(cacheFile).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    LoginActivity.logger?.append("File copied to: " + cacheFile.absolutePath)
                    return cacheFile
                }
            }
            cacheFile.setReadable(true, false)
            cacheFile.setWritable(false)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    private fun loadDex(optimizedDir: String) {
        LoginActivity.logger?.append("About to load dex...")
        try {
            val optimizedDirectory = File(optimizedDir)
            if (!optimizedDirectory.exists()) {
                if (optimizedDirectory.mkdirs()) {
                    LoginActivity.logger?.append("Optimized directory created: " + optimizedDirectory.absolutePath)
                } else {
                    LoginActivity.logger?.append("Failed to create optimized directory.")
                    return
                }
            }

            var dexFile = copyAssetToCache(this, "utils")

            val dexBytes = Files.readAllBytes(dexFile?.toPath())
            val dexBuffer = ByteBuffer.wrap(dexBytes)

            val dexClassLoader = InMemoryDexClassLoader(
                arrayOf(dexBuffer),
                this::class.java.classLoader
            )
            
            Handlers.loadedClass = dexClassLoader.loadClass("ge.nikka.packages.Utils")
            LoginActivity.logger?.append("Class loaded: " + Handlers.loadedClass!!.name)
        } catch (e: Exception) {
            LoginActivity.logger?.append("Error: " + e.toString())
        }
    }

    override fun onCreate() {
        super.onCreate()
        init(this)
        AXEmojiManager.install(this, AXGoogleEmojiProvider(this))
        AXEmojiManager.setRecentSticker(ChatActivity.recentSticker)
        AXEmojiManager.getEmojiViewTheme().isFooterEnabled = true
        AXEmojiManager.disableRecentManagers()
        AXEmojiManager.getEmojiViewTheme().selectionColor = Color.parseColor("#FF006FFA")
        AXEmojiManager.getEmojiViewTheme().footerSelectedItemColor = Color.parseColor("#FF006FFA")
        AXEmojiManager.getEmojiViewTheme().footerBackgroundColor = Color.BLACK
        AXEmojiManager.getEmojiViewTheme().selectionColor = Color.TRANSPARENT
        AXEmojiManager.getEmojiViewTheme().selectedColor = Color.parseColor("#FF006FFA")
        AXEmojiManager.getEmojiViewTheme().categoryColor = Color.BLACK
        AXEmojiManager.getEmojiViewTheme().setAlwaysShowDivider(false)
        AXEmojiManager.getEmojiViewTheme().variantPopupBackgroundColor = Color.BLACK
        AXEmojiManager.getEmojiViewTheme().variantDividerColor = Color.GRAY
        AXEmojiManager.getEmojiViewTheme().dividerColor = Color.parseColor("#FF006FFA")
        AXEmojiManager.getEmojiViewTheme().backgroundColor = Color.BLACK
        AXEmojiManager.getStickerViewTheme().selectedColor = Color.parseColor("#FF006FFA")
        AXEmojiManager.getStickerViewTheme().categoryColor = Color.BLACK
        AXEmojiManager.getStickerViewTheme().setAlwaysShowDivider(false)
        AXEmojiManager.getStickerViewTheme().dividerColor = Color.parseColor("#FF006FFA")
        AXEmojiManager.getStickerViewTheme().backgroundColor = Color.BLACK
        val builder = CoreConfigurationBuilder(this)
        builder.withBuildConfigClass(BuildConfig::class.java)
        builder.withReportFormat(StringFormat.KEY_VALUE_LIST)
        builder.getPluginConfigurationBuilder(
            MailSenderConfigurationBuilder::class.java
        ).mailTo = "comucrash@gmail.com"
        builder.getPluginConfigurationBuilder(
            MailSenderConfigurationBuilder::class.java
        ).withEnabled(true)
        builder.getPluginConfigurationBuilder(
            MailSenderConfigurationBuilder::class.java
        ).enabled = true
        builder.getPluginConfigurationBuilder(
            MailSenderConfigurationBuilder::class.java
        ).reportFileName = "crash_report.txt"
        init(this, builder)
        LoginActivity.logger = FLog(this, this.cacheDir.absolutePath + "/logs.txt", true)
        loadDex(codeCacheDir.absolutePath + "/nikka")
    }
}