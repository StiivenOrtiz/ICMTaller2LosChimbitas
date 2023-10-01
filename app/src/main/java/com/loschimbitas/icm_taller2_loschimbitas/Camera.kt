package com.loschimbitas.icm_taller2_loschimbitas

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.loschimbitas.icm_taller2_loschimbitas.databinding.ActivityCameraBinding

class Camera : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissions()

        binding.cameraButton.setOnClickListener {
            if (checkPermissions()) {
                Toast.makeText(this, "Camera", Toast.LENGTH_SHORT).show()
            }
        }

        binding.galleryButton.setOnClickListener {
            if (checkStoragePermissions()) {
                Toast.makeText(this, "Gallery", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * @name: checkPermissions
     * @description: Check if the app has the permissions to use the camera and read the storage
     * @return: void
     * @exception: none
     */
    private fun checkPermissions(): Boolean {
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            requestAllPermissions()
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            )
                requestCameraPermission()

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            )
                requestStoragePermission()
        }

        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED)
        )
            return true

        return false
    }

    /**
     * @name: checkCameraPermissions
     * @description: Check if the app has the permission to use the camera
     * @return: void
     * @exception: none
     */
    private fun checkStoragePermissions(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        )
            requestStoragePermission()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        )
            return true

        return false
    }

    /**
     * @name: requestAllPermissions
     * @description: Request the permission to use the camera and read the storage
     * @return: void
     * @exception: none
     */
    private fun requestAllPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) && ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) && ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
        // The user has previously denied this permission but not permanently.
            requestPermission(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 2
            )
        else
        // The user has never been asked for the permission.
            requestPermission(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 2
            )

    }

    /**
     * @name: requestPermission
     * @description: Request the permission to read the storage
     * @return: void
     * @exception: none
     */
    private fun requestPermission(permissionsArray: Array<String>, code: Int) {
        ActivityCompat.requestPermissions(
            this,
            permissionsArray,
            code
        )
    }

    /**
     * @name: requestCameraPermission
     * @description: Request the permission to use the camera
     * @return: void
     * @exception: none
     */
    private fun requestCameraPermission() {
        // Check if the user has previously denied the permission
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            )
        )
        // The user has previously denied this permission but not permanently.
            requestPermission(arrayOf(Manifest.permission.CAMERA), 3)
        else
        // The user has never been asked for the permission.
            requestPermission(arrayOf(Manifest.permission.CAMERA), 3)

    }

    /**
     * @name: requestStoragePermission
     * @description: Request the permission to read and write the storage
     * @return: void
     * @exception: none
     */
    private fun requestStoragePermission() {
        // Check if the user has previously denied the permission
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
        // The user has previously denied this permission but not permanently.
            requestPermission(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 4
            )
        else
        // The user has never been asked for the permission.
            requestPermission(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 4
            )
    }

    /**
     * @name: onRequestPermissionsResult
     * @description: Handle the result of the permission request
     * @return: void
     * @exception: none
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Check if the permission was granted or not
        if (requestCode == 2) {
            if (grantResults.isNotEmpty()
                && grantResults[0] != PackageManager.PERMISSION_GRANTED
                && grantResults[1] != PackageManager.PERMISSION_GRANTED
                && grantResults[2] != PackageManager.PERMISSION_GRANTED
            )
                Toast.makeText(this, "All permissions denied", Toast.LENGTH_SHORT)
                    .show()
            else if (grantResults.isNotEmpty()
                && grantResults[1] != PackageManager.PERMISSION_GRANTED
                && grantResults[2] != PackageManager.PERMISSION_GRANTED
            )
                Toast.makeText(
                    this,
                    "Read and write storage permissions denied",
                    Toast.LENGTH_SHORT
                ).show()
            else if (grantResults.isNotEmpty()
                && grantResults[0] != PackageManager.PERMISSION_GRANTED
            )
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT)
                    .show()
        }

        if (requestCode == 3) {
            if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED)
                Toast.makeText(
                    this, "Camera permission denied",
                    Toast.LENGTH_SHORT
                ).show()
        }

        if (requestCode == 4) {
            if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                grantResults[1] != PackageManager.PERMISSION_GRANTED
            )
                Toast.makeText(
                    this, "Read and write storage permission denied",
                    Toast.LENGTH_SHORT
                ).show()
        }
    }
}