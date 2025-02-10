package com.eric.mercaapp.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.eric.mercaapp.models.Ad
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FinishedAdsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _adsList = MutableLiveData<List<Ad>>()
    val adsList: LiveData<List<Ad>> get() = _adsList

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    init {
        checkUserRoleAndListenForAds()
    }

    private fun checkUserRoleAndListenForAds() {
        val userId = auth.currentUser?.uid ?: run {
            Log.e("FinishedAdsViewModel", "Error: Usuario no autenticado")
            return
        }

        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { document ->
                val userRole = document.getString("role") ?: "Cliente"
                Log.d("FinishedAdsViewModel", "Usuario autenticado como: $userRole")
                listenForFinishedAds(userId)
            }
            .addOnFailureListener {
                Log.e("FinishedAdsViewModel", "Error al obtener el rol del usuario")
            }
    }

    private fun listenForFinishedAds(userId: String) {
        _loading.value = true

        db.collection("ads")
            .whereEqualTo("status", "closed")
            .addSnapshotListener { documents, error ->
                _loading.value = false

                if (error != null) {
                    Log.e("FinishedAdsViewModel", "Error al escuchar cambios en Firestore: ${error.message}")
                    return@addSnapshotListener
                }

                if (documents != null) {
                    val adsList = mutableListOf<Ad>()

                    for (document in documents.documents) {
                        val ad = document.toObject(Ad::class.java)?.apply { id = document.id }
                        val isClient = ad?.clientId == userId
                        val isTransportista = ad?.lowestBidderId == userId

                        if (ad != null && (isClient || isTransportista)) {
                            adsList.add(ad)
                        }
                    }

                    _adsList.value = adsList
                }
            }
    }
}
