package goodtime.training.wod.timer.ui.timer

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator


class CircleProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val backgroundWidth = 7.5f
    private val progressWidth = 7.5f

    private val backgroundPaint = Paint().apply {
        color = Color.DKGRAY
        style = Paint.Style.STROKE
        strokeWidth = backgroundWidth
        isAntiAlias = true
    }

    private val progressPaint = Paint().apply {
        //TODO: refactoring needed for these colors
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = progressWidth
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    var progress: Float = 0f

    fun onTick(newProgress : Float) {
        val animator = ValueAnimator.ofFloat(if (newProgress <= 0F) 0F else progress, newProgress).apply {
            interpolator = LinearInterpolator()
            duration = 600
            addUpdateListener { animation ->
                val animatedVal = animation.animatedValue as Float
                progress = animatedVal
                invalidate()
            }
        }
        animator.start()
    }

    fun setColor(color: Int, darkColor: Int) {
        progressPaint.color = color
        backgroundPaint.color = darkColor
    }

    private val oval = RectF()
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var radius: Float = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        centerX = w.toFloat() / 2
        centerY = h.toFloat() / 2
        radius = w.toFloat() / 2 - progressWidth
        oval.set(centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius)
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawCircle(centerX, centerY, radius, backgroundPaint)
        canvas?.drawArc(oval, 270f, 360f * progress, false, progressPaint)
    }
}