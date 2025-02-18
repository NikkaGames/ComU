package ge.nikka.packages

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.SignInButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ge.nikka.packages.GoogleSignInActivity.Companion.signIn
import ge.nikka.packages.ui.main.PlaceholderFragment
import jp.wasabeef.blurry.Blurry
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.Executors

class LoginActivity : AppCompatActivity() {
    private fun ctx(): Context {
        return this.baseContext
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //DensityUtils.setCustomDensity(this, 405);
        prefs = getSharedPreferences("cred", MODE_PRIVATE)
        MainActivity.statusBarHeight = MainActivity.getStatusBarHeight(this).toFloat()
        thiz = this
        Handlers.currentActivity = this
        logger!!.append("started Activity: " + this.javaClass.name)
        //AXEmojiManager.install(this, new AXAppleEmojiProvider(this));
        if (SocketService.isRunning && ServerService.isRunning) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
            return
        }
        setContentView(R.layout.activity_login_new)
        val pm = getSystemService(PowerManager::class.java)
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        }


        /*
        usern = findViewById(R.id.loginEdit);
        usern.setSingleLine(true);
        usern.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        passw = findViewById(R.id.passEdit);
        passw.setSingleLine(true);
        passw.setImeOptions(EditorInfo.IME_ACTION_DONE);
        */
        /*
        if (prefs.contains("user") && prefs.contains("pwd")) {
            usern.setText(prefs.getString("user", null));
            passw.setText(prefs.getString("pwd", null));
            showProgressDialog();

            Handler handler = new Handler();
            handler.postDelayed(() -> {
                if (!SocketService.isRunning()) startService(new Intent(LoginActivity.this, SocketService.class));
                initiateThread();
                checkRsInBackground();
            }, 50);
        }
        */
        /*
        Button lbtn = findViewById(R.id.loginBtn);
        lbtn.setOnClickListener(v -> {
            PlaceholderFragment.dismissKeyboard(LoginActivity.this);
            showProgressDialog();

            Handler handler = new Handler();
            handler.postDelayed(() -> {
                //stopService(new Intent(LoginActivity.this, SocketService.class));
                if (!SocketService.isRunning()) startService(new Intent(LoginActivity.this, SocketService.class));
                initiateThread();
                checkRsInBackground();
            }, 50);
        });*/requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), 4535)
        val signInButton = findViewById<SignInButton>(R.id.sign_in_button)
        //signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setOnClickListener {
            showProgressDialog()
            val listn: GoogleSignInListener = SignInHandler()
            signIn(Handlers.currentActivity!!, listn)
        }
    }

    override fun onWindowFocusChanged(arg0: Boolean) {
        super.onWindowFocusChanged(arg0)
        if (arg0) Handlers.currentActivity = this
    }

    companion object {
        @JvmStatic
        fun addReq(req: String?) {
            SocketService.reqs.add(req!!)
        }

        var thiz: Activity? = null
        @JvmField
        var logger: FLog? = null
        @JvmField
        var dlg: AlertDialog? = null

        //static EditTextCursorWatcher usern = null;
        //static EditTextCursorWatcher passw = null;
        var prefs: SharedPreferences? = null
        var gtk = ""
        @JvmField
        var executor = Executors.newSingleThreadExecutor()
        @JvmField
        var executor2 = Executors.newSingleThreadExecutor()
        var executor3 = Executors.newSingleThreadExecutor()
        var rsthread = Thread {
            while (true) {
                Handlers.checkRequests()
                try {
                    Thread.sleep(50)
                } catch (ex: InterruptedException) {
                }
            }
        }
        private var mtmp: Bitmap? = null
        var thread = Thread(Runnable {
            while (!SocketService.isconnected) {
                try {
                    Thread.sleep(50)
                } catch (ex: InterruptedException) {
                    return@Runnable
                }
            }

            //runOnUiThread(() -> Handlers.login(usern.getText().toString(), passw.getText().toString()));
            Thread { Handlers.loginT(gtk) }.start()
            while (true) {
                val chr = Handlers.checkLoginResult()
                if (chr.length > 0 && chr != "empty") {
                    Handlers.currentActivity?.runOnUiThread { if (dlg != null) dlg!!.dismiss() }
                    try {
                        val jobj = JSONObject(chr)
                        if (jobj.getString("value") == "success") {
                            /*SharedPreferences.Editor editor = prefs.edit();
                                 editor.putString("user", usern.getText().toString());
                                 editor.putString("pwd", passw.getText().toString());
                                 editor.apply();    */
                            gtk = ""
                            Singleton.getInstance().myUid = jobj.getString("data")
                            Singleton.getInstance().setMyName(jobj.getString("uname"))
                            Handlers.getIcon(JSONObject(Handlers.myPfp).getString("p"))
                                .thenAccept { bitmap: Bitmap? ->
                                    if (bitmap != null) {
                                        mtmp = PlaceholderFragment.getRoundedCroppedBitmap(
                                            Bitmap.createScaledBitmap(
                                                bitmap,
                                                200,
                                                200,
                                                false
                                            )
                                        )
                                    }
                                }
                            while (mtmp == null) {
                            }
                            Singleton.getInstance().setMyPic(mtmp!!)
                            Handlers.currentActivity?.runOnUiThread {
                                Handlers.currentActivity?.startActivity(
                                    Intent(
                                        Handlers.currentActivity,
                                        MainActivity::class.java
                                    )
                                )
                                Handlers.currentActivity?.finish()
                            }
                        } /*else {
                            SharedPreferences.Editor editor = prefs.edit();
                            if (prefs.contains("user")) editor.remove("user");
                            if (prefs.contains("pwd")) editor.remove("pwd");
                            editor.apply();
                            thiz.runOnUiThread(() -> {
                                try {
                                    //stopService(new Intent(LoginActivity.this, SocketService.class));
                                    Toast.makeText(LoginActivity.thiz, "Error: " + jobj.getString("data"), Toast.LENGTH_SHORT).show();
                                } catch (JSONException exx) {
                                }
                            });
                            executor.shutdown();
                            executor2.shutdownNow();
                            break;
                        }*/
                    } catch (ex: JSONException) {
                    }
                    break
                }
                try {
                    Thread.sleep(10)
                } catch (ex: InterruptedException) {
                    return@Runnable  // Exit if interrupted
                }
            }
        })

        fun showProgressDialog() {
            Blurry.with(Handlers.currentActivity)
                .radius(10)
                .sampling(2)
                .async()
                .onto(Handlers.currentActivity?.window?.decorView as ViewGroup)
            val bld = MaterialAlertDialogBuilder(Handlers.currentActivity!!)
            bld.setTitle("Please wait!..").setView(R.layout.prog_dialog).setCancelable(false) /*.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
            }
        })*/
                .setOnDismissListener { Blurry.delete(Handlers.currentActivity?.window?.decorView as ViewGroup) }
                .create()
            dlg = bld.show()
        }

        @JvmStatic
        fun initiateThread(token: String) {
            gtk = token
            if (!executor.isShutdown) executor.shutdown()
            executor = Executors.newSingleThreadExecutor()
            executor.execute(thread)
        }

        @JvmStatic
        fun checkRsInBackground() {
            if (!executor2.isShutdown) executor2.shutdown()
            executor2 = Executors.newSingleThreadExecutor()
            executor2.execute(rsthread)
        }

        @JvmStatic
        fun checkMsgInBackground() {
            if (!executor3.isShutdown) executor3.shutdown()
            executor3 = Executors.newSingleThreadExecutor()
            executor3.execute(Handlers.messageCheck)
        }
    }
}