package ge.nikka.packages

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ge.nikka.packages.ChatActivity.Companion.getAverageColor
import ge.nikka.packages.Singleton.Companion.getInstance
import ge.nikka.packages.ui.main.PlaceholderFragment

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //AXEmojiManager.install(this, new AXAppleEmojiProvider(this));
        setContentView(R.layout.activity_profile)

        supportActionBar!!.setDisplayShowCustomEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        supportActionBar!!.setCustomView(R.layout.profile_bar)
        val view = supportActionBar!!.customView
        avgColor = getAverageColor(getInstance().icon!!)

        val backArrow = view.findViewById<ImageView>(R.id.backArrow2)
        backArrow.setColorFilter(avgColor!!.toArgb(), PorterDuff.Mode.SRC_ATOP)
        backArrow.setOnClickListener { onBackPressed() }
        val lay = view.findViewById<LinearLayout>(R.id.arrowCard2)
        lay.setOnClickListener { onBackPressed() }
        val profilePic = findViewById<ImageView>(R.id.profilePic)
        val user = getInstance().icon
        
        profilePic.setImageBitmap(
            PlaceholderFragment.getRoundedCroppedBitmap(
                Bitmap.createScaledBitmap(
                    user!!, 600, 600, false
                )
            )
        )
        val profileTxt = findViewById<TextView>(R.id.profileTxt)
        if (Handlers.currentActivity?.javaClass?.name?.contains("MainActivity")!!)
            profileTxt.text = getInstance().getMyName()
        else
            profileTxt.text = getInstance().name
    }

    override fun onWindowFocusChanged(arg0: Boolean) {
        super.onWindowFocusChanged(arg0)
        //if (arg0) Handlers.currentActivity = this
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (Handlers.currentActivity?.javaClass?.name?.contains("MainActivity")!!) {
            val intent = Intent(this, MainActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            this.startActivity(intent)
            this.finish()
        }
    }

    companion object {
        var avgColor: Color? = null
    }
}
