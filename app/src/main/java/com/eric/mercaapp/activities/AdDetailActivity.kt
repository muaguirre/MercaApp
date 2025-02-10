package com.eric.mercaapp.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.eric.mercaapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class AdDetailActivity : AppCompatActivity() {

    private lateinit var imageViewAd: ImageView
    private lateinit var textViewTitle: TextView
    private lateinit var textViewCategory: TextView
    private lateinit var textViewOrigin: TextView
    private lateinit var textViewDestination: TextView
    private lateinit var textViewDescription: TextView
    private lateinit var textViewLowestBid: TextView
    private lateinit var textViewTimeRemaining: TextView
    private lateinit var buttonContactClient: Button
    private lateinit var buttonPlaceBid: Button

    private var clientId: String? = null
    private var adId: String? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var userRole: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ad_detail)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        imageViewAd = findViewById(R.id.imageViewAdDetail)
        textViewTitle = findViewById(R.id.textViewTitleDetail)
        textViewCategory = findViewById(R.id.textViewCategoryDetail)
        textViewOrigin = findViewById(R.id.textViewOriginDetail)
        textViewDestination = findViewById(R.id.textViewDestinationDetail)
        textViewDescription = findViewById(R.id.textViewDescriptionDetail)
        textViewLowestBid = findViewById(R.id.textViewLowestBid)
        textViewTimeRemaining = findViewById(R.id.textViewTimeRemaining)
        buttonContactClient = findViewById(R.id.buttonContactClient)
        buttonPlaceBid = findViewById(R.id.buttonPlaceBid)

        buttonPlaceBid.isEnabled = false
        buttonContactClient.isEnabled = false

        adId = intent.getStringExtra("adId")
        if (adId.isNullOrEmpty()) {
            showErrorAndExit("Error: No se encontró el ID del anuncio.")
            return
        }

        loadAdDetails()
        checkUserRole()

        buttonPlaceBid.setOnClickListener {
            if (userRole == "Transportista") {
                showBidDialog()
            } else {
                Toast.makeText(this, "Solo los transportistas pueden hacer ofertas", Toast.LENGTH_SHORT).show()
            }
        }

        buttonContactClient.setOnClickListener {
            if (userRole == "Transportista") {
                openChat()
            } else {
                Toast.makeText(this, "Solo los transportistas pueden contactar al cliente", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadAdDetails() {
        db.collection("ads").document(adId!!).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val title = document.getString("titulo") ?: "Sin título"
                    val category = document.getString("categoria") ?: "Sin categoría"
                    val description = document.getString("descripcion") ?: "Sin descripción"
                    val imageUrl = document.getString("imagen") ?: ""
                    clientId = document.getString("clientId")

                    val provinciaRecogida = document.getString("provincia_recogida") ?: "Desconocido"
                    val poblacionRecogida = document.getString("poblacion_recogida") ?: "Desconocido"
                    val provinciaEntrega = document.getString("provincia_entrega") ?: "Desconocido"
                    val poblacionEntrega = document.getString("poblacion_entrega") ?: "Desconocido"

                    textViewTitle.text = title
                    textViewCategory.text = category
                    textViewDescription.text = description
                    textViewOrigin.text = "Recogida: $poblacionRecogida, $provinciaRecogida"
                    textViewDestination.text = "Entrega: $poblacionEntrega, $provinciaEntrega"

                    loadAdImage(imageUrl)

                    val endTime = document.getLong("endTime") ?: 0L
                    if (endTime > 0) {
                        startCountdownTimer(endTime)
                    } else {
                        textViewTimeRemaining.text = "Expiración: No disponible"
                    }

                    loadLowestBid()
                } else {
                    showErrorAndExit("Error: No se encontraron datos del anuncio.")
                }
            }
            .addOnFailureListener {
                showErrorAndExit("Error al obtener detalles del anuncio.")
            }
    }


    @SuppressLint("SetTextI18n")
    private fun loadLowestBid() {
        db.collection("ads").document(adId!!)
            .addSnapshotListener { document, error ->
                if (error != null || document == null) {
                    textViewLowestBid.text = "Error al cargar ofertas"
                    return@addSnapshotListener
                }

                val lowestBid = document.getDouble("lowestBid") ?: Double.MAX_VALUE
                textViewLowestBid.text = if (lowestBid == Double.MAX_VALUE) {
                    "Aún no hay ofertas"
                } else {
                    "Oferta más baja: %.2f€".format(lowestBid)
                }
            }
    }

    private fun showBidDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Hacer una oferta")

        val input = EditText(this)
        input.hint = "Ingrese su oferta"
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        builder.setView(input)

        builder.setPositiveButton("Ofertar") { _, _ ->
            val bidAmount = input.text.toString().toDoubleOrNull()
            if (bidAmount != null) {
                placeBid(bidAmount)
            } else {
                Toast.makeText(this, "Ingrese una oferta válida", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun placeBid(bidAmount: Double) {
        if (adId.isNullOrEmpty() || auth.currentUser == null || userRole != "Transportista") {
            Toast.makeText(this, "No puedes hacer una oferta", Toast.LENGTH_SHORT).show()
            return
        }

        val adRef = db.collection("ads").document(adId!!)
        adRef.get().addOnSuccessListener { document ->
            val currentBid = document.getDouble("lowestBid") ?: Double.MAX_VALUE
            if (bidAmount < currentBid) {
                adRef.update(
                    mapOf(
                        "lowestBid" to bidAmount,
                        "lowestBidderId" to auth.currentUser!!.uid
                    )
                ).addOnSuccessListener {
                    Toast.makeText(this, "Oferta registrada con éxito", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Error al registrar la oferta", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Tu oferta debe ser menor que la actual", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun startCountdownTimer(endTime: Long) {
        val currentTime = System.currentTimeMillis()
        val timeRemaining = endTime - currentTime

        if (timeRemaining > 0) {
            object : CountDownTimer(timeRemaining, 1000) {
                @SuppressLint("DefaultLocale")
                override fun onTick(millisUntilFinished: Long) {
                    val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60

                    textViewTimeRemaining.text = String.format("Tiempo restante: %02d:%02d:%02d", hours, minutes, seconds)
                }

                @SuppressLint("SetTextI18n")
                override fun onFinish() {
                    textViewTimeRemaining.text = "Anuncio expirado"
                }
            }.start()
        } else {
            textViewTimeRemaining.text = "Anuncio expirado"
        }
    }

    private fun openChat() {
        if (clientId.isNullOrEmpty()) {
            Toast.makeText(this, "No se puede contactar con el cliente.", Toast.LENGTH_SHORT).show()
            return
        }

        val chatIntent = Intent(this, ChatActivity::class.java).apply {
            putExtra("clientId", clientId)
            putExtra("adId", adId)
        }
        startActivity(chatIntent)
    }

    private fun checkUserRole() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { document ->
                userRole = document.getString("role") ?: "Cliente"
                val isTransportista = userRole == "Transportista"

                buttonPlaceBid.isEnabled = isTransportista
                buttonContactClient.isEnabled = isTransportista
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al obtener el rol del usuario", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showErrorAndExit(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }

    private fun loadAdImage(imageUrl: String?) {
        Glide.with(this)
            .load(imageUrl ?: R.drawable.placeholder_image)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.placeholder_image)
            .into(imageViewAd)
    }
}
