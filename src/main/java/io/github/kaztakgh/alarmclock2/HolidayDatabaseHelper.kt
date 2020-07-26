/**
 * @file HolidayDataDatabaseHelper.kt
 */
package io.github.kaztakgh.alarmclock2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteStatement
import io.github.kaztakgh.alarmclock2.HolidayTableObject.RecordColumns.Companion.COLUMN_DATE
import io.github.kaztakgh.alarmclock2.HolidayTableObject.RecordColumns.Companion.COLUMN_NAME
import io.github.kaztakgh.alarmclock2.HolidayTableObject.RecordColumns.Companion.TABLE_NAME
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 祝日データの記録
 *
 * @constructor
 * @see SQLiteOpenHelper
 *
 * @param context Context
 */
class HolidayDatabaseHelper(
    context: Context
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        /**
         * データベースのバージョン
         */
        const val DATABASE_VERSION = 1

        /**
         * データベースのファイル名
         */
        const val DATABASE_NAME = "Holiday.db"

        /**
         * テーブルを作成するSQL
         */
        private const val SQL_CREATE =
            "CREATE TABLE IF NOT EXISTS $TABLE_NAME ($COLUMN_DATE TEXT PRIMARY KEY, $COLUMN_NAME TEXT)"

        /**
         * テーブルを削除するSQL
         */
        private const val SQL_DELETE = "DROP TABLE IF EXISTS $TABLE_NAME"

        /**
         * カラム数
         */
        private const val COLUMN_NUM = 2
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    override fun onCreate(db: SQLiteDatabase?) {
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
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(SQL_DELETE)
        onCreate(db)
    }

    /**
     * データベースにデータを挿入する
     * 定期的に(月に1回)実行
     *
     * @param model 取得データ群
     * @return データのID(失敗時: -1)
     */
    fun addHoliday(
        model: HolidayModel
    ) : Long {
        val db = writableDatabase
        // Calendar型を文字列に変換
        val dateFormat = "yyyy-MM-dd"
        val dateString = android.text.format.DateFormat.format(dateFormat, model.date.time) as String
        // 挿入する値の決定
        val contentValues = ContentValues().apply {
            put(COLUMN_DATE, dateString)
            put(COLUMN_NAME, model.name)
        }
        // データを挿入
        return db.insert(TABLE_NAME, null, contentValues)
    }

    /**
     * データベースにデータを挿入する
     * 定期的に(月に1回)実行
     *
     * @param models 取得データ群
     * @return 最後に登録したデータのID(失敗時: -1)
     */
    fun addHolidays(
        models: ArrayList<HolidayModel>
    ) : Long {
        // データが存在しない場合は終了する
        if (models.size == 0) {
            return 0
        }

        val db = writableDatabase
        // Calendar型を文字列に変換
        val dateFormat = "yyyy-MM-dd"
        var result: Long = 0

        // トランザクション開始
        db.beginTransaction()
        try {
            // SQL文の作成
            val sql = StringBuilder("INSERT INTO $TABLE_NAME VALUES (?, ?)")
            for (i in 0 until models.size - 1) {
                sql.append(", (?, ?)")
            }

            // 値の挿入
            val insertStatement: SQLiteStatement = db.compileStatement(sql.toString())
            insertStatement.use { statement ->
                models.forEachIndexed { index, model ->
                    val valIndex = COLUMN_NUM * index
                    val dateString = android.text.format.DateFormat.format(dateFormat, model.date.time) as String
                    statement.bindString(valIndex + 1, dateString)
                    statement.bindString(valIndex + 2, model.name)
                }
                result = statement.executeInsert()
            }

            // 挿入に成功した場合、setTransactionSuccessfulで成功通知が必要
            if (result != (-1).toLong()) {
                db.setTransactionSuccessful()
            }
        } finally {
            // トランザクション終了
            db.endTransaction()
        }

        return result
    }

    /**
     * 祝日データの読み込み
     *
     * @param selection 検索条件の文字列<br>不要な場合はnullを指定
     * @param selectArgs 検索条件に一致する値の配列<br>不要な場合はnullを指定
     * @return ArrayList<HolidayDataModel>
     */
    fun readHoliday(
        selection: String? = null,
        selectArgs: Array<String>? = null
    ): ArrayList<HolidayModel> {
        val db = readableDatabase
        // 読み込むカラム
        val projectionArray = arrayOf(
            COLUMN_DATE,
            COLUMN_NAME
        )
        // DBからデータを読み込む
        val cursor = db.query(
            TABLE_NAME,
            projectionArray,
            selection,
            selectArgs,
            null,
            null,
            "$COLUMN_DATE ASC"
        )

        // 取得した結果をHolidayDataModelに変換
        val holidayDataArray = arrayListOf<HolidayModel>()
        with(cursor) {
            while (cursor.moveToNext()) {
                val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val modelDate = dateFormat.parse(getString(getColumnIndexOrThrow(COLUMN_DATE)))!!
                calendar.time = modelDate
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val model = HolidayModel(
                    date = calendar,
                    name = getString(getColumnIndexOrThrow(COLUMN_NAME))
                )
                // 要素を追加
                holidayDataArray.add(model)
            }
        }
        cursor.close()

        return holidayDataArray
    }

    /**
     * 祝日データの削除
     * 定期的に(月に1回)実行
     *
     * @param date 削除対象の日付
     * @return 削除件数(失敗時: 0)
     */
    fun deleteHoliday(
        date: String
    ): Int {
        val db = writableDatabase
        // 削除の条件
        val selection = "$COLUMN_DATE = ?"
        val selectionArgsArray = arrayOf(date)
        // 削除の実行
        return db.delete(TABLE_NAME, selection, selectionArgsArray)
    }

    /**
     * 祝日データの削除
     * 定期的に(月に1回)実行
     *
     * @param deleteModels 削除対象の日付の配列
     * @return 削除件数(失敗時: 0)
     */
    fun deleteHolidays(
        deleteModels: ArrayList<HolidayModel>
    ) : Int {
        // 削除対象の日付が存在しない場合
        if (deleteModels.size == 0) {
            return 0
        }

        val db = writableDatabase
        // 削除の条件
        // ?を日数分用意する
        val selectionBuilder = StringBuilder()
        selectionBuilder.append("$COLUMN_DATE in (")
            .append("?, ".repeat(deleteModels.size - 1))
            .append("?")
            .append(")")
        // 値の挿入
        val selectionArray = arrayOfNulls<String>(deleteModels.size)
        val dateArray = arrayListOf<String>()
        deleteModels.forEach {
            dateArray.add(android.text.format.DateFormat.format("yyyy-MM-dd", it.date).toString())
        }

        // 削除の実行
        // TODO:selectionArrayの挿入チェック
        return db.delete(TABLE_NAME, selectionBuilder.toString(), deleteModels.toArray(selectionArray))
    }
}