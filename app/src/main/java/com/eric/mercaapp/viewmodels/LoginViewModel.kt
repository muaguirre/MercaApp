package com.eric.mercaapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> get() = _loginSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun loginUser(email: String, password: String) {
        if (email.isEmpty()) {
            _errorMessage.value = "El correo es obligatorio"
            return
        }

        if (password.isEmpty()) {
            _errorMessage.value = "La contraseña es obligatoria"
            return
        }

        _loading.value = true

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _loading.value = false

                if (task.isSuccessful) {
                    _loginSuccess.value = true
                } else {
                    val errorMessage = when {
                        task.exception?.localizedMessage?.contains("There is no user record") == true ->
                            "No existe una cuenta con este correo"
                        task.exception?.localizedMessage?.contains("password is invalid") == true ->
                            "Contraseña incorrecta"
                        task.exception?.localizedMessage?.contains("badly formatted") == true ->
                            "Formato de correo inválido"
                        else -> task.exception?.localizedMessage ?: "Error desconocido"
                    }
                    _errorMessage.value = errorMessage
                }
            }
    }

    fun checkIfUserIsLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}
