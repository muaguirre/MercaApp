package com.eric.mercaapp.activities

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.eric.mercaapp.R
import com.eric.mercaapp.viewmodels.ProfileViewModel

class ProfileActivity : AppCompatActivity() {

    private val viewModel: ProfileViewModel by viewModels()

    private lateinit var editTextName: EditText
    private lateinit var editTextPopulation: EditText
    private lateinit var editTextProvince: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var buttonSave: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        editTextName = findViewById(R.id.editTextName)
        editTextPopulation = findViewById(R.id.editTextPopulation)
        editTextProvince = findViewById(R.id.editTextProvince)
        editTextEmail = findViewById(R.id.editTextEmail)
        buttonSave = findViewById(R.id.buttonSave)
        progressBar = findViewById(R.id.progressBar)

        editTextEmail.isEnabled = false

        observeViewModel()

        buttonSave.setOnClickListener {
            val name = editTextName.text.toString().trim()
            val population = editTextPopulation.text.toString().trim()
            val province = editTextProvince.text.toString().trim()
            viewModel.saveUserData(name, population, province)
        }
    }

    private fun observeViewModel() {
        viewModel.userData.observe(this) { userData ->
            editTextName.setText(userData["name"])
            editTextPopulation.setText(userData["population"])
            editTextProvince.setText(userData["province"])
            editTextEmail.setText(userData["email"])
        }

        viewModel.loading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
}
