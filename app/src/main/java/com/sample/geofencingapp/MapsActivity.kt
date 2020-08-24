package com.sample.geofencingapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.sample.timereminderapp.R

//OnMapLongClickListener
class MapsActivity : FragmentActivity(), OnMapReadyCallback{

    companion object {
        private const val TAG = "MapsActivity"
        private const val GEOFENCE_RADIUS = 50f
        private const val GEOFENCE_ID = "SOME_GEOFENCE_ID"
        private const val FINE_LOCATION_ACCESS_REQUEST_CODE = 10001
        private const val BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002
        private const val SETTING_PERMISSION_CODE = 50
    }

    private var mMap: GoogleMap? = null
    private var geofencingClient: GeofencingClient? = null
    private var geofenceHelper: GeofenceHelper? = null
//    private val curLoclatlng:LatLng = LatLng(11.0284, 76.9048) //myLoc
//    private var curLoclatlng:LatLng = LatLng(11.0276, 76.9056) //myLoc1
    private var curLoclatlng:LatLng = LatLng(11.0268, 76.9055) //myLoc2
//    private var curLoclatlng:LatLng = LatLng(12.853028, 80.068502) //chennai
//    private var curLoclatlng:LatLng = LatLng(8.544915, 76.878187) //Thiruvendram

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
        geofenceHelper = GeofenceHelper(this)
        getDeviceLocationState()
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(curLoclatlng, 16f))
//        enableUserLocation()
//        mMap!!.setOnMapLongClickListener(this)
    }

    private fun enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED)
        {
            mMap!!.isMyLocationEnabled = true
            handleGeofence(curLoclatlng)
        } else {
            //Ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)) {
                //We need to show user a dialog for displaying why the permission is needed and then ask for the permission...
                ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    FINE_LOCATION_ACCESS_REQUEST_CODE
                )
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    FINE_LOCATION_ACCESS_REQUEST_CODE
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<String>,grantResults: IntArray) {
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                mMap!!.isMyLocationEnabled = true
                handleGeofence(curLoclatlng)
            } else {
                //We do not have the permission..
            }
        }
        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                Toast.makeText(this, "You can add geofences...", Toast.LENGTH_SHORT).show()
            } else {
                //We do not have the permission..
                Toast.makeText(
                    this,
                    "Background location access is neccessary for geofences to trigger...",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun handleGeofence(latLng: LatLng) {
        mMap!!.clear()
        addMarker(latLng)
        addCircle(latLng,
            GEOFENCE_RADIUS
        )
        addGeofence(latLng,
            GEOFENCE_RADIUS
        )
    }


    @SuppressLint("MissingPermission")
    private fun addGeofence(latLng: LatLng, radius: Float) {
        val geofence: Geofence? = geofenceHelper?.getGeofence(
            GEOFENCE_ID,
            latLng,
            radius,
            Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
        )
        val geofencingRequest: GeofencingRequest = geofenceHelper?.getGeofencingRequest(geofence)!!
        val pendingIntent: PendingIntent = geofenceHelper!!.getPendingIntent()!!
        geofencingClient!!.addGeofences(geofencingRequest, pendingIntent)
            .addOnSuccessListener {
                Log.d(TAG, "onSuccess: Geofence Added...")
                }
            .addOnFailureListener { e ->
                val errorMessage: String = geofenceHelper!!.getErrorString(e)
                Log.d(TAG, "onFailure: $errorMessage")
            }
    }

    private fun getDeviceLocationState() {

        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = (10 * 1000).toLong()
        locationRequest.fastestInterval = 2000
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        val locationSettingsRequest = builder.build()
        val result = LocationServices.getSettingsClient(this)
            .checkLocationSettings(locationSettingsRequest)
        result.addOnCompleteListener { task ->
            try {
                val response = task.getResult(ApiException::class.java)
                if (response!!.locationSettingsStates.isLocationPresent){
                    enableUserLocation()
                }
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvable = exception as ResolvableApiException
                        resolvable.startResolutionForResult(this,
                            SETTING_PERMISSION_CODE
                        )
                    } catch (e: IntentSender.SendIntentException) {
                    } catch (e: ClassCastException) {
                    }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    }
                }
            }
        }
//        return locationState

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            SETTING_PERMISSION_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    enableUserLocation()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

//    override fun onMapLongClick(latLng: LatLng) {
//        if (Build.VERSION.SDK_INT >= 29) {
//            //We need background permission
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//                handleGeofence(latLng)
//            } else {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_BACKGROUND_LOCATION)
//                ) {
//                    //We show a dialog and ask for permission
//                    ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),BACKGROUND_LOCATION_ACCESS_REQUEST_CODE
//                    )
//                } else {
//                    ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),BACKGROUND_LOCATION_ACCESS_REQUEST_CODE
//                    )
//                }
//            }
//        } else {
//            handleGeofence(latLng)
//        }
//    }


    private fun addMarker(latLng: LatLng) {
        val markerOptions = MarkerOptions().position(latLng)
        mMap!!.addMarker(markerOptions)
    }
    private fun addCircle(latLng: LatLng, radius: Float) {
        val circleOptions = CircleOptions()
        circleOptions.center(latLng)
        circleOptions.radius(radius.toDouble())
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0))
        circleOptions.fillColor(Color.argb(64, 255, 0, 0))
        circleOptions.strokeWidth(4f)
        mMap!!.addCircle(circleOptions)
    }
}