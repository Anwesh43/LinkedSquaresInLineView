package com.anwesh.uiprojects.squaresinlineview

/**
 * Created by anweshmishra on 05/04/19.
 */

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF

val nodes : Int = 5
val squares : Int = 4
val scGap : Float = 0.05f
val scDiv : Double = 0.51
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val foreColor : Int = Color.parseColor("#3F51B5")
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.mirrorValue(a : Int, b : Int) : Float {
    val k : Float = scaleFactor()
    return (1 - k) * a.inverse() + k * b.inverse()
}
fun Float.updateValue(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * dir * scGap
fun Int.sf() : Float = 1f - 2 * (this % 2)

fun Canvas.drawXY(x : Float, y : Float, cb : (Canvas) -> Unit) {
    save()
    translate(x, y)
    cb(this)
    restore()
}

fun Canvas.drawMovingRect(i : Int, xGap : Float, h : Float, scale : Float, p : Paint) {
    val sc : Float = scale.divideScale(i, squares)
    drawXY(xGap * i, (h / 2 - xGap / 2) * i.sf() * sc, {
        drawRect(RectF(0f, 0f, xGap, xGap), p)
    })
}

fun Paint.setStyle(minSize : Float) {
    style = Paint.Style.STROKE
    strokeWidth = minSize / strokeFactor
    strokeCap = Paint.Cap.ROUND
    color = foreColor
}

fun Canvas.drawSILNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val size : Float = gap / sizeFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    val xGap : Float = (2 * size) / squares
    drawXY(gap * (i + 1), h / 2, {
        it.save()
        it.rotate(90f * sc2)
        it.drawLine(-size, 0f, size, 0f, paint)
        it.drawMovingRect(i, xGap, h, sc1, paint)
        it.restore()
    })
}

class SquaresInLineView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateValue(dir, squares, 1)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class SILNode(var i : Int, val state : State = State()) {

        private var next : SILNode? = null
        private var prev : SILNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = SILNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawSILNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : SILNode {
            var curr : SILNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return  curr
            }
            cb()
            return this
        }
    }

    data class SquaresInLine(var i : Int) {

        private val root : SILNode = SILNode(0)
        private var curr : SILNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }
}