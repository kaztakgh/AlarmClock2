/**
 * @file MainPagerViewModel.kt
 */
package io.github.kaztakgh.alarmclock2

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ページ位置の保存と読み込み
 */
class MainPagerViewModel
    : ViewModel() {
    /**
     * ページ位置
     */
    var pageIndex: MutableLiveData<Int> = MutableLiveData()

    /**
     * データ更新
     */
//    var isModified: MutableLiveData<Boolean> = MutableLiveData()

    /**
     * 初期化
     */
    init {
        pageIndex.value = 0
//        isModified.value = false
    }
}