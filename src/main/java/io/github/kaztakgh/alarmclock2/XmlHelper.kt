package io.github.kaztakgh.alarmclock2

import android.content.Context
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.IOException
import java.io.InputStream
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/**
 * XML関連のライブラリ
 * DOMを使用
 * 特定の文字列に依存しない関数をここに記載すること
 */
object XmlHelper {
    /**
     * 名前から属性値を配列形式で取得する
     *
     * @param context Context
     * @param fileName ファイル名(xmlファイル)
     * @param attrName ノードの属性の名前
     * @return ArrayList<String>
     */
    fun getAttributeArrayByNameFromAssets(
        context: Context,
        fileName: String,
        attrName: String
    ) : ArrayList<String> {
        // ファイルの読み込み
        val xmlFileStream: InputStream = kotlin.runCatching {
            context.assets.open(fileName)
        }.fold(
            // 成功時
            onSuccess = {
                // 読み込んだ値を返す
                return@fold it
            },
            // 失敗時
            onFailure = {
                it.printStackTrace()
                throw IOException()
            }
        )

        // DOMParserを利用してXMLファイルを読み込む
        val factory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        val builder: DocumentBuilder = factory.newDocumentBuilder()
        val xmlDocument: Document = builder.parse(xmlFileStream)
        val itemsElement: Element = xmlDocument.documentElement

        // ノードが存在しなかった場合は終了
        if (!itemsElement.hasChildNodes()) return arrayListOf()

        val attrArray = mutableListOf<String>()
        // ノード要素の名前に一致するノードから値をすべて取得する
        getAttrValue(itemsElement, attrName, attrArray)
        val attrValueArray: ArrayList<String> = arrayListOf()
        attrValueArray.addAll(attrArray)
        // 処理が終了したら、ファイルをクローズする
        xmlFileStream.close()
        return attrValueArray
    }

    /**
     * 要素内の属性を検索して、結果を取得する
     *
     * @param element XMLの要素
     * @param attrName 検索する属性名
     * @param attrArray 取得結果を挿入する配列
     * @return attrArray
     */
    private fun getAttrValue(
        element: Element,
        attrName: String,
        attrArray: MutableList<String>
    ) : MutableList<String> {
        loop@ for (i in 0 until element.childNodes.length) {
            // 各要素のアイテムの種類の取得
            val itemNode: Node = element.childNodes.item(i)
            // nodeTypeが要素または属性ではない場合、次のitemを見る
            if (itemNode.nodeType != Node.ELEMENT_NODE
                && itemNode.nodeType != Node.ATTRIBUTE_NODE) continue@loop
            // 子ノードを持つ場合は同じ関数を利用してattributeを取得
            if (itemNode.hasChildNodes()) {
                getAttrValue(itemNode as Element, attrName, attrArray)
            }
            for (j in 0 until itemNode.attributes.length) {
                // attributeNameが検索する属性名と一致する場合、配列に追加
                // 同一ノードに同じattributeNameは挿入できないため、配列に追加したらここのループを抜ける
                if (itemNode.attributes.item(j).nodeName == attrName) {
                    attrArray.add(itemNode.attributes.item(j).nodeValue)
                    continue@loop
                }
            }
        }

        return attrArray
    }
}