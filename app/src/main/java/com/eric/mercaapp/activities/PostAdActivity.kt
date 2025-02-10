package com.eric.mercaapp.activities

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.eric.mercaapp.R
import com.eric.mercaapp.viewmodels.PostAdViewModel

class PostAdActivity : AppCompatActivity() {

    private val viewModel: PostAdViewModel by viewModels()

    private lateinit var editTextTitle: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var editTextStartingPrice: EditText
    private lateinit var editTextTownOrigin: EditText
    private lateinit var editTextTownDestination: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerProvinceOrigin: Spinner
    private lateinit var spinnerProvinceDestination: Spinner
    private lateinit var spinnerDuration: Spinner
    private lateinit var buttonSelectImage: Button
    private lateinit var buttonPostAd: Button
    private lateinit var imageViewAd: ImageView
    private lateinit var progressBar: ProgressBar

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            viewModel.setImageUri(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_ad)

        editTextTitle = findViewById(R.id.editTextTitle)
        editTextDescription = findViewById(R.id.editTextDescription)
        editTextStartingPrice = findViewById(R.id.editTextStartingPrice)
        editTextTownOrigin = findViewById(R.id.editTextTownOrigin)
        editTextTownDestination = findViewById(R.id.editTextTownDestination)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerProvinceOrigin = findViewById(R.id.spinnerProvinceOrigin)
        spinnerProvinceDestination = findViewById(R.id.spinnerProvinceDestination)
        spinnerDuration = findViewById(R.id.spinnerDuration)
        buttonSelectImage = findViewById(R.id.buttonSelectImage)
        buttonPostAd = findViewById(R.id.buttonPostAd)
        imageViewAd = findViewById(R.id.imageViewAd)
        progressBar = findViewById(R.id.progressBar)

        setupSpinners()
        observeViewModel()

        buttonSelectImage.setOnClickListener { imagePickerLauncher.launch("image/*") }
        buttonPostAd.setOnClickListener { validateAndPostAd() }
    }

    private fun observeViewModel() {
        viewModel.imageUri.observe(this) { uri ->
            imageViewAd.setImageURI(uri)
        }

        viewModel.loading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            buttonPostAd.isEnabled = !isLoading
        }

        viewModel.adPosted.observe(this) { posted ->
            if (posted) {
                Toast.makeText(this, "Anuncio publicado con éxito", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupSpinners() {
        val categories = resources.getStringArray(R.array.categories)
        spinnerCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        val provinces = resources.getStringArray(R.array.provinces)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, provinces)
        spinnerProvinceOrigin.adapter = adapter
        spinnerProvinceDestination.adapter = adapter

        val durations = arrayOf("1 hora", "3 horas", "6 horas", "12 horas", "1 día", "2 días", "3 días")
        spinnerDuration.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, durations)
    }


    private fun validateAndPostAd() {
        val title = editTextTitle.text.toString().trim()
        val description = editTextDescription.text.toString().trim()
        val category = spinnerCategory.selectedItem?.toString() ?: ""
        val provinceOrigin = spinnerProvinceOrigin.selectedItem?.toString() ?: ""
        val townOrigin = editTextTownOrigin.text.toString().trim()
        val provinceDestination = spinnerProvinceDestination.selectedItem?.toString() ?: ""
        val townDestination = editTextTownDestination.text.toString().trim()
        val startingPrice = editTextStartingPrice.text.toString().trim().toDoubleOrNull()

        val durationIndex = spinnerDuration.selectedItemPosition
        val durationValues = arrayOf(1, 3, 6, 12, 24, 48, 72)
        val durationHours = durationValues.getOrElse(durationIndex) { 24 }

        if (title.isEmpty() || description.isEmpty() || category.isEmpty() || provinceOrigin.isEmpty() ||
            townOrigin.isEmpty() || provinceDestination.isEmpty() || townDestination.isEmpty() ||
            startingPrice == null || startingPrice <= 0) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }


        viewModel.postAd(title, description, category, provinceOrigin, townOrigin, provinceDestination, townDestination, startingPrice, durationHours)
    }
}
