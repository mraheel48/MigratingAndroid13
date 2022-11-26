package com.example.migratingandroid13

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.migratingandroid13.databinding.ActivityMainBinding
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private lateinit var mainBinding: ActivityMainBinding

    private val readImagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(
            Manifest.permission.ACCESS_MEDIA_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    } else {
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private val REQUEST_GALLERY_IMAGE = 20202
    private val REQUEST_Message_Sender = 10101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        mainBinding.btnRead.setOnClickListener {
            newImagePicker()
        }

        mainBinding.btnSend.setOnClickListener {
            newMessageSender()
        }
    }

    private fun newMessageSender() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (EasyPermissions.hasPermissions(
                    this@MainActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            ) {
                Log.d("myPermission", "hasPermissions allow Notification")
                sendNotification()
            } else {
                EasyPermissions.requestPermissions(
                    this, "Please allow permissions to proceed further",
                    REQUEST_Message_Sender, Manifest.permission.POST_NOTIFICATIONS
                )
            }

        } else {
            sendNotification()
        }

    }

    private fun sendNotification() {

        //for notification channel
        createNotificationChannel()

        val notificationManager = ContextCompat.getSystemService(
            applicationContext, NotificationManager::class.java
        ) as NotificationManager

        notificationManager.cancelNotifications()

        notificationManager.sendNotification(
            getString(R.string.app_name),
            getString(R.string.send_message),
            applicationContext
        )

    }

    //Note this method not Remove in Application
    //1. This is Allow to create Channet in Android
    //2. Also Allow the Android 33 Notifitaion Permission
    //3. Calling in In Your App.!!!
    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationChannel = NotificationChannel(
                applicationContext.getString(R.string.notification_channel_id),
                applicationContext.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setShowBadge(false)
            }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Reminder"

            val notificationManager = applicationContext.getSystemService(
                NotificationManager::class.java
            )

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun newImagePicker() {
        if (EasyPermissions.hasPermissions(this@MainActivity, *readImagePermission)) {
            Log.d("myPermission", "hasPermissions allow")
            openGalleryNewWay()
        } else {
            EasyPermissions.requestPermissions(
                this, "Please allow permissions to proceed further",
                REQUEST_GALLERY_IMAGE, *readImagePermission
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun openGalleryNewWay() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            // Launch the photo picker and allow the user to choose only images.
            if (packageManager != null) {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else {
                showToast(this, getString(R.string.something_went_wrong))
            }
        } else {
            // Your previous implementation
            val intent = Intent()
            intent.action = Intent.ACTION_PICK
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            if (packageManager != null) {
                getResult.launch(intent)
            } else {
                showToast(this, getString(R.string.something_went_wrong))
            }
        }
    }

    // Receiver
    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {

                val value = it.data?.data

                if (value != null) {

                    Log.d("myData", "$value")

                    Glide.with(this@MainActivity)
                        .load(value)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(mainBinding.imageView)

                } else {
                    Log.d("myData", "values is null")
                }

            }

        }

    //Select a single Media
    /* // Launch the photo picker and allow the user to choose images and videos.
     pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageAndVideo))

     // Launch the photo picker and allow the user to choose only images.
     pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))

     // Launch the photo picker and allow the user to choose only videos.
     pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.VideoOnly))

     // Launch the photo picker and allow the user to choose only images/videos of a
     // specific MIME type, such as GIFs.
     val mimeType = "image/gif"
     pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.SingleMimeType(mimeType)))*/

    // Registers a photo picker activity launcher in single-select mode.
    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")

                Glide.with(this@MainActivity)
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(mainBinding.imageView)

            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    // Registers a photo picker activity launcher in multi-select mode.
    // In this example, the app allows the user to select up to 5 media files.
    /*val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
            // Callback is invoked after the user selects media items or closes the
            // photo picker.
            if (uris.isNotEmpty()) {
                Log.d("PhotoPicker", "Number of items selected: ${uris.size}")
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }*/

    // For this example, launch the photo picker and allow the user to choose images
// and videos. If you want the user to select a specific type of media file,
// use the overloaded versions of launch(), as shown in the section about how
// to select a single media item.
//    pickMultipleMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageAndVideo))

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Log.d("myPermission", "allow permission ${perms.size}")

        when (requestCode) {
            REQUEST_GALLERY_IMAGE -> {
                openGalleryNewWay()
            }
            REQUEST_Message_Sender -> {
                sendNotification()
            }
            else -> {
                Log.d("myPermissionsGranted", "no any  Permission allow")
            }
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Log.d("myPermission", "not allow")
        if (EasyPermissions.somePermissionPermanentlyDenied(this@MainActivity, perms)) {
            AppSettingsDialog.Builder(this@MainActivity).build().show()
        }
    }

    private fun showToast(c: Context, message: String) {
        try {
            if (!(c as Activity).isFinishing) {
                c.runOnUiThread { //show your Toast here..
                    Toast.makeText(c.applicationContext, message, Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}