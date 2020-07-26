package io.github.kaztakgh.alarmclock2

import android.os.Bundle

/**
 * アラーム消去のダイアログのリスナー
 */
interface OnAlarmDeleteDialogSelectListener {
    /**
     * 削除を実行する場合
     *
     * @param reqCode
     * @param resultCode
     */
    fun onExecuteDelete(
        reqCode: Int,
        resultCode: Int
    )
}