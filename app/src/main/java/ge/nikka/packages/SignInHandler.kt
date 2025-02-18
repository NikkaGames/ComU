package ge.nikka.packages

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ge.nikka.packages.LoginActivity.Companion.checkMsgInBackground
import ge.nikka.packages.LoginActivity.Companion.checkRsInBackground
import ge.nikka.packages.LoginActivity.Companion.initiateThread
import ge.nikka.packages.SocketService
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException

class SignInHandler : GoogleSignInListener {
    override fun onError(str: String?) {
        if (!LoginActivity.executor.isShutdown) LoginActivity.executor.shutdown()
        if (!LoginActivity.executor2.isShutdown) LoginActivity.executor2.shutdown()
        Handlers.currentActivity?.runOnUiThread { if (LoginActivity.dlg != null) LoginActivity.dlg!!.dismiss() }
        val bld = MaterialAlertDialogBuilder(Handlers.currentActivity!!)
        bld.setTitle("Error").setMessage(str).setCancelable(false)
            .setNegativeButton("CLOSE") { dialog, which -> dialog.dismiss() }
        val tar = bld.create()
        tar.show()
        val positiveButton = tar.getButton(DialogInterface.BUTTON_POSITIVE)
        val negativeButton = tar.getButton(DialogInterface.BUTTON_NEGATIVE)
        val neutralButton = tar.getButton(DialogInterface.BUTTON_NEUTRAL)
        positiveButton.setTextColor(Color.parseColor("#FF006FFA"))
        negativeButton.setTextColor(Color.parseColor("#FF006FFA"))
        neutralButton.setTextColor(Color.parseColor("#FF006FFA"))
    }

    override fun onSuccess(str: String?) {
        //LoginActivity.logger.append(str);
        val edir =
            File(Handlers.currentActivity?.cacheDir?.absolutePath + "/image_manager_disk_cache")
        if (edir.exists()) {
            try {
                FileUtils.deleteDirectory(edir)
            } catch (ex: IOException) {
                LoginActivity.logger!!.append(ex.toString())
            }
        }
        if (!SocketService.isRunning) if (!SocketService.isconnected) Handlers.currentActivity?.startService(
            Intent(
                Handlers.currentActivity, SocketService::class.java
            )
        ) else {
            if (!SocketService.isconnected) {
                if (SocketService.isRunning) Handlers.currentActivity?.stopService(
                    Intent(
                        Handlers.currentActivity,
                        SocketService::class.java
                    )
                )
                Handlers.currentActivity?.startService(
                    Intent(
                        Handlers.currentActivity,
                        SocketService::class.java
                    )
                )
            }
        }
        //LoginActivity.initiateThread();
        //LoginActivity.checkRsInBackground();
        val handler = Handler()
        handler.postDelayed({

            //LoginActivity.showProgressDialog();
            while (!SocketService.isconnected) {
            }
            initiateThread(str!!)
            checkRsInBackground()
            checkMsgInBackground()
        }, 500)
        Handlers.startIconThread()
    }
}