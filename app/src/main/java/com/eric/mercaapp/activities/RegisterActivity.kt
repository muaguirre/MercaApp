package com.eric.mercaapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.eric.mercaapp.R
import com.eric.mercaapp.viewmodels.RegisterViewModel

class RegisterActivity : AppCompatActivity() {

    private val viewModel: RegisterViewModel by viewModels()

    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etName: EditText = findViewById(R.id.et_name)
        val etEmail: EditText = findViewById(R.id.et_email)
        val etPassword: EditText = findViewById(R.id.et_password)
        val etPopulation: EditText = findViewById(R.id.et_population)
        val spinnerProvince: Spinner = findViewById(R.id.spinner_province)
        val roleRadioGroup: RadioGroup = findViewById(R.id.roleRadioGroup)
        val registerButton: Button = findViewById(R.id.registerButton)
        progressBar = findViewById(R.id.progressBar)

        val provinces = resources.getStringArray(R.array.provinces)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, provinces)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProvince.adapter = adapter

        viewModel.loading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            registerButton.isEnabled = !isLoading
        }

        viewModel.registerSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        registerButton.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val population = etPopulation.text.toString().trim()
            val selectedProvince = spinnerProvince.selectedItem.toString()
            val selectedRole = when (roleRadioGroup.checkedRadioButtonId) {
                R.id.clientRadioButton -> "Cliente"
                R.id.transportistaRadioButton -> "Transportista"
                else -> ""
            }

            viewModel.registerUser(name, email, password, population, selectedProvince, selectedRole)
        }
    }
}
