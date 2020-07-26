/**
 * @file PageAdapter.kt
 */
package io.github.kaztakgh.alarmclock2

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

/**
 * ViewPagerのPageAdapter
 *
 * @constructor
 *
 * @param fm fragmentManager
 */
class PageAdapter(fm: FragmentManager)
    : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    // タブで表示する内容
    // 対応フラグメント
    private val fragmentArray = arrayListOf(
        ClockScreenFragment::class.java,
        AlarmListFragment::class.java
    )

    /**
     * Return the Fragment associated with a specified position.
     */
    override fun getItem(
        position: Int
    ): Fragment {
        return fragmentArray[position].newInstance()
    }

    /**
     * Return the title of Page.
     *
     * @param position page
     * @return
     */
    override fun getPageTitle(
        position: Int
    ): CharSequence? {
        return null
    }

    /**
     * Called when the host view is attempting to determine if an item's position
     * has changed. Returns [.POSITION_UNCHANGED] if the position of the given
     * item has not changed or [.POSITION_NONE] if the item is no longer present
     * in the adapter.
     *
     *
     * The default implementation assumes that items will never
     * change position and always returns [.POSITION_UNCHANGED].
     *
     * @param object Object representing an item, previously returned by a call to
     * [.instantiateItem].
     * @return object's new position index from [0, [.getCount]),
     * [.POSITION_UNCHANGED] if the object's position has not changed,
     * or [.POSITION_NONE] if the item is no longer present.
     */
    override fun getItemPosition(
        `object`: Any
    ): Int = PagerAdapter.POSITION_NONE

    /**
     * Return the number of views available.
     */
    override fun getCount(): Int = fragmentArray.size

    override fun destroyItem(
        container: ViewGroup,
        position: Int,
        `object`: Any
    ) {
        super.destroyItem(container, position, `object`)
        if (position <= count) {
            val manager = (`object` as Fragment).fragmentManager
            val trans: FragmentTransaction = manager!!.beginTransaction()
            trans.remove(`object`)
            trans.commit()
        }
    }

    fun destroyAllItem(
        pager: ViewPager
    ) {
        for (i in 0 until count - 1) {
            try {
                val obj: Any = this.instantiateItem(pager, i)
                destroyItem(pager, i, obj)
            } catch (e:Exception) { }
        }
    }
}