package com.example.kantahliliuygulamasi

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var settingsClient: SettingsClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationSettingsRequest: LocationSettingsRequest
    private lateinit var locationCallback: LocationCallback

    private val LOCATION_PERMISSION_REQUEST = 1
    private val REQUEST_CHECK_SETTINGS = 1001

    private val API_KEY = "AIzaSyD7evOCakMXRbogfFMdaxU0fCKQTJTxLPI"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        settingsClient = LocationServices.getSettingsClient(this)

        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000
            fastestInterval = 5000
            numUpdates = 1
        }

        locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    if (::mMap.isInitialized) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                        getNearbyPlaces(userLatLng)
                    }
                } else {
                    Toast.makeText(this@MapsActivity, "Konum alınamadı.", Toast.LENGTH_SHORT).show()
                }
                fusedLocationClient.removeLocationUpdates(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Burada mMap henüz initialize edilmemiş olabilir, bu yüzden kontrol et
        if (::mMap.isInitialized) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (isLocationEnabled()) {
                    enableMyLocationAndLoadPlaces()
                } else {
                    Toast.makeText(this, "Lütfen konum servisini açın", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // Eğer mMap henüz hazır değilse, hiçbir şey yapma, onMapReady çağrılınca işlem başlar
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as android.location.LocationManager
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap // mMap burada initialize edilir

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        } else {
            checkLocationSettingsAndEnable()
        }
    }

    private fun checkLocationSettingsAndEnable() {
        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                enableMyLocationAndLoadPlaces()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        exception.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Toast.makeText(this, "Konum servisi açılamadı", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Konum ayarları uygun değil", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun enableMyLocationAndLoadPlaces() {
        if (!::mMap.isInitialized) return // mMap hazır değilse çık

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return
        }

        try {
            mMap.isMyLocationEnabled = true
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun getNearbyPlaces(location: LatLng) {
        val types = listOf("pharmacy", "hospital")
        val radius = 1500

        for (type in types) {
            val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                    "location=${location.latitude},${location.longitude}" +
                    "&radius=$radius" +
                    "&type=$type" +
                    "&key=$API_KEY"

            val request = Request.Builder().url(url).build()
            val client = OkHttpClient()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@MapsActivity, "API isteği başarısız oldu: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.body?.string()?.let { responseBody ->
                        val jsonObject = JSONObject(responseBody)
                        val results = jsonObject.getJSONArray("results")

                        runOnUiThread {
                            for (i in 0 until results.length()) {
                                val place = results.getJSONObject(i)
                                val name = place.getString("name")
                                val lat = place.getJSONObject("geometry").getJSONObject("location").getDouble("lat")
                                val lng = place.getJSONObject("geometry").getJSONObject("location").getDouble("lng")
                                val placeLatLng = LatLng(lat, lng)
                                mMap.addMarker(MarkerOptions().position(placeLatLng).title(name))
                            }
                        }
                    }
                }
            })
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationSettingsAndEnable()
            } else {
                Toast.makeText(this, "Konum izni gerekli", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                enableMyLocationAndLoadPlaces()
            } else {
                Toast.makeText(this, "Konum servisi açılmadı", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
