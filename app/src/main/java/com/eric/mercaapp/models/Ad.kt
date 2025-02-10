package com.eric.mercaapp.models

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName

@Keep
data class Ad(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("titulo") @set:PropertyName("titulo") var titulo: String = "",
    @get:PropertyName("descripcion") @set:PropertyName("descripcion") var descripcion: String = "",
    @get:PropertyName("categoria") @set:PropertyName("categoria") var categoria: String = "",
    @get:PropertyName("provincia_recogida") @set:PropertyName("provincia_recogida") var provinciaRecogida: String = "",
    @get:PropertyName("poblacion_recogida") @set:PropertyName("poblacion_recogida") var poblacionRecogida: String = "",
    @get:PropertyName("provincia_entrega") @set:PropertyName("provincia_entrega") var provinciaEntrega: String = "",
    @get:PropertyName("poblacion_entrega") @set:PropertyName("poblacion_entrega") var poblacionEntrega: String = "",
    @get:PropertyName("imagen") @set:PropertyName("imagen") var imagen: String? = null,
    @get:PropertyName("clientId") @set:PropertyName("clientId") var clientId: String = "",
    @get:PropertyName("clientName") @set:PropertyName("clientName") var clientName: String = "",
    @get:PropertyName("fecha_publicacion") @set:PropertyName("fecha_publicacion") var fechaPublicacion: Long = System.currentTimeMillis(),

    //Subasta inversa
    @get:PropertyName("startingPrice") @set:PropertyName("startingPrice") var startingPrice: Double = Double.MAX_VALUE,
    @get:PropertyName("lowestBid") @set:PropertyName("lowestBid") var lowestBid: Double = Double.MAX_VALUE,
    @get:PropertyName("lowestBidderId") @set:PropertyName("lowestBidderId") var lowestBidderId: String = "",
    @get:PropertyName("endTime") @set:PropertyName("endTime") var endTime: Long = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000),
    @get:PropertyName("status") @set:PropertyName("status") var status: String = "open"
) {
    constructor() : this(
        id = "",
        titulo = "",
        descripcion = "",
        categoria = "",
        provinciaRecogida = "",
        poblacionRecogida = "",
        provinciaEntrega = "",
        poblacionEntrega = "",
        imagen = null,
        clientId = "",
        clientName = "",
        fechaPublicacion = System.currentTimeMillis(),
        startingPrice = Double.MAX_VALUE,
        lowestBid = Double.MAX_VALUE,
        lowestBidderId = "",
        endTime = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000),
        status = "open"
    )
}
