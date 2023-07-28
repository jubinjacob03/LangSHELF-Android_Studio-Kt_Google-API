package com.example.project

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.project.ui.theme.ProjectTheme
import com.google.android.material.button.MaterialButton
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var source: EditText
    private lateinit var targetlang: TextView
    private lateinit var srclangchoose: Button
    private lateinit var trgtlangchoose: Button
    private lateinit var transbtn: Button


    private lateinit var translatorOption: TranslatorOptions

    private lateinit var translator: Translator

    private lateinit var progressDialog: ProgressDialog

    companion object{

        private const val TAG = "MAIN_TAG"
    }

    private var languageArrayList: ArrayList<ModelLanguage>? = null

    private var sourceLanguageCode = "en"
    private var sourceLanguageTitle = "English"
    private var targetLanguageCode = "ta"
    private var targetLanguageTitle = "Tamil"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main1)

        source = findViewById(R.id.source)
        targetlang = findViewById(R.id.targetlang)
        srclangchoose = findViewById(R.id.srclangchoose)
        trgtlangchoose = findViewById(R.id.trgtlangchoose)
        transbtn = findViewById(R.id.transbtn)


        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        loadAvailableLanguages()

        srclangchoose.setOnClickListener {
            sourceLanguageChoose()
        }

        trgtlangchoose.setOnClickListener {
            targetLanguageChoose()
        }

        transbtn.setOnClickListener {
            validateData()
        }

    }

    private var sourceLanguageText = ""
    private fun validateData() {

        sourceLanguageText = source.text.toString().trim()

        Log.d(TAG,"validateData: sourceLanguageText: $sourceLanguageText")

        if (sourceLanguageText.isEmpty()){
            showToast("Error, Enter text to translate")
        }
        else{
            startTranslation()
        }

    }

    private fun startTranslation() {
        progressDialog.setMessage("Processing the Language ....")
        progressDialog.show()


        translatorOption = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguageCode)
            .setTargetLanguage(targetLanguageCode)
            .build()
        translator = Translation.getClient(translatorOption)

        val downloadConditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        translator.downloadModelIfNeeded(downloadConditions)
            .addOnSuccessListener {
                Log.d(TAG, "startTranslation: model is ready, start translation ...")

                progressDialog.setMessage("Translating ...")

                translator.translate(sourceLanguageText)
                    .addOnSuccessListener { translatedText ->
                        Log.d(TAG, "startTranslation: translatedText: $translatedText")

                        progressDialog.dismiss()

                        targetlang.text = translatedText

                    }
                    .addOnFailureListener{ e ->

                        progressDialog.dismiss()
                        Log.e(TAG, "startTranslation: ", e)

                        showToast("Failed to translate due to ${e.message}")
                    }

            }
            .addOnFailureListener{ e ->

                progressDialog.dismiss()
                Log.e(TAG, "startTranslation: ", e)

                showToast("Failed to translate due to ${e.message}")

            }

    }

    private fun loadAvailableLanguages(){

        languageArrayList = ArrayList()

        val languageCodeList = TranslateLanguage.getAllLanguages()

        for (languageCode in languageCodeList) {

            val languageTitle = Locale(languageCode).displayLanguage

            Log.d(TAG, "loadAvailableLanguages: LanguageCode: $languageCode")
            Log.d(TAG, "loadAvailableLanguages: LanguageTitle: $languageTitle")

            val modelLanguage = ModelLanguage(languageCode, languageTitle)

            languageArrayList!!.add(modelLanguage)
        }
    }

    private fun sourceLanguageChoose(){

        val popupMenu = PopupMenu(this, srclangchoose)

        for (i in languageArrayList!!.indices){

            popupMenu.menu.add(Menu.NONE, i, i, languageArrayList!![i].languageTitle)
        }

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { menuItem ->

            val position = menuItem.itemId

            sourceLanguageCode = languageArrayList!![position].languageCode
            sourceLanguageTitle = languageArrayList!![position].languageTitle

            srclangchoose.text = sourceLanguageTitle
            source.hint = "Enter $sourceLanguageTitle"

            Log.d(TAG, "sourceLanguageChoose: sourceLanguageCode: $sourceLanguageCode")
            Log.d(TAG, "sourceLanguageChoose: sourceLanguageTitle: $sourceLanguageTitle")

            false

        }

    }

    private fun targetLanguageChoose(){


        val popupMenu = PopupMenu(this, srclangchoose)

        for (i in languageArrayList!!.indices){

            popupMenu.menu.add(Menu.NONE, i, i, languageArrayList!![i].languageTitle)
        }

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { menuItem ->

            val position = menuItem.itemId

            targetLanguageCode = languageArrayList!![position].languageCode
            targetLanguageTitle = languageArrayList!![position].languageTitle

            trgtlangchoose.text = targetLanguageTitle


            Log.d(TAG, "targetLanguageChoose: targetLanguageCode: $targetLanguageCode")
            Log.d(TAG, "targetLanguageChoose: targetLanguageTitle: $targetLanguageTitle")

            false
        }
    }

    private fun  showToast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}