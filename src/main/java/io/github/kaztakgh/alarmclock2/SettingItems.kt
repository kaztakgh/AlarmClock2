/**
 * @file InsertSettingItems.kt
 */
package io.github.kaztakgh.alarmclock2

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import io.github.kaztakgh.settingitemsviewlibrary.*
import io.github.kaztakgh.viewhelper.ViewHelper
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.FileNotFoundException
import java.io.InputStream
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/**
 * SettingItemsViewを使用するための共通関数群
 */
object SettingItems {
    /**
     * assetsフォルダにあるxmlファイルからSettingItemsを作成する
     *
     * @param context Context
     * @param fileName ファイル名(xmlファイル)
     * @return SettingItemsViewの表示項目
     */
    fun readXmlSettingsFromAssets(
        context: Context,
        fileName: String
    ) : ArrayList<ItemInterface> {
        val settingsList = ArrayList<ItemInterface>()
        // ファイルの読み込み
        lateinit var xmlFileStream: InputStream
        try {
            xmlFileStream = context.assets.open(fileName)
        } catch (e: FileNotFoundException) {
            System.err.println(e)
        }

        // DOMParserを利用してXMLファイルを読み込む
        val factory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        val builder: DocumentBuilder = factory.newDocumentBuilder()
        val xmlDocument: Document = builder.parse(xmlFileStream)
        val itemsElement: Element = xmlDocument.documentElement

        // ノードが存在しなかった場合は終了
        if (!itemsElement.hasChildNodes()) return settingsList

        for (i in 0 until itemsElement.childNodes.length) {
            // 各要素のアイテムの種類の取得
            val itemNode: Node = itemsElement.childNodes.item(i)
            // nodeTypeが要素ではない場合、次のitemを見る
            if (itemNode.nodeType != Node.ELEMENT_NODE) continue

            // nodeTypeが要素の場合
            // nodeNameの取得
            // nodeNameによって作成する設定アイテムを分岐する
            when (itemNode.nodeName) {
                // Header
                "header" -> {
                    val item: HeaderItem = createHeaderItem(itemNode)
                    settingsList.add(item)
                }
                // Switch
                "switch" -> {
                    val item: SwitchItem = createSwitchItem(itemNode, context)
                    settingsList.add(item)
                }
                // Spinner
                "spinner" -> {
                    val item: SpinnerItem = createSpinnerItem(itemNode, context)
                    settingsList.add(item)
                }
                // SingleSelect
                "singleSelect" -> {
                    val item: SingleSelectItem = createSingleSelectItem(itemNode, context)
                    settingsList.add(item)
                }
                // MultiSelect
                "multiSelect" -> {
                    val item: MultiSelectItem = createMultiSelectItem(itemNode, context)
                    settingsList.add(item)
                }
                // SeekBar
                "seekBar" -> {
                    val item: SeekBarItem = createSeekBarItem(itemNode, context)
                    settingsList.add(item)
                }
                // Date
                "date" -> {
                    val item: DateItem = createDateItem(itemNode, context)
                    settingsList.add(item)
                }
                // Time
                "time" -> {
                    val item: TimeItem = createTimeItem(itemNode, context)
                    settingsList.add(item)
                }
                // InputText
                "inputText" -> {
                    val item: InputTextItem = createInputTextItem(itemNode, context)
                    settingsList.add(item)
                }
                // StorageFileSelect
                "storageFile" -> {
                    val item: StorageFileSelectItem = createStorageFileSelectItem(itemNode, context)
                    settingsList.add(item)
                }
                // 上記以外
                else -> {}
            }
        }
        // 処理が終了したら、ファイルをクローズする
        xmlFileStream.close()

        return settingsList
    }

