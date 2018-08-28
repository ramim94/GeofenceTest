package com.ideabinbd.maplocationtests

import com.google.android.gms.maps.model.LatLng

object Constants {
       val GEOFENCE_RADIUS_IN_METERS= 1000f
    var  entry= HashMap<String,LatLng>()

    //60 seconds or 1 minute
    val GEOFENCE_EXPIRATION_IN_MILLISECONDS=60*1000
}
