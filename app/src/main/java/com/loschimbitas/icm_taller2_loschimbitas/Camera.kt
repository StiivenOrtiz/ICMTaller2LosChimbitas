package com.loschimbitas.icm_taller2_loschimbitas

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.loschimbitas.icm_taller2_loschimbitas.databinding.ActivityCameraBinding

class Camera : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}