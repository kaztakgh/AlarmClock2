/**
 * @file RingAlarmActivity.kt
 */
package io.github.kaztakgh.alarmclock2

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager

/**
 * アラーム発動ダイアログを出力するためのactivity
 */
class RingAlarmActivity
    : ApplicationBaseActivity() {
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        // ロック画面がある場合、解除をする
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        }
        else {
            this.window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        // タイトル非表示
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_ring_alarm)
        // ウィンドウの外側をタップしても終了しないようにする
        setFinishOnTouchOutside(false)
        val alarmModelId = intent.getIntExtra(ALARM_ID_KEY, 0)

        if (savedInstanceState == null) {
            val ringAlarmFragment = RingAlarmFragment()
            // 挿入する値の指定
            // アラームのIDを送信する
            val bundle = Bundle()
            bundle.putInt(ALARM_ID_KEY, alarmModelId)
            ringAlarmFragment.arguments = bundle
            // RingAlarmFragmentの実行
            supportFragmentManager.beginTransaction()
                .add(R.id.ring_alarm_container, ringAlarmFragment)
                .commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 余計なウィンドウが出てくるが、終了させる
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(false)
            setTurnScreenOn(false)
        }
        else {
            this.window.clearFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }
}