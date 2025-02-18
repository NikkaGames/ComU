package ge.nikka.packages

import android.animation.ValueAnimator
import android.content.Context
import android.content.DialogInterface
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.ScrollView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import jp.wasabeef.blurry.Blurry
import kotlin.math.abs

class SwipeListener : LinearLayout {
    private var downX = 0f
    private var downY = 0f
    private var translationX = 0f
    private var isPressing = false
    private var isSwiping = false
    private var isSwipeEnabled = false
    private var swipeListener: OnSwipeListener? = null
    private var parentScrollView: ScrollView? = null
    var mid: String? = null
    var m: String? = null
    var mine = false

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    fun setOnSwipeListener(listener: OnSwipeListener?) {
        swipeListener = listener
    }

    fun setParentScrollView(scrollView: ScrollView?) {
        parentScrollView = scrollView
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                isSwiping = false
                isSwipeEnabled = false
                if (parentScrollView != null) {
                    parentScrollView!!.requestDisallowInterceptTouchEvent(false)
                }
                isPressing = true
                Handler(Looper.getMainLooper()).postDelayed({
                    if (isPressing) {
                        showLongClickDialog()
                    }
                }, 400)
                val animator = ValueAnimator.ofFloat(1f, 0.95f)
                animator.setDuration(150)
                animator.interpolator = DecelerateInterpolator()
                animator.addUpdateListener { animation: ValueAnimator ->
                    val `val` = animation.animatedValue as Float
                    this.scaleX = `val`
                    this.scaleY = `val`
                }
                animator.start()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.x - downX
                val deltaY = abs((event.y - downY).toDouble()).toFloat()

                if (abs(deltaX.toDouble()) > deltaY && abs(deltaX.toDouble()) > 20) {
                    isPressing = false
                    isSwiping = true
                    translationX += deltaX / 20
                    setTranslationX(translationX)

                    if (abs(translationX.toDouble()) > SWIPE_THRESHOLD * SCROLL_THRESHOLD_RATIO) {
                        isSwipeEnabled = true
                        if (parentScrollView != null && isSwipeEnabled) {
                            parentScrollView!!.requestDisallowInterceptTouchEvent(true)
                        }
                    }
                    return true
                }
            }

            MotionEvent.ACTION_UP -> {
                if (isSwiping) {
                    val totalDeltaX = event.x - downX

                    if (abs(totalDeltaX.toDouble()) > SWIPE_THRESHOLD) {
                        Singleton.getInstance().setReplyTo(this)
                        if (totalDeltaX > 0 && swipeListener != null) {
                            swipeListener!!.onSwipeRight()
                        } else if (swipeListener != null) {
                            swipeListener!!.onSwipeLeft()
                        }
                    }

                    smoothResetPosition()
                }

                if (parentScrollView != null) {
                    parentScrollView!!.requestDisallowInterceptTouchEvent(false)
                }
                isPressing = false
                val animator2 = ValueAnimator.ofFloat(0.95f, 1f)
                animator2.setDuration(150)
                animator2.interpolator = DecelerateInterpolator()
                animator2.addUpdateListener { animation: ValueAnimator ->
                    val `val` = animation.animatedValue as Float
                    this.scaleX = `val`
                    this.scaleY = `val`
                }
                animator2.start()
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                smoothResetPosition()

                if (parentScrollView != null) {
                    parentScrollView!!.requestDisallowInterceptTouchEvent(false)
                }
                isPressing = false
                val animator3 = ValueAnimator.ofFloat(0.95f, 1f)
                animator3.setDuration(150)
                animator3.interpolator = DecelerateInterpolator()
                animator3.addUpdateListener { animation: ValueAnimator ->
                    val `val` = animation.animatedValue as Float
                    this.scaleX = `val`
                    this.scaleY = `val`
                }
                animator3.start()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun smoothResetPosition() {
        val animator = ValueAnimator.ofFloat(translationX, 0f)
        animator.setDuration(200)
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation: ValueAnimator ->
            translationX = animation.animatedValue as Float
            setTranslationX(translationX)
        }
        animator.start()
    }

    private fun showLongClickDialog() {
        val vibrator = ChatActivity.thiz.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 10, 200, 20), -1))
        }
        if (m != null) {
            Blurry.with(Handlers.currentActivity)
                .radius(10)
                .sampling(2)
                .async()
                .onto(Handlers.currentActivity?.window?.decorView as ViewGroup)
            val dialg = MaterialAlertDialogBuilder(context)
                .setTitle("Message Info")
                .setMessage(
                    """
    Message: ${m}
    ID: ${mid}
    Mine: ${mine}
    """.trimIndent()
                )
                .setPositiveButton("OK") { dialog: DialogInterface, which: Int -> dialog.dismiss() }
                .create()
            dialg.setOnDismissListener {
                Blurry.delete(Handlers.currentActivity!!.window.decorView as ViewGroup)
                //fab.setVisibility(View.VISIBLE);
            }
            dialg.show()
            val positiveButton = dialg.getButton(DialogInterface.BUTTON_POSITIVE)
            val negativeButton = dialg.getButton(DialogInterface.BUTTON_NEGATIVE)
            val neutralButton = dialg.getButton(DialogInterface.BUTTON_NEUTRAL)
            positiveButton.setTextColor(ChatActivity.avgColor!!.toArgb())
            negativeButton.setTextColor(ChatActivity.avgColor!!.toArgb())
            neutralButton.setTextColor(ChatActivity.avgColor!!.toArgb())
        }
    }

    interface OnSwipeListener {
        fun onSwipeLeft()
        fun onSwipeRight()
    }

    companion object {
        private const val SWIPE_THRESHOLD = 100
        private const val SCROLL_THRESHOLD_RATIO = 0.2f
    }
}