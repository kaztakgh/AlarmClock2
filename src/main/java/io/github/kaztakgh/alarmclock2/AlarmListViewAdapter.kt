/**
 * @file AlarmListViewAdapter.kt
 */
package io.github.kaztakgh.alarmclock2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.alarm_list_item.view.*

/**
 * AlarmListにデータを表示するためのアダプター
 *
 * @property recordList AlarmRecordModelのリスト
 */
class AlarmListViewAdapter(
    var recordList: ArrayList<AlarmRecordModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    /**
     * 表示対象のRecyclerView
     */
    private var itemsView: RecyclerView? = null

    /**
     * アイテムをクリックしたときの処理<br>
     *     AlarmListFragmentで設定する(Fragmentが関わってくるため)
     */
    var recordClickListener: AlarmListItemViewHolder.ItemClickListener? = null

    /**
     * アイテムを長押ししたときの処理<br>
     *     AlarmListFragmentで設定する(Fragmentが関わってくるため)
     */
    var recordLongClickListener: AlarmListItemViewHolder.ItemLongClickListener? = null

    /**
     * スイッチを切り替えたときの処理<br>
     *     AlarmListFragmentで設定する(Fragmentが関わってくるため)
     */
    var recordStateChangeListener: AlarmListItemViewHolder.StateSwitchClickListener? = null

    private var context: Context? = null

    /**
     * Called by RecyclerView when it starts observing this Adapter.
     *
     *
     * Keep in mind that same adapter may be observed by multiple RecyclerViews.
     *
     * @param recyclerView The RecyclerView instance which started observing this adapter.
     * @see .onDetachedFromRecyclerView
     */
    override fun onAttachedToRecyclerView(
        recyclerView: RecyclerView
    ) {
        super.onAttachedToRecyclerView(recyclerView)
        itemsView = recyclerView
    }

    /**
     * Called by RecyclerView when it stops observing this Adapter.
     *
     * @param recyclerView The RecyclerView instance which stopped observing this adapter.
     * @see .onAttachedToRecyclerView
     */
    override fun onDetachedFromRecyclerView(
        recyclerView: RecyclerView
    ) {
        itemsView = null
        super.onDetachedFromRecyclerView(recyclerView)
    }

    /**
     * Called when RecyclerView needs a new [ViewHolder] of the given type to represent
     * an item.
     *
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     *
     *
     * The new ViewHolder will be used to display items of the adapter using
     * [.onBindViewHolder]. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary [View.findViewById] calls.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return A new ViewHolder that holds a View of the given view type.
     * @see .getItemViewType
     * @see .onBindViewHolder
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        context = parent.context
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.alarm_list_item, parent, false)
        val holder = AlarmListItemViewHolder(view)
        // アイテムをクリックしたときのリスナー
        view.setOnClickListener { v ->
            itemsView.let {
                holder.itemClickListener?.onItemClick(v, it!!.getChildAdapterPosition(v))
            }
        }
        // アイテムを長押ししたときのリスナー
        view.setOnLongClickListener { v ->
            itemsView.let {
                holder.itemLongClickListener?.onItemLongClick(v, it!!.getChildAdapterPosition(v))
                // 通常のクリックイベントを発生させないために、trueを返す
                return@let true
            }
        }
        // スイッチを切り替えたときのリスナー
        view.sw_state.setOnCheckedChangeListener { _, isChecked ->
            itemsView.let {
                holder.stateSwitchClickListener?.onCheckChanged(view, it!!.getChildAdapterPosition(view), isChecked)
            }
        }

        return holder
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int {
        return recordList.size
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the [ViewHolder.itemView] to reflect the item at the given
     * position.
     *
     *
     * Note that unlike [android.widget.ListView], RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the `position` parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use [ViewHolder.getAdapterPosition] which will
     * have the updated adapter position.
     *
     * Override [.onBindViewHolder] instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     * item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val itemViewHolder = holder as AlarmListItemViewHolder
        itemViewHolder.bind(context!!, recordList[position])
        // viewを押下したときの処理
        itemViewHolder.itemClickListener = recordClickListener
        // viewを長押ししたときの処理
        itemViewHolder.itemLongClickListener = recordLongClickListener
        // スイッチを切り替えたときの処理
        itemViewHolder.stateSwitchClickListener = recordStateChangeListener
    }
}