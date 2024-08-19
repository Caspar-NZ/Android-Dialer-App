package com.example.a21010371dialerapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val requestCallPermissions = 1
    private lateinit var dialNumberEditText: EditText
    private var permissionRequested = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dialNumberEditText = findViewById(R.id.dialNumber)

        if (intent?.action == Intent.ACTION_DIAL) {
            //Extract the phone number from the intent's data URI
            val data: Uri? = intent?.data
            val phoneNumber = data?.schemeSpecificPart
            if (!phoneNumber.isNullOrEmpty()) {
                dialNumberEditText.setText(phoneNumber)
            }
        }

        //Set click listeners for numeric buttons
        val numericButtons = arrayOf(
            findViewById(R.id.button0),
            findViewById(R.id.button1),
            findViewById(R.id.button2),
            findViewById(R.id.button3),
            findViewById(R.id.button4),
            findViewById(R.id.button5),
            findViewById(R.id.button6),
            findViewById(R.id.button7),
            findViewById(R.id.button8),
            findViewById<Button>(R.id.button9)
        )

        for (button in numericButtons) {
            button.setOnClickListener {
                appendToEditText(dialNumberEditText, button.text.toString())
            }
        }

        //Set click listener for * (star) button
        findViewById<Button>(R.id.buttonStar).setOnClickListener {
            appendToEditText(dialNumberEditText, "*")
        }

        //Set click listener for # (hash) button
        findViewById<Button>(R.id.buttonHash).setOnClickListener {
            appendToEditText(dialNumberEditText, "#")
        }

        //Set click listener for Back button
        findViewById<Button>(R.id.buttonBack).setOnClickListener {
            val currentText = dialNumberEditText.text.toString()
            if (currentText.isNotEmpty()) {
                val newText = currentText.substring(0, currentText.length - 1)
                dialNumberEditText.setText(newText)
            }
        }

        //Set click listener for the "Call" button
        findViewById<Button>(R.id.buttonCall).setOnClickListener {
            val phoneNumber = dialNumberEditText.text.toString()
            if (phoneNumber.isNotEmpty()) {
                if (checkCallPhonePermission()) {
                    makePhoneCall(phoneNumber)
                } else {
                    //Request permissions and show dialog first
                    showCallPermissionDialog()
                }
            }
        }
        //Request calling permissions if not granted
        if (!checkCallPhonePermission()) {
            //Show dialog for permission request
            showCallPermissionDialog()
        }
    }

    private fun makePhoneCall(phoneNumber: String) {
        val callUri = Uri.parse("tel:$phoneNumber")
        val callIntent = Intent(Intent.ACTION_CALL, callUri)
        //Check if the app has the permissions to make a call
        if (checkCallPhonePermission()) {
            startActivity(callIntent)
        } else {
            //Permission not granted
            if (!permissionRequested) {
                //Request permissions and show dialog first which explains to user why permissions are needed
                showCallPermissionDialog()
            } else {
                //If user has already denied permissions then dialog linking to app settings is shown
                showPermissionDeniedDialog()
            }
        }
    }

    private fun checkCallPhonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCallPhonePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CALL_PHONE),
            requestCallPermissions
        )
    }

    private fun showCallPermissionDialog() {
        permissionRequested = true
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Permission Required")
        alertDialogBuilder.setMessage("Please allow call permissions to enable this app to function.")
        alertDialogBuilder.setPositiveButton("OK") { _, _ ->
            requestCallPhonePermission()
        }
        alertDialogBuilder.setCancelable(false)
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
    //If permissions denied then tells the user that it is required and links to app settings
    private fun showPermissionDeniedDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Permission Denied")
        alertDialogBuilder.setMessage("The call permission is required to make phone calls, please go to settings and enable.")
        alertDialogBuilder.setNegativeButton("Later") { dialog, _ ->
            dialog.dismiss()
        }
        alertDialogBuilder.setPositiveButton("App Settings") { _, _ ->
            openAppSettings()
        }
        alertDialogBuilder.setCancelable(false)
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
    //If permissions have been denied and Android blocks the popup to approve permissions then it links to app settings
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }
    //Set the text field
    private fun appendToEditText(editText: EditText, textToAppend: String) {
        val currentText = editText.text.toString()
        val newText = currentText + textToAppend
        editText.setText(newText)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCallPermissions) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Permission granted, make the phone call
                val phoneNumber = dialNumberEditText.text.toString()
                makePhoneCall(phoneNumber)
            } else {
                showPermissionDeniedDialog()
            }
        }
    }

}
