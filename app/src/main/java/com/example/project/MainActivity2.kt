package com.example.project

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MainActivity2 : AppCompatActivity() {

    private lateinit var inputImageBtn: Button
    private lateinit var recognizeTextBtn: Button
    private lateinit var imageTv: ImageView
    private lateinit var recognizedTextEt: EditText
    private lateinit var intencapt: Button

    private companion object{
        private const val CAMERA_REQUEST_CODE = 100
        private const val STORAGE_REQUEST_CODE = 101
    }

    private var imageUri: Uri? = null

    private lateinit var cameraPermission: Array<String>
    private lateinit var storagePermission: Array<String>

    private lateinit var progressDialog : ProgressDialog

    private lateinit var textRecognizer:TextRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputImageBtn = findViewById(R.id.inputImageBtn)
        recognizeTextBtn = findViewById(R.id.recognizeTextBtn)
        imageTv = findViewById(R.id.imageTv)
        recognizedTextEt = findViewById(R.id.recognizedTextEt)
        intencapt = findViewById(R.id.intencapt)

        cameraPermission = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermission = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        progressDialog= ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        inputImageBtn.setOnClickListener {
            showInputImageDialog()
        }

        recognizeTextBtn.setOnClickListener {
            if (imageUri == null){
                showToast("Pick Image First...")
            }
            else{
                recognizeTextFromImage()
            }
        }
    }

    private fun recognizeTextFromImage() {
        progressDialog.setMessage("Preparing Image...")
        progressDialog.show()

        try {
            val inputImage = InputImage.fromFilePath(this,imageUri!!)
            progressDialog.setMessage("Recognizing text...")

            val textTaskResult = textRecognizer.process(inputImage)
                .addOnSuccessListener { text->

                    progressDialog.dismiss()
                    val recognizedText = text.text
                    recognizedTextEt.setText(recognizedText)
                    intencapt.setOnClickListener {
                        var intenttranslate = Intent(this@MainActivity2,MainActivity::class.java)
                        intenttranslate.putExtra("Recognizedtextintent",recognizedText.toString())
                        startActivity(intenttranslate)
                    }

                }
                .addOnFailureListener { e->
                    progressDialog.dismiss()
                    showToast("Failed to recognize text due to ${e.message}")
                }
        }
        catch (e: Exception){
            progressDialog.dismiss()
            showToast("Failed to prepare image due to ${e.message}")
        }
    }

    private fun showInputImageDialog() {
        val popupMenu = PopupMenu(this,inputImageBtn)

        popupMenu.menu.add(Menu.NONE,1,1,"CAMERA")
        popupMenu.menu.add(Menu.NONE,2,2,"GALLERY")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { menuItem->

            val id = menuItem.itemId
            if (id == 1){
                if (checkCameraPermission()){
                    pickImageCamera()
                }
                else{
                     requestCameraPermission()
                }
            }
            else if (id == 2){
                if (checkStoragePermission()){

                    pickImageGallery()
                }
                else {

                    requestStoragePermission()
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun pickImageGallery(){

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)

    }

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->

            if (result.resultCode == RESULT_OK){

                val data = result.data
                imageUri = data!!.data

                imageTv.setImageURI(imageUri)
            }
            else{

                showToast("Cancelled...!")
            }

            }
    private fun pickImageCamera(){
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Sample Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Sample Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->

            if (result.resultCode == Activity.RESULT_OK){
                imageTv.setImageURI(imageUri)
            }
            else{
                showToast("Cancelled...")
            }
        }

    private fun checkStoragePermission() : Boolean{

        return ContextCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkCameraPermission() : Boolean {

        val cameraResult = ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val storageResult = ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

        return cameraResult && storageResult
    }

    private fun requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE)
    }

    private fun requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermission, STORAGE_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            CAMERA_REQUEST_CODE ->{

                if (grantResults.isNotEmpty()){

                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED

                    if(cameraAccepted && storageAccepted){
                        pickImageCamera()
                    }
                    else{
                        showToast("Camera & Storage permission are required...")
                    }
                }
            }
            STORAGE_REQUEST_CODE->{

                if (grantResults.isNotEmpty()){

                    val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED

                    if(storageAccepted){
                        pickImageGallery()
                    }
                    else{
                        showToast("Storage permission are required...")
                    }
                }
            }
        }
    }

    private fun showToast(message:String){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
        }

}
