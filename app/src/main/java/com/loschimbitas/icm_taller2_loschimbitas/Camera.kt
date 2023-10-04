package com.loschimbitas.icm_taller2_loschimbitas

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.loschimbitas.icm_taller2_loschimbitas.databinding.ActivityCameraBinding
import java.io.File

class Camera : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding

//  Creación del contrato para obtener el objeto desde la galería y ponerlo en la image view
    private val selectSinglePhotoContract = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        // Handle the returned Uri
        uri?.let {
            val imageView = binding.imageView
            imageView.setImageURI(it)
        }
    }
//  Fin de creacion del contrato

    private var tempImageUri: Uri? = null

//    Creación del contrato para inicializar la cámara
    private val camaraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()){
       success ->
        if (success) {
            val imageView = binding.imageView
            imageView.setImageURI(null) //rough handling of image changes. Real code need to handle different API levels.
            imageView.setImageURI(tempImageUri)
        }
        else
            Toast.makeText(this, "Error taking picture", Toast.LENGTH_SHORT).show()
    }
//  Fin de creación del contrato para inicializar la cámara



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Crear URI temporal para la imagen (para que no salga pixelada)

        checkPermissions()

        binding.cameraButton.setOnClickListener {
            if (checkPermissions()) {
                tempImageUri = initTempUri()
                // Lanzo el contrato
                camaraLauncher.launch(tempImageUri)
            }
        }

        binding.galleryButton.setOnClickListener {
            if (checkStoragePermissions()) {
                // Lanzo el contrato
                selectSinglePhotoContract.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

            }
        }
    }

//  Funciones necesarias para la cámara:
//  Secuencialmente: initTempUri

    // Método: initTempUri que sirve para crear el directorio temporal que permite que la
    // imagen no salga pixelada (porque mapea la imagen a partir de el archivo completo como tal
    // y no de la vista previa)
    private fun initTempUri(): Uri {
        //gets the temp_images dir
        val tempImagesDir = File(
            applicationContext.filesDir, //this function gets the external cache dir
            getString(R.string.temp_images_dir)) //gets the directory for the temporary images dir

        tempImagesDir.mkdir() //Create the temp_images dir

        //Creates the temp_image.jpg file
        val tempImage = File(
            tempImagesDir, //prefix the new abstract path with the temporary images dir path
            getString(R.string.temp_image)) //gets the abstract temp_image file name

        //Returns the Uri object to be used with ActivityResultLauncher
        return FileProvider.getUriForFile(
            applicationContext,
            getString(R.string.authorities),
            tempImage)
    }
    // Fin de la función initTempUri

//  Fin de funciones necesarias para la cámara

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