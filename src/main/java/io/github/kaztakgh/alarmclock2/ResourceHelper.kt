/**
 * @file ResourceHelper.kt
 */
package io.github.kaztakgh.alarmclock2

import android.content.Context

/**
 * リソース関連のライブラリ
 * 特定の文字列を使うことを前提とするような関数は、ここには書かないこと
 */
object ResourceHelper {
    /**
     * リソースファイル名からresourceIdを特定する<br>
     * 有効なリソースではない場合は0を返す
     *
     * @param context Context
     * @param resourceFileName リソースファイル名
     * @return resourceId
     */
    fun convertResourceFileNameToId(
        context: Context,
        resourceFileName: String
    ) : Int {
        // リソースファイル名の分割
        val resourceFileNameArray : List<String> = splitClassAndId(resourceFileName)
        if (resourceFileNameArray === emptyList<String>()) return 0

        // リソースファイルIDの取得
        val typeName : String = Regex(pattern = "@+")
            .replace(resourceFileNameArray[0], "")
        val fileName : String = resourceFileNameArray[1]

        return context.resources.getIdentifier(fileName, typeName, context.packageName)
    }

    /**
     * resourceFileを示す@id/nameをリストに変換
     *
     * @param resourceName リソースファイル名
     * @return typeとresourceIdNameのリスト
     * @return emptyList(ファイル名がresourceのものではない場合)
     */
    fun splitClassAndId(
        resourceName: String
    ) : List<String> {
        // @id/nameの形式になるようにリソースファイル名をチェック
        val regexFileName = Regex(pattern = "@[ -~]+/[ -~]+")
            .find(resourceName)?.value
        // 形式が異なる場合、空配列を返す
        if (regexFileName === null) {
            return emptyList()
        }

        return resourceName.split(Regex("/"), 0)
    }
}