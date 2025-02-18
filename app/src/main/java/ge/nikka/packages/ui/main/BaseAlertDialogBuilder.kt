package ge.nikka.packages.ui.main

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.text.format.Formatter
import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ge.nikka.packages.ItemClass
import ge.nikka.packages.MainActivity
import ge.nikka.packages.MainActivity.Companion.ccolor

class BaseAlertDialogBuilder : MaterialAlertDialogBuilder {
    private var m_item: ItemClass
    private var ctx: Context

    constructor(context: Context, item: ItemClass) : super(context) {
        ctx = context
        m_item = item
    }

    constructor(context: Context, overrideThemeResId: Int, item: ItemClass) : super(
        context,
        overrideThemeResId
    ) {
        ctx = context
        m_item = item
    }

    override fun create(): AlertDialog {
        dialog = super.create()
        dialog.window!!.setDimAmount(0.2f)
        dialog.setTitle("Conversation: " + m_item.name)
        val layout = LinearLayout(ctx)
        layout.layoutParams = LinearLayout.LayoutParams(-1, -2)
        layout.orientation = LinearLayout.VERTICAL
        val layoutt = LinearLayout(ctx)
        layoutt.layoutParams = LinearLayout.LayoutParams(-1, -2)
        layoutt.orientation = LinearLayout.HORIZONTAL
        layoutt.setPadding(35, 25, 0, 0)
        val view = ImageView(ctx)
        view.setImageBitmap(m_item.icon)
        val sizeInDp = 55
        val sizeInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            sizeInDp.toFloat(),
            MainActivity.activity?.getResources()?.displayMetrics
        ).toInt()
        val pparams = LinearLayout.LayoutParams(sizeInPx, sizeInPx)
        pparams.gravity = Gravity.CENTER or Gravity.RIGHT
        pparams.setMargins(35, 0, 0, 0)
        view.layoutParams = pparams
        val text = TextView(ctx)
        text.text = m_item.name
        text.setPadding(25, 25, 0, 0)
        text.textSize = 17f
        text.setTextColor(Color.WHITE)
        val info = TextView(ctx)
        info.text = "No implementation (yet) :D"
        info.textSize = 12f
        info.setPadding(70, 30, 0, 0)
        layoutt.addView(view)
        layoutt.addView(text)
        layout.addView(layoutt)
        layout.addView(info)
        dialog.setView(layout)
        // dialog.getWindow().setBackgroundDrawable(new BitmapDrawable(MainActivity.activity.getResources(), blur(MainActivity.activity, captureScreenShot(MainActivity.activity))));
        return dialog
    }

    companion object {
        lateinit var dialog: AlertDialog
    }
}