package com.gagan.wheeldemo

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import java.util.*
import kotlin.math.abs
import kotlin.math.min


class WheelSpinner @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = -1
) :
    View(context, attrs, defStyleAttr, defStyleRes) {

    //Paint Objects
    private var paintOuterCircle: Paint? = null
    private var paintInnerBoundaryCircle: Paint? = null
    private var paintInnerBoundaryCircle2: Paint? = null
    private var paintInnerCircle: Paint? = null
    private var archPaint: Paint? = null
    private var spinnerPaint: Paint? = null
    private var tintTattooPaint: Paint? = null

    //Center Point of Screen Properties
    private var yLocation = 0f
    private var xLocation = 0f

    //Outer Circle Properties
    private var circleRadiusInPX = 400f
    private var circleRadiusInnerInPX = 100f
    private var circleStrokeWidthInPx = 2f
    private val rotationDegrees = 0f
    private var colorCircleStroke = 0

    //Boundary wheel properties
    private var boundaryWheelWidth = 0
    private var boundaryWheelHeight = 0
    private var bitmapTattooWidth = 0
    private var bitmapTattooHeight = 0

    //Inner Arc Properties
    private var arcRectContainer: RectF? = null
    private var spinnerContainer: RectF? = null
    private lateinit var bitmapBoundaryWheel: Bitmap
    private lateinit var bitmapTattoo: Bitmap
    private var isRotating = false
    private var sweepAngle = 0f
    private var middleTattooDegree = 0f
    private var isSliceSelected = false
    private val random = Random()
    private var onItemSelectListener: OnItemSelectListener? =
        null
    private val bitmapTattooList: MutableList<Bitmap?> = ArrayList()
    private var deviceWidth = -1
    private var deviceHeight = -1
    private var ivArrow: ImageView? = null
    private lateinit var rotationAnimator: ValueAnimator

    private fun init() {

        deviceWidth = resources.displayMetrics.widthPixels
        deviceHeight = resources.displayMetrics.heightPixels
        circleRadiusInPX = min(deviceHeight * 0.8f, deviceWidth / 2f)
        circleStrokeWidthInPx = circleRadiusInPX * 0.1f / 3

        //Outer Circle Paint Object initialization
        paintOuterCircle = Paint(Paint.ANTI_ALIAS_FLAG)
        paintOuterCircle!!.color = colorCircleStroke
        paintOuterCircle!!.strokeWidth = circleStrokeWidthInPx
        paintOuterCircle!!.style = Paint.Style.STROKE
        paintInnerBoundaryCircle = Paint(Paint.ANTI_ALIAS_FLAG)
        paintInnerBoundaryCircle!!.color = resources.getColor(R.color.dark_charcoal)
        paintInnerBoundaryCircle!!.strokeWidth = circleStrokeWidthInPx
        paintInnerBoundaryCircle!!.style = Paint.Style.STROKE
        paintInnerCircle = Paint(Paint.ANTI_ALIAS_FLAG)
        paintInnerCircle!!.color = resources.getColor(R.color.color_black)
        paintInnerCircle!!.alpha = 50
        paintInnerCircle!!.strokeWidth = circleStrokeWidthInPx
        paintInnerCircle!!.style = Paint.Style.FILL_AND_STROKE
        paintInnerBoundaryCircle2 = Paint(Paint.ANTI_ALIAS_FLAG)
        paintInnerBoundaryCircle2!!.color = resources.getColor(R.color.color_black)
        paintInnerBoundaryCircle2!!.alpha = 50
        paintInnerBoundaryCircle2!!.strokeWidth = circleStrokeWidthInPx
        paintInnerBoundaryCircle2!!.style = Paint.Style.STROKE
        archPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        archPaint!!.color = Color.WHITE
        archPaint!!.style = Paint.Style.FILL_AND_STROKE
        spinnerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        spinnerPaint!!.color = Color.GRAY
        spinnerPaint!!.style = Paint.Style.STROKE
        spinnerPaint!!.strokeWidth = circleStrokeWidthInPx / 4
        tintTattooPaint = Paint()
        tintTattooPaint!!.colorFilter =
            PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)

        //Initialize Arc Object
        arcRectContainer = RectF()
        spinnerContainer = RectF()
        bitmapTattooWidth = (circleRadiusInPX * 0.1f).toInt()
        bitmapTattooHeight = (circleRadiusInPX * 0.1f).toInt()
        bitmapTattoo = BitmapFactory.decodeResource(resources, R.drawable.tatoo_1)
        bitmapTattoo =
            Bitmap.createScaledBitmap(bitmapTattoo, bitmapTattooWidth, bitmapTattooHeight, false)

        //bitmap for wheel End
        boundaryWheelWidth = (circleRadiusInPX * 0.1f).toInt()
        boundaryWheelHeight = (circleRadiusInPX * 0.1f).toInt()
        bitmapBoundaryWheel = BitmapFactory.decodeResource(resources, R.drawable.wheel_light)
        bitmapBoundaryWheel = Bitmap.createScaledBitmap(
            bitmapBoundaryWheel,
            boundaryWheelWidth,
            boundaryWheelHeight,
            false
        )
        rotationAnimator = ValueAnimator.ofFloat(0f, 1f)
        rotationAnimator.duration = 1
        rotationAnimator.repeatCount = ValueAnimator.INFINITE
        rotationAnimator.interpolator = LinearInterpolator()
    }

    fun setBitmapsId(bitmapsId: List<Int?>) {
        for (bitmapId in bitmapsId) {
            bitmapTattoo = BitmapFactory.decodeResource(resources, bitmapId!!)
            bitmapTattoo = Bitmap.createScaledBitmap(
                bitmapTattoo,
                bitmapTattooWidth,
                bitmapTattooHeight,
                false
            )
            bitmapTattooList.add(bitmapTattoo)
        }
        invalidate()
    }

    fun setArrowPointer(ivArrow: ImageView?) {
        this.ivArrow = ivArrow
        //        setArrowConstraints(ivArrow);
//        invalidate();
    }

    private fun setArrowConstraints(ivArrow: ImageView) {
        val layoutParams = ivArrow.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.circleAngle = 33.75.toFloat()
        layoutParams.width = (circleRadiusInPX * 0.2f).toInt()
        layoutParams.height = (circleRadiusInPX * 0.2f).toInt()
        layoutParams.circleRadius = (circleRadiusInPX + circleStrokeWidthInPx).toInt()
        ivArrow.layoutParams = layoutParams
    }

    fun setOnItemSelectListener(onItemSelectListener: OnItemSelectListener) {
        this.onItemSelectListener = onItemSelectListener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredWidth = measureWidth()
        val measuredHeight = measureHeight()
        yLocation = measuredHeight.toFloat() / 2
        xLocation = measuredWidth.toFloat() / 2
        scaleX = 1.2f
        scaleY = 1.2f
        val translateY = measuredHeight * 0.4f
        translationY = translateY
        if (ivArrow != null) {
            ivArrow!!.translationY = translateY
        }
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //Set Inner Arc Position
        arcRectContainer!![xLocation - circleRadiusInPX, yLocation - circleRadiusInPX, xLocation + circleRadiusInPX] =
            yLocation + circleRadiusInPX
        canvas.save()
        val itemSize = ITEM_SIZE
        sweepAngle = 360 / itemSize.toFloat()
        var tempAngle = 0f
        for (i in 0 until itemSize) {
            archPaint!!.color =
                if (i % 2 == 0) resources.getColor(R.color.color_brown) else resources.getColor(R.color.color_dark_brown)
            var angle = DEFAULT_SELECTED_ANGEL - middleTattooDegree
            if (angle < 0) angle += 360f
            if (isSliceSelected && tempAngle == angle) {
                archPaint!!.color = resources.getColor(R.color.color_black)
            }
            canvas.drawArc(arcRectContainer!!, tempAngle, sweepAngle, true, archPaint!!)
            tempAngle += sweepAngle
        }


        //Rotate and Draw Circle and Inner Arc
        canvas.rotate(rotationDegrees, xLocation, yLocation)
        canvas.drawCircle(xLocation, yLocation, circleRadiusInPX, paintOuterCircle!!)
        canvas.drawCircle(
            xLocation, yLocation, circleRadiusInPX - circleStrokeWidthInPx,
            paintInnerBoundaryCircle!!
        )
        canvas.drawCircle(
            xLocation, yLocation, circleRadiusInPX - 2 * circleStrokeWidthInPx,
            paintInnerBoundaryCircle2!!
        )
        canvas.drawCircle(xLocation, yLocation, circleRadiusInPX * 0.2f, paintInnerCircle!!)
        tempAngle = 0f
        for (i in 0 until itemSize) {
            canvas.save()
            canvas.rotate(tempAngle, xLocation, yLocation)
            canvas.drawBitmap(
                bitmapBoundaryWheel,
                xLocation - boundaryWheelWidth.toFloat() / 2,
                yLocation - circleRadiusInPX - circleStrokeWidthInPx / 2,
                null
            )
            tempAngle += sweepAngle
            canvas.restore()
        }
        tempAngle = sweepAngle / 2
        for (i in 0 until itemSize) {
            canvas.save()
            canvas.rotate(tempAngle, xLocation, yLocation)
            val left = xLocation - bitmapTattooWidth.toFloat() / 2
            val top = yLocation - 0.8f * circleRadiusInPX
            canvas.drawBitmap(
                bitmapTattooList[i % bitmapTattooList.size]!!,
                left,
                top,
                if (i % 2 == 0) null else tintTattooPaint
            )
            tempAngle += sweepAngle
            canvas.restore()
        }
        canvas.restore()
    }

    private fun resetInitialState() {
        animate().setDuration(0)
            .rotation(0f).setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    rotate()
                    clearAnimation()
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
    }

    private fun rotate() {
        val defaultRotationTime = 3000
        // 50 + is done due to item size in not exactly divisible for circle. error removing
        val randomness = random.nextInt(361).toFloat()
        animate().setInterpolator(DecelerateInterpolator())
            .setDuration(defaultRotationTime.toLong())
            .rotation(360 * 5 + randomness)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    isRotating = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    rotateInMiddle(randomness)
                    clearAnimation()
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
            .start()
    }

    private fun rotateInMiddle(randomness: Float) {
        middleTattooDegree = getMiddleElement(randomness)
        val difference = abs(randomness - middleTattooDegree)
        val rotate = if (randomness > middleTattooDegree) -difference else difference
        sleep(250)
        animate().setInterpolator(AccelerateDecelerateInterpolator())
            .setDuration(500)
            .rotationBy(rotate)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    isRotating = false
                    isSliceSelected = true
                    invalidate()
                    onTattooSelected()
                    clearAnimation()
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
            .start()
    }

    private fun sleep(millis: Long) {
        try {
            Thread.sleep(millis)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun onTattooSelected() {
        val index = getCirclePositionFromDegree(middleTattooDegree) % bitmapTattooList.size
        onItemSelectListener?.onTattooSelected(bitmapTattooList[index])
    }

    private fun measureHeight(): Int {
        return (circleRadiusInPX * 2 + circleStrokeWidthInPx + paddingTop + paddingBottom).toInt()
    }

    private fun measureWidth(): Int {
        return (circleRadiusInPX * 2 + circleStrokeWidthInPx + paddingRight + paddingLeft).toInt()
    }

    private fun getCirclePositionFromDegree(middleTattooDegree: Float): Int {
        if (middleTattooDegree == 0f) {
            return 1
        } else if (middleTattooDegree.toDouble() == 22.5) {
            return 0
        } else if (middleTattooDegree == 45f) {
            return 15
        } else if (middleTattooDegree.toDouble() == 67.5) {
            return 14
        } else if (middleTattooDegree == 90f) {
            return 13
        } else if (middleTattooDegree.toDouble() == 112.5) {
            return 12
        } else if (middleTattooDegree == 135f) {
            return 11
        } else if (middleTattooDegree == 157.5f) {
            return 10
        } else if (middleTattooDegree == 180f) {
            return 9
        } else if (middleTattooDegree == 202.5f) {
            return 8
        } else if (middleTattooDegree == 225f) {
            return 7
        } else if (middleTattooDegree == 247.5f) {
            return 6
        } else if (middleTattooDegree == 270f) {
            return 5
        } else if (middleTattooDegree == 292.5f) {
            return 4
        } else if (middleTattooDegree == 315f) {
            return 3
        } else if (middleTattooDegree == 337.5f) {
            return 2
        }
        return 0
    }

    private fun getMiddleElement(randomness: Float): Float {
        if (randomness < 11.25) {
            return 0f
        } else if (randomness < 33.75) {
            return 22.5f
        } else if (randomness < 56.25) {
            return 45f
        } else if (randomness < 78.75) {
            return 67.5f
        } else if (randomness < 101.25) {
            return 90f
        } else if (randomness < 123.75) {
            return 112.5f
        } else if (randomness < 146.25) {
            return 135f
        } else if (randomness < 168.75) {
            return 157.5f
        } else if (randomness < 191.25) {
            return 180f
        } else if (randomness < 213.75) {
            return 202.5f
        } else if (randomness < 236.25) {
            return 225f
        } else if (randomness < 258.75) {
            return 247.5f
        } else if (randomness < 281.25) {
            return 270f
        } else if (randomness < 303.75) {
            return 292.5f
        } else if (randomness < 326.25) {
            return 315f
        } else if (randomness < 348.75) {
            return 337.5f
        }
        return 360f
    }

    private fun dpToPx(value: Float): Float {
        return value * context.resources.displayMetrics.density
    }

    fun rotateWheel() {
        if (isRotating) return
        isSelected = false
        invalidate()
        resetInitialState()
    }

    companion object {
        // Default Properties
        private const val DEFAULT_CIRCLE_STROKE_COLOR = 0xEEEEEE
        private const val DEFAULT_CIRCLE_RADIUS_IN_DP = 70
        private const val DEFAULT_INNER_CIRCLE_RADIUS_IN_DP = 20
        private const val DEFAULT_CIRCLE_STROKE_WIDTH = 20
        const val DEFAULT_SELECTED_ANGEL = 292.5f
        const val ITEM_SIZE = 8
    }

    init {
        if (attrs != null) {
            val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.WheelSpinner)
            try {
                circleRadiusInPX = dpToPx(
                    typedArray.getInt(
                        R.styleable.WheelSpinner_m_circle_radius,
                        DEFAULT_CIRCLE_RADIUS_IN_DP
                    ).toFloat()
                )
                circleRadiusInnerInPX = dpToPx(
                    typedArray.getInt(
                        R.styleable.WheelSpinner_m_circle_inner_radius,
                        DEFAULT_INNER_CIRCLE_RADIUS_IN_DP
                    ).toFloat()
                )
                circleStrokeWidthInPx = dpToPx(
                    typedArray.getInt(
                        R.styleable.WheelSpinner_m_cicle_stroke_width,
                        DEFAULT_CIRCLE_STROKE_WIDTH
                    ).toFloat()
                )
                colorCircleStroke = typedArray.getColor(
                    R.styleable.WheelSpinner_m_circle_stroke_color,
                    DEFAULT_CIRCLE_STROKE_COLOR
                )
            } finally {
                typedArray.recycle()
            }
        }
        init()
    }
}

interface OnItemSelectListener {
    fun onTattooSelected(bitmap: Bitmap?)
}