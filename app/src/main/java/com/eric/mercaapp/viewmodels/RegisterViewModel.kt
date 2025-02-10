package com.eric.mercaapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class RegisterViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _registerSuccess = MutableLiveData<Boolean>()
    val registerSuccess: LiveData<Boolean> get() = _registerSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun registerUser(name: String, email: String, password: String, population: String, selectedProvince: String, selectedRole: String) {
        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || population.isEmpty() || selectedRole.isEmpty()) {
            _errorMessage.value = "Por favor, completa todos los campos"
            return
        }

        if (password.length < 6) {
            _errorMessage.value = "La contraseña debe tener al menos 6 caracteres"
            return
        }

        _loading.value = true

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        FirebaseMessaging.getInstance().token
                            .addOnCompleteListener { tokenTask ->
                                if (!tokenTask.isSuccessful) {
                                    _errorMessage.value = "Error obteniendo FCM Token"
                                    _loading.value = false
                                    return@addOnCompleteListener
                                }

                                val fcmToken = tokenTask.result

                                val user = hashMapOf(
                                    "clientId" to userId,
                                    "name" to name,
                                    "email" to email,
                                    "population" to population,
                                    "province" to selectedProvince,
                                    "role" to selectedRole,
                                    "fcmToken" to fcmToken
                                )

                                db.collection("usuarios").document(userId)
                                    .set(user)
                                    .addOnSuccessListener {
                                        _registerSuccess.value = true
                                    }
                                    .addOnFailureListener { e ->
                                        _errorMessage.value = "Error en Firestore: ${e.message}"
                                    }
                            }
                    }
                } else {
                    _errorMessage.value = "Error en el registro: ${task.exception?.message}"
                }

                _loading.value = false
            }
    }
}