    /**
     * xmlファイルからHeaderItemの作成
     *
     * @param node 要素ノード
     * @return SettingItemsViewに表示するHeaderItem
     * @see io.github.kaztakgh.settingitemsviewlibrary.HeaderItem
     */
    private fun createHeaderItem(
        node: Node
    ) : HeaderItem {
        // 要素の初期化
        val element: Element = node as Element
        var title = ""
        val tagName = element.attributes.item(0).nodeValue

        for (i in 0 until element.childNodes.length) {
            val childNode: Node = element.childNodes.item(i)
            // nodeTypeが要素ではない場合、次のitemを見る
            if (childNode.nodeType != Node.ELEMENT_NODE) continue
            // nodeNameによって設定を変更する
            when (childNode.nodeName) {
                // タイトル
                "title" -> {
                    title = childNode.textContent
                }
                else -> {}
            }
        }

        return HeaderItem(
            title = title,
            tag = tagName
        )
    }

    /**
     * xmlファイルからSwitchItemの作成
     *
     * @param node 要素ノード
     * @param context Context
     * @return SettingItemsViewに表示するSwitchItem
     * @see io.github.kaztakgh.settingitemsviewlibrary.SwitchItem
     */
    private fun createSwitchItem(
        node: Node,
        context: Context
    ) : SwitchItem {
        // 要素の初期化
        val element: Element = node as Element
        var title = ""
        var bmpIcon: Bitmap? = null
        var enabled = true
        var checked = false
        var textOn = ""
        var textOff = ""
        val tagName = element.attributes.item(0).nodeValue

        for (i in 0 until element.childNodes.length) {
            val childNode: Node = element.childNodes.item(i)
            // nodeTypeが要素ではない場合、次のitemを見る
            if (childNode.nodeType != Node.ELEMENT_NODE) continue
            // nodeNameによって設定を変更する
            when (childNode.nodeName) {
                // タイトル
                "title" -> {
                    title = childNode.textContent
                }
                // アイコン
                "bmpIcon" -> {
                    // リソースファイル名から画像を取得
                    bmpIcon = findBitmapFromResource(context, childNode.textContent)
                }
                // クリック可能か
                "enabled" -> {
                    enabled = childNode.textContent!!.toBoolean()
                }
                // スイッチ状態
                "checked" -> {
                    checked = childNode.textContent!!.toBoolean()
                }
                // スイッチONの時のテキスト
                "stateOnText" -> {
                    textOn = childNode.textContent
                }
                // スイッチOFFの時のテキスト
                "stateOffText" -> {
                    textOff = childNode.textContent
                }
                else -> {}
            }
        }

        return SwitchItem(
            title = title,
            tag = tagName,
            bmpIcon = bmpIcon,
            enabled = enabled,
            checked = checked,
            textOnTrue = textOn,
            textOnFalse = textOff
        )
    }

    /**
     * xmlファイルからSpinnerItemの作成
     *
     * @param node 要素ノード
     * @param context Context
     * @return SettingItemsViewに表示するSpinnerItem
     * @see io.github.kaztakgh.settingitemsviewlibrary.SpinnerItem
     */
    private fun createSpinnerItem(
        node: Node,
        context: Context
    ) : SpinnerItem {
        // 要素の初期化
        val element: Element = node as Element
        var title = ""
        var bmpIcon: Bitmap? = null
        var enabled = true
        var optionsTextArray: Array<String> = arrayOf()
        var initSelect = 0
        val tagName = element.attributes.item(0).nodeValue

        for (i in 0 until element.childNodes.length) {
            val childNode: Node = element.childNodes.item(i)
            // nodeTypeが要素ではない場合、次のitemを見る
            if (childNode.nodeType != Node.ELEMENT_NODE) continue
            // nodeNameによって設定を変更する
            when (childNode.nodeName) {
                // タイトル
                "title" -> {
                    title = childNode.textContent
                }
                // アイコン
                "bmpIcon" -> {
                    // リソースファイル名から画像を取得
                    bmpIcon = findBitmapFromResource(context, childNode.textContent)
                }
                // クリック可能か
                "enabled" -> {
                    enabled = childNode.textContent!!.toBoolean()
                }
                // 選択肢(テキスト)
                "options" -> {
                    val optionsArray: Array<XmlOptions.ValueSet> = XmlOptions.parse(childNode, context).toTypedArray()
                    optionsTextArray = optionsArray.map { it.text }.toTypedArray()
                }
                // 初期選択状態
                "select" -> {
                    initSelect = childNode.textContent.toInt()
                }
            }
        }

        return SpinnerItem(
            title = title,
            tag = tagName,
            bmpIcon = bmpIcon,
            enabled = enabled,
            options = optionsTextArray,
            select = initSelect
        )
    }

