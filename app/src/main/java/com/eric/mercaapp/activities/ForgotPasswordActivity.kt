package com.eric.mercaapp.activities

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.eric.mercaapp.R
import com.eric.mercaapp.viewmodels.ForgotPasswordViewModel

class ForgotPasswordActivity : AppCompatActivity() {

    private val viewModel: ForgotPasswordViewModel by viewModels()

    private lateinit var etEmail: EditText
    private lateinit var btnResetPassword: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        etEmail = findViewById(R.id.et_email)
        btnResetPassword = findViewById(R.id.btnResetPassword)
        progressBar = findViewById(R.id.progressBar)

        btnResetPassword.setOnClickListener {
            val email = etEmail.text.toString().trim()
            viewModel.sendPasswordReset(email)
        }

        viewModel.loading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnResetPassword.isEnabled = !isLoading
        }

        viewModel.resetStatus.observe(this) { statusMessage ->
            statusMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                if (it.startsWith("Se ha enviado")) finish()
            }
        }
    }
}
