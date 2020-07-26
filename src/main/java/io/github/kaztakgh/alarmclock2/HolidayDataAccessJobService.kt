/**
 * @file HolidayDataAccessJobService.kt
 */
package io.github.kaztakgh.alarmclock2

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.HttpsURLConnection
import kotlin.collections.ArrayList

/**
 * 祝日データの取得・更新を行うサービス
 */
class HolidayDataAccessJobService
    : JobIntentService() {
    companion object {
        /**
         * タイムアウト時間(msec)
         * 0を指定した場合は無制限
         */
        private const val TIMEOUT_MSEC = 0

        /**
         * 祝日APIのURL
         */
        private const val HOLIDAY_API_URL = "https://holidays-jp.github.io/api/v1/date.json"

        /**
         * 祝日データの定期取得サービスのリクエストコード
         */
        private const val JOB_ID = 400

        /**
         * サービスの実行
         *
         * @param context Context
         * @param intent サービス実行時に送信するデータ
         */
        fun enqueueWork(
            context: Context,
            intent: Intent
        ) {
            enqueueWork(context, HolidayDataAccessJobService::class.java, JOB_ID, intent)
        }
    }

    /**
     * Called serially for each work dispatched to and processed by the service.  This
     * method is called on a background thread, so you can do long blocking operations
     * here.  Upon returning, that work will be considered complete and either the next
     * pending work dispatched here or the overall service destroyed now that it has
     * nothing else to do.
     *
     *
     * Be aware that when running as a job, you are limited by the maximum job execution
     * time and any single or total sequential items of work that exceeds that limit will
     * cause the service to be stopped while in progress and later restarted with the
     * last unfinished work.  (There is currently no limit on execution duration when
     * running as a pre-O plain Service.)
     *
     * @param intent The intent describing the work to now be processed.
     */
    override fun onHandleWork(intent: Intent) {
        var result = false
        // 祝日データの取得
        val jsonText = getHolidaysString()
        // ArrayList<HolidayModel>に変換
        val resultArray = convertToHolidayDataModel(jsonText)
        // DBとの比較
        val dbObject = HolidayDatabaseHelper(this)
        val dbHolidayDataArray = dbObject.readHoliday(null, null)

        // 読み込み時にエラーが発生した場合は削除・登録は行わない
        if (resultArray != null) {
            // 不足分をDBから削除(DB minus result)
            val deleteHolidayDataArray = dbHolidayDataArray.minus(resultArray)
            dbObject.deleteHolidays(deleteHolidayDataArray as ArrayList<HolidayModel>)
            // 追加分を登録(result minus DB)
            val addHolidayDataArray = resultArray.minus(dbHolidayDataArray)
            dbObject.addHolidays(addHolidayDataArray as ArrayList<HolidayModel>)
            result = true
        }
        dbObject.close()

        // 送信先の指定
        val sourceClass = when (intent.getStringExtra(HOLIDAY_DATA_SEND_ACTIVITY)) {
            "HolidayListFragment" -> HOLIDAY_ACCESS_SERVICE
            "MainPagerFragment" -> HOLIDAY_DATA_CREATE_SERVICE
            else -> ""
        }
        // 送信先が指定されている場合
        if (sourceClass.isNotEmpty()) {
            val sendDataIntent = Intent()
            // 取得結果を送信
            sendDataIntent.putExtra(HOLIDAY_DATA_KEY, resultArray)
            sendDataIntent.putExtra(HOLIDAY_DATA_UPDATE_RESULT_KEY, result)

            sendDataIntent.action = sourceClass
            // Receiverにデータを送信
            sendBroadcast(sendDataIntent)
        }
        else {
            // 定期実行の登録
            HolidayDataUpdateReceiver.setUpdate(this)
        }
    }

    /**
     * JSONデータの取得
     *
     * @return JSONデータの文字列
     */
    private fun getHolidaysString(): String? {
        val connection: HttpURLConnection?
        var reader: BufferedReader? = null
        var inputStream: InputStream? = null
        var inputStreamReader: InputStreamReader? = null
        val stringBuffer = StringBuffer("")

        // 祝日APIの読み込み
        val url = URL(HOLIDAY_API_URL)
        connection = url.openConnection() as HttpURLConnection
        // 接続にかかる時間
        connection.connectTimeout = TIMEOUT_MSEC
        // データの読み込みにかかる時間
        connection.readTimeout = TIMEOUT_MSEC
        // HTTPメソッド
        connection.requestMethod = "GET"
        // キャッシュ利用
        connection.useCaches = false
        // GETを利用するため、リクエストのボディの送信をfalseにする
        connection.doOutput = false
        // レスポンスのボディの受信を許可
        connection.doInput = true

        try {
            // データの送信
            connection.connect()

            // 接続結果の取得
            val responseCode = connection.responseCode
            // 取得成功時
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                inputStream = connection.inputStream
                inputStreamReader = InputStreamReader(inputStream!!, "UTF-8")
                reader = BufferedReader(inputStreamReader)
                // 結果を文字列に変換
                var line: String?
                // 各行を挿入する
                while (reader.readLine().also { line = it } != null) {
                    stringBuffer.append(line)
                }
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            return null
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            // リーダーの後始末
            reader?.close()
            inputStreamReader?.close()
            inputStream?.close()
            // 接続を終了する
            connection.disconnect()
        }

        // 結果を文字列に変換?
        return stringBuffer.toString()
    }

    /**
     * HolidayDataModel型のArrayListを作成する
     *
     * @param jsonText APIから取得した結果
     * @return ArrayList<HolidayDataModel> or null
     */
    private fun convertToHolidayDataModel(
        jsonText: String?
    ): ArrayList<HolidayModel>? {
        val holidayArray: ArrayList<HolidayModel> = arrayListOf()
        val jsonObject: JSONObject?

        // nullが入力された場合
        if (jsonText.isNullOrEmpty()) {
            return arrayListOf()
        }

        // JSONObjectへの変換
        try {
            jsonObject = JSONObject(jsonText)
        } catch (e: JSONException) {
            e.printStackTrace()
            return null
        }

        // 祝日データの有効範囲を設定
        // 今の日付から1年間を設定
        val dateFrom = Date()
        val calendarTo = Calendar.getInstance()
        calendarTo.add(Calendar.YEAR, 1)
        val dateTo = calendarTo.time

        // 各要素をArrayListのオブジェクトに変換する
        val keyDate: Iterator<String> = jsonObject.keys()
        run loop@ {
            keyDate.forEach {
                val calendar = Calendar.getInstance()
                // 文字列をDate型に変換
                // nullを取得した場合はループを終了する
                val date = stringToDate(it) ?: return@loop
                calendar.time = date
                // データに入れる日付の範囲の限定
                // 有効範囲ではない場合は次の日付に移動する
                if (date.before(dateFrom) || date.after(dateTo)) {
                    return@forEach
                }
                // データの挿入
                val model = HolidayModel(
                    calendar,
                    jsonObject.getString(it)
                )
                holidayArray.add(model)
            }
        }
        return holidayArray
    }

    /**
     * 文字列からDate型に変換
     *
     * @param dateStr 日付の文字列型(yyyy-MM-dd)
     * @return Date型(失敗時null)
     */
    private fun stringToDate(
        dateStr: String
    ): Date? {
        // フォーマットは固定であるため、直接指定
        val sdFormat = try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        } catch (e: IllegalArgumentException) {
            null
        }
        return sdFormat?.let {
            try {
                it.parse(dateStr)
            } catch (e: ParseException){
                null
            }
        }
    }
}