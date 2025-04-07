package com.example.nutrifill.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.example.nutrifill.models.Nutrients

data class ScanHistoryItem(
    @SerializedName("_id") val id: String = "",
    @SerializedName("foodName") val foodName: String = "",
    @SerializedName("nutrients") private val _nutrients: Nutrients = Nutrients(calories = 0, protein = 0f, carbs = 0f, fat = 0f),
    @SerializedName("timestamp") val timestamp: Long = 0
) : Parcelable {
    val nutrients: Nutrients
        get() = _nutrients
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readParcelable(Nutrients::class.java.classLoader, Nutrients::class.java) ?: Nutrients(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(foodName)
        parcel.writeParcelable(nutrients, flags)
        parcel.writeLong(timestamp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ScanHistoryItem> {
        override fun createFromParcel(parcel: Parcel): ScanHistoryItem {
            return ScanHistoryItem(parcel)
        }

        override fun newArray(size: Int): Array<ScanHistoryItem?> {
            return arrayOfNulls(size)
        }
    }
}

