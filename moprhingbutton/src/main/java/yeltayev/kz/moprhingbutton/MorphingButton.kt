package yeltayev.kz.moprhingbutton

/**
 *  Created by Nurdaulet Yeltayev
 *  https://github.com/yeltayev22
 *  Copyright 2019
 */

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.support.v4.graphics.drawable.DrawableCompat
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import e.kz.moprhingbutton.R

class MorphingButton
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    View(context, attrs, defStyle) {

    private val screenWidth = context.screenWidth()
    private val buttonCornerRadius = context.dpToPx(BUTTON_CORNER_RADIUS_DP)
    private var buttonHeight = context.dpToPx(BUTTON_HEIGHT_DP)
    private var circularButtonSize = context.dpToPx(CIRCULAR_BUTTON_SIZE_DP)
    private var iconSize = context.dpToPx(ICON_SIZE_DP)

    private val minusIcon = context.drawable(R.drawable.ic_minus_white)?.let { DrawableCompat.wrap(it).mutate() }
    private val plusIcon = context.drawable(R.drawable.ic_add_white)?.toBitmap(iconSize.toInt(), iconSize.toInt())

    private val rect = RectF()
    private val leftRect = RectF()
    private val rightRect = RectF()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = context.color(R.color.colorPrimaryDark)
    }
    private val plusIconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private val ticketTypeTextPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
        color = context.color(R.color.white)
        textSize = context.spToPx(16F).toFloat()
    }

    private val ticketQuantityTextPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
        color = context.color(R.color.black)
        textAlign = Paint.Align.CENTER
        textSize = context.spToPx(16F).toFloat()
    }

    private val rightButtonWidthAnimator = object : AnimatorConfig() {
        override fun getStartValue(): Float = measuredWidth * 2f / 3f + buttonCornerRadius * 2
        override fun getEndValue(): Float = measuredWidth / 2f
    }

    private val leftButtonWidthAnimator = object : AnimatorConfig() {
        override fun getStartValue(): Float = measuredWidth / 3f + buttonCornerRadius * 2
        override fun getEndValue(): Float = circularButtonSize
    }

    private val leftButtonRadiusAnimator = object : AnimatorConfig() {
        override fun getStartValue(): Float = buttonCornerRadius
        override fun getEndValue(): Float = circularButtonSize / 2
    }

    private val leftButtonHeightAnimator = object : AnimatorConfig() {
        override fun getStartValue(): Float = buttonHeight
        override fun getEndValue(): Float = circularButtonSize
    }

    private val plusIconPositionAnimator = object : AnimatorConfig() {
        override fun getStartValue(): Float = measuredWidth / 2f
        override fun getEndValue(): Float = measuredWidth / 2f + iconSize / 2f
    }

    private val plusIconRotationAnimator = object : AnimatorConfig() {
        override fun getStartValue(): Float = -90f
        override fun getEndValue(): Float = 90f
    }

    private val alphaAnimator = object : AnimatorConfig() {
        override fun getStartValue(): Float = 0f
        override fun getEndValue(): Float = 255f
    }

    private val ticketTypeTextAnimator = object : AnimatorConfig() {
        override fun getStartValue(): Float = measuredWidth / 2f
        override fun getEndValue(): Float = measuredWidth / 2f + rightButtonWidthAnimator.getAnimatedValue() / 4f
    }

    private val animator = AnimatorSet().apply {
        duration = 150
        interpolator = DecelerateInterpolator(1.5f)

        rightButtonWidthAnimator.animator.addUpdateListener {
            invalidate()
        }

        playTogether(
            rightButtonWidthAnimator.animator,
            leftButtonWidthAnimator.animator,
            leftButtonRadiusAnimator.animator,
            leftButtonHeightAnimator.animator,
            ticketTypeTextAnimator.animator,
            plusIconPositionAnimator.animator,
            plusIconRotationAnimator.animator,
            alphaAnimator.animator
        )
    }

    private var buttonColor = context.color(R.color.colorPrimaryDark)

    private var isMorphed = false
    private var isLeftButtonPressed = false
    private var isRightButtonPressed = false

    private var ticketQuantityLabel: CharSequence? = null
    private var ticketTypeLabel: CharSequence? = null

    private var ticketQuantityLabelLayout: StaticLayout? = null
    private var ticketTypeLabelLayout: StaticLayout? = null

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MorphingButton, 0, 0)
        try {
            ticketTypeLabel = a.getString(R.styleable.MorphingButton_text)
            buttonColor = a.getColor(R.styleable.MorphingButton_color, buttonColor)
            buttonHeight = a.getDimension(R.styleable.MorphingButton_button_height, buttonHeight)
            circularButtonSize = a.getDimension(R.styleable.MorphingButton_circular_button_height, circularButtonSize)
            iconSize = a.getDimension(R.styleable.MorphingButton_icon_size, iconSize)
        } finally {
            a.recycle()
        }
    }

    private var ticketQuantity = 0
        set(value) {
            if (field != value) {
                field = value

                ticketQuantityLabel = ticketQuantity.toString()

                updateTextLayouts()

                if (value == 0 || !isMorphed) {
                    isMorphed = !isMorphed
                    updateAnimators()
                    if (!animator.isRunning) {
                        animator.start()
                    }
                }

                invalidate()
            }
        }

    private fun updateTextLayouts() {
        val ticketQuantityLabel = ticketQuantityLabel
        if (ticketQuantityLabel != null && measuredWidth > 0) {
            val labelWidth = screenWidth -
                    rightButtonWidthAnimator.getEndValue() -
                    leftButtonWidthAnimator.getEndValue() -
                    (buttonHeight - leftButtonHeightAnimator.getEndValue()) / 2f

            ticketQuantityLabelLayout = makeStaticLayout(
                ticketQuantityLabel,
                ticketQuantityTextPaint,
                labelWidth.toInt()
            )
        }

        val ticketTypeLabel = ticketTypeLabel
        if (ticketTypeLabel != null && measuredWidth > 0) {
            val labelWidth = rightButtonWidthAnimator.getEndValue() - (plusIcon?.width ?: 0)

            ticketTypeLabelLayout = makeStaticLayout(
                ticketTypeLabel,
                ticketTypeTextPaint,
                labelWidth.toInt()
            )
        }
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            resolveSize(screenWidth, widthMeasureSpec),
            resolveSize(buttonHeight.toInt(), heightMeasureSpec)
        )

        updateTextLayouts()
        updateAnimators()
    }

    private fun updateAnimators() {
        rightButtonWidthAnimator.updateAnimatorValues(isMorphed)
        leftButtonWidthAnimator.updateAnimatorValues(isMorphed)
        leftButtonRadiusAnimator.updateAnimatorValues(isMorphed)
        leftButtonHeightAnimator.updateAnimatorValues(isMorphed)
        ticketTypeTextAnimator.updateAnimatorValues(isMorphed)
        plusIconPositionAnimator.updateAnimatorValues(isMorphed)
        plusIconRotationAnimator.updateAnimatorValues(isMorphed)
        alphaAnimator.updateAnimatorValues(isMorphed)

        if (!isMorphed) {
            ticketTypeTextPaint.textAlign = Paint.Align.CENTER
        } else {
            ticketTypeTextPaint.textAlign = Paint.Align.LEFT
        }
    }

    override fun onDraw(canvas: Canvas) {
        drawTicketQuantityText(canvas)
        drawLeftButton(canvas)
        drawRightButton(canvas)
        drawTicketTypeText(canvas)
    }

    private fun drawLeftButton(canvas: Canvas) {
        val leftButtonWidth = leftButtonWidthAnimator.getAnimatedValue()
        val leftButtonRadius = leftButtonRadiusAnimator.getAnimatedValue()
        val leftButtonHeight = leftButtonHeightAnimator.getAnimatedValue()
        val leftButtonPadding = (measuredHeight - leftButtonHeightAnimator.getAnimatedValue()) / 2

        rect.set(
            leftButtonPadding,
            leftButtonPadding,
            leftButtonPadding + leftButtonWidth,
            leftButtonPadding + leftButtonHeight
        )

        paint.color = getButtonColor(isLeftButtonPressed)
        canvas.drawRoundRect(rect, leftButtonRadius, leftButtonRadius, paint)
        leftRect.set(rect)

        if (isMorphed) {
            minusIcon?.let {
                val y = (measuredHeight - iconSize) / 2
                val x = y

                minusIcon.setBounds(x.toInt(), y.toInt(), ((x + iconSize).toInt()), ((y + iconSize).toInt()))
                minusIcon.draw(canvas)
            }
        }
    }

    private fun drawRightButton(canvas: Canvas) {
        val rightButtonWidth = rightButtonWidthAnimator.getAnimatedValue()
        val rightButtonX = measuredWidth - rightButtonWidth

        rect.set(rightButtonX, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())

        paint.color = getButtonColor(isRightButtonPressed)
        canvas.drawRoundRect(rect, buttonCornerRadius, buttonCornerRadius, paint)
        rightRect.set(rect)

        if (plusIcon != null) {
            val x = plusIconPositionAnimator.getAnimatedValue().toInt()
            val y = (measuredHeight - iconSize) / 2
            val rotation = plusIconRotationAnimator.getAnimatedValue()
            val alpha = alphaAnimator.getAnimatedValue().toInt()

            canvas.save()
            canvas.rotate(rotation, x + iconSize / 2f, y + iconSize / 2f)
            plusIconPaint.alpha = alpha
            canvas.drawBitmap(plusIcon, x.toFloat(), y, plusIconPaint)
            canvas.restore()
        }
    }

    private fun getButtonColor(isPressed: Boolean): Int {
        return if (isPressed) {
            buttonColor.pressedColor(buttonColor, 0.8f)
        } else {
            buttonColor
        }
    }

    private fun drawTicketTypeText(canvas: Canvas) {
        ticketTypeLabelLayout?.let { textLayout ->
            val x = ticketTypeTextAnimator.getAnimatedValue()
            val y = (measuredHeight - textLayout.height) / 2f
            canvas.save()
            canvas.translate(x, y)
            textLayout.draw(canvas)
            canvas.restore()
        }
    }

    private fun drawTicketQuantityText(canvas: Canvas) {
        val alpha = alphaAnimator.getAnimatedValue().toInt()

        if (alpha > 0) {
            ticketQuantityTextPaint.alpha = alpha

            ticketQuantityLabelLayout?.let { textLayout ->
                val leftButtonWidth = leftButtonWidthAnimator.getAnimatedValue()
                val rightButtonWidth = rightButtonWidthAnimator.getAnimatedValue()
                val ticketTypeTextWidth = measuredWidth - leftButtonWidth - rightButtonWidth
                val x = ticketTypeTextWidth / 2f + leftButtonWidth
                val y = (measuredHeight - textLayout.height) / 2f
                canvas.save()
                canvas.translate(x, y)
                textLayout.draw(canvas)
                canvas.restore()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!isMorphed) {
                    isLeftButtonPressed = true
                    isRightButtonPressed = true
                } else {
                    if (leftRect.contains(x, y)) {
                        isLeftButtonPressed = true
                    } else if (rightRect.contains(x, y)) {
                        isRightButtonPressed = true
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (!isMorphed && (isLeftButtonPressed || isRightButtonPressed)) {
                    ticketQuantity++
                } else {
                    if (leftRect.contains(x, y)) {
                        ticketQuantity--
                    } else if (rightRect.contains(x, y)) {
                        ticketQuantity++
                    }
                }

                isLeftButtonPressed = false
                isRightButtonPressed = false
            }
            MotionEvent.ACTION_CANCEL -> {
                isLeftButtonPressed = false
                isRightButtonPressed = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isMorphed) {
                    if (!leftRect.contains(x, y) && !rightRect.contains(x, y)) {
                        isLeftButtonPressed = false
                        isRightButtonPressed = false
                    }
                } else {
                    if (!leftRect.contains(x, y)) {
                        isLeftButtonPressed = false
                    }
                    if (!rightRect.contains(x, y)) {
                        isRightButtonPressed = false
                    }
                }
            }
        }

        invalidate()
        return true
    }

    private abstract class AnimatorConfig {

        val animator: ValueAnimator = ValueAnimator()

        abstract fun getStartValue(): Float
        abstract fun getEndValue(): Float

        fun updateAnimatorValues(isMorphed: Boolean) {
            if (isMorphed) {
                animator.setFloatValues(getStartValue(), getEndValue())
            } else {
                animator.setFloatValues(getEndValue(), getStartValue())
            }
        }

        fun getAnimatedValue(): Float {
            val value = animator.animatedValue as Float
            if (value == 0f) {
                return getStartValue()
            }
            return value
        }
    }
}

private const val BUTTON_HEIGHT_DP = 80f
private const val CIRCULAR_BUTTON_SIZE_DP = 60f
private const val BUTTON_CORNER_RADIUS_DP = 4f

private const val ICON_SIZE_DP = 30f