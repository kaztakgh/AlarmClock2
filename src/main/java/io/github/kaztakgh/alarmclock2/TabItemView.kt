/**
 * @file TabItemView.kt
 */
package io.github.kaztakgh.alarmclock2

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import io.github.kaztakgh.viewhelper.ViewHelper
import kotlinx.android.synthetic.main.tab_item.view.*

/**
 * タブのビューを作成する
 */
class TabItemView : ConstraintLayout{
    /**
     * コンストラクタ
     */
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    init {
        val rootView = LayoutInflater.from(context).inflate(R.layout.tab_item, this)
        icon = rootView.iv_icon
        text = rootView.tv_tab_text
    }

    /**
     * テキスト
     */
    var text : TextView

    /**
     * アイコン
     */
    var icon : ImageView

    /**
     * 選択状態
     */
    var isSelectedMenu : Boolean = false
        set(value) {
            field = value
            // 選択状態によってタブの表示を変える
            if (value) {
                // アクセントに使用している色を使用する
                val fillColor = ViewHelper.getColorAttr(context, android.R.attr.colorAccent)
                icon.setColorFilter(fillColor, PorterDuff.Mode.SRC_IN)
                text.setTextColor(fillColor)
            }
            else {
                // テキストで使用している色にする
                val fillColor = ViewHelper.getColorAttr(context, android.R.attr.textColorPrimary)
                icon.setColorFilter(fillColor, PorterDuff.Mode.SRC_IN)
                text.setTextColor(fillColor)
            }
        }
}