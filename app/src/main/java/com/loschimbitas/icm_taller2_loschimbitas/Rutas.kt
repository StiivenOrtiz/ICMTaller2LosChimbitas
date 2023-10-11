package com.loschimbitas.icm_taller2_loschimbitas

import android.app.UiModeManager
import android.content.Context
import android.content.res.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.model.Tile
import com.loschimbitas.icm_taller2_loschimbitas.databinding.ActivityRutasBinding
import org.osmdroid.api.IMapController
import org.osmdroid.library.BuildConfig
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay


class Rutas : AppCompatActivity() {

    private lateinit var  binding: ActivityRutasBinding
    private val latitude = 4.62
    private val longitude = -74.07
    private val startPoint = GeoPoint(latitude,longitude)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().setUserAgentValue(BuildConfig.BUILD_TYPE)

        binding = ActivityRutasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.osmMap.setTileSource(TileSourceFactory.MAPNIK)
        binding.osmMap.setMultiTouchControls(true)

        val markrPoint = GeoPoint(4.62,-74.07)
        val marker = Marker(binding.osmMap)
        marker.title = "Mi marca uwu"
        val myIcon = resources.getDrawable(org.osmdroid.library.R.drawable.marker_default,theme)
        marker.icon = myIcon
        marker.position = markrPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        binding.osmMap.overlays.add(marker)

    }

    override fun onResume() {
        super.onResume()
        binding.osmMap.onResume()
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

    override fun onPause() {
        super.onPause()
        binding.osmMap.onPause()
    }

}