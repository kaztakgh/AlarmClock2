/**
 * @file AlarmRecordDatabaseHelper.kt
 */
package io.github.kaztakgh.alarmclock2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import io.github.kaztakgh.alarmclock2.AlarmRecordTableObject.RecordColumns.Companion.COLUMN_CREATED_DATETIME
import io.github.kaztakgh.alarmclock2.AlarmRecordTableObject.RecordColumns.Companion.COLUMN_HOLIDAY_COUNT
import io.github.kaztakgh.alarmclock2.AlarmRecordTableObject.RecordColumns.Companion.COLUMN_HOUR
import io.github.kaztakgh.alarmclock2.AlarmRecordTableObject.RecordColumns.Companion.COLUMN_ID
import io.github.kaztakgh.alarmclock2.AlarmRecordTableObject.RecordColumns.Companion.COLUMN_MIN
import io.github.kaztakgh.alarmclock2.AlarmRecordTableObject.RecordColumns.Companion.COLUMN_NAME
import io.github.kaztakgh.alarmclock2.AlarmRecordTableObject.RecordColumns.Companion.COLUMN_SNOOZE_COUNT
import io.github.kaztakgh.alarmclock2.AlarmRecordTableObject.RecordColumns.Companion.COLUMN_SNOOZE_REPEATED
import io.github.kaztakgh.alarmclock2.AlarmRecordTableObject.RecordColumns.Companion.COLUMN_SNOOZE_TIMER
import io.github.kaztakgh.alarmclock2.AlarmRecordTableObject.RecordColumns.Companion.COLUMN_SOUND_FILENAME
import io.github.kaztakgh.alarmclock2.AlarmRecordTableObject.RecordColumns.Companion.COLUMN_SOUND_SELECT
import io.github.kaztakgh.alarmclock2.AlarmRecordTableObject.RecordColumns.Companion.COLUMN_STATE
import io.github.kaztakgh.alarmclock2.AlarmRecordTableObject.RecordColumns.Companion.COLUMN_UPDATED_DATETIME
import io.github.kaztakgh.alarmclock2.AlarmRecordTableObject.RecordColumns.Companion.COLUMN_USE_HDD_FILE
import io.github.kaztakgh.alarmclock2.AlarmRecordTableObject.RecordColumns.Companion.COLUMN_VIBRATE
import io.github.kaztakgh.alarmclock2.AlarmRecordTableObject.RecordColumns.Companion.COLUMN_VOLUME
import io.github.kaztakgh.alarmclock2.AlarmRecordTableObject.RecordColumns.Companion.COLUMN_WEEK_FRI
import io.github.kaztakgh.alarmclock2.AlarmRecordTableObject.RecordColumns.Companion.COLUMN_WEEK_MON
import io.github.kaztakgh.alarmclock2.AlarmRecordTableObject.RecordColumns.Companion.COLUMN_WEEK_SAT
import io.github.kaztakgh.alarmclock2.AlarmRecordTableObject.RecordColumns.Companion.COLUMN_WEEK_SUN
import io.github.kaztakgh.alarmclock2.AlarmRecordTableObject.RecordColumns.Companion.COLUMN_WEEK_THU
import io.github.kaztakgh.alarmclock2.AlarmRecordTableObject.RecordColumns.Companion.COLUMN_WEEK_TUE
import io.github.kaztakgh.alarmclock2.AlarmRecordTableObject.RecordColumns.Companion.COLUMN_WEEK_WED
import io.github.kaztakgh.alarmclock2.AlarmRecordTableObject.RecordColumns.Companion.TABLE_NAME
import java.util.*
import kotlin.collections.ArrayList

/**
 * データベースへのアクセスを行うHelper
 *
 * @constructor
 * @see SQLiteOpenHelper
 *
 * @param context Context
 */
