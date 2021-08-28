package com.example.deltahackathon

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import java.lang.Thread.sleep
import java.util.*
import java.util.logging.Handler
import kotlin.concurrent.thread


class GameView(context: Context, attrs: AttributeSet) : View(context, attrs)  {
    lateinit var animation: AnimationDrawable
    lateinit var playerBitmap:Bitmap
    var touchX = 0f
    var touchY = 0f
    var playerY = 0f
    val playerWidth = 364f
    val playerHeight = 457f
    var playerJumping = false
    var playerSliding = false
    var playerRunState = 1
    var msalX = 0f
    val msalWidth = 432f
    val msalHeight = 518f
    var msalRunState = 1
    val gravity = -5
    var jumpV = 30
    var groundX = 0f
    lateinit var msalBitmap: Bitmap
    lateinit var groundBitmap: Bitmap
    private val paintWhite: Paint = Paint()
    private var GAME_RUNNING = true

    private fun dpToPx(dp: Int): Float {
        val r = resources
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            r.getDisplayMetrics())).toFloat()
    }

    private fun getBitmap(vectorDrawable: VectorDrawable): Bitmap? {
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return bitmap
    }
    private fun getBitmap(context: Context, drawableId: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, drawableId)
        return if (drawable is BitmapDrawable) {
            BitmapFactory.decodeResource(context.resources, drawableId)
        } else if (drawable is VectorDrawable) {
            getBitmap(drawable)
        } else {
            throw IllegalArgumentException("unsupported drawable type")
        }
    }

    init {
        paintWhite.color =Color.WHITE
        paintWhite.style =Paint.Style.FILL
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        msalX = width - msalWidth + 50f
        if (::playerBitmap.isInitialized)playerBitmap.recycle()
        if (::msalBitmap.isInitialized)msalBitmap.recycle()
        playerBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        msalBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        groundBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        playerBitmap = getBitmap(context, R.drawable.ic_r1f1)!!
        msalBitmap = getBitmap(context, R.drawable.ic_msal1)!!
        groundBitmap = getBitmap(context, R.drawable.ic_ground)!!
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(groundBitmap, groundX, height - 205f, paintWhite)
        canvas?.drawBitmap(groundBitmap, dpToPx(100) + groundX, height - 205f, paintWhite)
        canvas?.drawBitmap(groundBitmap, dpToPx(200) + groundX, height - 205f, paintWhite)
        canvas?.drawBitmap(groundBitmap, dpToPx(300) + groundX, height - 205f, paintWhite)
        canvas?.drawBitmap(groundBitmap, dpToPx(400) + groundX, height - 205f, paintWhite)
        canvas?.drawBitmap(groundBitmap, dpToPx(500) + groundX, height - 205f, paintWhite)
        canvas?.drawBitmap(groundBitmap, dpToPx(600) + groundX, height - 205f, paintWhite)
        canvas?.drawBitmap(groundBitmap, dpToPx(700) + groundX, height - 205f, paintWhite)
        canvas?.drawBitmap(groundBitmap, dpToPx(800) + groundX, height - 205f, paintWhite)
        canvas?.drawBitmap(groundBitmap, dpToPx(900) + groundX, height - 205f, paintWhite)
        canvas?.drawBitmap(playerBitmap, 0f, height - playerHeight - playerY, paintWhite)
        canvas?.drawBitmap(msalBitmap, msalX + 50f, height - playerHeight, paintWhite)

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action){
            MotionEvent.ACTION_DOWN -> {
                touchX = event.x
                touchY = event.y
                if(GAME_RUNNING){
                    playerY = 0f
                    if (!playerSliding and !playerJumping){
                        if (touchY in ((0f)..(height / 2).toFloat())) {
                            playerJumping = true
                        } else if (touchY in (height / 2).toFloat()..(height).toFloat()) {
                            playerSliding = true
                        }
                    }

                }
            }
            MotionEvent.ACTION_MOVE -> {
                /*
                if (GAME_RUNNING) {
                    if (!playerSliding and !playerJumping){
                        handleMove(event)
                    }
                }

                 */
            }
            MotionEvent.ACTION_UP -> {

            }
        }
        return true
    }

    private fun handleMove(event: MotionEvent) {
        var direction = (touchY - event.y)
        if (direction > 10){
            playerJumping = true
            jumpAnimation()
        }
        else if (direction < -10) {
            playerSliding = true
            slideAnimation()
        }
    }

    inner class PlayerThread: Thread(){
        override fun run() {
            while (GAME_RUNNING){
                if (!playerJumping and !playerSliding){
                    runAnimation()
                    postInvalidate()
                    sleep(15)
                }
                else if (playerJumping){
                    jumpAnimation()
                    postInvalidate()
                    sleep(45)
                }
                else if (playerSliding){
                    slideAnimation()
                    postInvalidate()
                    sleep(300)
                    playerSliding = false
                    playerY = 0f
                    postInvalidate()
                }
            }
        }
    }
    inner class msalThread: Thread(){
        override fun run() {
            while (GAME_RUNNING){
                msalAnimation()
                msalX -= 20
                postInvalidate()
                if (msalX <= 0)
                    msalX = width.toFloat()
                sleep(15)
            }
        }
    }
    inner class groundThread: Thread(){
        override fun run() {
            while (GAME_RUNNING){
                groundX -= 10
                postInvalidate()
                if (groundX <= -dpToPx(100))
                    groundX = 0f
                sleep(15)
            }
        }
    }

    private fun msalAnimation() {
        when (msalRunState){
            1 -> {msalBitmap = getBitmap(context, R.drawable.ic_msal1)!!
                msalRunState += 1
                return}
            2 -> {msalBitmap = getBitmap(context, R.drawable.ic_msal2)!!
                msalRunState += 1
                return}
            3 -> {msalBitmap = getBitmap(context, R.drawable.ic_msal3)!!
                msalRunState += 1
                return}
            4 -> {msalBitmap = getBitmap(context, R.drawable.ic_msal4)!!
                msalRunState += 1
                return}
            5 -> {msalBitmap = getBitmap(context, R.drawable.ic_msal5)!!
                msalRunState += 1
                return}
            6 -> {msalBitmap = getBitmap(context, R.drawable.ic_msal6)!!
                msalRunState += 1
                return}
            7 -> {msalBitmap = getBitmap(context, R.drawable.ic_msal7)!!
                msalRunState += 1
                return}
            8 -> {msalBitmap = getBitmap(context, R.drawable.ic_msal8)!!
                msalRunState += 1
                return}
            9 -> {msalBitmap = getBitmap(context, R.drawable.ic_msal9)!!
                msalRunState += 1
                return}
            10 -> {msalBitmap = getBitmap(context, R.drawable.ic_msal10)!!
                msalRunState = 1
                return}
        }

    }

    fun runAnimation(){
        when (playerRunState) {
            1 -> {
                playerBitmap = getBitmap(context, R.drawable.ic_r1f1)!!
                playerRunState += 1
                return
            }
            2 -> {
                playerBitmap = getBitmap(context, R.drawable.ic_r1f2)!!
                playerRunState += 1
                return
            }
            3 -> {
                playerBitmap = getBitmap(context, R.drawable.ic_r1f3)!!
                playerRunState += 1
                return
            }
            4 -> {
                playerBitmap = getBitmap(context, R.drawable.ic_r1f4)!!
                playerRunState += 1
                return
            }
            5 -> {
                playerBitmap = getBitmap(context, R.drawable.ic_r1f5)!!
                playerRunState += 1
                return
            }
            6 -> {
                playerBitmap = getBitmap(context, R.drawable.ic_r1f6)!!
                playerRunState += 1
                return
            }
            7 -> {
                playerBitmap = getBitmap(context, R.drawable.ic_r1f7)!!
                playerRunState += 1
                return
            }
            8 -> {
                playerBitmap = getBitmap(context, R.drawable.ic_r1f8)!!
                playerRunState += 1
                return
            }
            9 -> {
                playerBitmap = getBitmap(context, R.drawable.ic_r1f9)!!
                playerRunState += 1
                return
            }
            10 -> {
                playerBitmap = getBitmap(context, R.drawable.ic_r1f10)!!
                playerRunState = 1
                return
            }
        }
    }

    fun jumpAnimation(){
        playerY += dpToPx(jumpV)
        jumpV += gravity
        if (playerY < 0){
            playerY = 0f
            jumpV = 30
            playerJumping = false
        }
    }

    fun slideAnimation(){
        playerBitmap = getBitmap(context, R.drawable.ic_r1f1)!!
        playerY = -dpToPx(50)
        //sleep(100)
        //playerSliding = false
    }
}