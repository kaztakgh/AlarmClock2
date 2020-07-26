/**
 * @file DigitalClockView.kt
 */
package io.github.kaztakgh.alarmclock2

import android.content.Context
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextClock
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.digital_clock.view.*
import java.util.*

/**
 * デジタル時計のViewを作成・表示
 */
class DigitalClockView
    : ConstraintLayout {
    private lateinit var clock: TextClock
    private lateinit var ampmLeft: TextClock
    private lateinit var ampmRight: TextClock
    /**
     * 秒表示
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
            attrs, R.styleable.DigitalClockView, 0, 0
        )
        displaySecond = displaySecParamsArray.getBoolean(R.styleable.DigitalClockView_displaySecond, true)
        displaySecParamsArray.recycle()
        initLayout()
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        // 秒針表示のパラメータをXMLから読み込む
        val displaySecParamsArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.DigitalClockView, defStyleAttr, 0
        )
        displaySecond = displaySecParamsArray.getBoolean(R.styleable.DigitalClockView_displaySecond, true)
        displaySecParamsArray.recycle()
        initLayout()
    }

    /**
     * 初期化
     * Layoutの読み込み、変数の初期化、表示方法の指定
     */
    private fun initLayout() {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.digital_clock, this, true)
        clock = tc_clock
        ampmLeft = tc_ampm_l
        ampmRight = tc_ampm_r
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // 秒表示によって時刻のテキストを変更
        clock.format12Hour = if (displaySecond) "hh:mm:ss" else "hh:mm"
        clock.format24Hour = if (displaySecond) "HH:mm:ss" else "HH:mm"

        // 24時間表記の場合
        if (DateFormat.is24HourFormat(context)) {
            // 午前午後表記の削除
            ampmLeft.visibility = View.GONE
            ampmRight.visibility = View.GONE
        }
        else {
            // 日本語環境のみ
            if (Locale.getDefault() == Locale.JAPAN || Locale.getDefault() == Locale.JAPANESE) {
                // 午前午後は左側に表示
                ampmRight.visibility = View.GONE
            }
            // それ以外の場合
            else {
                // 午前午後は右側に表示
                ampmLeft.visibility = View.GONE
            }
        }
    }
}