    /**
     * xmlファイルからSingleSelectItemの作成
     *
     * @param node 要素ノード
     * @param context Context
     * @return SettingItemsViewに表示するSingleSelectItem
     * @see io.github.kaztakgh.settingitemsviewlibrary.SingleSelectItem
     */
    private fun createSingleSelectItem(
        node: Node,
        context: Context
    ) : SingleSelectItem {
        //
        // 要素の初期化
        val element: Element = node as Element
        var title = ""
        var bmpIcon: Bitmap? = null
        var enabled = true
        var optionsTextArray: Array<String>? = null
        var optionsBitmapArray: Array<Bitmap>? = null
        var optionsSoundArray: IntArray? = null
        var optionsSelectableArray: BooleanArray? = null
        var initSelect = 0
        var requestCode = 1
        val tagName = element.attributes.item(0).nodeValue

        for (i in 0 until element.childNodes.length) {
            val childNode: Node = element.childNodes.item(i)
            // nodeTypeが要素ではない場合、次のitemを見る
            if (childNode.nodeType != Node.ELEMENT_NODE) continue
            // nodeNameによって設定を変更する
            when (childNode.nodeName) {
                // タイトル
                "title" -> {
                    title = childNode.textContent
                }
                // アイコン
                "bmpIcon" -> {
                    // リソースファイル名から画像を取得
                    bmpIcon = findBitmapFromResource(context, childNode.textContent)
                }
                // クリック可能か
                "enabled" -> {
                    enabled = childNode.textContent!!.toBoolean()
                }
                // 選択肢
                "options" -> {
                    val optionsArray: Array<XmlOptions.ValueSet> =
                        XmlOptions.parse(childNode, context).toTypedArray()
                    // 各要素の配列に分離
                    optionsTextArray = optionsArray.map { it.text }.toTypedArray()
//                    optionsBitmapArray = optionsArray.map { it.bitmap }.toTypedArray()
                    optionsSoundArray = optionsArray.map { it.soundId }.toIntArray()
                    optionsSelectableArray = optionsArray.map { it.selectable }.toBooleanArray()
                }
                // 初期選択状態
                "select" -> {
                    initSelect = childNode.textContent.toInt()
                }
                // リクエストコード
                "requestCode" -> {
                    requestCode = childNode.textContent.toInt()
                }
            }
        }

        return SingleSelectItem(
            title = title,
            tag = tagName,
            bmpIcon = bmpIcon,
            enabled = enabled,
            options = optionsTextArray,
            optionBitmaps = optionsBitmapArray,
            optionSounds = optionsSoundArray,
            optionSelectable = optionsSelectableArray,
            select = initSelect,
            requestCode = requestCode
        )
    }

