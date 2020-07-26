package io.github.kaztakgh.alarmclock2

import android.content.Intent
import android.os.Bundle
import androidx.core.app.AppLaunchChecker
import androidx.fragment.app.Fragment

class MainActivity
    : ApplicationBaseActivity() {
    companion object {
        /**
         * 起動済みであるかのデータ送信キー
         */
        const val KEY_IS_STARTED = "isStarted"
    }

    /**
     * 起動済みかの確認
     */
    private var hasStarted = true

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        // 起動済みかのチェック
        hasStarted = AppLaunchChecker.hasStartedFromLauncher(this)

        // 初回起動時orストレージ消去後に起動時
        if (!hasStarted) {
            // 起動済みに設定する
            AppLaunchChecker.onActivityCreate(this)
        }

        if(savedInstanceState == null) {
            // fragment機能の搭載
            val mainPagerFragment = MainPagerFragment()
            val bundle = Bundle()
            bundle.putBoolean(KEY_IS_STARTED, hasStarted)
            mainPagerFragment.arguments = bundle
            supportFragmentManager.beginTransaction()
                .add(R.id.main_container, mainPagerFragment)
                .commit()
        }
    }

    /**
     * Dispatch incoming result to the correct fragment.
     */
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        // 各fragmentのActivityResultを確認する
        val fragments: List<Fragment> = supportFragmentManager.fragments
        for (fragment in fragments) {
            fragment.onActivityResult(requestCode, resultCode, data)
        }
    }
}
