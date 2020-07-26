/**
 * @file RingAlarmReceiver.kt
 */
package io.github.kaztakgh.alarmclock2

import android.annotation.TargetApi
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

/**
 * アラームサービスの受信時に処理を行うクラス
 */
class RingAlarmReceiver
    : BroadcastReceiver(){
    companion object {
        /**
         * アラーム発動アクション
         */
        const val ACTION_RING_ALARM = "io.github.kaztakgh.alarmclock2.action.RING_ALARM"

        /**
         * アラームのセット
         *
         * @param context Context
         * @param model 発動するアラームのモデル
         * @param ringAlarmTime 発動指定時間
         */
        fun setAlarm(
            context: Context,
            model: AlarmRecordModel,
            ringAlarmTime: Calendar? = null
        ) {
            // IDの読み取り
            val id = model.id
            // Intentの指定
            val serviceIntent = Intent(context, RingAlarmReceiver::class.java)
            serviceIntent.putExtra(ALARM_ID_KEY, id)
            serviceIntent.action = ACTION_RING_ALARM
            val pendingIntent = getPendingIntent(context, id, serviceIntent)
            val alarmManager : AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // 発動時間の指定
            val holidayArray = HolidayUtil.getHolidayList(context)
            val ringAlarmDatetime : Calendar? = ringAlarmTime ?: model.getNextAlarmTime(holidayArray)

            // broadcastのセット
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(ringAlarmDatetime!!.timeInMillis, pendingIntent),
                pendingIntent
            )
        }

        @TargetApi(Build.VERSION_CODES.O)
        fun setAlarmDateTime(
            context: Context,
            model: AlarmRecordModel,
            ringAlarmTime: LocalDateTime? = null
        ) {
            // IDの読み取り
            val id = model.id
            // Intentの指定
            val serviceIntent = Intent(context, RingAlarmReceiver::class.java)
            serviceIntent.putExtra(ALARM_ID_KEY, id)
            serviceIntent.action = ACTION_RING_ALARM
            val pendingIntent = getPendingIntent(context, id, serviceIntent)
            val alarmManager : AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // 発動時間の指定
            val holidayArray = HolidayUtil.getHolidayList(context)
            val ringAlarmDatetime : LocalDateTime? = ringAlarmTime ?: model.getNextAlarmDateTime(holidayArray)

            // broadcastのセット
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(ringAlarmDatetime!!.toInstant(ZoneOffset.UTC).toEpochMilli(), pendingIntent),
                pendingIntent
            )
        }

        /**
         * アラームのキャンセル
         *
         * @param context Context
         * @param model キャンセルするアラームのモデル
         */
        fun cancelAlarm(
            context: Context,
            model: AlarmRecordModel
        ) {
            // IDの読み取り
            val id = model.id
            // Intentの指定
            val serviceIntent = Intent(context, RingAlarmReceiver::class.java)
            val pendingIntent = getPendingIntent(context, id, serviceIntent)
            val alarmManager : AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // broadcastのキャンセル
            alarmManager.cancel(pendingIntent)
            pendingIntent!!.cancel()
        }

        /**
         * PendingIntentの指定
         *
         * @param context Context
         * @param id アラームID
         * @param intent 起動アクティビティなどを指定したintent
         * @return
         */
        private fun getPendingIntent(
            context: Context,
            id: Int,
            intent: Intent
        ): PendingIntent? {
            return PendingIntent.getBroadcast(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    /**
     * This method is called when the BroadcastReceiver is receiving an Intent
     * broadcast.  During this time you can use the other methods on
     * BroadcastReceiver to view/modify the current result values.  This method
     * is always called within the main thread of its process, unless you
     * explicitly asked for it to be scheduled on a different thread using
     * [android.content.Context.registerReceiver]. When it runs on the main
     * thread you should
     * never perform long-running operations in it (there is a timeout of
     * 10 seconds that the system allows before considering the receiver to
     * be blocked and a candidate to be killed). You cannot launch a popup dialog
     * in your implementation of onReceive().
     *
     *
     * **If this BroadcastReceiver was launched through a &lt;receiver&gt; tag,
     * then the object is no longer alive after returning from this
     * function.** This means you should not perform any operations that
     * return a result to you asynchronously. If you need to perform any follow up
     * background work, schedule a [android.app.job.JobService] with
     * [android.app.job.JobScheduler].
     *
     * If you wish to interact with a service that is already running and previously
     * bound using [bindService()][android.content.Context.bindService],
     * you can use [.peekService].
     *
     *
     * The Intent filters used in [android.content.Context.registerReceiver]
     * and in application manifests are *not* guaranteed to be exclusive. They
     * are hints to the operating system about how to find suitable recipients. It is
     * possible for senders to force delivery to specific recipients, bypassing filter
     * resolution.  For this reason, [onReceive()][.onReceive]
     * implementations should respond only to known actions, ignoring any unexpected
     * Intents that they may receive.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(
        context: Context?,
        intent: Intent?
    ) {
        when(intent!!.action) {
            // アラーム発動の場合
            ACTION_RING_ALARM -> {
                // データの受信
                val alarmId = intent.getIntExtra(ALARM_ID_KEY, 0)
                // サービスの準備
                val serviceIntent = Intent(context, RingAlarmService::class.java).apply {
                    putExtra(ALARM_ID_KEY, alarmId)
                }

                // Android8以降の場合
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // ForegroundServiceとしてサービスを起動
                    context!!.startForegroundService(serviceIntent)
                }
                else {
                    // Serviceの起動
                    context!!.startService(serviceIntent)
                }
            }
            // 再起動時、TimeZoneの変更時、日付変更時、時刻変更時
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED -> {
                // DBからデータを取得
                val alarmRecordsObject = AlarmRecordDatabaseHelper(context!!)
                val alarmRecordArray = alarmRecordsObject.readRecordModels(null, null)
                alarmRecordsObject.close()

                // アラームのキャンセル
                alarmRecordArray.forEach {
                    cancelAlarm(context, it)
                }
                // アラームがONの物のみ再度セットする
                alarmRecordArray.filter { it.state }.forEach {
                    setAlarm(context, it)
                }
            }
            // 上記以外
            else -> {}
        }
    }
}