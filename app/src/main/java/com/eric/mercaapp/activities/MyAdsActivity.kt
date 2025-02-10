package com.eric.mercaapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eric.mercaapp.R
import com.eric.mercaapp.adapters.MyAdsAdapter
import com.eric.mercaapp.models.Ad
import com.eric.mercaapp.viewmodels.MyAdsViewModel

class MyAdsActivity : AppCompatActivity() {

    private val viewModel: MyAdsViewModel by viewModels()

    private lateinit var recyclerViewAds: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var myAdsAdapter: MyAdsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_ads)

        recyclerViewAds = findViewById(R.id.recyclerViewAds)
        progressBar = findViewById(R.id.progressBar)

        recyclerViewAds.layoutManager = LinearLayoutManager(this)
        myAdsAdapter = MyAdsAdapter(this, mutableListOf()) { ad -> openEditAd(ad) }
        recyclerViewAds.adapter = myAdsAdapter

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.adsList.observe(this) { ads ->
            myAdsAdapter.updateList(ads.toMutableList())
        }

        viewModel.loading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshAds()
    }

    private fun openEditAd(ad: Ad) {
        if (ad.id.isEmpty()) {
            Toast.makeText(this, "Error: El anuncio no tiene un ID válido", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, EditAdActivity::class.java).apply {
            putExtra("adId", ad.id)
        }
        startActivity(intent)
    }
}
