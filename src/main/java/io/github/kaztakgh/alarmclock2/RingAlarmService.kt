package io.github.kaztakgh.alarmclock2

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import io.github.kaztakgh.settingitemsviewlibrary.SingleSelectItem
import java.io.IOException

/**
 * アラームを発動するサービス
 */
class RingAlarmService
    : Service() {
    companion object {
        /**
         * 通知チャンネルID
         */
        const val ALARM_NOTIFICATION_CHANNEL_ID = "alarmService"
        /**
         * 通知チャンネル
         */
        const val ALARM_NOTIFICATION_CHANNEL = "alarmChannel"
    }

    /**
     * 起動アラームID
     */
    private var alarmId = 0

    /**
     * Audio再生要素
     */
    private val audioAttributes: AudioAttributes?
        get() {
            return AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        }

    /**
     * Alarm音再生に使用するMediaPlayer
     */
    private val mediaPlayer: MediaPlayer by lazy {
        val player = MediaPlayer()
        player
    }

    /**
     * Alarm音再生に使用するSoundPool
     */
    private val alarmSoundPool: SoundPool? by lazy {
        val alarmSoundPool = buildSoundPool()
        alarmSoundPool
    }

    /**
     * SoundPool使用時に音を止めるためのID
     */
    private var alarmSoundStreamId: Int? = null

    /**
     * 環境設定のパラメータ群
     */
    private lateinit var preferences: SharedPreferences

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    override fun onCreate() {
        super.onCreate()
        preferences = getSharedPreferences(
            GeneralSettingsFragment.GENERAL_SETTING_PREFERENCE,
            Context.MODE_PRIVATE
        )
    }

    /**
     * Called by the system every time a client explicitly starts the service by calling
     * [android.content.Context.startService], providing the arguments it supplied and a
     * unique integer token representing the start request.  Do not call this method directly.
     *
     *
     * For backwards compatibility, the default implementation calls
     * [.onStart] and returns either [.START_STICKY]
     * or [.START_STICKY_COMPATIBILITY].
     *
     *
     * Note that the system calls this on your
     * service's main thread.  A service's main thread is the same
     * thread where UI operations take place for Activities running in the
     * same process.  You should always avoid stalling the main
     * thread's event loop.  When doing long-running operations,
     * network calls, or heavy disk I/O, you should kick off a new
     * thread, or use [android.os.AsyncTask].
     *
     * @param intent The Intent supplied to [android.content.Context.startService],
     * as given.  This may be null if the service is being restarted after
     * its process has gone away, and it had previously returned anything
     * except [.START_STICKY_COMPATIBILITY].
     * @param flags Additional data about this start request.
     * @param startId A unique integer representing this specific request to
     * start.  Use with [.stopSelfResult].
     *
     * @return The return value indicates what semantics the system should
     * use for the service's current started state.  It may be one of the
     * constants associated with the [.START_CONTINUATION_MASK] bits.
     *
     * @see .stopSelfResult
     */
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        // AlarmRecordModelの読み込み
        alarmId = intent!!.getIntExtra(ALARM_ID_KEY, 0)
        val alarmRecordModel = getAlarmRecordModel(alarmId)
        // マナーモードorサイレントモードの場合
        val isMannerOrSilent =
            (getSystemService(Context.AUDIO_SERVICE) as AudioManager).ringerMode != AudioManager.RINGER_MODE_NORMAL
        val isMannerMode =
            (getSystemService(Context.AUDIO_SERVICE) as AudioManager).ringerMode == AudioManager.RINGER_MODE_VIBRATE
        val isVibratePriority =
            preferences.getString(
                GeneralSettingsFragment.DATA_MODE_PRIORITY,
                GeneralSettingsFragment.MODE_RING_ALARM_SOUND
            ) == GeneralSettingsFragment.MODE_SILENT

        // 音の再生
        // アラーム音の読込、準備
        // マナーモード優先の場合は0にする
        val volume = if (isVibratePriority && isMannerOrSilent) 0.0f else alarmRecordModel.volume / 100.0f
        // ファイルから選択した場合かつ、ファイル名がある場合にMediaPlayerを使用する
        val useMediaPlayer = (
                alarmRecordModel.useFile == AlarmSettingFragment.VAL_SELECT_FILE
                        && alarmRecordModel.soundFileName.isNotEmpty()
                )
        // MediaPlayer使用
        if (useMediaPlayer) {
            // 音の再生
            ringAlarmSoundOnMediaPlayer(alarmRecordModel.soundFileName, volume)
        }
        // SoundPool使用
        else {
            // プリセット音の配列を取得する
            val alarmSoundArray = getAlarmPresetSoundIdArray()
            // 音の再生
            ringAlarmSoundOnSoundPool(alarmSoundArray[alarmRecordModel.selectSound], volume)
        }

        // バイブレーションの設定がONまたはマナーモード優先の時、バイブレーションを開始する(繰り返し)
        if (alarmRecordModel.vibrate || (isVibratePriority && isMannerMode)) {
            startVibrate()
        }

        // 通知の作成
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = registerNotificationChannel()
            manager.createNotificationChannel(channel)
        }
        val notification = buildServiceNotification(alarmRecordModel.name)
        manager.notify(alarmId, notification)

        // Foregroundでのサービス開始
        startForeground(startId, notification)

        // アクティビティの準備
        val action = Intent(this, RingAlarmActivity::class.java)
        action.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        action.putExtra(ALARM_ID_KEY, alarmId)

        // Activityの開始
        startActivity(action)

        // 未完了のジョブを再起動しないようにしている
        return START_NOT_STICKY
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    override fun onDestroy() {
        super.onDestroy()
        // 音の停止
        stopSoundAndVibrate()

        // 通知の消去
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(alarmId)
    }

    override fun onBind(
        intent: Intent
    ): IBinder? {
        return null
    }

    /**
     * Notificationの作成
     *
     * @param contentText 通知に表示させる文
     * @return notification
     */
    private fun buildServiceNotification(
        contentText: String = resources.getText(R.string.alarm_set).toString()
    ) : Notification? {
        // NotificationBuilderの準備
        val builder = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            NotificationCompat.Builder(this, ALARM_NOTIFICATION_CHANNEL_ID)
        else NotificationCompat.Builder(this)

        // 通知の表示、動作内容の指定
        builder.apply {
            setContentTitle(resources.getText(R.string.app_name))
            setContentText(contentText)
            setSmallIcon(R.mipmap.ic_launcher)
            setCategory(NotificationCompat.CATEGORY_ALARM)
            setAutoCancel(true)
        }
        return builder.build()
    }

    /**
     * Notificationを表示するためのチャンネルを登録
     *
     * @return NotificationChannel
     */
    @TargetApi(Build.VERSION_CODES.O)
    private fun registerNotificationChannel(): NotificationChannel {
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        // システムにチャンネルを登録する
        val channel = NotificationChannel(
            ALARM_NOTIFICATION_CHANNEL_ID,
            ALARM_NOTIFICATION_CHANNEL,
            importance
        )
        channel.vibrationPattern = longArrayOf(0)
        channel.enableVibration(true)
        return channel
    }

    /**
     * アラーム設定画面で使用するプリセット音のIDの配列を取得する
     *
     * @return プリセット音IDの配列
     */
    private fun getAlarmPresetSoundIdArray(): IntArray {
        // AlarmSettingsからプリセット音の配列を取得する
        val alarmSoundSelectItem = SettingItems.readXmlSettingsFromAssets(
            this,
            AlarmSettingFragment.FILE_ALARM_SETTING
        ).first {
            it.tag == AlarmSettingFragment.TAG_SOUND_SELECT
        } as SingleSelectItem

        return alarmSoundSelectItem.optionSounds!!
    }

    /**
     * SoundPoolの構築
     *
     * @return SoundPool
     */
    private fun buildSoundPool(): SoundPool? {
        return SoundPool.Builder()
            .setAudioAttributes(audioAttributes)
            .setMaxStreams(1)
            .build()
    }

    /**
     * 音楽ファイルを利用してアラームを鳴らす
     *
     * @param fileName ファイル名
     * @param volume 音量
     */
    private fun ringAlarmSoundOnMediaPlayer(
        fileName: String,
        volume: Float
    ) {
        val player = mediaPlayer
        // ファイルの準備
        try {
            player.setDataSource(this, Uri.parse(fileName))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        player.setAudioAttributes(audioAttributes)

        // 音量調節
        player.setVolume(volume, volume)
        // ループ適用
        player.isLooping = true

        // プレイヤーの準備
        try {
            player.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // 再生開始
        player.start()
    }

    /**
     * 音楽ファイルの再生を止める
     */
    private fun stopAlarmSoundOnMediaPlayer() {
        mediaPlayer.stop()
        mediaPlayer.reset()
        mediaPlayer.release()
    }

    /**
     * プリセット音を使用してアラームを鳴らす
     *
     * @param soundId プリセット音ID
     * @param volume 音量
     */
    private fun ringAlarmSoundOnSoundPool(
        soundId: Int,
        volume: Float
    ) {
        val alarmSound = alarmSoundPool!!.load(this, soundId, 1)
        alarmSoundPool!!.setOnLoadCompleteListener { sp, _, _ ->
            /**
             * Called when a sound has completed loading.
             *
             * @param sp SoundPool object from the load() method
             */
            alarmSoundStreamId = sp.play(alarmSound, volume, volume, 0, -1, 1.0f)
        }
    }

    /**
     * プリセット音の再生を止める
     *
     * @param streamId ストリームID
     */
    private fun stopAlarmSoundOnSoundPool(
        streamId: Int
    ) {
        // プリセット音の配列を取得する
        val alarmSoundArray = getAlarmPresetSoundIdArray()
        // DBからデータを取得する
        val alarmRecordModel = getAlarmRecordModel(alarmId)

        // soundPoolのリソース解放
        alarmSoundPool!!.stop(streamId)
        alarmSoundPool!!.unload(alarmSoundArray[alarmRecordModel.selectSound])
        alarmSoundPool!!.release()
    }

    /**
     * バイブレーションを起動する
     */
    private fun startVibrate() {
        // バイブレーションの設定
        val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (!vibrator.hasVibrator()) return

        val vibratePattern = longArrayOf(500, 2000, 500, 2000)
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                val vibrationEffect = VibrationEffect.createWaveform(
                    vibratePattern,
                    intArrayOf(0, VibrationEffect.EFFECT_CLICK, 0, VibrationEffect.EFFECT_CLICK),
                    2
                )
                vibrator.vibrate(vibrationEffect)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                    && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> {
                val vibrationEffect = VibrationEffect.createWaveform(
                    vibratePattern,
                    intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE),
                    2
                )
                vibrator.vibrate(vibrationEffect)
            }
            else -> {
                vibrator.vibrate(vibratePattern, 2)
            }
        }
    }

    /**
     * アラーム音とバイブレーションを停止する
     */
    private fun stopSoundAndVibrate() {
        // バイブレーションがONの場合、バイブレーションを止める
        val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.cancel()

        // DBからデータを取得する
        val alarmRecordModel = getAlarmRecordModel(alarmId)
        when (alarmRecordModel.useFile) {
            // ファイル使用時
            AlarmSettingFragment.VAL_SELECT_FILE -> {
                // MediaPlayerの再生を止める
                stopAlarmSoundOnMediaPlayer()
            }
            // プリセット音使用時
            else -> {
                // SoundPoolの再生を止める
                stopAlarmSoundOnSoundPool(alarmSoundStreamId!!)
            }
        }
    }

    /**
     * AlarmRecordModelの取得
     *
     * @param id アラームID
     * @return AlarmRecordModel
     */
    private fun getAlarmRecordModel(
        id: Int
    ): AlarmRecordModel {
        val alarmRecordObject = AlarmRecordDatabaseHelper(this)
        // DBから該当する1件を読み込む
        val alarmRecordModel = alarmRecordObject.readRecordModels(
            "${AlarmRecordTableObject.RecordColumns.COLUMN_ID} = ?",
            arrayOf(id.toString())
        ).first()
        // DBをクローズする
        alarmRecordObject.close()

        return alarmRecordModel
    }
}
