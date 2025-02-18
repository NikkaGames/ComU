package ge.nikka.packages.ui.main

import android.content.Context
import android.graphics.Color
import android.text.Html
import android.text.format.DateFormat
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import ge.nikka.packages.ItemClass
import ge.nikka.packages.LoginActivity
import ge.nikka.packages.MainActivity
import ge.nikka.packages.R
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

class AppsListAdapter(var ctx: Context, items: ArrayList<ItemClass>) : BaseAdapter() {
    private var itsems: MutableList<ItemClass>
    private var size = 0

    init {
        itsems = items
    }

    private fun formatSize(size: Long): String {
        return Formatter.formatFileSize(ctx, size)
    }

    override fun notifyDataSetChanged() {
        size = itsems.size
        super.notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return size
    }

    val value: List<ItemClass>
        get() = itsems

    fun clear() {
        itsems.clear()
    }

    fun setValue(arg: ArrayList<ItemClass>) {
        itsems = arg
    }

    override fun getItem(position: Int): Any {
        return try {
            itsems[position]
        } catch (e: Error) {
            LoginActivity.logger!!.append(e.toString())
            ItemClass()
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun is24Hour(): Boolean {
        return DateFormat.is24HourFormat(ctx)
    }

    fun formatTimestamp(timestampSeconds: Long): String {
        val dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochSecond(timestampSeconds),
            ZoneId.systemDefault()
        )
        val currentDate = LocalDate.now()
        val weekFields = WeekFields.of(Locale.getDefault())
        val currentWeek = currentDate[weekFields.weekOfWeekBasedYear()]
        val timestampWeek = dateTime.toLocalDate()[weekFields.weekOfWeekBasedYear()]
        return if (dateTime.toLocalDate().year != currentDate.year) {
            val yearFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
            dateTime.format(yearFormatter)
        } else if (timestampWeek == currentWeek && !dateTime.toLocalDate()
                .isEqual(currentDate)
        ) {
            val dayNameFormatter = DateTimeFormatter.ofPattern("EEEE")
            dateTime.format(dayNameFormatter)
        } else if (!dateTime.toLocalDate().isEqual(currentDate)) {
            val dayFormatter = DateTimeFormatter.ofPattern("dd MMMM")
            dateTime.format(dayFormatter)
        } else {
            val timeFormatter: DateTimeFormatter
            timeFormatter = if (is24Hour()) {
                DateTimeFormatter.ofPattern("HH:mm")
            } else {
                DateTimeFormatter.ofPattern("hh:mm a")
            }
            dateTime.format(timeFormatter)
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater =
            MainActivity.activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(R.layout.custom_list_item, parent, false)
        val textView1 = rowView.findViewById<TextView>(R.id.textView1)
        val textView2 = rowView.findViewById<TextView>(R.id.textView2)
        val textView3 = rowView.findViewById<TextView>(R.id.textView3)
        //TextView textView4 = rowView.findViewById(ge.nikka.packages.R.id.sizeText);
        val imageView = rowView.findViewById<ImageView>(R.id.imageView)
        textView1.text = itsems[position].name
        textView1.setTextColor(Color.WHITE)
        textView1.textSize = 17.0f
        textView3.text = formatTimestamp(itsems[position].time)
        textView3.textSize = 12.0f
        textView3.setPadding(0, 0, 4, 2)
        textView3.setTextColor(Color.LTGRAY)
        if (itsems[position].isread) {
            textView2.setTypeface(ResourcesCompat.getFont(ctx, R.font.googlereg))
            if (itsems[position].isme) {
                textView2.text = "You: " + itsems[position].pname
            } else {
                textView2.text = itsems[position].pname
            }
        } else {
            textView2.setTypeface(ResourcesCompat.getFont(ctx, R.font.googlebold))
            if (itsems[position].isme) {
                textView2.text = Html.fromHtml("You: " + itsems[position].pname)
            } else {
                textView2.text = Html.fromHtml("New: " + itsems[position].pname)
            }
        }
        textView2.textSize = 14.0f
        textView2.setPadding(0, 4, 0, 0)
        textView2.setTextColor(Color.LTGRAY)
        //textView3.setText("Version: " + itsems.get(position).versionName);
        // textView3.setTextSize(11.5f);
        //  textView4.setText("APK Size: " + formatSize(new File(itsems.get(position).path).length()));
        //  textView4.setTextSize(10.0f);
        imageView.setImageBitmap(itsems[position].icon)
        imageView.scaleX = 0.77f
        imageView.scaleY = 0.77f
        return rowView
    }
}