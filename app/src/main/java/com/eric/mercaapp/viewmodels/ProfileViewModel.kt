package com.eric.mercaapp.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid.orEmpty()

    private val _userData = MutableLiveData<Map<String, String>>()
    val userData: LiveData<Map<String, String>> get() = _userData

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    init {
        loadUserData()
    }

    private fun loadUserData() {
        if (currentUserId.isEmpty()) {
            Log.e("ProfileViewModel", "Error: Usuario no autenticado")
            return
        }

        _loading.value = true

        db.collection("usuarios").document(currentUserId).addSnapshotListener { document, error ->
            _loading.value = false

            if (error != null) {
                Log.e("ProfileViewModel", "Error al cargar datos: ${error.message}")
                return@addSnapshotListener
            }

            if (document != null && document.exists()) {
                val userData = mapOf(
                    "name" to (document.getString("name") ?: ""),
                    "population" to (document.getString("population") ?: ""),
                    "province" to (document.getString("province") ?: ""),
                    "email" to (document.getString("email") ?: "")
                )

                _userData.value = userData
            } else {
                Log.e("ProfileViewModel", "No se encontraron datos de usuario")
            }
        }
    }

    fun saveUserData(name: String, population: String, province: String) {
        if (currentUserId.isEmpty()) {
            Log.e("ProfileViewModel", "Error: Usuario no autenticado")
            return
        }

        if (name.isEmpty() || population.isEmpty() || province.isEmpty()) {
            Log.e("ProfileViewModel", "Error: Todos los campos son obligatorios")
            return
        }

        _loading.value = true

        val userUpdates = mapOf(
            "name" to name,
            "population" to population,
            "province" to province
        )

        db.collection("usuarios").document(currentUserId)
            .update(userUpdates)
            .addOnSuccessListener {
                _loading.value = false
                Log.d("ProfileViewModel", "Datos actualizados correctamente")
            }
            .addOnFailureListener { e ->
                _loading.value = false
                Log.e("ProfileViewModel", "Error al actualizar datos: ${e.message}")
            }
    }
}
