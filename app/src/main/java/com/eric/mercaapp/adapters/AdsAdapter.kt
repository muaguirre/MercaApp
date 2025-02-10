package com.eric.mercaapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.eric.mercaapp.R
import com.eric.mercaapp.activities.AdDetailActivity
import com.eric.mercaapp.models.Ad

class AdsAdapter(
    private val context: Context,
    private var adsList: MutableList<Ad>
) : RecyclerView.Adapter<AdsAdapter.AdsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_ad, parent, false)
        return AdsViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: AdsViewHolder, position: Int) {
        val ad = adsList[position]

        holder.textViewTitle.text = ad.titulo.ifEmpty { "Sin título" }
        holder.textViewLowestBid.text = "Oferta más baja: ${ad.lowestBid}€"

        Glide.with(context)
            .load(ad.imagen?.takeIf { it.isNotEmpty() } ?: R.drawable.placeholder_image)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.placeholder_image)
            .into(holder.imageViewAd)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, AdDetailActivity::class.java).apply {
                putExtra("adId", ad.id)
                putExtra("title", ad.titulo)
                putExtra("category", ad.categoria)
                putExtra("description", ad.descripcion)
                putExtra("imageUrl", ad.imagen)
                putExtra("provinceOrigin", ad.provinciaRecogida)
                putExtra("townOrigin", ad.poblacionRecogida)
                putExtra("provinceDestination", ad.provinciaEntrega)
                putExtra("townDestination", ad.poblacionEntrega)
                putExtra("clientId", ad.clientId)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = adsList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: MutableList<Ad>) {
        adsList.clear()
        adsList.addAll(newList)
        notifyDataSetChanged()
    }

    inner class AdsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewAd: ImageView = itemView.findViewById(R.id.imageViewAd)
        val textViewTitle: TextView = itemView.findViewById(R.id.textViewTitle)
        val textViewLowestBid: TextView = itemView.findViewById(R.id.textViewLowestBid)
    }
}
