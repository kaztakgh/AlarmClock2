/**
 * @file AnalogClockView.kt
 */
package io.github.kaztakgh.alarmclock2

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.analog_clock.view.*
import java.util.*

/**
 * アナログ時計のViewを作成・表示
 * Resourceに画像を用意すること
 */
class AnalogClockView
    : ConstraintLayout {
    private lateinit var hourHand: ImageView
    private lateinit var minuteHand: ImageView
    private lateinit var secondHand: ImageView
    /**
     * 秒針表示
     */
    var displaySecond: Boolean = true

    /**
     * コンストラクタ
     */
    constructor(context: Context)
            : super(context, null) {
        initLayout()
    }
    constructor(context: Context, attrs: AttributeSet?)
            : super(context, attrs) {
        // 秒針表示のパラメータをXMLから読み込む
        val displaySecParamsArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.AnalogClockView, 0, 0
        )
        displaySecond = displaySecParamsArray.getBoolean(R.styleable.AnalogClockView_displaySecondHand, true)
        displaySecParamsArray.recycle()
        initLayout()
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        // 秒針表示のパラメータをXMLから読み込む
        val displaySecParamsArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.AnalogClockView, defStyleAttr, 0
        )
        displaySecond = displaySecParamsArray.getBoolean(R.styleable.AnalogClockView_displaySecondHand, true)
        displaySecParamsArray.recycle()
        initLayout()
    }

    /**
     * 初期化
     * Layoutの読み込み、変数の初期化
     */
    private fun initLayout() {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.analog_clock, this, true)
        hourHand = clock_hour
        minuteHand = clock_minute
        secondHand = clock_second
        // 秒針非表示の場合
        if (!displaySecond) {
            secondHand.visibility = View.GONE
        }
    }

    override fun onAttachedToWindow() {
        // タイマーの開始
        handler.post(runnable)
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // タイマーの終了
        handler.removeCallbacks(runnable)
    }

    /**
     * 起動中に常時行う処理
     * 時計の絵画
     */
    private val runnable = object : Runnable {
        /**
         * When an object implementing interface `Runnable` is used
         * to create a thread, starting the thread causes the object's
         * `run` method to be called in that separately executing
         * thread.
         *
         *
         * The general contract of the method `run` is that it may
         * take any action whatsoever.
         *
         * @see java.lang.Thread.run
         */
        override fun run() {
            // 時刻の取得
            val calendar = Calendar.getInstance()
            val milliSec = calendar.get(Calendar.MILLISECOND)

            // 時計の針の回転
            // onLayoutまでは呼ばれているため、位置の調節は不要
            rotateGraphic(hourHand, (calendar.get(Calendar.HOUR) * 60 + calendar.get(Calendar.MINUTE)) * 0.5f)
            rotateGraphic(minuteHand, calendar.get(Calendar.MINUTE) * 6.0f)

            // 秒針を表示する場合のみ、秒針の回転も行う
            if (displaySecond) {
                rotateGraphic(secondHand, calendar.get(Calendar.SECOND) * 6.0f)
            }
            // 表示しない場合
            else {
                // 非表示を指定する
                secondHand.visibility = View.GONE
            }

            handler.postDelayed(this, (1000 - milliSec).toLong())
        }
    }

    /**
     * 時計の針の回転絵画
     *
     * @param graphic ImageView
     * @param degree 回転角度(時計方向)
     */
    private fun rotateGraphic(
        graphic: ImageView,
        degree: Float
    ) {
        // Matrixを利用して画像を回転する
        val matrix = Matrix()
        // 回転の中心を画面の中心に設定する
        matrix.postTranslate(- graphic.width / 2.0f, - graphic.height / 2.0f)
        matrix.postRotate(degree)
        matrix.postTranslate(graphic.width / 2.0f, graphic.height / 2.0f)
        graphic.imageMatrix = matrix

        // 再絵画
        graphic.invalidate()
    }
}