    /**
     * xmlファイルからMultiSelectItemの作成
     *
     * @param node 要素ノード
     * @param context Context
     * @return SettingItemsViewに表示するMultiSelectItem
     * @see io.github.kaztakgh.settingitemsviewlibrary.MultiSelectItem
     */
    private fun createMultiSelectItem(
        node: Node,
        context: Context
    ) : MultiSelectItem {
        // 要素の初期化
        val element: Element = node as Element
        var title = ""
        var bmpIcon: Bitmap? = null
        var enabled = true
        var optionsTextArray: Array<String>? = null
        var optionsBitmapArray: Array<Bitmap>? = null
        var optionsSoundArray: IntArray? = null
        var optionsSelectableArray: BooleanArray? = null
        var nothingSelectedText = ""
        var requestCode = 1
        val tagName = element.attributes.item(0).nodeValue

        for (i in 0 until element.childNodes.length) {
            val childNode: Node = element.childNodes.item(i)
            // nodeTypeが要素ではない場合、次のitemを見る
            if (childNode.nodeType != Node.ELEMENT_NODE) continue
            // nodeNameによって設定を変更する
            when (childNode.nodeName) {
                // タイトル
                "title" -> {
                    title = childNode.textContent
                }
                // アイコン
                "bmpIcon" -> {
                    // リソースファイル名から画像を取得
                    bmpIcon = findBitmapFromResource(context, childNode.textContent)
                }
                // クリック可能か
                "enabled" -> {
                    enabled = childNode.textContent!!.toBoolean()
                }
                // 選択肢(テキスト)
                "options" -> {
                    val optionsArray: Array<XmlOptions.ValueSet> = XmlOptions.parse(childNode, context).toTypedArray()
                    // 各要素の配列に分離
                    optionsTextArray = optionsArray.map { it.text }.toTypedArray()
//                    optionsBitmapArray = optionsArray.map { it.bitmap }.toTypedArray()
                    optionsSoundArray = optionsArray.map { it.soundId }.toIntArray()
                    optionsSelectableArray = optionsArray.map { it.selectable }.toBooleanArray()
                }
                // 選択されていない状態の場合のテキスト
                "nothingSelectedText" -> {
                    nothingSelectedText = childNode.textContent
                }
                // リクエストコード
                "requestCode" -> {
                    requestCode = childNode.textContent.toInt()
                }
            }
        }

        return MultiSelectItem(
            title = title,
            tag = tagName,
            bmpIcon = bmpIcon,
            enabled = enabled,
            options = optionsTextArray,
            optionBitmaps = optionsBitmapArray,
            optionSounds = optionsSoundArray,
            optionSelectable = optionsSelectableArray,
            nothingsSelectString = nothingSelectedText,
            requestCode = requestCode
        )
    }

    /**
     * xmlファイルからSeekBarItemの作成
     *
     * @param node 要素ノード
     * @param context Context
     * @return SettingItemsViewに表示するSeekBarItem
     * @see io.github.kaztakgh.settingitemsviewlibrary.SeekBarItem
     */
    private fun createSeekBarItem(
        node: Node,
        context: Context
    ) : SeekBarItem {
        // 要素の初期化
        val element: Element = node as Element
        var title = ""
        var bmpIcon: Bitmap? = null
        var enabled = true
        var max = 100
        var min = 0
        var state = 0
        var div = 1
        var unit = ""
        var paramsArray: IntArray? = null
        val tagName = element.attributes.item(0).nodeValue

        for (i in 0 until element.childNodes.length) {
            val childNode: Node = element.childNodes.item(i)
            // nodeTypeが要素ではない場合、次のitemを見る
            if (childNode.nodeType != Node.ELEMENT_NODE) continue
            // nodeNameによって設定を変更する
            when (childNode.nodeName) {
                // タイトル
                "title" -> {
                    title = childNode.textContent
                }
                // アイコン
                "bmpIcon" -> {
                    // リソースファイル名から画像を取得
                    bmpIcon = findBitmapFromResource(context, childNode.textContent)
                }
                // クリック可能か
                "enabled" -> {
                    enabled = childNode.textContent!!.toBoolean()
                }
                // 表記最大値
                "max" -> {
                    max = childNode.textContent.toInt()
                }
                // 表記最小値
                "min" -> {
                    min = childNode.textContent.toInt()
                }
                // 初期値
                "init" -> {
                    state = childNode.textContent.toInt()
                }
                // 分割値
                "divide" -> {
                    div = childNode.textContent.toInt()
                }
                // 単位
                "unit" -> {
                    unit = childNode.textContent
                }
                // ステータスの配列
                "parameters" -> {
                    //
                }
            }
        }

        return SeekBarItem(
            title = title,
            tag = tagName,
            bmpIcon = bmpIcon,
            enabled = enabled,
            max = max,
            min = min,
            state = state,
            div = div,
            unit = unit,
            paramsArray = paramsArray
        )
    }

