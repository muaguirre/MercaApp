package com.eric.mercaapp.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eric.mercaapp.R
import com.eric.mercaapp.adapters.AdsAdapter
import com.eric.mercaapp.viewmodels.FinishedAdsViewModel

class FinishedAdsActivity : AppCompatActivity() {

    private val viewModel: FinishedAdsViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var adsAdapter: AdsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finished_ads)

        recyclerView = findViewById(R.id.recyclerViewFinishedAds)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adsAdapter = AdsAdapter(this, mutableListOf())
        recyclerView.adapter = adsAdapter

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.adsList.observe(this) { ads ->
            adsAdapter.updateList(ads.toMutableList())
        }

        viewModel.loading.observe(this) { isLoading ->
            if (isLoading) {
                Toast.makeText(this, "Cargando anuncios finalizados...", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
