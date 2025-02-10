package com.eric.mercaapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.eric.mercaapp.R
import com.eric.mercaapp.models.Ad

class MyAdsAdapter(
    private val context: Context,
    private var adsList: MutableList<Ad>,
    private val onEditClick: (Ad) -> Unit
) : RecyclerView.Adapter<MyAdsAdapter.MyAdsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAdsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_my_ad, parent, false)
        return MyAdsViewHolder(view, onEditClick)
    }

    override fun onBindViewHolder(holder: MyAdsViewHolder, position: Int) {
        val ad = adsList[position]
        holder.bind(ad, context)
    }

    override fun getItemCount(): Int = adsList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newAdsList: List<Ad>) {
        adsList.clear()
        adsList.addAll(newAdsList)
        notifyDataSetChanged()
    }

    class MyAdsViewHolder(itemView: View, private val onEditClick: (Ad) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val imageViewAd: ImageView = itemView.findViewById(R.id.imageViewAd)
        private val textViewTitle: TextView = itemView.findViewById(R.id.textViewTitle)
        private val textViewCategory: TextView = itemView.findViewById(R.id.textViewCategory)
        private val textViewPickupLocation: TextView = itemView.findViewById(R.id.textViewPickupLocation)
        private val textViewDeliveryLocation: TextView = itemView.findViewById(R.id.textViewDeliveryLocation)
        private val buttonEdit: Button = itemView.findViewById(R.id.buttonEdit)

        @SuppressLint("SetTextI18n")
        fun bind(ad: Ad, context: Context) {
            textViewTitle.text = ad.titulo
            textViewCategory.text = "Categoría: ${ad.categoria}"
            textViewPickupLocation.text = "Recoger en: ${ad.poblacionRecogida}, ${ad.provinciaRecogida}"
            textViewDeliveryLocation.text = "Entregar en: ${ad.poblacionEntrega}, ${ad.provinciaEntrega}"

            Glide.with(context)
                .load(ad.imagen ?: R.drawable.placeholder_image)
                .placeholder(R.drawable.placeholder_image)
                .into(imageViewAd)

            buttonEdit.setOnClickListener { onEditClick(ad) }
        }
    }
}

