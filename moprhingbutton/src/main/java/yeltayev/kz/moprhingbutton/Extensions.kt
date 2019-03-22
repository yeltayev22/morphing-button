package yeltayev.kz.moprhingbutton

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.TypedValue
import android.view.WindowManager


fun Context.dpToPx(dp: Float): Float {
    val px = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        this.resources.displayMetrics
    )
    return px
}

fun Context.spToPx(sp: Float): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, this.resources.displayMetrics).toInt()
}

fun Context.screenDimensionsAsPoint(): Point {
    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = windowManager.defaultDisplay
    val point = Point()
    if (Build.VERSION.SDK_INT >= 17) {
        display.getRealSize(point)
    } else {
        display.getSize(point)
    }
    return point
}

fun Context.screenHeight(): Int {
    return screenDimensionsAsPoint().y
}

fun Context.screenWidth(): Int {
    return screenDimensionsAsPoint().x
}

fun Context.drawable(@DrawableRes drawableRes: Int): Drawable? =
    ContextCompat.getDrawable(this, drawableRes)

fun Context.color(@ColorRes colorRes: Int): Int =
    ContextCompat.getColor(this, colorRes)

fun Drawable.toBitmap(width: Int, height: Int): Bitmap {
    val wrapped = DrawableCompat.wrap(this).mutate()

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    wrapped.setBounds(0, 0, canvas.width, canvas.height)
    wrapped.draw(canvas)

    return bitmap
}

fun makeStaticLayout(
    source: CharSequence,
    textPaint: TextPaint,
    width: Int
): StaticLayout {
    // isMarshmallow
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        return StaticLayout.Builder.obtain(source, 0, source.length, textPaint, width)
            .build()
    } else {
        @Suppress("DEPRECATION")
        return StaticLayout(
            source,
            textPaint,
            width,
            Layout.Alignment.ALIGN_NORMAL,
            1f,
            0f,
            false
        )
    }
}

fun Int.pressedColor(color: Int, factor: Float): Int {
    val a = Color.alpha(color)
    val r = Math.round(Color.red(color) * factor)
    val g = Math.round(Color.green(color) * factor)
    val b = Math.round(Color.blue(color) * factor)
    return Color.argb(
        a,
        Math.min(r, 255),
        Math.min(g, 255),
        Math.min(b, 255)
    )
}