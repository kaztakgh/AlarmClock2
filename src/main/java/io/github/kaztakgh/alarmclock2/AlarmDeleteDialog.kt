/**
 * @file AlarmDeleteDialog.kt
 */
package io.github.kaztakgh.alarmclock2

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

/**
 * アラームを消去するときに表示するダイアログ
 *
 * @see DialogFragment
 */
class AlarmDeleteDialog
    : DialogFragment() {
    companion object {
        /**
         * リクエストコードのキー
         */
        const val DIALOG_REQUEST_CODE = "reqCode"
    }

    /**
     * ダイアログを選択したときの処理
     */
    private var selectListener: OnAlarmDeleteDialogSelectListener? = null

    override fun onAttach(
        context: Context
    ) {
        super.onAttach(context)
        val fragment = targetFragment
        try {
            // fragmentが存在するはフラグメントにリスナを紐づける
            // 存在しない場合はコンテキスト(Activity)にリスナを紐づける
            this.selectListener =
                if (fragment != null) fragment as OnAlarmDeleteDialogSelectListener?
                else context as OnAlarmDeleteDialogSelectListener
        } catch (e: ClassCastException) {
            // クラスキャストに失敗した場合のエラー処理
            e.printStackTrace()
        }
    }

    override fun onDetach() {
        super.onDetach()
        this.selectListener = null
    }

    /**
     * Override to build your own custom Dialog container.  This is typically
     * used to show an AlertDialog instead of a generic Dialog; when doing so,
     * [.onCreateView] does not need
     * to be implemented since the AlertDialog takes care of its own content.
     *
     *
     * This method will be called after [.onCreate] and
     * before [.onCreateView].  The
     * default implementation simply instantiates and returns a [Dialog]
     * class.
     *
     *
     * *Note: DialogFragment own the [ Dialog.setOnCancelListener][Dialog.setOnCancelListener] and [ Dialog.setOnDismissListener][Dialog.setOnDismissListener] callbacks.  You must not set them yourself.*
     * To find out about these events, override [.onCancel]
     * and [.onDismiss].
     *
     * @param savedInstanceState The last saved instance state of the Fragment,
     * or null if this is a freshly created Fragment.
     *
     * @return Return a new Dialog instance to be displayed by the Fragment.
     */
    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {
        // ダイアログビルダーの作成
        val builder = activity?.let { AlertDialog.Builder(it) }
        val resources = requireActivity().resources
        val reqCode: Int = requireArguments().getInt(DIALOG_REQUEST_CODE)

        // テキストのセット
        builder!!.setMessage(resources.getText(R.string.alarm_record_delete_dialog_text))
        // 消去実行
        builder.setPositiveButton(resources.getText(R.string.alarm_record_delete_yes)) { dialog, which ->
            selectListener?.onExecuteDelete(reqCode, which)
            dialog.dismiss()
        }
        // 消去キャンセル
        builder.setNegativeButton(resources.getText(R.string.alarm_record_delete_no)) { dialog, _ ->
            dialog.dismiss()
        }

        // ダイアログはキャンセル可能
        this.isCancelable = true

        // ダイアログの作成
        return builder.create()
    }
}