    private fun createDateItem(
        node: Node,
        context: Context
    ) : DateItem {
        // 要素の初期化
        val element: Element = node as Element
        var title = ""
        var bmpIcon: Bitmap? = null
        var enabled = true
        var format = "yyyy/MM/dd"
        var requestCode = 1
        val tagName = element.attributes.item(0).nodeValue

        for (i in 0 until element.childNodes.length) {
            val childNode: Node = element.childNodes.item(i)
            // nodeTypeが要素ではない場合、次のitemを見る
            if (childNode.nodeType != Node.ELEMENT_NODE) continue
            // nodeNameによって設定を変更する
            when (childNode.nodeName) {
                // タイトル
                "title" -> {
                    title = childNode.textContent
                }
                // アイコン
                "bmpIcon" -> {
                    // リソースファイル名から画像を取得
                    bmpIcon = findBitmapFromResource(context, childNode.textContent)
                }
                // クリック可能か
                "enabled" -> {
                    enabled = childNode.textContent!!.toBoolean()
                }
                // フォーマット
                "format" -> {
                    format = childNode.textContent
                }
                // リクエストコード
                "requestCode" -> {
                    requestCode = childNode.textContent.toInt()
                }
                else -> {}
            }
        }

        return DateItem(
            title = title,
            tag = tagName,
            bmpIcon = bmpIcon,
            enabled = enabled,
            format = format,
            requestCode = requestCode
        )
    }

    /**
     * xmlファイルからTimeItemの作成
     *
     * @param node 要素ノード
     * @param context Context
     * @return SettingItemsViewに表示するTimeItem
     * @see io.github.kaztakgh.settingitemsviewlibrary.TimeItem
     */
    private fun createTimeItem(
        node: Node,
        context: Context
    ) : TimeItem {
        // 要素の初期化
        val element: Element = node as Element
        var title = ""
        var bmpIcon: Bitmap? = null
        var enabled = true
        var format24h = "H:mm"
        var format12h = "h:mm a"
        var requestCode = 1
        val tagName = element.attributes.item(0).nodeValue

        for (i in 0 until element.childNodes.length) {
            val childNode: Node = element.childNodes.item(i)
            // nodeTypeが要素ではない場合、次のitemを見る
            if (childNode.nodeType != Node.ELEMENT_NODE) continue
            // nodeNameによって設定を変更する
            when (childNode.nodeName) {
                // タイトル
                "title" -> {
                    title = childNode.textContent
                }
                // アイコン
                "bmpIcon" -> {
                    // リソースファイル名から画像を取得
                    bmpIcon = findBitmapFromResource(context, childNode.textContent)
                }
                // クリック可能か
                "enabled" -> {
                    enabled = childNode.textContent!!.toBoolean()
                }
                // 24時間表記
                "format24h" -> {
                    format24h = childNode.textContent
                }
                // 12時間表記
                "format12h" -> {
                    format12h = childNode.textContent
                }
                // リクエストコード
                "requestCode" -> {
                    requestCode = childNode.textContent.toInt()
                }
                else -> {}
            }
        }

        return TimeItem(
            title = title,
            tag = tagName,
            bmpIcon = bmpIcon,
            enabled = enabled,
            display24h = SettingsHelper.is24HFormatUsing(context),
            format24h = format24h,
            format12h = format12h,
            requestCode = requestCode
        )
    }

