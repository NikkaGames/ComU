package ge.nikka.packages

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import ge.nikka.packages.ChatActivity
import ge.nikka.packages.Handlers.RefreshChats
import ge.nikka.packages.MainActivity
import ge.nikka.packages.Singleton.Companion.getInstance
import ge.nikka.packages.ui.main.PlaceholderFragment
import org.json.JSONObject
import java.io.File
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.math.abs

class ServerService : Service() {
    private val UPDATE_INTERVAL = 700
    private val timer: Timer? = Timer()
    var vibrationPattern = longArrayOf(0, 70)
    fun isDarkThemeEnabled(context: Context): Boolean {
        val nightModeFlags =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        thiz = this
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startid: Int): Int {
        isRunning = true
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val icon = android.R.drawable.ic_dialog_info
        val mBuilder = NotificationCompat.Builder(applicationContext, "comu_001")
        val bigText = NotificationCompat.BigTextStyle()
        bigText.bigText("Don't dismiss this notification!")
        bigText.setBigContentTitle("Messaging service")
        //bigText.setSummaryText("Content has changed");
        var pendingIntent: PendingIntent? = null
        val notificationIntent = Intent(this@ServerService, MainActivity::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(
                this@ServerService,
                0,
                notificationIntent,
                PendingIntent.FLAG_MUTABLE
            )
        }
        mBuilder.setContentIntent(pendingIntent)
        mBuilder.setSmallIcon(R.drawable.ic_stat_message)
        mBuilder.setContentTitle("Messaging service")
        //mBuilder.setContentText("User has sent you a new message!");
        mBuilder.setPriority(Notification.PRIORITY_MIN)
        mBuilder.setStyle(bigText)
        mBuilder.setOngoing(true)
        mBuilder.setDefaults(0)
        // === Removed some obsoletes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "messageservice"
            val channel = NotificationChannel(
                channelId,
                "Keep alive service",
                NotificationManager.IMPORTANCE_MIN
            )
            channel.enableVibration(false)
            notificationManager.createNotificationChannel(channel)
            mBuilder.setChannelId(channelId)
        }
        //notificationManager.notify(NOTIFICATION_EX, mBuilder.build());
        timer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val inc = Handlers.pcall()
                if (inc !== "nothing") {
                    try {
                        val jobj = JSONObject(inc)
                        val dobj = jobj.getJSONObject("data")
                        val notificationManager2 =
                            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                        val icon2 = android.R.drawable.ic_dialog_info
                        val mBuilder2 =
                            NotificationCompat.Builder(applicationContext, dobj.getString("i"))
                        // NotificationCompat.BigTextStyle bigText2 = new NotificationCompat.BigTextStyle();
                        var pendingIntent2: PendingIntent? = null
                        val notificationIntent2 =
                            Intent(this@ServerService, ChatActivity::class.java)
                        notificationIntent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        notificationIntent2.putExtra("uid_value", dobj.getString("i"))
                        notificationIntent2.putExtra("name_value", dobj.getString("n"))
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            pendingIntent2 = PendingIntent.getActivity(
                                this@ServerService,
                                0,
                                notificationIntent2,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                        }
                        val notificationLayout =
                            RemoteViews(baseContext.packageName, R.layout.notify)
                        val file =
                            File(baseContext.cacheDir.absolutePath + "/" + dobj.getString("i") + ".png")
                        if (file.exists()) {
                            val icc = PlaceholderFragment.getRoundedCroppedBitmap(
                                Bitmap.createScaledBitmap(
                                    BitmapFactory.decodeFile(file.absolutePath),
                                    200,
                                    200,
                                    false
                                )
                            )
                            getInstance().icon = icc
                            notificationLayout.setImageViewBitmap(R.id.custom_icon, icc)
                        } else {
                            if (Handlers.icons.containsKey(dobj.getString("i"))) {
                                val icc = PlaceholderFragment.getRoundedCroppedBitmap(
                                    Bitmap.createScaledBitmap(
                                        Handlers.icons[dobj.getString("i")]!!, 200, 200, false
                                    )
                                )
                                getInstance().icon = icc
                                notificationLayout.setImageViewBitmap(R.id.custom_icon, icc)
                            } else {
                                getInstance().icon = BitmapFactory.decodeResource(
                                    baseContext.resources, R.drawable.profile
                                )
                                notificationLayout.setImageViewBitmap(
                                    R.id.custom_icon, PlaceholderFragment.getRoundedCroppedBitmap(
                                        BitmapFactory.decodeResource(
                                            baseContext.resources, R.drawable.profile
                                        )
                                    )
                                )
                            }
                        }
                        if (!stacked.containsKey(dobj.getString("i"))) {
                            stacked[dobj.getString("i")] =
                                Handlers.iterMsg(jobj.getString("value"), 2)!!
                        } else {
                            val sbuilder = StringBuilder()
                            sbuilder.append(stacked[dobj.getString("i")] + "\n")
                            if (sbuilder.length >= 36) {
                                stacked.remove(dobj.getString("i"))
                                val msg = Handlers.iterMsg(jobj.getString("value"), 2)
                                if (msg?.length!! >= 36) stacked[dobj.getString("i")] =
                                    Handlers.iterMsg(jobj.getString("value"), 2)
                                        ?.substring(0, 36) + "…" else stacked[dobj.getString("i")] =
                                    Handlers.iterMsg(jobj.getString("value"), 2)!!
                            } else {
                                sbuilder.append(Handlers.iterMsg(jobj.getString("value"), 2))
                                stacked.remove(dobj.getString("i"))
                                stacked[dobj.getString("i")] = sbuilder.toString()
                            }
                        }
                        notificationLayout.setTextViewText(R.id.custom_title, dobj.getString("n"))
                        notificationLayout.setTextViewText(
                            R.id.custom_text,
                            stacked[dobj.getString("i")]
                        )
                        if (isDarkThemeEnabled(baseContext)) {
                            notificationLayout.setTextColor(R.id.custom_title, Color.WHITE)
                            notificationLayout.setTextColor(R.id.custom_text, Color.LTGRAY)
                        }

                        //bigText2.bigText(Handlers.iterMsg(jobj.getString("value"), 2));
                        //bigText2.setBigContentTitle(dobj.getString("n"));
                        mBuilder2.setSmallIcon(R.drawable.ic_stat_message)
                        mBuilder2.setContentTitle(dobj.getString("n"))
                        mBuilder2.setContentText(Handlers.iterMsg(jobj.getString("value"), 2))
                        //mBuilder2.setStyle(bigText2);
                        mBuilder2.setContentIntent(pendingIntent2)
                        mBuilder2.setPriority(Notification.PRIORITY_MAX)
                        mBuilder2.setAutoCancel(true)
                        mBuilder2.setStyle(NotificationCompat.DecoratedCustomViewStyle())
                        mBuilder2.setCustomContentView(notificationLayout)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val channelId = dobj.getString("i")
                            val channel = NotificationChannel(
                                channelId,
                                dobj.getString("n"),
                                NotificationManager.IMPORTANCE_HIGH
                            )
                            channel.enableVibration(true)
                            channel.vibrationPattern = vibrationPattern
                            notificationManager2.createNotificationChannel(channel)
                            mBuilder2.setChannelId(channelId)
                        }
                        if (!ChatActivity.isFocused || !getInstance().uid.contains(dobj.getString("i"))) {
                            notificationManager2.notify(
                                abs(dobj.getString("i").hashCode().toDouble()).toInt(),
                                mBuilder2.build()
                            )
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                                val vibrator =
                                    baseContext.getSystemService(VIBRATOR_SERVICE) as Vibrator
                                if (vibrator != null && vibrator.hasVibrator()) {
                                    vibrator.vibrate(
                                        VibrationEffect.createWaveform(
                                            vibrationPattern,
                                            -1
                                        )
                                    )
                                }
                            }
                            /*if (!ChatActivity.isFocused && Singleton.getInstance().getUid().contains(dobj.getString("i"))) {
                                    ChatActivity.thiz.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                ChatActivity.addNewMessage(jobj.getString("value"), false, true, false);
                                            } catch (JSONException ex) {}
                                        }
                                    });
                                }*/
                        } /*  else {
                                ChatActivity.thiz.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                ChatActivity.addNewMessage(jobj.getString("value"), false, true, false);
                                            } catch (JSONException ex) {}
                                        }
                                });
                            }*/
                        if (MainActivity.isFocused) {
                            //PlaceholderFragment.people.clear();
                            //new Handler().postDelayed(() -> {
                            if (!MainActivity.isFocused) return
                            MainActivity.activity!!.runOnUiThread {
                                Handler().postDelayed(
                                    {
                                        if (!MainActivity.isFocused) return@postDelayed
                                        val lchat: Thread = RefreshChats()
                                        if (!MainActivity.isFocused) return@postDelayed
                                        if (!PlaceholderFragment.exts.isShutdown) PlaceholderFragment.exts.shutdown()
                                        PlaceholderFragment.exts =
                                            Executors.newSingleThreadExecutor()
                                        if (!MainActivity.isFocused) return@postDelayed
                                        PlaceholderFragment.exts.execute(lchat)
                                    }, 100
                                )
                            }
                            // }, 500);
                        }
                    } catch (ex: Exception) {
                        LoginActivity.logger!!.append(ex.toString())
                    }
                }
            }
        }, 0, UPDATE_INTERVAL.toLong())
        startForeground(NOTIFICATION_EX, mBuilder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        timer?.cancel()
    }

    private fun stopService() {
        isRunning = false
        timer?.cancel()
    }

    companion object {
        private const val NOTIFICATION_EX = 1

        //private NotificationManager notificationManager;
        var isRunning = false
            private set
        val stacked: MutableMap<String, String> = ConcurrentHashMap()
        var thiz: Context? = null
        fun countLineBreaks(text: String?): Int {
            return if (text == null || text.isEmpty()) {
                0
            } else text.chars().filter { ch: Int -> ch == '\n'.code }.count().toInt()
        }
    }
}