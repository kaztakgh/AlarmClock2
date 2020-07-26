/**
 * @file HolidayDataModel.kt
 */
package io.github.kaztakgh.alarmclock2

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * 祝日のデータモデル
 * テーブルではないが、祝日一覧として利用する
 *
 * @property date 日付
 * @property name 祝日名
 */
data class HolidayModel(
    val date: Calendar,
    val name: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readSerializable() as Calendar,
        parcel.readString() as String
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeSerializable(date)
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HolidayModel> {
        override fun createFromParcel(parcel: Parcel): HolidayModel {
            return HolidayModel(parcel)
        }

        override fun newArray(size: Int): Array<HolidayModel?> {
            return arrayOfNulls(size)
        }
    }
}