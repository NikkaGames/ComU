package ge.nikka.packages

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.res.ResourcesCompat
import com.aghajari.emojiview.AXEmojiManager
import com.aghajari.emojiview.listener.OnStickerActions
import com.aghajari.emojiview.sticker.RecentSticker
import com.aghajari.emojiview.sticker.Sticker
import com.aghajari.emojiview.view.AXEmojiPager
import com.aghajari.emojiview.view.AXEmojiPopupLayout
import com.aghajari.emojiview.view.AXEmojiView
import com.aghajari.emojiview.view.AXStickerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.vdurmont.emoji.EmojiParser
import ge.nikka.packages.Handlers.LoadChatPage
import ge.nikka.packages.SwipeListener.OnSwipeListener
import ge.nikka.packages.ui.main.EditTextCursorWatcher
import ge.nikka.packages.ui.main.PlaceholderFragment
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.min

class ChatActivity : AppCompatActivity() {
    private lateinit var buttonSend: ImageButton
    private lateinit var emojiButton: ImageButton
    private lateinit var pplayout: AXEmojiPopupLayout
    private var isLoadingMore = false
    private var util1 = false
    private var terminate = false
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //DensityUtils.setCustomDensity(this, 405);
        //AXEmojiManager.install(this, new AXAppleEmojiProvider(this));
        thiz = this
        setContentView(R.layout.activity_chat)
        loadedMessages = ArrayList()
        page = 1
        util1 = false
        terminate = false
        if (intent.hasExtra("uid_value")) {
            val iuid = intent.getStringExtra("uid_value")
            if (iuid!!.length == 24) Singleton.getInstance().setUid(iuid)
        }
        if (intent.hasExtra("name_value")) {
            val iuid = intent.getStringExtra("name_value")
            if (iuid!!.length > 0) Singleton.getInstance().name = iuid
        }
        avgColor = try {
            getAverageColor(Singleton.getInstance().getIcon()!!)
        } catch (e: NullPointerException) {
            Color.valueOf(Color.parseColor("#FF006FFA"))
        }
        AXEmojiManager.getEmojiViewTheme().selectionColor = avgColor!!.toArgb()
        AXEmojiManager.getEmojiViewTheme().footerSelectedItemColor = avgColor!!.toArgb()
        AXEmojiManager.getEmojiViewTheme().selectedColor = avgColor!!.toArgb()
        AXEmojiManager.getEmojiViewTheme().dividerColor = avgColor!!.toArgb()
        AXEmojiManager.getStickerViewTheme().selectedColor = avgColor!!.toArgb()
        AXEmojiManager.getStickerViewTheme().dividerColor = avgColor!!.toArgb()
        AXEmojiManager.getStickerViewTheme().variantDividerColor = avgColor!!.toArgb()
        AXEmojiManager.getStickerViewTheme().selectionColor = avgColor!!.toArgb()
        supportActionBar!!.setDisplayShowCustomEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setCustomView(R.layout.custom_action_bar)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        val view = supportActionBar!!.customView
        val arrow = view.findViewById<ImageView>(R.id.backArrow)
        arrow.setColorFilter(avgColor!!.toArgb(), PorterDuff.Mode.SRC_ATOP)
        horizontalLayout = LinearLayout(this)
        horizontalLayout!!.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        horizontalLayout!!.gravity = Gravity.CENTER
        val progressBar = CircularProgressIndicator(this)
        progressBar.isIndeterminate = true
        progressBar.layoutParams = FrameLayout.LayoutParams(
            120, 120
        )
        progressBar.setPadding(0, 8, 0, 8)
        progressBar.setIndicatorColor(avgColor!!.toArgb())
        horizontalLayout!!.addView(progressBar)
        val backArrow = view.findViewById<ImageView>(R.id.backArrow)
        backArrow.setOnClickListener { onBackPressed() }
        val lay = view.findViewById<LinearLayout>(R.id.arrowCard)
        lay.setOnClickListener { onBackPressed() }
        val play = view.findViewById<LinearLayout>(R.id.profileCard)
        play.setOnClickListener {
            val intent = Intent(this@ChatActivity, ProfileActivity::class.java)
            this@ChatActivity.startActivity(intent)
        }
        val profilePicture = view.findViewById<ImageView>(R.id.profilePicture)
        val profileName = view.findViewById<TextView>(R.id.profileName)
        val profileStatus = view.findViewById<TextView>(R.id.profileStatus)
        if (!sexecutor.isShutdown) sexecutor.shutdown()
        sexecutor = Executors.newSingleThreadExecutor()
        val getStatus = Thread {
            while (true) {
                var vval: String = ""
                while (vval.length <= 0) {
                    vval = Handlers.getStatus(Singleton.getInstance().uid)
                }
                try {
                    val jobj: JSONObject = JSONObject(vval)
                    if ((jobj.getString("data") == Singleton.getInstance().uid)) {
                        val stat: Int = jobj.getString("value").toInt()
                        thiz.runOnUiThread(Runnable {
                            when (stat) {
                                0 -> {
                                    profileStatus.setText("Offline")
                                    profileStatus.setTextColor(Color.GRAY)
                                }

                                1 -> {
                                    profileStatus.setText("Away")
                                    profileStatus.setTextColor(Color.LTGRAY)
                                }

                                2 -> {
                                    profileStatus.setText("Online")
                                    profileStatus.setTextColor(Color.parseColor("#FF40B575"))
                                }
                            }
                        })
                    }
                } catch (ex: JSONException) {
                }
                if (terminate) break
                while (!isFocused) {
                    try {
                        Thread.sleep(50)
                    } catch (exx: InterruptedException) {
                    }
                }
                try {
                    Thread.sleep(1000)
                } catch (exx: InterruptedException) {
                }
            }
        }
        sexecutor.execute(getStatus)
        profileName.text = Singleton.getInstance().name
        val user = Singleton.getInstance().icon
        profilePicture.setImageBitmap(
            PlaceholderFragment.getRoundedCroppedBitmap(
                Bitmap.createScaledBitmap(user!!, 150, 150, false)
            )
        )
        val infL = view.findViewById<LinearLayout>(R.id.infCard)
        val inf = infL.findViewById<ImageButton>(R.id.infPic)
        //inf.setBackgroundColor(Color.TRANSPARENT);
        val sizeInDp = 55
        val sizeInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            sizeInDp.toFloat(),
            getResources().displayMetrics
        ).toInt()
        val pparams = LinearLayout.LayoutParams(sizeInPx, sizeInPx)
        pparams.gravity = Gravity.CENTER or Gravity.RIGHT
        pparams.setMargins(MainActivity.getPercentOfScreenWidthInPx(this, 0.47f), 0, 0, 0)
        inf.layoutParams = pparams
        inf.setColorFilter(avgColor!!.toArgb(), PorterDuff.Mode.SRC_ATOP)
        inf.setOnClickListener {
            val intent = Intent(this@ChatActivity, ProfileActivity::class.java)
            this@ChatActivity.startActivity(intent)
        }

        //((ViewGroup)view).addView(inf);
        if (ServerService.isRunning) {
            val notificationManager = NotificationManagerCompat.from(ServerService.thiz!!)
            notificationManager.cancel(
                abs(Singleton.getInstance().uid.hashCode().toDouble()).toInt()
            )
            if (ServerService.stacked.containsKey(Singleton.getInstance().uid)) ServerService.stacked.remove(
                Singleton.getInstance().uid
            )
        }
        messageContainer = findViewById(R.id.messageContainer)
        editTextMessage = findViewById(R.id.messageEditText)
        val eview = AXEmojiView(this)
        eview.editText = editTextMessage
        val stickerView = AXStickerView(this, "stickers", NikkasEmojiProvider())
        stickerView.editText = editTextMessage
        val emj = ImageView(this)
        emj.setImageResource(R.drawable.emoji)
        val emojiPager = AXEmojiPager(this)
        emojiPager.addPage(eview, R.drawable.ic_msg_panel_smiles)
        emojiPager.addPage(stickerView, R.drawable.ic_msg_panel_stickers)
        emojiPager.setSwipeWithFingerEnabled(true)
        emojiPager.editText = editTextMessage
        emojiPager.setLeftIcon(R.drawable.search)
        stickerView.setOnStickerActionsListener(object : OnStickerActions {
            override fun onClick(view: View, sticker: Sticker<*>, fromRecent: Boolean) {
                if (recentSticker != null) recentSticker!!.addSticker(sticker)
                val vibrator: Vibrator = thiz.getSystemService(VIBRATOR_SERVICE) as Vibrator
                if (vibrator != null && vibrator.hasVibrator()) {
                    vibrator.vibrate(
                        VibrationEffect.createWaveform(
                            longArrayOf(0, 10, 200, 20),
                            -1
                        )
                    )
                }
                val listener = Singleton.getInstance().getReplyTo()
                if (listener != null) {
                    val lanim = ObjectAnimator.ofInt(replyBox!!.height, 0)
                    lanim.setDuration(100)
                    lanim.addUpdateListener { animation ->
                        val aparams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            animation.animatedValue as Int
                        )
                        replyBox!!.layoutParams = aparams
                    }
                    lanim.start()
                    Singleton.getInstance().setReplyTo(null)
                    val tkn = Handlers.cancellationToken()
                    val rinfo = StringBuilder()
                    if (listener.mine) {
                        rinfo.append(Singleton.getInstance().myUid + "-")
                    } else {
                        rinfo.append(Singleton.getInstance().uid + "-")
                    }
                    rinfo.append(Handlers.iterMsg(listener.m, 1))
                    Handlers.sendMessage(
                        Singleton.getInstance().uid,
                        Handlers.iterMsg((sticker as NikkasSticker).title, 1),
                        rinfo.toString(),
                        tkn
                    )
                    addNewMessage(sticker.title, tkn, rinfo.toString(), true, false, false)
                    editTextMessage.setText("")
                } else {
                    val tkn = Handlers.cancellationToken()
                    Handlers.sendMessage(
                        Singleton.getInstance().uid,
                        Handlers.iterMsg((sticker as NikkasSticker).title, 1),
                        "none",
                        tkn
                    )
                    addNewMessage(sticker.title, tkn, true, false, false)
                    editTextMessage.setText("")
                }
                //Toast.makeText(Handlers.currentActivity, "Sticker clicked: " + ((NikkasSticker)sticker).title, Toast.LENGTH_SHORT).show();
            }

            override fun onLongClick(
                view: View,
                sticker: Sticker<*>?,
                fromRecent: Boolean
            ): Boolean {
                return false
            }
        })
        pplayout = findViewById(R.id.playout)
        pplayout.initPopupView(emojiPager)
        val params = LinearLayout.LayoutParams(0, 0)
        pplayout.setLayoutParams(params)
        replyBox = findViewById(R.id.replyBox)
        val rparams = LinearLayout.LayoutParams(
            0,
            0
        )
        val crbtn = replyBox.findViewById<ImageButton>(R.id.closeRbtn)
        crbtn.setColorFilter(avgColor!!.toArgb(), PorterDuff.Mode.SRC_ATOP)
        crbtn.setOnClickListener {
            val sanim = ObjectAnimator.ofInt(replyBox.getHeight(), 0)
            sanim.setDuration(100)
            sanim.addUpdateListener { animation ->
                val aparams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    animation.animatedValue as Int
                )
                replyBox.setLayoutParams(aparams)
            }
            sanim.start()
            Singleton.getInstance().setReplyTo(null)
        }
        replyBoxHeight = replyBox.getHeight()
        replyBox.setLayoutParams(rparams)
        replyBox.setOrientation(LinearLayout.HORIZONTAL)
        editTextMessage.setOnTouchListener(OnTouchListener { v, event -> /*ValueAnimator anim2 = ValueAnimator.ofFloat(1f, 1.025f);
                anim2.setDuration(100);
                anim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        editTextMessage.setScaleX((Float)animation.getAnimatedValue());
                        editTextMessage.setScaleY((Float)animation.getAnimatedValue());
                    }
                });
                anim2.setRepeatCount(1);
                anim2.setRepeatMode(ValueAnimator.REVERSE);
                if (event.getAction() == MotionEvent.ACTION_DOWN)    
                anim2.start();*/
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val animator = ValueAnimator.ofFloat(1f, 0.95f)
                    animator.setDuration(150)
                    animator.interpolator = DecelerateInterpolator()
                    animator.addUpdateListener { animation: ValueAnimator ->
                        val `val`: Float = animation.getAnimatedValue() as Float
                        editTextMessage.setScaleX(`val`)
                        editTextMessage.setScaleY(`val`)
                    }
                    animator.start()
                }

                MotionEvent.ACTION_UP -> {
                    val animator2 = ValueAnimator.ofFloat(0.95f, 1f)
                    animator2.setDuration(150)
                    animator2.interpolator = DecelerateInterpolator()
                    animator2.addUpdateListener { animation: ValueAnimator ->
                        val `val`: Float = animation.getAnimatedValue() as Float
                        editTextMessage.setScaleX(`val`)
                        editTextMessage.setScaleY(`val`)
                    }
                    animator2.start()
                }
            }
            if (pplayout.isShowing()) {
                editTextMessage.requestFocus()
                Handlers.showKeyboard(editTextMessage)
                val params = LinearLayout.LayoutParams(0, 0)
                pplayout.setLayoutParams(params)
                pplayout.dismiss()
                editTextMessage.requestFocus()
                val sanim = ObjectAnimator.ofFloat(0.5f, 0f, 0.5f)
                sanim.setDuration(300)
                sanim.addUpdateListener { animation ->
                    emojiButton!!.scaleX = animation.animatedValue as Float
                    emojiButton!!.scaleY = animation.animatedValue as Float
                }
                sanim.start()
                emojiButton!!.postDelayed({ emojiButton!!.setImageResource(R.drawable.emoji) }, 150)
            }
            val scrollView = messageContainer.getParent() as ScrollView
            scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
            false
        })
        editTextMessage.setOnClickListener(View.OnClickListener {
            if (pplayout.isShowing()) {
                editTextMessage.requestFocus()
                Handlers.showKeyboard(editTextMessage)
                val params = LinearLayout.LayoutParams(0, 0)
                pplayout.setLayoutParams(params)
                pplayout.dismiss()
            }
            val scrollView = messageContainer.getParent() as ScrollView
            scrollView.postDelayed({ scrollView.fullScroll(View.FOCUS_DOWN) }, 100)
        })
        buttonSend = findViewById(R.id.sendButton)
        buttonSend.setOnClickListener(View.OnClickListener {
            val sanim = ObjectAnimator.ofFloat(0.45f, 0.225f, 0.3375f, 0.3f, 0.67f, 0.45f)
            sanim.setDuration(400)
            sanim.addUpdateListener { animation ->
                buttonSend.setScaleX(animation.animatedValue as Float)
                buttonSend.setScaleY(animation.animatedValue as Float)
            }
            sanim.start()
            val message = editTextMessage.getText().toString()
            if (!message.isEmpty()) {
                if (Singleton.getInstance().uid.length == 24) {
                    val vibrator: Vibrator = thiz.getSystemService(VIBRATOR_SERVICE) as Vibrator
                    if (vibrator != null && vibrator.hasVibrator()) {
                        vibrator.vibrate(
                            VibrationEffect.createWaveform(
                                longArrayOf(0, 10, 200, 20),
                                -1
                            )
                        )
                    }
                    val listener = Singleton.getInstance().getReplyTo()
                    if (listener != null) {
                        val lanim = ObjectAnimator.ofInt(replyBox.getHeight(), 0)
                        lanim.setDuration(100)
                        lanim.addUpdateListener { animation ->
                            val aparams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                animation.animatedValue as Int
                            )
                            replyBox.setLayoutParams(aparams)
                        }
                        lanim.start()
                        Singleton.getInstance().setReplyTo(null)
                        val tkn = Handlers.cancellationToken()
                        val rinfo = StringBuilder()
                        if (listener.mine) {
                            rinfo.append(Singleton.getInstance().myUid + "-")
                        } else {
                            rinfo.append(Singleton.getInstance().uid + "-")
                        }
                        rinfo.append(Handlers.iterMsg(listener.m, 1))
                        Handlers.sendMessage(
                            Singleton.getInstance().uid,
                            Handlers.iterMsg(message, 1),
                            rinfo.toString(),
                            tkn
                        )
                        addNewMessage(message, tkn, rinfo.toString(), true, false, false)
                        editTextMessage.setText("")
                    } else {
                        val tkn = Handlers.cancellationToken()
                        Handlers.sendMessage(
                            Singleton.getInstance().uid,
                            Handlers.iterMsg(message, 1),
                            "none",
                            tkn
                        )
                        addNewMessage(message, tkn, true, false, false)
                        editTextMessage.setText("")
                    }
                }
            }
        })
        buttonSend.setAlpha(0.5f)
        buttonSend.setEnabled(false)
        buttonSend.setColorFilter(avgColor!!.toArgb(), PorterDuff.Mode.SRC_ATOP)
        editTextMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.length > 0) {
                    val sanim = ObjectAnimator.ofFloat(0.45f, 0.6f, 0.62f, 0.64f, 0.66f, 0.45f)
                    sanim.setDuration(250)
                    sanim.addUpdateListener { animation ->
                        buttonSend.setScaleX(animation.animatedValue as Float)
                        buttonSend.setScaleY(animation.animatedValue as Float)
                    }
                    val sanimm = ObjectAnimator.ofFloat(0.5f, 1f)
                    sanimm.setDuration(250)
                    sanimm.addUpdateListener { animation -> buttonSend.setAlpha(animation.animatedValue as Float) }
                    if (!util1) {
                        sanim.start()
                        sanimm.start()
                        util1 = !util1
                    }
                    buttonSend.setEnabled(true)
                } else {
                    val sanim = ObjectAnimator.ofFloat(0.45f, 0.4f, 0.39f, 0.37f, 0.36f, 0.45f)
                    sanim.setDuration(250)
                    sanim.addUpdateListener { animation ->
                        buttonSend.setScaleX(animation.animatedValue as Float)
                        buttonSend.setScaleY(animation.animatedValue as Float)
                    }
                    val sanimm = ObjectAnimator.ofFloat(1f, 0.5f)
                    sanimm.setDuration(250)
                    sanimm.addUpdateListener { animation -> buttonSend.setAlpha(animation.animatedValue as Float) }
                    if (util1) {
                        sanim.start()
                        sanimm.start()
                        util1 = !util1
                    }
                    buttonSend.setEnabled(false)
                }
            }
        })
        emojiButton = findViewById(R.id.emojiButton)
        emojiButton.setOnClickListener(View.OnClickListener {
            val vibrator: Vibrator = thiz.getSystemService(VIBRATOR_SERVICE) as Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 20), -1))
            }
            val sanim = ObjectAnimator.ofFloat(0.5f, 0f, 0.5f)
            sanim.setDuration(300)
            sanim.addUpdateListener { animation ->
                emojiButton.setScaleX(animation.animatedValue as Float)
                emojiButton.setScaleY(animation.animatedValue as Float)
            }
            sanim.start()
            if (!pplayout.isShowing()) {
                PlaceholderFragment.dismissKeyboard(this@ChatActivity)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                val handl = Handler()
                handl.postDelayed({
                    pplayout.setLayoutParams(params)
                    pplayout.show()
                    pplayout.setAnimation(fadeout())
                    val handler = Handler()
                    handler.postDelayed({
                        val scrollView: ScrollView = messageContainer.getParent() as ScrollView
                        scrollView.post(object : Runnable {
                            override fun run() {
                                scrollView.fullScroll(View.FOCUS_DOWN)
                            }
                        })
                    }, 250)
                }, 50)
                emojiButton.postDelayed(
                    Runnable { emojiButton.setImageResource(R.drawable.keyb) },
                    150
                )
            } else {
                editTextMessage.requestFocus()
                Handlers.showKeyboard(editTextMessage)
                val params = LinearLayout.LayoutParams(0, 0)
                pplayout.setLayoutParams(params)
                pplayout.dismiss()
                emojiButton.postDelayed(
                    Runnable { emojiButton.setImageResource(R.drawable.emoji) },
                    150
                )
            }
            val handler = Handler()
            handler.postDelayed({
                val scrollView = messageContainer.getParent() as ScrollView
                scrollView.post(Runnable { scrollView.fullScroll(View.FOCUS_DOWN) })
            }, 200)
        })
        emojiButton.setColorFilter(avgColor!!.toArgb(), PorterDuff.Mode.SRC_ATOP)
        val mainScroll = messageContainer.getParent() as ScrollView
        mainScroll.isVerticalScrollBarEnabled = false
        mainScroll.layoutParams = LinearLayout.LayoutParams(
            0,
            0
        )
        val horizontalLayout2 = findViewById<LinearLayout>(R.id.progressLayout)
        val ind = horizontalLayout2.findViewById<CircularProgressIndicator>(R.id.chatCirProg)
        val flay = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        /*
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int halfScreenWidth = screenWidth / 2;
        
        flay.setMargins(halfScreenWidth, 0, 0, 0);*/

        //ind.setLayoutParams(flay);
        ind.setIndicatorColor(avgColor!!.toArgb())
        mainScroll.visibility = View.INVISIBLE
        val scrv = messageContainer.getParent() as ScrollView
        scrv.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (!isLoadingMore && scrollY == 0) {
                isLoadingMore = true
                val vibrator: Vibrator = thiz.getSystemService(VIBRATOR_SERVICE) as Vibrator
                if (vibrator != null && vibrator.hasVibrator()) {
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 20), -1))
                }
                if (messageContainer.indexOfChild(horizontalLayout) == -1) {
                    messageContainer.addView(horizontalLayout, 0)
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.setDuration(250)
                    animator.addUpdateListener { animation: ValueAnimator ->
                        horizontalLayout!!.setScaleX(animation.getAnimatedValue() as Float)
                        horizontalLayout!!.setScaleY(animation.getAnimatedValue() as Float)
                    }
                    animator.start()
                }
                scrv.fullScroll(View.FOCUS_UP)
                mloader.execute(loadMessages)
            }
        }
        executor.execute(LoadChatPage())
        //Handlers.getMessage(Singleton.getInstance().getUid(), "7dda22-912a6e-f3104f-b93212");
    }

    var mloader = Executors.newSingleThreadExecutor()
    var loadMessages = Thread(Runnable {
        var msgs = ""
        while (msgs.length <= 0) {
            msgs = Handlers.getMessages(Singleton.getInstance().uid, page, 16)
        }
        try {
            val mobj = JSONObject(msgs)
            val size = mobj.getString("value").toInt()
            val start = mobj.getString("start").toInt()
            if (size == 0 && start == 0) {
                val jdat = mobj.getJSONObject("data")
                if (jdat.has("0")) {
                    val arr = jdat.getJSONArray("0")
                    val mid = StringBuilder()
                    val `val` = StringBuilder()
                    val replyto = StringBuilder()
                    var time: Long = 0
                    for (j in 0 until arr.length()) {
                        val typ = arr.getJSONObject(j)
                        if ((typ.getString("Name") == "mid")) {
                            mid.append(typ.getString("Value"))
                        } else if ((typ.getString("Name") == "val")) {
                            `val`.append(typ.getString("Value"))
                        } else if ((typ.getString("Name") == "replyto")) {
                            replyto.append(typ.getString("Value"))
                        } else if ((typ.getString("Name") == "time")) {
                            time = typ.getLong("Value")
                        }
                    }
                    //ChatActivity.loadedMessages.add(mid.toString());
                    val mes = `val`.toString()
                    if ((mes.substring(0, 24) == Singleton.getInstance().myUid)) {
                        val rinfo = replyto.toString()
                        if (rinfo.length >= 27) {
                            thiz!!.runOnUiThread({
                                addNewMessage(
                                    Handlers.iterMsg(
                                        mes.substring(25),
                                        2
                                    ), mid.toString(), rinfo, true, false, true
                                )
                            })
                        } else {
                            thiz!!.runOnUiThread({
                                addNewMessage(
                                    Handlers.iterMsg(
                                        mes.substring(25),
                                        2
                                    ), mid.toString(), "none", true, false, true
                                )
                            })
                        }
                    } else {
                        val rinfo = replyto.toString()
                        if (rinfo.length >= 27) {
                            thiz!!.runOnUiThread({
                                addNewMessage(
                                    Handlers.iterMsg(
                                        mes.substring(25),
                                        2
                                    ), mid.toString(), rinfo, false, false, true
                                )
                            })
                        } else {
                            thiz!!.runOnUiThread({
                                addNewMessage(
                                    Handlers.iterMsg(
                                        mes.substring(25),
                                        2
                                    ), mid.toString(), "none", false, false, true
                                )
                            })
                        }
                    }
                }
                thiz!!.runOnUiThread({
                    val scrollView: ScrollView = messageContainer!!.parent as ScrollView
                    scrollView.post(object : Runnable {
                        override fun run() {
                            if (messageContainer!!.indexOfChild(horizontalLayout) != -1) {
                                val animator: ValueAnimator = ValueAnimator.ofFloat(1f, 0f)
                                animator.setDuration(100)
                                animator.addUpdateListener({ animation: ValueAnimator ->
                                    horizontalLayout!!.scaleX = animation.animatedValue as Float
                                    horizontalLayout!!.scaleY = animation.animatedValue as Float
                                })
                                animator.start()
                                messageContainer!!.postDelayed({
                                    messageContainer!!.removeView(horizontalLayout)
                                    scrollView.smoothScrollTo(0, 1)
                                }, 100)
                            }
                        }
                    })
                })
                return@Runnable
            }
            for (i in size downTo start) {
                val arr = mobj.getJSONObject("data").getJSONArray(i.toString())
                val mid = StringBuilder()
                val `val` = StringBuilder()
                val replyto = StringBuilder()
                var time: Long = 0
                for (j in 0 until arr.length()) {
                    val typ = arr.getJSONObject(j)
                    if ((typ.getString("Name") == "mid")) {
                        mid.append(typ.getString("Value"))
                    } else if ((typ.getString("Name") == "val")) {
                        `val`.append(typ.getString("Value"))
                    } else if ((typ.getString("Name") == "replyto")) {
                        replyto.append(typ.getString("Value"))
                    } else if ((typ.getString("Name") == "time")) {
                        time = typ.getLong("Value")
                    }
                }
                //ChatActivity.loadedMessages.add(mid.toString());
                val mes = `val`.toString()
                if ((mes.substring(0, 24) == Singleton.getInstance().myUid)) {
                    val rinfo = replyto.toString()
                    if (rinfo.length >= 27) {
                        thiz!!.runOnUiThread({
                            addNewMessage(
                                Handlers.iterMsg(mes.substring(25), 2),
                                mid.toString(),
                                rinfo,
                                true,
                                false,
                                true
                            )
                        })
                    } else {
                        thiz!!.runOnUiThread({
                            addNewMessage(
                                Handlers.iterMsg(mes.substring(25), 2),
                                mid.toString(),
                                "none",
                                true,
                                false,
                                true
                            )
                        })
                    }
                } else {
                    val rinfo = replyto.toString()
                    if (rinfo.length >= 27) {
                        thiz!!.runOnUiThread({
                            addNewMessage(
                                Handlers.iterMsg(mes.substring(25), 2),
                                mid.toString(),
                                rinfo,
                                false,
                                false,
                                true
                            )
                        })
                    } else {
                        thiz!!.runOnUiThread({
                            addNewMessage(
                                Handlers.iterMsg(mes.substring(25), 2),
                                mid.toString(),
                                "none",
                                false,
                                false,
                                true
                            )
                        })
                    }
                }
            }
            page++
            isLoadingMore = false
            thiz!!.runOnUiThread({
                val scrollView: ScrollView = messageContainer!!.parent as ScrollView
                scrollView.post(object : Runnable {
                    override fun run() {
                        if (messageContainer!!.indexOfChild(horizontalLayout) != -1) {
                            val animator: ValueAnimator = ValueAnimator.ofFloat(1f, 0f)
                            animator.setDuration(100)
                            animator.addUpdateListener({ animation: ValueAnimator ->
                                horizontalLayout!!.scaleX = animation.animatedValue as Float
                                horizontalLayout!!.scaleY = animation.animatedValue as Float
                            })
                            animator.start()
                            messageContainer!!.postDelayed({
                                messageContainer!!.removeView(horizontalLayout)
                                scrollView.smoothScrollTo(0, 1)
                            }, 100)
                        }
                    }
                })
            })
        } catch (ex: JSONException) {
            LoginActivity.logger?.append(ex.toString())
        }
    })

    override fun onWindowFocusChanged(arg0: Boolean) {
        super.onWindowFocusChanged(arg0)
        if (arg0) {
            Handlers.currentActivity = this
            Handlers.setStatus("2")
            isFocused = true
        } else {
            Handlers.setStatus("1")
            isFocused = false
        }
    }

    override fun onBackPressed() {
        if (!executor.isShutdown) executor.shutdown()
        executor = Executors.newSingleThreadExecutor()
        if (pplayout!!.isShowing) {
            val params = LinearLayout.LayoutParams(0, 0)
            pplayout!!.layoutParams = params
            pplayout!!.dismiss()
            val sanim = ObjectAnimator.ofFloat(0.5f, 0f, 0.5f)
            sanim.setDuration(300)
            sanim.addUpdateListener { animation ->
                emojiButton!!.scaleX = animation.animatedValue as Float
                emojiButton!!.scaleY = animation.animatedValue as Float
            }
            sanim.start()
            emojiButton!!.postDelayed({ emojiButton!!.setImageResource(R.drawable.emoji) }, 150)
            return
        }
        terminate = true
        Singleton.getInstance().cleanUp()
        page = 1
        loadedMessages = ArrayList()
        val intent = Intent(this, MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        this.startActivity(intent)
        finish()
    }

    companion object {
        private lateinit var messageContainer: LinearLayout

        lateinit var editTextMessage: EditTextCursorWatcher
        private lateinit var horizontalLayout: LinearLayout
        private var page = 1
        var loadedMessages = ArrayList<String>()
        var executor = Executors.newSingleThreadExecutor()
        var sexecutor = Executors.newSingleThreadExecutor()
        private lateinit var replyBox: LinearLayout
        private var replyBoxHeight = 0
        lateinit var thiz: Activity
        @JvmField
        var isFocused = false
        var recentSticker: RecentSticker? = null
        private fun fadeout(): Animation {
            val fadeIn: Animation = AlphaAnimation(0f, 1f)
            fadeIn.duration = 200
            return fadeIn
        }

        fun popOut(): Animation {
            val fadeIn: Animation = AlphaAnimation(0f, 1f)
            fadeIn.duration = 80
            return fadeIn
        }

        fun popIn(): Animation {
            val fadeIn: Animation = AlphaAnimation(1f, 0f)
            fadeIn.duration = 80
            return fadeIn
        }

        @JvmStatic
        fun containsOnlyEmojis(input: String?): Boolean {
            if (input == null || input.isEmpty()) {
                return false
            }
            val emojis = EmojiParser.extractEmojis(input)
            return emojis.joinToString("") == input
        }

        @JvmStatic
        fun getAverageColor(bitmap: Bitmap): Color {
            val saturationBoost = 1.05f
            val lightnessBoost = 1.25f
            var width = 0
            var height = 0
            try {
                width = bitmap.width
                height = bitmap.height
            } catch (ex: NullPointerException) {
                return Color.valueOf(Color.parseColor("#FF006FFA"))
            }
            val size = width * height
            var redSum: Long = 0
            var greenSum: Long = 0
            var blueSum: Long = 0
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val pixel = bitmap.getPixel(x, y)
                    redSum += Color.red(pixel).toLong()
                    greenSum += Color.green(pixel).toLong()
                    blueSum += Color.blue(pixel).toLong()
                }
            }
            val redAvg = redSum / size.toFloat()
            val greenAvg = greenSum / size.toFloat()
            val blueAvg = blueSum / size.toFloat()
            val hsv = FloatArray(3)
            Color.RGBToHSV(redAvg.toInt(), greenAvg.toInt(), blueAvg.toInt(), hsv)
            hsv[1] = min(1.0, (hsv[1] * saturationBoost).toDouble()).toFloat()
            hsv[2] = min(1.0, (hsv[2] * lightnessBoost).toDouble()).toFloat()
            if (hsv[2] < 0.3f || hsv[2] > 0.85f) {
                return Color.valueOf(Color.parseColor("#FF006FFA"))
            }
            val colorInt = Color.HSVToColor(hsv)
            return Color.valueOf(
                Color.red(colorInt) / 255f,
                Color.green(colorInt) / 255f,
                Color.blue(colorInt) / 255f
            )
        }

        @JvmField
        var avgColor: Color? = null
        fun drawableToBitmap(drawable: Drawable?): Bitmap {
            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }
            val bitmap = Bitmap.createBitmap(
                drawable!!.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }

        fun addNewMessage(messag: String?, mid: String, isMine: Boolean, d: Boolean, old: Boolean) {
            addNewMessage(messag, mid, "none", isMine, d, old)
        }

        @JvmStatic
        fun addNewMessage(
            messag: String?,
            mid: String,
            replyto: String,
            isMine: Boolean,
            d: Boolean,
            old: Boolean
        ) {
            if (loadedMessages.contains(mid)) return
            loadedMessages.add(mid)
            val message = if (d) Handlers.iterMsg(messag, 2) else messag!!
            val isemj = containsOnlyEmojis(message)
            val newMessageView = TextView(thiz)
            newMessageView.text = message
            newMessageView.textSize = 14.0f
            newMessageView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val displayMetrics = Handlers.currentActivity?.resources?.displayMetrics
            val screenWidth = displayMetrics?.widthPixels
            val maxWidth = (screenWidth?.times(0.6))?.toInt()
            newMessageView.maxWidth = maxWidth!!
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val layoutParams3 = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams3.setMargins(12, 10, 12, 10)
            val replyParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            replyParams.setMargins(12, 10, 12, 0)
            val layoutParams2 = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val scrollView = messageContainer!!.parent as ScrollView
            val mlayout = LinearLayout(thiz)
            layoutParams.gravity = if (isMine) Gravity.END else Gravity.START
            mlayout.layoutParams = layoutParams
            val replylayout = LinearLayout(thiz)
            replyParams.gravity = if (isMine) Gravity.END else Gravity.START
            replylayout.layoutParams = replyParams
            val ranim = ObjectAnimator.ofInt(0, 130)
            ranim.setDuration(100)
            ranim.addUpdateListener { animation ->
                val aparams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    animation.animatedValue as Int
                )
                replyBox!!.layoutParams = aparams
            }
            val mlayout2 = SwipeListener(thiz)
            mlayout2.layoutParams = layoutParams2
            mlayout2.gravity = if (isMine) Gravity.END else Gravity.START
            mlayout2.setParentScrollView(scrollView)
            mlayout2.orientation = LinearLayout.VERTICAL
            mlayout2.setOnSwipeListener(object : OnSwipeListener {
                override fun onSwipeLeft() {
                    if (isMine) {
                        val vibrator: Vibrator =
                            thiz!!.getSystemService(VIBRATOR_SERVICE) as Vibrator
                        if (vibrator != null && vibrator.hasVibrator()) {
                            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 20), -1))
                        }
                        val replyto = replyBox!!.findViewById<TextView>(R.id.replyTo)
                        val replytom = replyBox!!.findViewById<TextView>(R.id.replyToMessage)
                        replyto.maxLines = 1
                        replytom.maxLines = 1
                        replyto.text = "Replying to yourself"
                        replytom.text = message?.replace("\n", " ")
                        replyBox!!.orientation = LinearLayout.HORIZONTAL
                        ranim.start()
                        Handlers.showKeyboard(editTextMessage)
                        scrollView.postDelayed({ scrollView.scrollToDescendant(mlayout2) }, 200)
                    }
                }

                override fun onSwipeRight() {
                    if (!isMine) {
                        val vibrator: Vibrator =
                            thiz!!.getSystemService(VIBRATOR_SERVICE) as Vibrator
                        if (vibrator != null && vibrator.hasVibrator()) {
                            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 20), -1))
                        }
                        val replyto = replyBox!!.findViewById<TextView>(R.id.replyTo)
                        val replytom = replyBox!!.findViewById<TextView>(R.id.replyToMessage)
                        replyto.maxLines = 1
                        replytom.maxLines = 1
                        replyto.text = "Replying to " + Singleton.getInstance().name
                        replytom.text = message?.replace("\n", " ")
                        replyBox!!.orientation = LinearLayout.HORIZONTAL
                        ranim.start()
                        Handlers.showKeyboard(editTextMessage)
                        scrollView.postDelayed({ scrollView.scrollToDescendant(mlayout2) }, 200)
                    }
                }
            })
            mlayout2.mid = mid
            mlayout2.m = message
            mlayout2.mine = isMine

            //newMessageView.setBackgroundResource(isMine ? R.drawable.convt : R.drawable.conv);
            //newMessageView.setGravity(Gravity.CENTER);
            newMessageView.setTextColor(Color.WHITE)
            newMessageView.textSize = 16.2f
            //newMessageView.setMinimumWidth(200);
            newMessageView.typeface = ResourcesCompat.getFont((thiz)!!, R.font.googlereg)
            newMessageView.setPadding(5, 5, 5, 5)
            if (isemj) {
                newMessageView.textSize = 35.0f
            }
            val newDrawable = GradientDrawable()
            newDrawable.shape = GradientDrawable.RECTANGLE
            newDrawable.cornerRadius =
                22 * Handlers.currentActivity?.resources?.displayMetrics?.density!!
            newDrawable.setColor(avgColor!!.toArgb())
            newDrawable.setSize(
                newMessageView.width,
                36 * Handlers.currentActivity?.resources?.displayMetrics?.density?.toInt()!!
            )
            if (messageContainer!!.indexOfChild(
                    messageContainer!!.findViewById(
                        abs("nomsgtitle".hashCode().toDouble()).toInt()
                    )
                ) != -1
            ) {
                messageContainer!!.removeView(
                    messageContainer!!.findViewById(
                        abs("nomsgtitle".hashCode().toDouble()).toInt()
                    )
                )
                messageContainer!!.gravity = Gravity.NO_GRAVITY
            }
            val cnt = LinearLayout(thiz)
            cnt.layoutParams = layoutParams3
            if (isMine) {
                if (!isemj) {
                    cnt.background = newDrawable
                    cnt.setPadding(36, 20, 36, 20)
                } else {
                    cnt.setPadding(12, 20, 12, 20)
                }
            } else {
                if (!isemj) cnt.setBackgroundResource(R.drawable.conv)
            }
            cnt.gravity = Gravity.CENTER
            cnt.addView(newMessageView)
            if (replyto.length >= 27) {
                val ismyrp = if (replyto.substring(0, 25)
                        .contains(Singleton.getInstance().myUid)
                ) true else false
                val mesj = Handlers.iterMsg(replyto.substring(25), 2)
                val replyMessageView = TextView(thiz)
                replyMessageView.text = mesj
                replyMessageView.setTextColor(Color.WHITE)
                replyMessageView.textSize = 13.0f
                replyMessageView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                replyMessageView.maxWidth = maxWidth
                replyMessageView.typeface = ResourcesCompat.getFont((thiz)!!, R.font.googlereg)
                replyMessageView.setPadding(5, 5, 5, 0)
                val cnt2 = LinearLayout(thiz)
                cnt2.layoutParams = replyParams
                if (ismyrp) {
                    //cnt2.setBackground(newDrawable);
                    cnt2.setBackgroundResource(R.drawable.conv)
                } else {
                    cnt2.setBackgroundResource(R.drawable.conv)
                }
                val replytot = TextView(thiz)
                replytot.setTextColor(Color.LTGRAY)
                replytot.textSize = 11f
                replytot.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                replytot.typeface = ResourcesCompat.getFont((thiz)!!, R.font.googlereg)
                replytot.setPadding(0, 0, 0, 12)
                replytot.gravity = Gravity.BOTTOM
                if (isMine) {
                    if (ismyrp) {
                        replytot.text = "Replied to yourself"
                    } else {
                        replytot.text = "Replied to " + Singleton.getInstance().name
                    }
                } else {
                    if (ismyrp) {
                        replytot.text = "Replied to you"
                    } else {
                        replytot.text = "Replied to " + Singleton.getInstance().name
                    }
                }
                cnt2.gravity = Gravity.CENTER
                cnt2.addView(replyMessageView)
                replylayout.orientation = LinearLayout.HORIZONTAL
                if (isMine) {
                    replylayout.addView(replytot)
                    replylayout.addView(cnt2)
                } else {
                    replylayout.addView(cnt2)
                    replylayout.addView(replytot)
                }
                replylayout.alpha = 0.7f
                replylayout.setPadding(0, 0, 0, -12)
                mlayout2.addView(replylayout)
                replylayout.startAnimation(AnimationUtils.loadAnimation(thiz, R.anim.anim_in))
            }
            if (message?.contains("[sticker:")!!) {
                var istr: Drawable? = null
                if (message == "[sticker:troll2]") {
                    istr = thiz!!.resources.getDrawable(R.drawable.troll2)
                } else if (message == "[sticker:troll1]") {
                    istr = thiz!!.resources.getDrawable(R.drawable.troll)
                } else if (message == "[sticker:stevy]") {
                    istr = thiz!!.resources.getDrawable(R.drawable.stevy)
                } else if (message == "[sticker:spidy]") {
                    istr = thiz!!.resources.getDrawable(R.drawable.spidy)
                } else if (message == "[sticker:skeleton]") {
                    istr = thiz!!.resources.getDrawable(R.drawable.skeleton)
                } else if (message == "[sticker:smile]") {
                    istr = thiz!!.resources.getDrawable(R.drawable.smile)
                } else if (message == "[sticker:notsigma]") {
                    istr = thiz!!.resources.getDrawable(R.drawable.notsigma)
                } else if (message == "[sticker:jonkler]") {
                    istr = thiz!!.resources.getDrawable(R.drawable.jonkler)
                } else if (message == "[sticker:baby]") {
                    istr = thiz!!.resources.getDrawable(R.drawable.baby)
                } else if (message == "[sticker:fine]") {
                    istr = thiz!!.resources.getDrawable(R.drawable.fine)
                } else if (message == "[sticker:stare]") {
                    istr = thiz!!.resources.getDrawable(R.drawable.stare)
                } else if (message == "[sticker:thumbsup]") {
                    istr = thiz!!.resources.getDrawable(R.drawable.thumbsup)
                } else if (message == "[sticker:clap]") {
                    istr = thiz!!.resources.getDrawable(R.drawable.clap)
                } else if (message == "[sticker:what]") {
                    istr = thiz!!.resources.getDrawable(R.drawable.what)
                } else {
                    mlayout.addView(cnt)
                    mlayout2.addView(mlayout)
                    if (old) messageContainer!!.addView(
                        mlayout2,
                        0
                    ) else messageContainer!!.addView(mlayout2)
                    //mlayout.setAnimation(fadeout());
                    cnt.animation = popOut()
                    val set: Animation
                    set = if (isMine) AnimationUtils.loadAnimation(
                        thiz,
                        R.anim.slide_in_right
                    ) else AnimationUtils.loadAnimation(
                        thiz, R.anim.slide_in_left
                    )
                    cnt.startAnimation(set)
                    val sanim = ObjectAnimator.ofFloat(1.5f, 1f)
                    sanim.setDuration(300)
                    sanim.addUpdateListener { animation ->
                        cnt.scaleX = animation.animatedValue as Float
                        cnt.scaleY = animation.animatedValue as Float
                    }
                    sanim.start()
                    if (!old) {
                        scrollView.postDelayed({ scrollView.fullScroll(View.FOCUS_DOWN) }, 180)
                    }
                    return
                }
                val matrix = Matrix()
                matrix.postScale(0.28f, 0.28f)
                val stk = ImageView(thiz)
                val obit = drawableToBitmap(istr)
                stk.setImageBitmap(
                    Bitmap.createBitmap(
                        obit,
                        0,
                        0,
                        obit.width,
                        obit.height,
                        matrix,
                        true
                    )
                )
                stk.setPadding(4, 4, 4, 4)
                mlayout.addView(stk)
                mlayout2.addView(mlayout)
                if (old) messageContainer!!.addView(mlayout2, 0) else messageContainer!!.addView(
                    mlayout2
                )
                stk.animation = popOut()
                val set: Animation
                if (!old) {
                    set = if (isMine) AnimationUtils.loadAnimation(
                        thiz,
                        R.anim.anim_in
                    ) else AnimationUtils.loadAnimation(
                        thiz, R.anim.anim_in
                    )
                    stk.startAnimation(set)
                }
                val sanim = ObjectAnimator.ofFloat(1f, 0.9f)
                sanim.setDuration(300)
                sanim.addUpdateListener { animation ->
                    stk.scaleX = animation.animatedValue as Float
                    stk.scaleY = animation.animatedValue as Float
                }
                sanim.start()
            } else {
                mlayout.addView(cnt)
                mlayout2.addView(mlayout)
                if (old) messageContainer!!.addView(mlayout2, 0) else messageContainer!!.addView(
                    mlayout2
                )
                //mlayout.setAnimation(fadeout());
                cnt.animation = popOut()
                val set: Animation
                if (old) {
                    set = if (isMine) AnimationUtils.loadAnimation(
                        thiz,
                        R.anim.slide_in_right
                    ) else AnimationUtils.loadAnimation(
                        thiz, R.anim.slide_in_left
                    )
                    cnt.startAnimation(set)
                } else {
                    set = if (isMine) AnimationUtils.loadAnimation(
                        thiz,
                        R.anim.anim_in
                    ) else AnimationUtils.loadAnimation(
                        thiz, R.anim.anim_in
                    )
                    cnt.startAnimation(set)
                }
                val sanim = ObjectAnimator.ofFloat(1.5f, 1f)
                sanim.setDuration(300)
                sanim.addUpdateListener { animation ->
                    cnt.scaleX = animation.animatedValue as Float
                    cnt.scaleY = animation.animatedValue as Float
                }
                sanim.start()
            }
            if (!old) {
                scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
                scrollView.postDelayed({ scrollView.fullScroll(View.FOCUS_DOWN) }, 300)
            }
        }

        @JvmStatic
        fun forceClose() {
            if (!executor.isShutdown) executor.shutdown()
            executor = Executors.newSingleThreadExecutor()
            Singleton.getInstance().cleanUp()
            page = 1
            loadedMessages = ArrayList()
            val intent = Intent(thiz, MainActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            thiz!!.startActivity(intent)
            thiz!!.finish()
        }
    }
}