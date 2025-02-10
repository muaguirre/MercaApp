package com.eric.mercaapp.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _resetStatus = MutableLiveData<String?>()
    val resetStatus: LiveData<String?> get() = _resetStatus

    fun sendPasswordReset(email: String) {
        if (email.isEmpty()) {
            _resetStatus.value = "El correo es obligatorio"
            return
        }

        _loading.value = true

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                _loading.value = false
                if (task.isSuccessful) {
                    _resetStatus.value = "Se ha enviado un enlace para restablecer la contraseña"
                } else {
                    _resetStatus.value = "Error: ${task.exception?.message}"
                    Log.e("ForgotPasswordViewModel", "Error al enviar email: ${task.exception?.message}")
                }
            }
    }
}
