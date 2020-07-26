package io.github.kaztakgh.alarmclock2

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.*

/**
 * 祝日データの更新通知を受け取る
 */
class HolidayDataUpdateReceiver
    : BroadcastReceiver() {
    companion object {
        /**
         * 祝日データ更新アクション
         */
        const val ACTION_HOLIDAY_DATA_UPDATE = "io.github.kaztakgh.alarmclock2.action.HOLIDAY_DATA_UPDATE"

        /**
         * 更新サービスのセッティング
         *
         * @param context Context
         */
        fun setUpdate(
            context: Context
        ) {
            // Intentの指定
            val intent = Intent(context, HolidayDataUpdateReceiver::class.java)
            intent.action = ACTION_HOLIDAY_DATA_UPDATE
            val pendingIntent = PendingIntent.getBroadcast(context, 1000, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val alarmManager : AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // 発動時間の指定
            val nextDateTime = Calendar.getInstance()
            val nextMonth = nextDateTime.get(Calendar.MONTH) + 1
            nextDateTime.set(Calendar.MILLISECOND, 0)
            nextDateTime.set(nextDateTime.get(Calendar.YEAR), nextMonth, 1, 0, 0, 0)

            // サービスの実行時間をセット
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextDateTime.timeInMillis, pendingIntent)
            }
            else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, nextDateTime.timeInMillis, pendingIntent)
            }
        }
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        when (intent.action) {
            // 端末起動時
            Intent.ACTION_BOOT_COMPLETED -> {
                // 再度サービスをスタートする
                setUpdate(context)
            }
            // アプリからの呼び出し時
            ACTION_HOLIDAY_DATA_UPDATE -> {
                // 定期実行のサービスをスタートする
                HolidayDataAccessJobService.enqueueWork(context, intent)
            }
        }
    }
}
