package com.uits.takephoto

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


const val REQUEST_IMAGE_CAPTURE = 100

class MainActivity : AppCompatActivity() {

    private lateinit var currentPhotoPath: String
    private lateinit var photoUri: Uri
    private lateinit var photoFile: File
    private lateinit var mBtnPhoto: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mBtnPhoto = findViewById(R.id.mBtnPhoto) as Button


        mBtnPhoto.setOnClickListener {
            takePhoto()
        }
    }


    /**
     * take photo using intent
     */
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file = createImageFile()
        val photoURI: Uri = FileProvider.getUriForFile(
            this,
            "com.uits.takephoto.fileprovider",
            file
        )
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }

    /**
     * take photo and permission
     */
    fun takePhoto() {
        photoFile = File(this.application.filesDir, "IMG_TAKE_PHOTO_${Date().time.toString()}.jpg")
        photoUri = FileProvider.getUriForFile(this, "com.uits.takephoto.fileprovider", photoFile)
        val packageManager: PackageManager = this.packageManager
        val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val resolveActivity: ResolveInfo = packageManager.resolveActivity(captureImage, PackageManager.MATCH_DEFAULT_ONLY) ?: return

        captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        val cameraActivities: List<ResolveInfo> = packageManager.queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY)

        for (cameraActivity in cameraActivities) {
            this.grantUriPermission(cameraActivity.activityInfo.packageName, photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        startActivityForResult(captureImage, REQUEST_IMAGE_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            //imageView.setImageBitmap(imageBitmap)

           // val bitmap: Bitmap = getScaledBitmap(photoFile.getPath(), this)!!
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    fun getScaledBitmap(path: String?, activity: Activity): Bitmap? {
        val size = Point()
        val outMetrics = DisplayMetrics()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val display = activity.display
            display?.getRealMetrics(outMetrics)
        } else {
            @Suppress("DEPRECATION")
            val display = activity.windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getMetrics(outMetrics)
        }
        return getScaledBitmap(path, size.x, size.y)
    }

    fun getScaledBitmap(path: String?, destWidth: Int, destHeight: Int): Bitmap? { // Read in the dimensions of the image on disk
        var options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)
        val srcWidth = options.outWidth.toFloat()
        val srcHeight = options.outHeight.toFloat()
        // Figure out how much to scale down by
        var inSampleSize = 1
        if (srcHeight > destHeight || srcWidth > destWidth) {
            val heightScale = srcHeight / destHeight
            val widthScale = srcWidth / destWidth
            inSampleSize = Math.round(if (heightScale > widthScale) heightScale else widthScale)
        }
        options = BitmapFactory.Options()
        options.inSampleSize = inSampleSize
        // Read in and create final bitmap
        return BitmapFactory.decodeFile(path, options)
    }
}
