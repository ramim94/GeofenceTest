package com.ideabinbd.maplocationtests

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.ideabinbd.maplocationtests.Constants.entry
import java.util.jar.Manifest

class MapsActivity : AppCompatActivity(), OnMapReadyCallback ,GoogleMap.OnMapClickListener{

    private lateinit var mMap: GoogleMap
    lateinit var geofencingClient: GeofencingClient

    lateinit var geofenceRequest: GeofencingRequest
    lateinit var geofenceBuilder: Geofence

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        geofencingClient= LocationServices.getGeofencingClient(this)

    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(25.927525,-80.181146)

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,20f))
        mMap.setOnMapClickListener(this)

        mMap.setMyLocationEnabled(true);
    }


    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceTransitionsIntentService::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    override fun onMapClick(p0: LatLng?) {
        mMap.addMarker(MarkerOptions().position(p0!!).title("geoKey"))
        entry.put("geoKey", p0)

        buildGeofenceObject()
        buildGeofenceRequest()
        addGeofence()
    }

    private fun buildGeofenceObject() {
        geofenceBuilder= Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId("geoKey")

                // Set the circular region of this geofence.
                .setCircularRegion(
                        entry["geoKey"]!!.latitude,
                        entry["geoKey"]!!.longitude,
                        Constants.GEOFENCE_RADIUS_IN_METERS
                )

                // Set the expiration duration of the geofence. This geofence gets automatically
                // removed after this period of time.
                .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS.toLong())

                // Set the transition types of interest. Alerts are only generated for these
                // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)

                // Create the geofence.
                .build()
    }
    private fun buildGeofenceRequest() {
        geofenceRequest= GeofencingRequest.Builder()
                .setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER)
                .addGeofence(geofenceBuilder)
                .build()
    }
    private fun addGeofence() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            Toast.makeText(this,"Permission Not Granted",Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),1)
        }else{
            geofencingClient.addGeofences(geofenceRequest, geofencePendingIntent)?.run {
                addOnSuccessListener {
                    // Geofences added
                    // ...
                    Toast.makeText(this@MapsActivity,"Fence Added",Toast.LENGTH_SHORT).show()
                    addCircleToTheMap()
                }
                addOnFailureListener {
                    // Failed to add geofences
                    // ...
                    Toast.makeText(this@MapsActivity,"Fence Adding Failed",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addCircleToTheMap() {

        val circleOp= CircleOptions().center(entry["geoKey"]).radius(1000.0)
                .strokeColor(Color.argb(50,70,70,70))
                .fillColor(Color.argb(100,150,150,150))

        mMap.addCircle(circleOp)
    }

    override fun onResume() {
        super.onResume()
        startService(Intent(this,GeofenceTransitionsIntentService::class.java))
    }

    @SuppressLint("MissingPermission")
    override fun onPause() {
        mMap.setMyLocationEnabled(false);
        super.onPause()
    }
}
