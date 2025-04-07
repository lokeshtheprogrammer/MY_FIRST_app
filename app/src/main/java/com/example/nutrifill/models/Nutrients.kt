package com.example.nutrifill.models

import android.os.Parcel
import android.os.Parcelable

data class Nutrients(
    var calories: Int = 0,
    var protein: Float = 0f,
    var carbs: Float = 0f,
    var fat: Float = 0f
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(calories)
        parcel.writeFloat(protein)
        parcel.writeFloat(carbs)
        parcel.writeFloat(fat)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Nutrients> {
        override fun createFromParcel(parcel: Parcel): Nutrients {
            return Nutrients(parcel)
        }

        override fun newArray(size: Int): Array<Nutrients?> {
            return arrayOfNulls(size)
        }
    }
}