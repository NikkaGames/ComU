package ge.nikka.packages.ui.main

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import androidx.appcompat.widget.AppCompatEditText

class EditTextCursorWatcher : AppCompatEditText {
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!,
        attrs,
        defStyle
    ) {
        setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val animator = ValueAnimator.ofFloat(1f, 0.95f)
                    animator.setDuration(150)
                    animator.interpolator = DecelerateInterpolator()
                    animator.addUpdateListener { animation: ValueAnimator ->
                        val `val` = animation.animatedValue as Float
                        this@EditTextCursorWatcher.scaleX = `val`
                        this@EditTextCursorWatcher.scaleY = `val`
                    }
                    animator.start()
                }

                MotionEvent.ACTION_UP -> {
                    val animator2 = ValueAnimator.ofFloat(0.95f, 1f)
                    animator2.setDuration(150)
                    animator2.interpolator = DecelerateInterpolator()
                    animator2.addUpdateListener { animation: ValueAnimator ->
                        val `val` = animation.animatedValue as Float
                        this@EditTextCursorWatcher.scaleX = `val`
                        this@EditTextCursorWatcher.scaleY = `val`
                    }
                    animator2.start()
                }
            }
            /*ValueAnimator anim2 = ValueAnimator.ofFloat(1f, 1.025f);
                    anim2.setDuration(100);
                    anim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            EditTextCursorWatcher.this.setScaleX((Float)animation.getAnimatedValue());
                            EditTextCursorWatcher.this.setScaleY((Float)animation.getAnimatedValue());
                        }
                    });
                    anim2.setRepeatCount(1);
                    anim2.setRepeatMode(ValueAnimator.REVERSE);
                    if (event.getAction() == MotionEvent.ACTION_DOWN)    
                    anim2.start();*/false
        }
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val animator = ValueAnimator.ofFloat(1f, 0.95f)
                    animator.setDuration(150)
                    animator.interpolator = DecelerateInterpolator()
                    animator.addUpdateListener { animation: ValueAnimator ->
                        val `val` = animation.animatedValue as Float
                        this@EditTextCursorWatcher.scaleX = `val`
                        this@EditTextCursorWatcher.scaleY = `val`
                    }
                    animator.start()
                }

                MotionEvent.ACTION_UP -> {
                    val animator2 = ValueAnimator.ofFloat(0.95f, 1f)
                    animator2.setDuration(150)
                    animator2.interpolator = DecelerateInterpolator()
                    animator2.addUpdateListener { animation: ValueAnimator ->
                        val `val` = animation.animatedValue as Float
                        this@EditTextCursorWatcher.scaleX = `val`
                        this@EditTextCursorWatcher.scaleY = `val`
                    }
                    animator2.start()
                }
            }
            /*ValueAnimator anim2 = ValueAnimator.ofFloat(1f, 1.025f);
                    anim2.setDuration(100);
                    anim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            EditTextCursorWatcher.this.setScaleX((Float)animation.getAnimatedValue());
                            EditTextCursorWatcher.this.setScaleY((Float)animation.getAnimatedValue());
                        }
                    });
                    anim2.setRepeatCount(1);
                    anim2.setRepeatMode(ValueAnimator.REVERSE);
                    if (event.getAction() == MotionEvent.ACTION_DOWN)    
                    anim2.start();*/false
        }
    }

    constructor(context: Context?) : super(context!!) {
        setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val animator = ValueAnimator.ofFloat(1f, 0.95f)
                    animator.setDuration(150)
                    animator.interpolator = DecelerateInterpolator()
                    animator.addUpdateListener { animation: ValueAnimator ->
                        val `val` = animation.animatedValue as Float
                        this@EditTextCursorWatcher.scaleX = `val`
                        this@EditTextCursorWatcher.scaleY = `val`
                    }
                    animator.start()
                }

                MotionEvent.ACTION_UP -> {
                    val animator2 = ValueAnimator.ofFloat(0.95f, 1f)
                    animator2.setDuration(150)
                    animator2.interpolator = DecelerateInterpolator()
                    animator2.addUpdateListener { animation: ValueAnimator ->
                        val `val` = animation.animatedValue as Float
                        this@EditTextCursorWatcher.scaleX = `val`
                        this@EditTextCursorWatcher.scaleY = `val`
                    }
                    animator2.start()
                }
            }
            /*ValueAnimator anim2 = ValueAnimator.ofFloat(1f, 1.025f);
                    anim2.setDuration(100);
                    anim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            EditTextCursorWatcher.this.setScaleX((Float)animation.getAnimatedValue());
                            EditTextCursorWatcher.this.setScaleY((Float)animation.getAnimatedValue());
                        }
                    });
                    anim2.setRepeatCount(1);
                    anim2.setRepeatMode(ValueAnimator.REVERSE);
                    if (event.getAction() == MotionEvent.ACTION_DOWN)    
                    anim2.start();*/false
        }
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        val anim2 = ValueAnimator.ofFloat(1f, 1.01f)
        anim2.setDuration(50)
        anim2.addUpdateListener { animation ->
            this@EditTextCursorWatcher.scaleX = (animation.animatedValue as Float)
            this@EditTextCursorWatcher.scaleY = (animation.animatedValue as Float)
        }
        anim2.repeatCount = 1
        anim2.repeatMode = ValueAnimator.REVERSE
        anim2.start()
    }
}