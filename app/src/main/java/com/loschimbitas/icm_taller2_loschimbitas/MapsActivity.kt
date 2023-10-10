package com.loschimbitas.icm_taller2_loschimbitas

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.loschimbitas.icm_taller2_loschimbitas.databinding.ActivityMapsBinding
import org.json.JSONArray
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.Writer
import java.util.Date
import kotlin.math.roundToInt


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    // Variables de localización
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationCallback: LocationCallback
    // Fin variables localización

    //Variables sensor luminosidad
    private lateinit var sensorManager: SensorManager
    private lateinit var lightSensor: Sensor
    private lateinit var lightSensorListener: SensorEventListener
    //Fin variables sensor luminosidad


    companion object {
        val RADIUS_OF_EARTH_KM = 6371
        var latitudGlobal = 0.0
        var longitudGlobal = 0.0
        var flag = true
        var localizaciones = JSONArray()
    }

    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.

            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
            }
            else -> {
                // No location access granted.
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Inicialización del mapa
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        inicializarVariables()

        binding.texto.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.ACTION_UP && event.action == KeyEvent.ACTION_DOWN) {
                val mGeocoder = Geocoder(baseContext)
                val addressString = binding.texto.text.toString()
                if (addressString.isNotEmpty()) {
                    try {
                        val addresses = mGeocoder.getFromLocationName(addressString, 2)
                        if (!addresses.isNullOrEmpty()) {
                            val addressResult = addresses[0]
                            val coordenadas = LatLng(addressResult.latitude, addressResult.longitude)
                            if (::mMap.isInitialized) {
                                var nombreLugar = addresses[0].countryName + " , lat: " + addresses[0].latitude.toString() + " , lon: " + addresses[0].longitude.toString()
                                mMap.addMarker(MarkerOptions().position(coordenadas)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                    .title(nombreLugar))
                                mMap.moveCamera(CameraUpdateFactory.zoomTo(15f))
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(coordenadas))

                                // Cálculo punto 8
                                var distancia = calculoDistancia(latitudGlobal, longitudGlobal, addressResult.latitude, addressResult.longitude)

                                // Cálculo distancia usuario a punto
                                Toast.makeText(this, "Se encuentra a ${distancia} kilometros de distancia de este punto", Toast.LENGTH_SHORT).show()
                            }
                            //Agregar Marcador al mapa
                            else {
                                Toast.makeText(this, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(this, "La dirección esta vacía", Toast.LENGTH_SHORT).show()
                }
                return@setOnKeyListener true
            }
            false
        }



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    private fun inicializarVariables() {
        // Variables para ubicación inicializadas
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        askForPermissions()
        mLocationRequest = createLocationRequest()

        // Variables para el uso de sensores
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        lightSensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (::mMap.isInitialized) {
                    if (event.values[0] < 5000) {
//                        Log.i("MAPS", "DARK MAP " + event.values[0])
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@MapsActivity, R.raw.style_json_night))
                    } else {
//                        Log.i("MAPS", "LIGHT MAP " + event.values[0])
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@MapsActivity, R.raw.style_json_day))
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        // Fin variables para el uso de sensores
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(lightSensorListener, lightSensor,
            SensorManager.SENSOR_DELAY_NORMAL)
        flag = true
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mLocationRequest = createLocationRequest()
    }
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(lightSensorListener)
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
    }


    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            null
        )

    }

    //
    private fun createLocationRequest(): LocationRequest
    {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,10000).build()
//            LocationRequest.create()
//            .setInterval(10000)
//            .setFastestInterval(5000)
//            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        return locationRequest
    }

    private fun askForPermissions() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun calculoDistancia(latitudGlobal: Double, longitudGlobal: Double, latitudGlobalTemp: Double, longitudGlobalTemp: Double): Double {
        val latDistance = Math.toRadians(latitudGlobal - latitudGlobalTemp)
        val lngDistance = Math.toRadians(longitudGlobal - longitudGlobalTemp)
        val a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(latitudGlobal)) * Math.cos(Math.toRadians(latitudGlobalTemp))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2))
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val result = RADIUS_OF_EARTH_KM * c
        // Distancia en metros
        return (result * 100.0).roundToInt() / 100.0
    }

    private fun writeJSONObject(latitudGlobal: Double, longitudGlobal: Double) {

        // Agrego un nuevo objeto my location y lo transformo en JSON para meterlo dentro del array: Localizaciones
        localizaciones.put(MyLocation(
            Date(System.currentTimeMillis()), latitudGlobal,
            longitudGlobal).toJSON())

        var output: Writer?
        val filename = "locations.json"
        try {
            // Tomo un directorio para crear el archivo
            val file = File(baseContext.getExternalFilesDir(null), filename)
            Log.w("LOCATION", "Ubicacion de archivo: $file")
            output = BufferedWriter(FileWriter(file))
            output.write(localizaciones.toString())
            output.close()
//            Toast.makeText(applicationContext, "Json location saved!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
        //Log error
        }
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

        iniciarLocationCallback()
        crearMarcadorConLongClick()

    }

    private fun crearMarcadorConLongClick() {

        // Inicio LongClickListener para poner un marcador
        mMap.setOnMapLongClickListener { latLng ->
            var latitudLongitud = latLng
            val mGeocoder = Geocoder(baseContext)
            try {
                val addresses = mGeocoder.getFromLocation(latitudLongitud.latitude, latitudLongitud.longitude,1)
                if (!addresses.isNullOrEmpty()) {
                    var nombreLugar = addresses[0].countryName + " , " + addresses[0].locality + " , " + addresses[0].featureName
                    mMap.addMarker(MarkerOptions().position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .title(nombreLugar))

                    // Cálculo punto 8
                    var distancia = calculoDistancia(latitudGlobal, longitudGlobal, latitudLongitud.latitude, latitudLongitud.longitude)


                    // Cálculo distancia usuario a punto
                    Toast.makeText(this, "Se encuentra a ${distancia} kilometros de distancia de este punto", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun iniciarLocationCallback() {
        mLocationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {

                // Variables para comparación
                var latitudGlobalTemp = latitudGlobal
                var longitudGlobalTemp = longitudGlobal
                // fin variables comparación

                // Variable que recibe el flojo de datos de la ubicación
                val location = locationResult.lastLocation
                Log.d("LOCATION", "Location update in the callback: $location")
                if (location != null) {

                    // Recibo el valor de la ubicación del flujo de datos
                    latitudGlobal = location.latitude
                    longitudGlobal = location.longitude

                    // Caso base
                    if(flag){
                        val coordenadasIniciales = LatLng(latitudGlobal, longitudGlobal)
                        mMap.addMarker(MarkerOptions().position(coordenadasIniciales)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            .title("Marcador en $coordenadasIniciales"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(coordenadasIniciales))
                        flag = false
                        writeJSONObject(latitudGlobal, longitudGlobal)
                    }
                    // fin caso base

                    // Operaciones matemáticas
                    var distancia = calculoDistancia(latitudGlobal, longitudGlobal, latitudGlobalTemp, longitudGlobalTemp)


                    // Cambio en el pin de la ubicación
                    if(distancia > 0.03){
                        val nuevasCoordenadas = LatLng(latitudGlobal, longitudGlobal)
                        mMap.addMarker(MarkerOptions().position(nuevasCoordenadas)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            .title("Marcador en $nuevasCoordenadas"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(nuevasCoordenadas))
                        writeJSONObject(latitudGlobal, longitudGlobal)
                    }
                    // final cambio en el pin de la ubicación

                }
            }
        }
        startLocationUpdates()
    }


}