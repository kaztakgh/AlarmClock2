/**
 * @file XmlOptions.kt
 */
package io.github.kaztakgh.alarmclock2

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import io.github.kaztakgh.viewhelper.ViewHelper
import org.w3c.dom.Element
import org.w3c.dom.Node

/**
 * XMLの選択肢形式要素の解析
 */
object XmlOptions {
    /**
     * XMLノードの解析から、選択肢のArrayListを作成する
     *
     * @param node ノードオブジェクト
     * @param context Context
     * @return 選択肢のArrayList
     */
    fun parse(
        node: Node,
        context: Context
    ) : ArrayList<ValueSet> {
        // 要素の初期化
        val element: Element = node as Element
        // 選択肢の集合
        val valueSetArrayList: ArrayList<ValueSet> = ArrayList()
        for (i in 0 until element.childNodes.length) {
            val elementChildNode: Node = element.childNodes.item(i)
            // nodeTypeが要素ではないかノード名がitemではない場合、次のitemを見る
            if (elementChildNode.nodeType != Node.ELEMENT_NODE
                || elementChildNode.nodeName != "item") continue

            // XMLから値を入れていく
            val values = ValueSet()

            // itemの内部の項目を解析する
            loop@ for (j in 0 until elementChildNode.childNodes.length) {
                val itemNode: Node = elementChildNode.childNodes.item(j)
                if (itemNode.nodeType != Node.ELEMENT_NODE) continue
                when (itemNode.nodeName) {
                    // テキスト
                    "text" -> {
                        val resourceTextId =
                            ResourceHelper.convertResourceFileNameToId(context, itemNode.textContent)
                        // 通常のテキストの場合
                        if (resourceTextId == 0) {
                            values.text = itemNode.textContent
                        }
                        // リソースから取得可能な場合、リソースから文字列に変換
                        else {
                            values.text = context.resources.getString(resourceTextId)
                        }
                    }
                    // 画像
                    "bitmap" -> {
                        // リソースファイルIDの取得
                        val resourceBitmapId : Int =
                            ResourceHelper.convertResourceFileNameToId(context, itemNode.textContent)
                        // リソースファイルIDが0の場合は次のノードを見る
                        if (resourceBitmapId == 0) continue@loop

                        // bitmapへの変換
                        val drawable: Drawable? = context.getDrawable(resourceBitmapId)
                        val bitmap: Bitmap = ViewHelper.drawableToBitmap(drawable!!)
                        values.bitmap = bitmap
                    }
                    // 音
                    "soundId" -> {
                        val resourceSoundId =
                            ResourceHelper.convertResourceFileNameToId(context, itemNode.textContent)
                        // リソースファイルIDが0の場合は次のノードを見る
                        if (resourceSoundId == 0) continue@loop

                        // 取得したリソースファイルIDを代入
                        values.soundId = resourceSoundId
                    }
                    // 選択可能であるか
                    "selectable" -> {
                        values.selectable = itemNode.textContent!!.toBoolean()
                    }
                    else -> {}
                }
            }
            valueSetArrayList.add(values)
        }

        return valueSetArrayList
    }

    /**
     * XMLの解析から導き出される値の集合クラス
     */
    class ValueSet {
        /**
         * テキスト
         */
        var text: String = ""
        /**
         * アイコン
         */
        var bitmap: Bitmap? = null
        /**
         * 音楽リソースID
         */
        var soundId: Int = -1
        /**
         * 選択可能であるか
         */
        var selectable: Boolean = true
    }
}