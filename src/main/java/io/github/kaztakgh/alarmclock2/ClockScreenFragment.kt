/**
 * @file ClockScreenFragment.kt
 */
package io.github.kaztakgh.alarmclock2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.clock_screen.view.*

/**
 * アプリを起動したときの時計表示の画面
 */
class ClockScreenFragment
    : Fragment() {
    /**
     * インスタンス作成
     * MainPagerで使用するため
     */
    companion object {
        fun newInstance() : ClockScreenFragment {
            return ClockScreenFragment()
        }
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null (which
     * is the default implementation).  This will be called between
     * [.onCreate] and [.onActivityCreated].
     *
     *
     * If you return a View from here, you will later be called in
     * [.onDestroyView] when the view is being released.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return Return the View for the fragment's UI, or null.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Viewのロード
        val view: View = inflater.inflate(R.layout.clock_screen, container, false)
        // アイコンをクリックしたときの動作
        view.isFocusableInTouchMode = true
        view.requestFocus()
        // 設定の読み込み
        val preferences = requireActivity().getSharedPreferences(
            GeneralSettingsFragment.GENERAL_SETTING_PREFERENCE,
            Context.MODE_PRIVATE
        )

        // 時間の表記の指定
        val displaySec = preferences.getBoolean(GeneralSettingsFragment.DATA_CLOCK_SECOND, false)
        // 時計の種類の指定
        when (preferences.getString(GeneralSettingsFragment.DATA_CLOCK_TYPE, CLOCK_TYPE_DEFAULT)) {
            // アナログ時計
            GeneralSettingsFragment.CLOCK_TYPE_ANALOG -> {
                // アナログ時計の表示
                setAnalogClock(view, displaySec)
            }
            // デジタル時計
            GeneralSettingsFragment.CLOCK_TYPE_DIGITAL -> {
                // デジタル時計の表示
                setDigitalClock(view, displaySec)
            }
            // 設定されていない場合
            else -> {
                // デジタル時計の表示
                setDigitalClock(view, displaySec)
            }
        }

        return view
    }

    /**
     * アナログ時計の表示
     *
     * @param view 画面
     * @param displaySec 秒針表示
     */
    private fun setAnalogClock(
        view: View,
        displaySec: Boolean
    ) {
        // アナログ時計のみを表示
        view.analog_clock.visibility = View.VISIBLE
        // 秒針の表示の設定
        view.analog_clock.displaySecond = displaySec
    }

    /**
     * デジタル時計の表示
     *
     * @param view 画面
     * @param displaySec 秒針表示
     */
    private fun setDigitalClock(
        view: View,
        displaySec: Boolean
    ) {
        // デジタル時計のみを表示
        view.digital_clock.visibility = View.VISIBLE
        // 秒表示の設定
        view.digital_clock.displaySecond = displaySec
    }
}