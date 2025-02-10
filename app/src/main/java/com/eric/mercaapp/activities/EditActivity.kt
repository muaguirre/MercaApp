package com.eric.mercaapp.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.eric.mercaapp.R
import com.google.firebase.firestore.FirebaseFirestore

class EditAdActivity : AppCompatActivity() {

    private lateinit var editTextTitle: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var buttonUpdate: Button
    private lateinit var buttonDelete: Button
    private lateinit var imageViewAd: ImageView

    private val db = FirebaseFirestore.getInstance()
    private var adId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_ad)

        editTextTitle = findViewById(R.id.editTextTitle)
        editTextDescription = findViewById(R.id.editTextDescription)
        buttonUpdate = findViewById(R.id.buttonSave)
        buttonDelete = findViewById(R.id.buttonDelete)
        imageViewAd = findViewById(R.id.imageViewAd)

        adId = intent.getStringExtra("adId") ?: ""

        if (adId.isEmpty()) {
            Log.e("EditAdActivity", "No se recibió un ID de anuncio válido")
            Toast.makeText(this, "Error: No se pudo obtener el ID del anuncio", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("EditAdActivity", "Recibido adId: $adId")

        // Cargar datos del anuncio
        loadAdData()

        // Botón para actualizar el anuncio
        buttonUpdate.setOnClickListener {
            updateAd()
        }

        // Botón para eliminar el anuncio
        buttonDelete.setOnClickListener {
            deleteAd()
        }
    }

    private fun loadAdData() {
        Log.d("EditAdActivity", "Buscando anuncio en Firestore con ID: $adId")

        db.collection("ads").document(adId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    editTextTitle.setText(document.getString("titulo"))
                    editTextDescription.setText(document.getString("descripcion"))

                    //Cargar imagen si existe
                    val imageUrl = document.getString("imagen")
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image)
                            .into(imageViewAd)
                        Log.d("EditAdActivity", "Imagen cargada correctamente")
                    } else {
                        imageViewAd.setImageResource(R.drawable.placeholder_image)
                        Log.w("EditAdActivity", "No se encontró una URL de imagen")
                    }

                    Log.d("EditAdActivity", "Anuncio encontrado: ${document.getString("titulo")}")

                } else {
                    Log.e("EditAdActivity", "Error: El anuncio no existe en Firestore")
                    Toast.makeText(this, "Error: El anuncio no existe", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Log.e("EditAdActivity", "Error al cargar el anuncio: ${e.message}")
                Toast.makeText(this, "Error al cargar el anuncio", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateAd() {
        val newTitle = editTextTitle.text.toString().trim()
        val newDescription = editTextDescription.text.toString().trim()

        if (newTitle.isEmpty() || newDescription.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mapOf(
            "titulo" to newTitle,
            "descripcion" to newDescription
        )

        db.collection("ads").document(adId)
            .update(updates)
            .addOnSuccessListener {
                Log.d("EditAdActivity", "Anuncio actualizado correctamente")
                Toast.makeText(this, "Anuncio actualizado correctamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("EditAdActivity", "Error al actualizar: ${e.message}")
                Toast.makeText(this, "Error al actualizar el anuncio", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteAd() {
        db.collection("ads").document(adId)
            .delete()
            .addOnSuccessListener {
                Log.d("EditAdActivity", "Anuncio eliminado correctamente")
                Toast.makeText(this, "Anuncio eliminado correctamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("EditAdActivity", "Error al eliminar el anuncio: ${e.message}")
                Toast.makeText(this, "Error al eliminar el anuncio", Toast.LENGTH_SHORT).show()
            }
    }
}
