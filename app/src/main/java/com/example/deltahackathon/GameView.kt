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
import kotlin.random.Random


class GameView(context: Context, attrs: AttributeSet) : View(context, attrs)  {
    lateinit var playerBitmap:Bitmap
    var playerScore = 0
    var touchX = 0f
    var touchY = 0f
    var playerY = 0f
    var slideY = 0f
    val playerWidth = 364f
    val playerHeight = 457f
    var playerJumping = false
    var playerSliding = false
    var playerRunState = 1
    var playerJumpState = 1
    var msalX = 0f
    var msalY = 0f
    val msalWidth = 432f
    val msalHeight = 518f
    var msalRunState = 1
    var enemy = 0
    val gravity = -5
    var jumpV = 30
    var groundX = 0f
    lateinit var msalBitmap: Bitmap
    lateinit var flyBitmap: Bitmap
    lateinit var groundBitmap: Bitmap
    private val paintWhite: Paint = Paint()
    private val paintBlack: Paint = Paint()
    private val joystickPaint: Paint = Paint()
    var joystickX = 0f
    var joystickY = 0f
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

        paintBlack.color = Color.BLACK
        paintBlack.style = Paint.Style.FILL
        paintBlack.textSize =100f
        paintBlack.textAlign = Paint.Align.CENTER

        joystickPaint.color = Color.BLACK
        joystickPaint.style = Paint.Style.FILL
        joystickPaint.alpha = 50
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        msalX = width - msalWidth + 50f
        playerBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        msalBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        flyBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        groundBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        playerBitmap = getBitmap(context, R.drawable.ic_r1f7)!!
        msalBitmap = getBitmap(context, R.drawable.ic_msal1)!!
        flyBitmap = getBitmap(context, R.drawable.ic_fly1)!!
        groundBitmap = getBitmap(context, R.drawable.ic_ground)!!
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawText("$playerScore", 100f, 100f, paintBlack)
        groundX -= 10
        if (groundX <= -dpToPx(100))
            groundX = 0f
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

        //runAnimation()
        canvas?.drawBitmap(playerBitmap, 0f, height - playerHeight - playerY, paintWhite)

        msalX -= 40
        msalAnimation()
        canvas?.drawBitmap(if(enemy == 0)msalBitmap else flyBitmap, msalX + 50f, height - playerHeight - msalY, paintWhite)
        if (msalX <= 0) {
            msalX = width.toFloat()
            enemy = (0..2).random()
            if (enemy == 2) msalY = 120f
            else if (enemy == 1) msalY = -50f
            else msalY = 0f
        }

        canvas?.drawCircle(300f, 500f, 150f, joystickPaint)
        canvas?.drawCircle(300f, 500f + joystickY, 50f, joystickPaint)

        if (!GAME_RUNNING){
            canvas?.drawText("GAME OVER",(width/2).toFloat(), (height/2).toFloat(), paintBlack)
        }
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
                else{
                    GAME_RUNNING = true
                    playerScore = 0
                    PlayerThread().start()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (GAME_RUNNING) {
                    if ((touchX in (250f..350f)) and (touchY in (250f..350f))){
                        handleMove(event)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {

            }
        }
        return true
    }

    private fun handleMove(event: MotionEvent) {
        joystickY -= (touchY - event.y)
        touchY = event.y
        joystickY = when{
            touchY - 50 <0 -> touchY - 50
            touchY + 50 > 100 -> touchY + 50
            else ->joystickY
        }
    }

    inner class PlayerThread: Thread(){
        override fun run() {
            while (GAME_RUNNING){
                if (!playerJumping and !playerSliding){
                    runAnimation()
                    postInvalidate()
                    sleep(5)
                }
                else if (playerJumping){
                    jumpAnimation()
                    postInvalidate()
                    sleep(100)
                }
                else if (playerSliding){
                    slideAnimation()
                    postInvalidate()
                    sleep(100)
                }
                if (msalX<20){
                    if (enemy == 0){
                        if (!playerJumping)
                            GAME_RUNNING = false
                    } else if (enemy == 2){
                        if (playerJumping)
                            GAME_RUNNING = false
                    } else {
                        if (!playerSliding)
                            GAME_RUNNING = false
                    }
                }
                playerScore += 1
            }
        }
    }
    /*
    inner class msalThread: Thread(){
        override fun run() {
            while (GAME_RUNNING){
                //msalAnimation()
                postInvalidate()
                sleep(15)
            }
        }
    }

     */

    private fun msalAnimation() {
        when (msalRunState){
            1 -> {msalBitmap = getBitmap(context, R.drawable.ic_msal1)!!
                flyBitmap = getBitmap(context, R.drawable.ic_fly1)!!
                msalRunState += 1
                return}
            2 -> {msalBitmap = getBitmap(context, R.drawable.ic_msal2)!!
                msalRunState += 1
                return}
            3 -> {msalBitmap = getBitmap(context, R.drawable.ic_msal3)!!
                msalRunState += 1
                return}
            4 -> {msalBitmap = getBitmap(context, R.drawable.ic_msal4)!!
                flyBitmap = getBitmap(context, R.drawable.ic_fly2)!!
                msalRunState += 1
                return}
            5 -> {msalBitmap = getBitmap(context, R.drawable.ic_msal5)!!
                msalRunState += 1
                return}
            6 -> {msalBitmap = getBitmap(context, R.drawable.ic_msal6)!!
                msalRunState += 1
                return}
            7 -> {msalBitmap = getBitmap(context, R.drawable.ic_msal7)!!
                flyBitmap = getBitmap(context, R.drawable.ic_fly1)!!
                msalRunState += 1
                return}
            8 -> {msalBitmap = getBitmap(context, R.drawable.ic_msal8)!!
                msalRunState += 1
                return}
            9 -> {msalBitmap = getBitmap(context, R.drawable.ic_msal9)!!
                msalRunState += 1
                return}
            10 -> {msalBitmap = getBitmap(context, R.drawable.ic_msal10)!!
                flyBitmap = getBitmap(context, R.drawable.ic_fly2)!!
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
        playerBitmap = getBitmap(context, R.drawable.ic_slide1)!!
        slideY += dpToPx(jumpV)
        jumpV += gravity
        playerY = -100f
        if (slideY < 0){
            slideY = 0f
            playerY = 0f
            jumpV = 30
            playerSliding = false
        }
        //sleep(100)
        //playerSliding = false
    }

    fun gameRunning(){

    }
}