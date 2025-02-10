package com.eric.mercaapp.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class PostAdViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _imageUri = MutableLiveData<Uri?>()
    val imageUri: LiveData<Uri?> get() = _imageUri

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _adPosted = MutableLiveData<Boolean>()
    val adPosted: LiveData<Boolean> get() = _adPosted

    fun setImageUri(uri: Uri?) {
        _imageUri.value = uri
    }

    fun postAd(
        title: String,
        description: String,
        category: String,
        provinceOrigin: String,
        townOrigin: String,
        provinceDestination: String,
        townDestination: String,
        startingPrice: Double,
        durationHours: Int
    ) {
        val userId = auth.currentUser?.uid ?: run {
            Log.e("PostAdViewModel", "Error: Usuario no autenticado")
            return
        }

        val imageUri = _imageUri.value
        if (imageUri == null) {
            Log.e("PostAdViewModel", "Error: No se ha seleccionado una imagen")
            return
        }

        _loading.value = true

        val imageRef = storage.reference.child("ads/${UUID.randomUUID()}.jpg")
        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                    saveAdToFirestore(
                        title,
                        description,
                        category,
                        provinceOrigin,
                        townOrigin,
                        provinceDestination,
                        townDestination,
                        startingPrice,
                        durationHours,
                        imageUrl.toString(),
                        userId
                    )
                }
            }
            .addOnFailureListener {
                _loading.value = false
                Log.e("PostAdViewModel", "Error al subir imagen: ${it.message}")
            }
    }

    private fun saveAdToFirestore(
        title: String,
        description: String,
        category: String,
        provinceOrigin: String,
        townOrigin: String,
        provinceDestination: String,
        townDestination: String,
        startingPrice: Double,
        durationHours: Int,
        imageUrl: String,
        userId: String
    ) {
        val fechaPublicacion = System.currentTimeMillis()
        val endTime = fechaPublicacion + (durationHours * 60 * 60 * 1000)

        val ad = mapOf(
            "titulo" to title,
            "descripcion" to description,
            "categoria" to category,
            "provincia_recogida" to provinceOrigin,
            "poblacion_recogida" to townOrigin,
            "provincia_entrega" to provinceDestination,
            "poblacion_entrega" to townDestination,
            "imagen" to imageUrl,
            "startingPrice" to startingPrice,
            "lowestBid" to startingPrice,
            "lowestBidderId" to "",
            "fecha_publicacion" to fechaPublicacion,
            "endTime" to endTime,
            "status" to "open",
            "clientId" to userId
        )

        db.collection("ads").add(ad)
            .addOnSuccessListener {
                _loading.value = false
                _adPosted.value = true
                Log.d("PostAdViewModel", "Anuncio publicado con éxito")
            }
            .addOnFailureListener {
                _loading.value = false
                Log.e("PostAdViewModel", "Error al publicar anuncio: ${it.message}")
            }
    }
}
