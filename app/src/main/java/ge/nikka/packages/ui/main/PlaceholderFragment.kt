package ge.nikka.packages.ui.main

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.text.format.Formatter
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import ge.nikka.packages.ChatActivity
import ge.nikka.packages.ChatActivity.Companion.getAverageColor
import ge.nikka.packages.Handlers
import ge.nikka.packages.Handlers.LoadChats
import ge.nikka.packages.ItemClass
import ge.nikka.packages.MainActivity
import ge.nikka.packages.R
import ge.nikka.packages.Singleton
import ge.nikka.packages.Singleton.Companion.getInstance
import jp.wasabeef.blurry.Blurry
import java.io.File
import java.util.concurrent.Executors

class PlaceholderFragment : Fragment() {
    private var pageViewModel: PageViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ctx = MainActivity.activity
        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java)
        var index = 1
        if (arguments != null) {
            index = requireArguments().getInt(ARG_SECTION_NUMBER)
        }
        pageViewModel!!.setIndex(index)
    }

    private fun dpToPx(dp: Float): Int {
        val r = resources
        return Math.round(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp.toInt()
                    .toFloat(), r.displayMetrics
            )
        )
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        var width = drawable.intrinsicWidth
        width = if (width > 0) width else 1
        var height = drawable.intrinsicHeight
        height = if (height > 0) height else 1
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun formatSize(size: Long): String {
        return Formatter.formatFileSize(ctx, size)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = this.view
        thiz = root
        val sectionNumber = if (arguments != null) requireArguments().getInt(ARG_SECTION_NUMBER) else 1
        val clrr = try {
            getAverageColor(getInstance().getMyPic()!!)
        } catch (e: NullPointerException) {
            Color.valueOf(Color.parseColor("#FF006FFA"))
        }
        horizontalLayout =
            inflater.inflate(R.layout.chat_loading, null).findViewById(R.id.horizontal_layout)
        horizontalLayout!!.setLayoutParams(
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        horizontalLayout!!.setGravity(Gravity.CENTER)
        val ind = horizontalLayout!!.findViewById<CircularProgressIndicator>(R.id.progress_bar)
        ind.setIndicatorColor(clrr.toArgb())
        val layout = LinearLayout(ctx)
        layout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        layout.gravity = Gravity.CENTER
        layout.orientation = LinearLayout.VERTICAL
        llayout = inflater.inflate(R.layout.chats_page, null).findViewById(R.id.chatsPage)
        llayout!!.setLayoutParams(
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        )
        llayout!!.setGravity(Gravity.CENTER)
        llayout!!.setOrientation(LinearLayout.VERTICAL)
        val textView = TextView(ctx)
        val button = MaterialButton(ctx!!)
        apps = ListView(ctx)
        apps!!.layoutParams = LinearLayout.LayoutParams(-1, -1)
        apps!!.setBackgroundColor(Color.TRANSPARENT)
        apps!!.setPadding(18, 45, 18, 150)
        apps!!.divider = null
        apps!!.setSelector(R.drawable.selector)
        apps!!.isVerticalScrollBarEnabled = false
        val editl = LinearLayout(ctx)
        editl.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        editl.gravity = Gravity.TOP
        //editl.setOrientation(LinearLayout.HORIZONTAL);
        editl.setPadding(50, 45, 50, 0)
        val search = EditTextCursorWatcher(ctx)
        search.hint = "Search conversations…"
        search.isSingleLine = true
        //search.setGravity(Gravity.CENTER);
        search.imeOptions = EditorInfo.IME_ACTION_DONE
        search.setBackgroundResource(R.drawable.edit)
        search.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                val txt = search.getText().toString()
                if (txt.length > 0) {
                    val arrayl = ArrayList<ItemClass>()
                    for (i in people.indices) {
                        if (people[i].name.contains(txt)) {
                            arrayl.add(people[i])
                        } else if (people[i].pname.contains(txt)) {
                            arrayl.add(people[i])
                        }
                    }
                    arr = AppsListAdapter(ctx!!, arrayl)
                    sarray = arrayl
                } else {
                    sarray = people
                    arr = AppsListAdapter(ctx!!, people)
                }
                apps!!.adapter = arr
                arr!!.notifyDataSetChanged()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
        editl.addView(search)
        arr = AppsListAdapter(ctx!!, people)
        apps!!.onItemLongClickListener = OnItemLongClickListener { arg0, arg1, position, id ->
            dismissKeyboard(MainActivity.activity)
            Blurry.with(Handlers.currentActivity)
                .radius(10)
                .sampling(2)
                .async()
                .onto(Handlers.currentActivity?.window?.decorView as ViewGroup)
            val dialog: BaseAlertDialogBuilder
            dialog = if (search.getText().toString().length > 0) {
                BaseAlertDialogBuilder(ctx!!, sarray[position])
            } else {
                BaseAlertDialogBuilder(ctx!!, people[position])
            }
            dialog.setCancelable(false)
            dialog.setNegativeButton("CANCEL") { dialog, which -> dialog.dismiss() }
            dialog.setOnDismissListener { Blurry.delete(Handlers.currentActivity!!.window.decorView as ViewGroup) }
            dialog.show()
            val positiveButton = BaseAlertDialogBuilder.dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            val negativeButton = BaseAlertDialogBuilder.dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            val neutralButton = BaseAlertDialogBuilder.dialog.getButton(DialogInterface.BUTTON_NEUTRAL)
            positiveButton.setTextColor(MainActivity.ccolor!!.toArgb())
            negativeButton.setTextColor(MainActivity.ccolor!!.toArgb())
            neutralButton.setTextColor(MainActivity.ccolor!!.toArgb())
            true
        }
        apps!!.isLongClickable = true
        apps!!.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            dismissKeyboard(MainActivity.activity)
            var dialog: BaseAlertDialogBuilder
            if (search.getText().toString().length > 0) {
                getInstance().icon = sarray[position].icon
                getInstance().name = sarray[position].name
                getInstance().uid = sarray[position].path
            } else {
                getInstance().icon = people[position].icon
                getInstance().name = people[position].name
                getInstance().uid = people[position].path
            }
            val intent = Intent(ctx, ChatActivity::class.java)
            ctx!!.startActivity(intent)
        }
        apps!!.adapter = arr

        val st = TextView(ctx)
        st.text =
            "No implementation (yet) :D"
        st.textSize = 16f
        st.gravity = Gravity.CENTER
        st.setTextColor(Color.WHITE)
        if (sectionNumber == 1) {
            llayout!!.addView(editl)
            llayout!!.addView(horizontalLayout)
            fragment = this
            Handler().postDelayed({
                val lchat: Thread = LoadChats()
                if (!exts.isShutdown) exts.shutdown()
                exts = Executors.newSingleThreadExecutor()
                exts.execute(lchat)
            }, 100)
            return llayout
        } else if (sectionNumber == 2) {
            layout.addView(st)
            return layout
        }
        /*
        pageViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                layout.removeAllViews();
                if (s.equals("section1")) {
                    llayout.addView(search);
                    llayout.addView(horizontalLayout);
                    new Handler().postDelayed(() -> {
                        new Handlers.LoadChats(llayout).start();
                    }, 500);
                } else if (s.equals("section2")) {
                    layout.addView(st);
                }
            }
        });*/return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"
        var ctx: Activity? = null
        var exts = Executors.newSingleThreadExecutor()
        fun newInstance(index: Int): PlaceholderFragment {
            val fragment = PlaceholderFragment()
            val bundle = Bundle()
            bundle.putInt(ARG_SECTION_NUMBER, index)
            fragment.setArguments(bundle)
            return fragment
        }

        @JvmStatic
        fun getRoundedCroppedBitmap(bitmap: Bitmap): Bitmap {
            val widthLight = bitmap.width
            val heightLight = bitmap.height
            val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paintColor = Paint()
            paintColor.flags = Paint.ANTI_ALIAS_FLAG
            val rectF = RectF(Rect(0, 0, widthLight, heightLight))
            canvas.drawRoundRect(
                rectF,
                (widthLight / 2).toFloat(),
                (heightLight / 2).toFloat(),
                paintColor
            )
            val paintImage = Paint()
            paintImage.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP))
            canvas.drawBitmap(bitmap, 0f, 0f, paintImage)
            return output
        }

        @JvmField
        var arr: AppsListAdapter? = null
        fun dismissKeyboard(activity: Activity?) {
            val imm =
                activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (null != activity.currentFocus) imm.hideSoftInputFromWindow(
                activity.currentFocus!!.applicationWindowToken, 0
            )
        }

        var thiz: View? = null
        @JvmField
        var people = ArrayList<ItemClass>()
        var sarray = ArrayList<ItemClass>()
        var horizontalLayout: LinearLayout? = null
        @JvmField
        var apps: ListView? = null
        var llayout: LinearLayout? = null
        @JvmField
        var fragment: PlaceholderFragment? = null
    }
}