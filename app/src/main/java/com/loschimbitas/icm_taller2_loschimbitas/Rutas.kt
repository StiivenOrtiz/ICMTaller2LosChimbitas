package com.loschimbitas.icm_taller2_loschimbitas

import android.Manifest
import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay




class Rutas : AppCompatActivity() {

    private lateinit var  binding: ActivityRutasBinding
    private val latitude = 4.62
    private val longitude = -74.07
    private val startPoint = GeoPoint(latitude,longitude)
    private var longPressedMarker: Marker?= null
    private lateinit var roadManager: RoadManager
    private var roadOverlay:Polyline?= null
    private var locationPermissionCode = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().setUserAgentValue(BuildConfig.BUILD_TYPE)

        binding = ActivityRutasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.osmMap.setTileSource(TileSourceFactory.MAPNIK)
        binding.osmMap.setMultiTouchControls(true)

        binding.osmMap.overlays.add(createOverlayEvents())

        roadManager = OSRMRoadManager(this,"ANDROID")

        // Verificar permisos de ubicación
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // Con el permiso concedido, muestra la ubicación actual y centra el mapa
            showAndCenterCurrentLocation()
        } else {
            // Si no tiene permisos, los solicita
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }
    }


    override fun onResume() {
        super.onResume()
        binding.osmMap.onResume()

        //verivicar permisos de ubicacion
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ){
            // con el permiso concedido se muestra la ubicacion actual
            showAndCenterCurrentLocation()
        }else{
            // si no tiene permisos los solicita
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }

        val mapController: IMapController = binding.osmMap.controller
        mapController.setZoom(18.0)
        mapController.setCenter(this.startPoint)

        //ajuste de tema para mapa segun el modo del dispoditivo
        val uiManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        if(uiManager.nightMode == UiModeManager.MODE_NIGHT_YES){
            binding.osmMap.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
        }

        //para poner el dark Mode de forma constante
        //binding.osmMap.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)

    }

    private fun showAndCenterCurrentLocation() {
        // Utiliza LocationManager para obtener la ubicación actual
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // Centra el mapa en la ubicación actual
                val mapController: IMapController = binding.osmMap.controller
                mapController.setZoom(18.0)
                mapController.setCenter(GeoPoint(location.latitude, location.longitude))

                // Muestra un marcador en la ubicación actual
                showMarker(GeoPoint(location.latitude, location.longitude))
            }

            override fun onProviderDisabled(provider: String) {}

            override fun onProviderEnabled(provider: String) {}

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }

        // Registra el LocationListener para obtener actualizaciones de ubicación
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
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,
            0.toFloat(),
            locationListener
        )

        // Agrega un marcador personalizado en la ubicación actual
        showMarker(GeoPoint(latitude, longitude))
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
                longPressOnMap(p)
                return true
            }
        })
        return overlayEventos
    }


    private fun longPressOnMap(p:GeoPoint){
        longPressedMarker?.let { binding.osmMap.overlays.remove(it) }
        longPressedMarker = createMarker(p,"location",null, org.osmdroid.library.R.drawable.ic_menu_mylocation)
        longPressedMarker?.let { binding.osmMap.overlays.add(it) }
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

    private fun drawRoute(start:GeoPoint,finish:GeoPoint){

        val routePoints =ArrayList<GeoPoint>()
        routePoints.add(start)
        routePoints.add(finish)
        val road = roadManager.getRoad(routePoints)
        Log.i("OSM_acticity","Route length: ${road.mLength} Km")
        Log.i("OSM_acticity","Duration: ${road.mDuration} min")

        if(binding.osmMap != null){
            roadOverlay?.let { binding.osmMap.overlays.remove(it) }
            roadOverlay = RoadManager.buildRoadOverlay(road)
            roadOverlay?.outlinePaint?.color = ContextCompat.getColor(this,R.color.Red)
            roadOverlay?.outlinePaint?.strokeWidth =10f
            binding.osmMap.overlays.add(roadOverlay)
        }

    }

}