    /**
     * xmlファイルからInputTextItemの作成
     *
     * @param node 要素ノード
     * @param context Context
     * @return SettingItemsViewに表示するInputTextItem
     * @see io.github.kaztakgh.settingitemsviewlibrary.InputTextItem
     */
    private fun createInputTextItem(
        node: Node,
        context: Context
    ) : InputTextItem {
        // 要素の初期化
        val element: Element = node as Element
        var title = ""
        var bmpIcon: Bitmap? = null
        var enabled = true
        var text = ""
        var length = INIT_TEXT_LENGTH
        val tagName = element.attributes.item(0).nodeValue

        for (i in 0 until element.childNodes.length) {
            val childNode: Node = element.childNodes.item(i)
            // nodeTypeが要素ではない場合、次のitemを見る
            if (childNode.nodeType != Node.ELEMENT_NODE) continue
            // nodeNameによって設定を変更する
            when (childNode.nodeName) {
                // タイトル
                "title" -> {
                    title = childNode.textContent
                }
                // アイコン
                "bmpIcon" -> {
                    // リソースファイル名から画像を取得
                    bmpIcon = findBitmapFromResource(context, childNode.textContent)
                }
                // クリック可能か
                "enabled" -> {
                    enabled = childNode.textContent!!.toBoolean()
                }
                // テキスト
                "text" -> {
                    text = childNode.textContent
                }
                // 長さ
                "length" -> {
                    length = childNode.textContent.toInt()
                }
                else -> {}
            }
        }

        return InputTextItem(
            title = title,
            tag = tagName,
            bmpIcon = bmpIcon,
            enabled = enabled,
            text = text,
            length = length
        )
    }

    /**
     * xmlファイルからStorageFileSelectItemの作成
     *
     * @param node 要素ノード
     * @param context Context
     * @return SettingItemsViewに表示するStorageFileSelectItem
     * @see io.github.kaztakgh.settingitemsviewlibrary.StorageFileSelectItem
     */
    private fun createStorageFileSelectItem(
        node: Node,
        context: Context
    ) : StorageFileSelectItem {
        // 要素の初期化
        val element: Element = node as Element
        var title = ""
        var bmpIcon: Bitmap? = null
        var enabled = true
        var uri: Uri? = null
        var mimeType = "*/*"
        var requestCode = 1
        val tagName = element.attributes.item(0).nodeValue

        for (i in 0 until element.childNodes.length) {
            val childNode: Node = element.childNodes.item(i)
            // nodeTypeが要素ではない場合、次のitemを見る
            if (childNode.nodeType != Node.ELEMENT_NODE) continue
            // nodeNameによって設定を変更する
            when (childNode.nodeName) {
                // タイトル
                "title" -> {
                    title = childNode.textContent
                }
                // アイコン
                "bmpIcon" -> {
                    // リソースファイル名から画像を取得
                    bmpIcon = findBitmapFromResource(context, childNode.textContent)
                }
                // クリック可能か
                "enabled" -> {
                    enabled = childNode.textContent!!.toBoolean()
                }
                // URI
                "uri" -> {
                    uri = Uri.parse(childNode.textContent)
                }
                // ファイル種別
                "mimeType" -> {
                    mimeType = childNode.textContent
                }
                // リクエストコード
                "requestCode" -> {
                    requestCode = childNode.textContent.toInt()
                }
                else -> {}
            }
        }

        return StorageFileSelectItem(
            title = title,
            tag = tagName,
            bmpIcon = bmpIcon,
            enabled = enabled,
            uri = uri,
            mimeType = mimeType,
            requestCode = requestCode
        )
    }

    /**
     * ファイル名からBitmapデータを取得する
     * 取得できなかった場合はnullを返す
     *
     * @param context Context
     * @param bmpFileName リソース内の画像ファイル名
     * @return bitmap: 画像が存在する場合
     * @return null: 画像が存在しない場合
     */
    private fun findBitmapFromResource (
        context: Context,
        bmpFileName: String
    ) : Bitmap? {
        val resourceFileNameArray : List<String> = ResourceHelper.splitClassAndId(bmpFileName)
        if (resourceFileNameArray === emptyList<String>()) return null

        // リソースファイルIDの取得
        val typeName : String = Regex(pattern = "@+")
            .replace(resourceFileNameArray[0], "")
        val fileName : String = resourceFileNameArray[1]
        val fileId : Int = context.resources.getIdentifier(fileName, typeName, context.packageName)
        // リソースファイルIDが0の場合は次のノードを見る
        if (fileId == 0) return null

        // bitmapへの変換
        val drawable: Drawable? = context.getDrawable(fileId)
        return ViewHelper.drawableToBitmap(drawable!!)
    }

    private const val INIT_TEXT_LENGTH = 64
}