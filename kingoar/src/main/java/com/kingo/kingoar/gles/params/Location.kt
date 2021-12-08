package com.kingo.kingoar.gles.params

import android.os.Parcel
import android.os.Parcelable

/**
 * 经纬度信息
 * @author Xuwl
 * @date 2021/11/9
 *
 */
data class Location(var latitude: Double, var longitude: Double, var altitude: Double) :
    Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeDouble(altitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Location> {
        override fun createFromParcel(parcel: Parcel): Location {
            return Location(parcel)
        }

        override fun newArray(size: Int): Array<Location?> {
            return arrayOfNulls(size)
        }
    }
}