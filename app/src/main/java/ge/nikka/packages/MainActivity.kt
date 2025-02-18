package ge.nikka.packages

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import ge.nikka.packages.ChatActivity.Companion.getAverageColor
import ge.nikka.packages.Handlers.NewChat
import ge.nikka.packages.databinding.ActivityMainBinding
import ge.nikka.packages.ui.main.EditTextCursorWatcher
import ge.nikka.packages.ui.main.PlaceholderFragment
import ge.nikka.packages.ui.main.SectionsPagerAdapter
import jp.wasabeef.blurry.Blurry

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private lateinit var tlay: TabLayout
    fun Storage() {
        if (Build.VERSION.SDK_INT < 30 || Environment.isExternalStorageManager()) {
            return
        }
        startActivity(
            Intent(
                "android.settings.MANAGE_APP_ALL_FILES_ACCESS_PERMISSION", Uri.parse(
                    "package:$packageName"
                )
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //DensityUtils.setCustomDensity(this, 405);
        //AXEmojiManager.install(this, new AXAppleEmojiProvider(this));
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }*/activity = this
        //Storage();
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.getRoot())
        setSupportActionBar(binding!!.toolbar)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        binding!!.viewPager.setAdapter(sectionsPagerAdapter)
        binding!!.tabs.setupWithViewPager(binding!!.viewPager)
        bindingRoot = binding!!.getRoot().rootView
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        ccolor = try {
            getAverageColor(Singleton.getInstance().getMyPic()!!)
        } catch (e: NullPointerException) {
            Color.valueOf(Color.parseColor("#FF006FFA"))
        }
        val shapeAppearanceModel = ShapeAppearanceModel()
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, 120f)
            .build()
        val materialShapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
        materialShapeDrawable.fillColor = ColorStateList.valueOf(ccolor!!.toArgb())
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.removeAllViews()
        val pla = LinearLayout(this)
        pla.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        pla.gravity = Gravity.CENTER
        pla.orientation = LinearLayout.HORIZONTAL
        pla.setPadding(-16, 0, 0, 0)
        val pfp = ImageView(this)
        if (Singleton.getInstance().getMyPic() != null) pfp.setImageBitmap(Singleton.getInstance().getMyPic())
        pfp.setBackgroundResource(R.drawable.circular_ripple)
        pfp.scaleX = 0.8f
        pfp.scaleY = 0.8f
        pfp.isClickable = true
        pfp.setPadding(4, 4, 4, 4)
        pfp.setOnClickListener {
            Singleton.getInstance().icon = Singleton.getInstance().getMyPic()
            val intent = Intent(this@MainActivity, ProfileActivity::class.java)
            this@MainActivity.startActivity(intent)
        }
        pla.addView(pfp)
        val label = TextView(this)
        label.text = "ComU"
        label.textSize = 24f
        label.setTypeface(ResourcesCompat.getFont(this, R.font.mfont))
        label.setTextColor(Color.WHITE)
        pla.addView(label)
        val fab = ImageButton(this)
        fab.setImageResource(R.drawable.bxf)
        fab.background = materialShapeDrawable
        val sizeInDp = 40
        val sizeInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            sizeInDp.toFloat(),
            getResources().displayMetrics
        ).toInt()
        val params = FrameLayout.LayoutParams(sizeInPx, sizeInPx)
        params.gravity = Gravity.RIGHT
        params.setMargins(getPercentOfScreenWidthInPx(this, 0.47f), 0, 0, 0)
        fab.layoutParams = params
        //Drawable drawable = fab.getDrawable();
        //Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
        //Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 20, 20, true);
        //fab.setImageBitmap(scaledBitmap);
        fab.scaleType = ImageView.ScaleType.CENTER_CROP
        fab.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        pla.addView(fab)
        toolbar.addView(pla)
        tlay = findViewById(R.id.tabs)
        tlay.setTabRippleColor(ColorStateList.valueOf(ccolor!!.toArgb()))
        tlay.setSelectedTabIndicatorColor(ccolor!!.toArgb())
        tlay.setTabTextColors(Color.LTGRAY, ccolor!!.toArgb())
        tlay.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val tview = tab.customView
                val ivw = tview!!.findViewById<ImageView>(R.id.tabIcon)
                ivw.setColorFilter(ccolor!!.toArgb(), PorterDuff.Mode.SRC_ATOP)
                val tvw = tview.findViewById<TextView>(R.id.tabText)
                tvw.setTextColor(ccolor!!.toArgb())
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val tview = tab.customView
                val ivw = tview!!.findViewById<ImageView>(R.id.tabIcon)
                ivw.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
                val tvw = tview.findViewById<TextView>(R.id.tabText)
                tvw.setTextColor(Color.GRAY)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        for (i in 0 until tlay.getTabCount()) {
            val tab = tlay.getTabAt(i)
            val tname = tab!!.text.toString()
            if (tname == "Chats") {
                val tview = layoutInflater.inflate(R.layout.tab_content, null)
                val ivw = tview.findViewById<ImageView>(R.id.tabIcon)
                ivw.setImageResource(R.drawable.chats)
                ivw.setColorFilter(ccolor!!.toArgb(), PorterDuff.Mode.SRC_ATOP)
                val tvw = tview.findViewById<TextView>(R.id.tabText)
                tvw.text = "Chats"
                tvw.setTextColor(ccolor!!.toArgb())
                tab.setCustomView(tview)
            } else {
                val tview = layoutInflater.inflate(R.layout.tab_content, null)
                val ivw = tview.findViewById<ImageView>(R.id.tabIcon)
                ivw.setImageResource(R.drawable.people)
                ivw.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
                val tvw = tview.findViewById<TextView>(R.id.tabText)
                tvw.text = "People"
                tvw.setTextColor(Color.GRAY)
                tab.setCustomView(tview)
            }
        }
        val states = arrayOf(
            intArrayOf(android.R.attr.state_selected),
            intArrayOf(-android.R.attr.state_selected)
        )
        val colors = intArrayOf(
            ccolor!!.toArgb(),
            Color.GRAY
        )
        val colorStateList = ColorStateList(states, colors)
        tlay.setTabIconTint(colorStateList)
        fab.setOnLongClickListener { view ->
            Toast.makeText(view.context, "Start new chat…", Toast.LENGTH_SHORT).show()
            true
        }
        fab.setOnClickListener { //fab.setVisibility(View.INVISIBLE);
            Blurry.with(Handlers.currentActivity)
                .radius(10)
                .sampling(2)
                .async()
                .onto(Handlers.currentActivity?.window?.decorView as ViewGroup)
            val dview = this@MainActivity.layoutInflater.inflate(R.layout.new_chat, null)
            val dedit = dview.findViewById<EditTextCursorWatcher>(R.id.userId)
            dedit.isSingleLine = true
            dedit.imeOptions = EditorInfo.IME_ACTION_DONE
            dedit.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO || event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    NewChat(dedit.text.toString().replace(" ", "").replace("\n", "")).start()
                    return@OnEditorActionListener true
                }
                false
            })
            val bld = MaterialAlertDialogBuilder(Handlers.currentActivity!!)
            bld.setTitle("New chat").setView(dview).setCancelable(true)
                .setPositiveButton("OK") { dialog, which ->
                    NewChat(
                        dedit.text.toString().replace(" ", "").replace("\n", "")
                    ).start()
                }
                .setNeutralButton("CANCEL") { dialog, which -> dialog.dismiss() }
                .setOnDismissListener {
                    Blurry.delete(Handlers.currentActivity?.window?.decorView as ViewGroup)
                    //fab.setVisibility(View.VISIBLE);
                }
            val dialg = bld.create()
            dialg.show()
            val positiveButton = dialg.getButton(DialogInterface.BUTTON_POSITIVE)
            val negativeButton = dialg.getButton(DialogInterface.BUTTON_NEGATIVE)
            val neutralButton = dialg.getButton(DialogInterface.BUTTON_NEUTRAL)
            positiveButton.setTextColor(ccolor!!.toArgb())
            negativeButton.setTextColor(ccolor!!.toArgb())
            neutralButton.setTextColor(ccolor!!.toArgb())
            dedit.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO || event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    dialg.dismiss()
                    NewChat(dedit.text.toString().replace(" ", "").replace("\n", "")).start()
                    return@OnEditorActionListener true
                }
                false
            })
            dedit.requestFocus()
            dedit.postDelayed({ Handlers.showKeyboard(dedit) }, 150)
        }
        if (!ServerService.isRunning) startService(Intent(this, ServerService::class.java)) else {
            if (!SocketService.isconnected) {
                stopService(Intent(this, ServerService::class.java))
                startService(Intent(this, ServerService::class.java))
            }
        }
    }

    override fun onBackPressed() {
        var chats: TabLayout.Tab? = null
        for (i in 0 until tlay!!.tabCount) {
            val tab = tlay!!.getTabAt(i)
            val tname = tab!!.text.toString()
            if (tname == "Chats") {
                chats = tab
            }
        }
        val ctab = tlay!!.getTabAt(tlay!!.selectedTabPosition)
        if (ctab!!.text.toString() == "People") {
            if (chats != null) {
                chats.select()
                return
            }
        }
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        Handlers.currentActivity?.finish()
    }

    override fun onPause() {
        super.onPause()
        isFocused = false
        finish()
    }

    /*
    @Override
    protected void onResume() {
        super.onResume();
        if (!MainActivity.isFocused) return;
        Thread lchat = new Handlers.RefreshChats();
        if (!MainActivity.isFocused) return;
        if (!PlaceholderFragment.exts.isShutdown()) PlaceholderFragment.exts.shutdown();
        PlaceholderFragment.exts = Executors.newSingleThreadExecutor();
        if (!MainActivity.isFocused) return;
        PlaceholderFragment.exts.execute(lchat);
    }*/
    override fun onWindowFocusChanged(arg0: Boolean) {
        super.onWindowFocusChanged(arg0)
        isFocused = arg0
        if (arg0) {
            Handlers.currentActivity = this
            Handlers.setStatus("2")
        } else {
            Handlers.setStatus("1")
        }
    }

    companion object {
        @JvmField
        var activity: Activity? = null
        @JvmField
        var isFocused = false
        @JvmField
        var bindingRoot: View? = null
        @JvmField
        var ccolor: Color? = null
        var statusBarHeight = 12f
        fun getStatusBarHeight(context: Activity): Int {
            val resources = context.resources
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
        }

        fun getPercentOfScreenWidthInPx(
            context: Context,
            percent: Float
        ): Int {
            val displayMetrics = context.resources.displayMetrics
            val screenWidthPx = displayMetrics.widthPixels
            return (screenWidthPx * percent).toInt()
        }
    }
}