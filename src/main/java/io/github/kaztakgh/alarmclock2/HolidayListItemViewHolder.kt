/**
 * @file HolidayListItemViewHolder.kt
 */
package io.github.kaztakgh.alarmclock2

import android.text.format.DateFormat
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.holiday_list_item.view.*

class HolidayListItemViewHolder(view: View)
    : RecyclerView.ViewHolder(view) {
    private val dateTextView: TextView = view.tv_date
    private val holidayNameView: TextView = view.tv_holiday_name

    /**
     * データを挿入したときにその内容を表示する
     *
     * @param model 祝日データ
     */
    fun bind(
        model: HolidayModel
    ) {
        // 日付
        dateTextView.text = DateFormat.format("yyyy/MM/dd", model.date)
        // 祝日名
        holidayNameView.text = model.name
    }
}