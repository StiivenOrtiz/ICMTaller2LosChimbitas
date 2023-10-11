package com.loschimbitas.icm_taller2_loschimbitas

import android.Manifest
import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.loschimbitas.icm_taller2_loschimbitas.databinding.ActivityRutasBinding
import org.osmdroid.api.IMapController
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.library.BuildConfig
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay
import java.io.IOException
import android.os.AsyncTask
import org.osmdroid.bonuspack.routing.Road

class Rutas : AppCompatActivity() {

    private lateinit var  binding: ActivityRutasBinding
    private var latitude = 0.0
    private var longitude = 0.0
    private val startPoint = GeoPoint(latitude,longitude)
    private var longPressedMarker: Marker?= null
    private lateinit var roadManager: RoadManager
    private var roadOverlay:Polyline?= null
    private var locationPermissionCode = 1
    private lateinit var editTextLocation: EditText
    private lateinit var buttonNavigate: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().setUserAgentValue(BuildConfig.BUILD_TYPE)

        binding = ActivityRutasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editTextLocation = findViewById(R.id.editTextLocation)
        buttonNavigate = findViewById(R.id.buttonNavigate)

        binding.osmMap.setTileSource(TileSourceFactory.MAPNIK)
        binding.osmMap.setMultiTouchControls(true)

        binding.osmMap.overlays.add(createOverlayEvents())

        roadManager = OSRMRoadManager(this,"ANDROID")



        // Verificar permisos de ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // Con el permiso concedido, muestra la ubicación actual y centra el mapa
            showAndCenterCurrentLocation()
        } else {
            // Si no tiene permisos, los solicita
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }


        buttonNavigate.setOnClickListener {
            val locationName = editTextLocation.text.toString()
            if (locationName.isNotEmpty()) {
                val destinationPoint = buscarCiudadPorNombre(locationName)
                if (destinationPoint != null) {
                    val toastMessage = "En camino a $locationName"
                    showToast(toastMessage)

                    // Dibujar la ruta desde la ubicación actual del usuario
                    drawRoute(GeoPoint(latitude, longitude), destinationPoint)
                } else {
                    showToast("Ubicación no encontrada")
                }
            } else {
                showToast("Ingrese una ubicación válida")
            }
        }

    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        binding.osmMap.onResume()

        //verivicar permisos de ubicacion
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ){
            // con el permiso concedido se muestra la ubicacion actual
            showAndCenterCurrentLocation()
        }else{
            // si no tiene permisos los solicita
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }

        val mapController: IMapController = binding.osmMap.controller
        mapController.setZoom(18.0)
        mapController.setCenter(this.startPoint)

        //ajuste de tema para mapa segun el modo del dispoditivo
        val uiManager = getSystemService(UI_MODE_SERVICE) as UiModeManager
        if(uiManager.nightMode == UiModeManager.MODE_NIGHT_YES){
            binding.osmMap.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
        }

        //para poner el dark Mode de forma constante
        //binding.osmMap.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)

    }

    private fun showAndCenterCurrentLocation() {
        // Utiliza LocationManager para obtener la ubicación actual
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // Actualiza las coordenadas de la ubicación actual del usuario
                latitude = location.latitude
                longitude = location.longitude

                // Centra el mapa en la ubicación actual
                val mapController: IMapController = binding.osmMap.controller
                mapController.setZoom(18.0)
                mapController.setCenter(GeoPoint(latitude, longitude))

                // Muestra un marcador en la ubicación actual
                showMarker(GeoPoint(latitude, longitude))

                // Deja de escuchar las actualizaciones después de obtener la ubicación
                locationManager.removeUpdates(this)
            }

            override fun onProviderDisabled(provider: String) {}

            override fun onProviderEnabled(provider: String) {}

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }

        // Registra el LocationListener para obtener una actualización única de la ubicación
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationManager.requestSingleUpdate(
            LocationManager.GPS_PROVIDER,
            locationListener,
            null
        )
    }


    private fun showMarker(geoPoint: GeoPoint) {
        // Elimina cualquier marcador existente
        binding.osmMap.overlays.removeAll { it is Marker }

        // Crea y muestra un nuevo marcador en la ubicación proporcionada
        val marker = Marker(binding.osmMap)
        marker.title = "Mi ubicación"
        marker.position = geoPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        // Aquí puedes personalizar el icono del marcador según tus necesidades
        // marker.icon = resources.getDrawable(R.drawable.mi_icono_personalizado)

        binding.osmMap.overlays.add(marker)
    }



    override fun onPause() {
        super.onPause()
        binding.osmMap.onPause()
    }


    private fun createOverlayEvents(): MapEventsOverlay {
        val overlayEventos = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                return false
            }
            override fun longPressHelper(p: GeoPoint): Boolean {
                return true
            }
        })
        return overlayEventos
    }




    private fun createMarker(p:GeoPoint,title:String?,desc:String?,iconID:Int):Marker?{
        var marker:Marker?= null
        if(binding.osmMap != null){
            marker = Marker(binding.osmMap)
            title?.let { marker.title =it }
            desc?.let {marker.subDescription = it}
            if(iconID !=0){
                val myIcon = resources.getDrawable(iconID,this.theme)
                marker.icon = myIcon
            }
            marker.position =p
            marker.setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_BOTTOM)
        }
        return marker
    }



    private inner class GetRouteTask : AsyncTask<GeoPoint, Void, Road>() {
        override fun doInBackground(vararg params: GeoPoint): Road? {
            val routePoints = ArrayList<GeoPoint>()

            // Obtener la ubicación actual del usuario
            val currentLocation = getCurrentLocation()
            if (currentLocation != null) {
                routePoints.add(GeoPoint(currentLocation.latitude, currentLocation.longitude))
            } else {
                showToast("No se pudo obtener la ubicación actual")
                return null
            }

            // Agregar el destino a los puntos de la ruta
            routePoints.add(params[0]) // Destino

            return roadManager.getRoad(routePoints)
        }

        override fun onPostExecute(result: Road?) {
            super.onPostExecute(result)
            if (result != null) {
                // Dibujar la ruta
                drawRoadOverlay(result)
            } else {
                showToast("Error al obtener la ruta")
            }
        }
    }

    private fun getCurrentLocation(): Location? {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return null
            }
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun drawRoute(start: GeoPoint, finish: GeoPoint) {
        GetRouteTask().execute(finish)
    }

    private fun drawRoadOverlay(road: Road) {
        roadOverlay?.let { binding.osmMap.overlays.remove(it) }
        roadOverlay = RoadManager.buildRoadOverlay(road)
        roadOverlay?.outlinePaint?.color = ContextCompat.getColor(this, R.color.Red)
        roadOverlay?.outlinePaint?.strokeWidth = 10f
        binding.osmMap.overlays.add(roadOverlay)
    }

    private fun buscarCiudadPorNombre(nombreCiudad: String): GeoPoint? {
        val mGeocoder = Geocoder(baseContext)
        val addressString = nombreCiudad
        if (addressString.isNotEmpty()) {
            try {
                val addresses = mGeocoder.getFromLocationName(addressString, 2)
                if (!addresses.isNullOrEmpty()) {
                    val addressResult = addresses[0]
                    return GeoPoint(addressResult.latitude, addressResult.longitude)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }


}