class AlarmRecordDatabaseHelper(
    context: Context
) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    companion object {
        /**
         * データベースのバージョン
         */
        const val DATABASE_VERSION = 1
        /**
         * データベースのファイル名
         */
        const val DATABASE_NAME = "AlarmRecord.db"
        /**
         * テーブルを作成するSQL
         */
        private const val SQL_CREATE =
            "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
            "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COLUMN_STATE INTEGER CHECK($COLUMN_STATE = 0 OR $COLUMN_STATE = 1), " +
            "$COLUMN_NAME TEXT, " +
            "$COLUMN_HOUR INTEGER CHECK($COLUMN_HOUR >= 0 AND $COLUMN_HOUR < 24), " +
            "$COLUMN_MIN INTEGER CHECK($COLUMN_MIN >= 0 AND $COLUMN_MIN < 60), " +
            "$COLUMN_WEEK_SUN INTEGER CHECK($COLUMN_WEEK_SUN = 0 OR $COLUMN_WEEK_SUN = 1), " +
            "$COLUMN_WEEK_MON INTEGER CHECK($COLUMN_WEEK_MON = 0 OR $COLUMN_WEEK_MON = 1), " +
            "$COLUMN_WEEK_TUE INTEGER CHECK($COLUMN_WEEK_TUE = 0 OR $COLUMN_WEEK_TUE = 1), " +
            "$COLUMN_WEEK_WED INTEGER CHECK($COLUMN_WEEK_WED = 0 OR $COLUMN_WEEK_WED = 1), " +
            "$COLUMN_WEEK_THU INTEGER CHECK($COLUMN_WEEK_THU = 0 OR $COLUMN_WEEK_THU = 1), " +
            "$COLUMN_WEEK_FRI INTEGER CHECK($COLUMN_WEEK_FRI = 0 OR $COLUMN_WEEK_FRI = 1), " +
            "$COLUMN_WEEK_SAT INTEGER CHECK($COLUMN_WEEK_SAT = 0 OR $COLUMN_WEEK_SAT = 1), " +
            "$COLUMN_HOLIDAY_COUNT INTEGER DEFAULT 0, " +
            "$COLUMN_USE_HDD_FILE INTEGER DEFAULT 0, " +
            "$COLUMN_SOUND_SELECT INTEGER DEFAULT 0, " +
            "$COLUMN_SOUND_FILENAME TEXT, " +
            "$COLUMN_VOLUME INTEGER, " +
            "$COLUMN_VIBRATE INTEGER CHECK($COLUMN_VIBRATE = 0 OR $COLUMN_VIBRATE = 1), " +
            "$COLUMN_SNOOZE_TIMER INTEGER DEFAULT 0, " +
            "$COLUMN_SNOOZE_COUNT INTEGER DEFAULT 0, " +
            "$COLUMN_SNOOZE_REPEATED INTEGER DEFAULT 0, " +
            "$COLUMN_CREATED_DATETIME TEXT, " +
            "$COLUMN_UPDATED_DATETIME TEXT)"

        /**
         * テーブルを削除するSQL
         */
        private const val SQL_DELETE = "DROP TABLE IF EXISTS $TABLE_NAME"
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    override fun onCreate(
        db: SQLiteDatabase?
    ) {
        // テーブルの作成
        db?.execSQL(SQL_CREATE)
    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     *
     *
     *
     * The SQLite ALTER TABLE documentation can be found
     * [here](http://sqlite.org/lang_altertable.html). If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     *
     *
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     *
     *
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    override fun onUpgrade(
        db: SQLiteDatabase?,
        oldVersion: Int,
        newVersion: Int
    ) {
        // 一時的なテーブルを作成し、データを移動する
        // テーブルを削除する
        db?.execSQL(SQL_DELETE)
        // 再度テーブルを作成する
        onCreate(db)
    }

    /**
     * データベースにデータを挿入する
     * アラーム設定画面で利用する
     *
     * @param model 設定画面で決定したパラメータ群
     * @return データのID(失敗時: -1)
     */
    fun addRecordModel(
        model: AlarmRecordModel
    ): Long {
        val db = writableDatabase
        // 現在時刻の文字列の準備
        val calendar = Calendar.getInstance()
        val datetimeFormat = "yyyy/MM/dd HH:mm:ss"
        val createDateTimeString = android.text.format.DateFormat.format(datetimeFormat, calendar.time) as String
        val repeatWeekArray = model.getRepeatWeekValArray()

        // 挿入する値の決定
        val contentValues = ContentValues().apply {
            put(COLUMN_STATE, model.getStateVal())
            put(COLUMN_NAME, model.name)
            put(COLUMN_HOUR, model.hour)
            put(COLUMN_MIN, model.min)
            put(COLUMN_WEEK_SUN, repeatWeekArray[0])
            put(COLUMN_WEEK_MON, repeatWeekArray[1])
            put(COLUMN_WEEK_TUE, repeatWeekArray[2])
            put(COLUMN_WEEK_WED, repeatWeekArray[3])
            put(COLUMN_WEEK_THU, repeatWeekArray[4])
            put(COLUMN_WEEK_FRI, repeatWeekArray[5])
            put(COLUMN_WEEK_SAT, repeatWeekArray[6])
            put(COLUMN_HOLIDAY_COUNT, model.holidayCount)
            put(COLUMN_USE_HDD_FILE, model.useFile)
            put(COLUMN_SOUND_SELECT, model.selectSound)
            put(COLUMN_SOUND_FILENAME, model.soundFileName)
            put(COLUMN_VOLUME, model.volume)
            put(COLUMN_VIBRATE, model.getVibrateVal())
            put(COLUMN_SNOOZE_TIMER, model.snoozeTime)
            put(COLUMN_SNOOZE_COUNT, model.snoozeCount)
            put(COLUMN_SNOOZE_REPEATED, model.snoozeRepeated)
            put(COLUMN_CREATED_DATETIME, createDateTimeString)
            put(COLUMN_UPDATED_DATETIME, createDateTimeString)
        }

        // データを挿入
        return db.insert(TABLE_NAME, null, contentValues)
    }

    /**
     * データベースから情報を取得する
     * アラーム一覧画面、アラーム発動ダイアログで使用する
     *
     * @param selection 検索条件の文字列
     * @param selectArgs 検索条件に一致する値の配列
     * @return ArrayList<AlarmRecordModel>
     */
    fun readRecordModels(
        selection: String?,
        selectArgs: Array<String>?
    ): ArrayList<AlarmRecordModel> {
        val db = readableDatabase
        // 読み込むカラム
        val projectionArray = arrayOf(
            COLUMN_ID,
            COLUMN_STATE,
            COLUMN_NAME,
            COLUMN_HOUR,
            COLUMN_MIN,
            COLUMN_WEEK_SUN,
            COLUMN_WEEK_MON,
            COLUMN_WEEK_TUE,
            COLUMN_WEEK_WED,
            COLUMN_WEEK_THU,
            COLUMN_WEEK_FRI,
            COLUMN_WEEK_SAT,
            COLUMN_HOLIDAY_COUNT,
            COLUMN_USE_HDD_FILE,
            COLUMN_SOUND_SELECT,
            COLUMN_SOUND_FILENAME,
            COLUMN_VOLUME,
            COLUMN_VIBRATE,
            COLUMN_SNOOZE_TIMER,
            COLUMN_SNOOZE_COUNT,
            COLUMN_SNOOZE_REPEATED
        )

        // DBからデータを読み込む
        val cursor = db.query(
            TABLE_NAME,
            projectionArray,
            selection,
            selectArgs,
            null,
            null,
            "$COLUMN_ID ASC"
        )
        // 取得したデータをAlarmRecordModelに変換
        val recordModelArray = ArrayList<AlarmRecordModel>()
        with(cursor) {
            // 取得データが存在する場合
            while (cursor.moveToNext()) {
                // 曜日の設定
                val repeatWeek: BooleanArray = booleanArrayOf(
                    getInt(getColumnIndexOrThrow(COLUMN_WEEK_SUN)) != 0,
                    getInt(getColumnIndexOrThrow(COLUMN_WEEK_MON)) != 0,
                    getInt(getColumnIndexOrThrow(COLUMN_WEEK_TUE)) != 0,
                    getInt(getColumnIndexOrThrow(COLUMN_WEEK_WED)) != 0,
                    getInt(getColumnIndexOrThrow(COLUMN_WEEK_THU)) != 0,
                    getInt(getColumnIndexOrThrow(COLUMN_WEEK_FRI)) != 0,
                    getInt(getColumnIndexOrThrow(COLUMN_WEEK_SAT)) != 0
                )
                val model = AlarmRecordModel(
                    id = getLong(getColumnIndexOrThrow(COLUMN_ID)).toInt(),
                    state = getInt(getColumnIndexOrThrow(COLUMN_STATE)) != 0,
                    name = getString(getColumnIndexOrThrow(COLUMN_NAME)),
                    hour = getInt(getColumnIndexOrThrow(COLUMN_HOUR)),
                    min = getInt(getColumnIndexOrThrow(COLUMN_MIN)),
                    repeatWeek = repeatWeek,
                    holidayCount = getInt(getColumnIndexOrThrow(COLUMN_HOLIDAY_COUNT)),
                    useFile = getInt(getColumnIndexOrThrow(COLUMN_USE_HDD_FILE)),
                    selectSound = getInt(getColumnIndexOrThrow(COLUMN_SOUND_SELECT)),
                    soundFileName = getString(getColumnIndexOrThrow(COLUMN_SOUND_FILENAME)),
                    volume = getInt(getColumnIndexOrThrow(COLUMN_VOLUME)),
                    vibrate = getInt(getColumnIndexOrThrow(COLUMN_VIBRATE)) != 0,
                    snoozeTime = getInt(getColumnIndexOrThrow(COLUMN_SNOOZE_TIMER)),
                    snoozeCount = getInt(getColumnIndexOrThrow(COLUMN_SNOOZE_COUNT)),
                    snoozeRepeated = getInt(getColumnIndexOrThrow(COLUMN_SNOOZE_REPEATED))
                )
                // 要素を追加
                recordModelArray.add(model)
            }
        }
        cursor.close()

        // 取得した結果
        return recordModelArray
    }

    /**
     * データベースにデータを更新する
     * アラーム設定画面で利用する
     *
     * @param model 設定画面で決定したパラメータ群
     * @return 更新件数(失敗時: 0 or -1)
     */
    fun updateRecordModel(
        model: AlarmRecordModel
    ): Int {
        val db = writableDatabase
        // 現在時刻の文字列の準備
        val calendar = Calendar.getInstance()
        val datetimeFormat = "yyyy/MM/dd HH:mm:ss"
        val updateDateTimeString = android.text.format.DateFormat.format(datetimeFormat, calendar.time) as String
        val repeatWeekArray = model.getRepeatWeekValArray()
        val alarmName = if (model.name.isEmpty()) "" else model.name
        val soundFileName = if (model.soundFileName.isEmpty()) "" else model.soundFileName
        // 更新の条件
        val selection = "$COLUMN_ID = ?"
        val selectionArgsArray = arrayOf(model.id.toString())

        // 値の挿入
        // 挿入する値の決定
        val contentValues = ContentValues().apply {
            put(COLUMN_STATE, model.getStateVal())
            put(COLUMN_NAME, alarmName)
            put(COLUMN_HOUR, model.hour)
            put(COLUMN_MIN, model.min)
            put(COLUMN_WEEK_SUN, repeatWeekArray[0])
            put(COLUMN_WEEK_MON, repeatWeekArray[1])
            put(COLUMN_WEEK_TUE, repeatWeekArray[2])
            put(COLUMN_WEEK_WED, repeatWeekArray[3])
            put(COLUMN_WEEK_THU, repeatWeekArray[4])
            put(COLUMN_WEEK_FRI, repeatWeekArray[5])
            put(COLUMN_WEEK_SAT, repeatWeekArray[6])
            put(COLUMN_HOLIDAY_COUNT, model.holidayCount)
            put(COLUMN_USE_HDD_FILE, model.useFile)
            put(COLUMN_SOUND_SELECT, model.selectSound)
            put(COLUMN_SOUND_FILENAME, soundFileName)
            put(COLUMN_VOLUME, model.volume)
            put(COLUMN_VIBRATE, model.getVibrateVal())
            put(COLUMN_SNOOZE_TIMER, model.snoozeTime)
            put(COLUMN_SNOOZE_COUNT, model.snoozeCount)
            put(COLUMN_SNOOZE_REPEATED, model.snoozeRepeated)
            put(COLUMN_UPDATED_DATETIME, updateDateTimeString)
        }

        // 更新の実行
        return db.update(TABLE_NAME, contentValues, selection, selectionArgsArray)
    }

    /**
     * データベースからデータを削除する
     * アラーム設定画面とアラーム一覧画面で利用する
     *
     * @param id AlarmRecordModelのID
     * @return 削除件数(失敗時: 0)
     */
    fun deleteRecordModel(
        id: Int
    ): Int {
        val db = writableDatabase
        // 削除の条件
        val selection = "$COLUMN_ID = ?"
        val selectionArgsArray = arrayOf(id.toString())

        // 削除の実行
        return db.delete(TABLE_NAME, selection, selectionArgsArray)
    }

    /**
     * 最後に挿入したデータのIDを取得する
     *
     * @return 最後に挿入したデータのID
     */
    fun getLastInsertRowId(): Long {
        val db = readableDatabase
        // 読み込むSQL
        val query = "select seq from SQLITE_SEQUENCE where name = ?"
        val cursor = db.rawQuery(query, arrayOf(TABLE_NAME))
        cursor.moveToLast()
        val id = cursor.getLong(0)
        cursor.close()

        return id
    }
}