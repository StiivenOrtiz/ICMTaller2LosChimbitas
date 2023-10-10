package com.loschimbitas.icm_taller2_loschimbitas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.loschimbitas.icm_taller2_loschimbitas.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Change to Camera activity
        binding.cameraButton.setOnClickListener {
            val intent = Intent(this, Camera::class.java)
            startActivity(intent)
        }

        // Change to Contacts activity
        binding.contactsButton.setOnClickListener {
            val intent = Intent(this, Contacts::class.java)
            startActivity(intent)
        }

        binding.osmapButton.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
    }
}