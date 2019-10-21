package com.alextodoran.blur

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.TypedValue
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        private const val MIN_BLUR_ITERATIONS = 0
        private const val MAX_BLUR_ITERATIONS = 4
        private const val SQUARE_TRANSPARENCY_LEVEL = 0.85F
        private const val BLUR_RADIUS = 25F

        private const val REGULAR_INDEX = 0
        private const val LIGHT_INDEX = 1
        private const val DARK_INDEX = 2

        private const val DELAY_UTIL_READ_VIEW_SIZE = 300L
    }

    var colorIndex: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupSquares()
        createScreenshot(backgroundImageContainer)
    }

    private fun setupSquares() {
        val squareWidth =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 170f, resources.displayMetrics)
        val squareHeight =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150f, resources.displayMetrics)

        val layoutParams = RelativeLayout.LayoutParams(squareWidth.toInt(), squareHeight.toInt())
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)

        blurredSquare.layoutParams = layoutParams

        transparentSquare.alpha =
            SQUARE_TRANSPARENCY_LEVEL
        transparentSquare.layoutParams = layoutParams
        transparentSquare.setBackgroundColor(
            ContextCompat.getColor(
                this,
                android.R.color.transparent
            )
        )

        transparentSquare.setOnClickListener {
            if (colorIndex == DARK_INDEX) {
                onSquareClick(colorIndex)
                colorIndex = -1
            } else {
                onSquareClick(colorIndex)
            }
            colorIndex++
        }

    }

    private fun onSquareClick(index: Int) {
        when (index) {
            REGULAR_INDEX -> transparentSquare.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    android.R.color.transparent
                )
            )
            LIGHT_INDEX -> transparentSquare.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    android.R.color.background_light
                )
            )
            DARK_INDEX -> transparentSquare.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    android.R.color.background_dark
                )
            )
        }
    }

    private fun blurSquare(initialBitmap: Bitmap) {

        val squareWidth =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 170f, resources.displayMetrics)
        val squareHeight =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150f, resources.displayMetrics)

        val startX = blurredSquare.left
        val startY = blurredSquare.top

        val initialBitmapResized = Bitmap.createBitmap(
            initialBitmap,
            startX, startY,
            squareWidth.toInt(),
            squareHeight.toInt(),
            null,
            false
        )


        val renderScript = RenderScript.create(this)

        val blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        blurScript.setRadius(BLUR_RADIUS)

        for (blurIteration in MIN_BLUR_ITERATIONS..MAX_BLUR_ITERATIONS) {
            val allIn = Allocation.createFromBitmap(renderScript, initialBitmapResized)
            val allOut = Allocation.createFromBitmap(renderScript, initialBitmapResized)

            blurScript.setInput(allIn)
            blurScript.forEach(allOut)

            allOut.copyTo(initialBitmapResized)
        }

        initialBitmap.recycle()

        renderScript.destroy()

        blurredSquare.setImageBitmap(initialBitmapResized)

        root.visibility = View.VISIBLE
    }

    private fun createScreenshot(root: View) {
        Handler().postDelayed(
            {
                val bitmap = Bitmap.createBitmap(
                    backgroundImageContainer.width,
                    backgroundImageContainer.height,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                root.draw(canvas)
                blurSquare(bitmap)
            },
            DELAY_UTIL_READ_VIEW_SIZE
        )
    }
}
