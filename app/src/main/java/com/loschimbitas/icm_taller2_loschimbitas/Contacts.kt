package com.loschimbitas.icm_taller2_loschimbitas

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.test_icm.semana5.ContactsAdapter
import com.loschimbitas.icm_taller2_loschimbitas.databinding.ActivityContactsBinding

class Contacts : AppCompatActivity() {

    private lateinit var binding: ActivityContactsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if the app has the permission to read contacts
        checkPermissions()
    }

    /**
     * @name: checkPermissions
     * @description: Check if the app has the permission to read contacts
     * @return: void
     * @exception: none
     */
    private fun checkPermissions() {
        // Check if the permission is not granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Check if the user has previously denied the permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_CONTACTS
                )
            )
            // The user has previously denied the permission but not permanently.
                requestContactsPermission()
            else
            // The user has never been asked for the permission.
                requestContactsPermission()
        } else
        // Permission granted
            initContacts()

    }

    /**
     * @name: requestContactsPermission
     * @description: Request the permission to read contacts
     * @return: void
     * @exception: none
     */
    private fun requestContactsPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_CONTACTS),
            1
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
        if (requestCode == 1)
        // Permission granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Initialize the contacts
                initContacts()
            } else
            // Permission denied permanently
                Toast.makeText(this, "Permission denied permanently", Toast.LENGTH_SHORT).show()
    }

    /**
     * @name: initContacts
     * @description: Initialize the list of contacts
     * @return: void
     * @exception: none
     */
    @SuppressLint("Recycle")
    private fun initContacts() {
        val mProjection: Array<String> =
            arrayOf(ContactsContract.Profile._ID, ContactsContract.Profile.DISPLAY_NAME_PRIMARY)
        val mContactsAdapter = ContactsAdapter(this, null, 0)

        binding.listView.adapter = mContactsAdapter
        val mCursor: Cursor? = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            mProjection,
            null,
            null,
            null
        )
        mContactsAdapter.changeCursor(mCursor)

    }
}