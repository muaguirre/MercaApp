package com.eric.mercaapp.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.eric.mercaapp.models.Ad
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _adsList = MutableLiveData<List<Ad>>()
    val adsList: LiveData<List<Ad>> get() = _adsList

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _categories = MutableLiveData<Set<String>>()
    val categories: LiveData<Set<String>> get() = _categories

    private val _provinces = MutableLiveData<Set<String>>()
    val provinces: LiveData<Set<String>> get() = _provinces

    private val _auctionEndedEvent = MutableLiveData<Triple<String, String, String>>()
    val auctionEndedEvent: LiveData<Triple<String, String, String>> get() = _auctionEndedEvent

    init {
        obtenerTokenFCM()
        observeAds()
    }

    private fun obtenerTokenFCM() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("MainViewModel", "No se pudo obtener el token", task.exception)
                    return@addOnCompleteListener
                }
                val token = task.result
                Log.d("MainViewModel", "Token del usuario: $token")
            }
    }

    private fun observeAds() {
        _loading.value = true

        db.collection("ads")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("MainViewModel", "Error al escuchar cambios en Firestore: ${error.message}")
                    _loading.value = false
                    return@addSnapshotListener
                }

                val currentTime = System.currentTimeMillis()
                val updatedAdsList = mutableListOf<Ad>()
                val categoriesSet = mutableSetOf<String>()
                val provincesSet = mutableSetOf<String>()

                snapshots?.documents?.forEach { document ->
                    val ad = document.toObject(Ad::class.java)
                    if (ad != null) {
                        ad.id = document.id

                        if (ad.endTime <= currentTime && ad.status == "open") {
                            db.collection("ads").document(document.id)
                                .update("status", "closed")
                                .addOnSuccessListener {
                                    Log.d("MainViewModel", "Anuncio ${document.id} cerrado")
                                    fetchAuctionWinners(ad)
                                }
                                .addOnFailureListener { e ->
                                    Log.e("MainViewModel", "Error al cerrar anuncio: ${e.message}")
                                }
                        }

                        if (ad.endTime > currentTime && ad.status == "open") {
                            updatedAdsList.add(ad)
                            categoriesSet.add(ad.categoria)
                            provincesSet.add(ad.provinciaRecogida)
                        }
                    }
                }

                _adsList.value = updatedAdsList
                _categories.value = categoriesSet
                _provinces.value = provincesSet
                _loading.value = false
            }
    }

    private fun fetchAuctionWinners(ad: Ad) {
        db.collection("ads").document(ad.id).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val clientId = document.getString("clientId") ?: return@addOnSuccessListener
                val winnerId = document.getString("lowestBidderId") ?: return@addOnSuccessListener
                val adTitle = document.getString("titulo") ?: return@addOnSuccessListener

                Log.d("MainViewModel", "Subasta finalizada: Cliente ($clientId) - Ganador ($winnerId)")


                _auctionEndedEvent.value = Triple(clientId, winnerId, adTitle)
            }
        }.addOnFailureListener { e ->
            Log.e("MainViewModel", "Error obteniendo datos del anuncio: ${e.message}")
        }
    }

    fun logout() {
        auth.signOut()
    }
}
