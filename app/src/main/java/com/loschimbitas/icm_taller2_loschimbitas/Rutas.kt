package com.loschimbitas.icm_taller2_loschimbitas

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

    }

    override fun onResume() {
        super.onResume()
        binding.osmMap.onResume()
        val mapController: IMapController = binding.osmMap.controller
        mapController.setZoom(18.0)
        mapController.setCenter(this.startPoint)
    }

    override fun onPause() {
        super.onPause()
        binding.osmMap.onPause()
    }

}