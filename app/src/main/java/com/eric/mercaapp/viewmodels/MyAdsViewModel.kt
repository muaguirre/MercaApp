package com.eric.mercaapp.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.eric.mercaapp.models.Ad
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyAdsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _adsList = MutableLiveData<List<Ad>>()
    val adsList: LiveData<List<Ad>> get() = _adsList

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    init {
        listenForAdsChanges()
    }

    private fun listenForAdsChanges() {
        if (currentUserId.isEmpty()) {
            Log.e("MyAdsViewModel", " Error: Usuario no autenticado")
            return
        }

        _loading.value = true

        db.collection("ads")
            .whereEqualTo("clientId", currentUserId)
            .addSnapshotListener { snapshots, error ->
                _loading.value = false

                if (error != null) {
                    Log.e("MyAdsViewModel", "Error al escuchar cambios: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val tempAdsList = snapshots.documents.mapNotNull { doc ->
                        doc.toObject(Ad::class.java)?.apply { id = doc.id }
                    }

                    Log.d("MyAdsViewModel", "Anuncios detectados: ${tempAdsList.size}")
                    _adsList.value = tempAdsList
                }
            }
    }

    fun refreshAds() {
        _loading.value = true

        db.collection("ads")
            .whereEqualTo("clientId", currentUserId)
            .get()
            .addOnSuccessListener { snapshots ->
                _loading.value = false

                val tempAdsList = snapshots.documents.mapNotNull { doc ->
                    doc.toObject(Ad::class.java)?.apply { id = doc.id }
                }

                Log.d("MyAdsViewModel", "Carga manual de anuncios: ${tempAdsList.size}")
                _adsList.value = tempAdsList
            }
            .addOnFailureListener { error ->
                _loading.value = false
                Log.e("MyAdsViewModel", "Error al cargar anuncios manualmente: ${error.message}")
            }
    }
}
