package com.eric.mercaapp.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eric.mercaapp.R
import com.eric.mercaapp.adapters.AdsAdapter
import com.eric.mercaapp.firebase.FirebaseNotificationSender
import com.eric.mercaapp.utils.NotificationHelper
import com.eric.mercaapp.viewmodels.MainViewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var searchView: SearchView
    private lateinit var categorySpinner: Spinner
    private lateinit var provinceSpinner: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var adsAdapter: AdsAdapter
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NotificationHelper.createNotificationChannel(this)
        requestNotificationPermission()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        searchView = findViewById(R.id.searchView)
        categorySpinner = findViewById(R.id.spinnerCategory)
        provinceSpinner = findViewById(R.id.spinnerProvince)
        recyclerView = findViewById(R.id.recyclerViewAds)
        progressBar = findViewById(R.id.progressBar)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adsAdapter = AdsAdapter(this, mutableListOf())
        recyclerView.adapter = adsAdapter

        observeViewModel()
        setupFilters()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                101
            )
        }
    }

    private fun observeViewModel() {
        viewModel.adsList.observe(this) { ads ->
            adsAdapter.updateList(ads.toMutableList())
        }

        viewModel.loading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.categories.observe(this) { categories ->
            setupSpinner(categorySpinner, categories)
        }

        viewModel.provinces.observe(this) { provinces ->
            setupSpinner(provinceSpinner, provinces)
        }

        viewModel.auctionEndedEvent.observe(this) { (clientId, winnerId, titulo) ->
            notifyUsersOnAuctionEnd(clientId, winnerId, titulo)
        }
    }

    private fun notifyUsersOnAuctionEnd(clientId: String, winnerId: String, adTitle: String) {
        FirebaseNotificationSender.sendNotificationToReceiver(
            this,
            clientId,
            "Tu anuncio '$adTitle' ha finalizado."

        )

        FirebaseNotificationSender.sendNotificationToReceiver(
            this,
            winnerId,
            "¡Has ganado la subasta de este anuncio '$adTitle'!"
        )
    }


    private fun setupFilters() {
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterAds()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        provinceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterAds()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                filterAds()
                return true
            }
        })
    }

    private fun setupSpinner(spinner: Spinner, items: Set<String>) {
        val list = mutableListOf("Todas").apply { addAll(items) }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, list)
        spinner.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun filterAds() {
        val selectedCategory = categorySpinner.selectedItem?.toString() ?: "Todas"
        val selectedProvince = provinceSpinner.selectedItem?.toString() ?: "Todas"
        val searchQuery = searchView.query?.toString()?.lowercase().orEmpty()

        val filteredList = viewModel.adsList.value?.filter { ad ->
            (selectedCategory == "Todas" || ad.categoria == selectedCategory) &&
                    (selectedProvince == "Todas" || ad.provinciaRecogida == selectedProvince) &&
                    (searchQuery.isEmpty() || ad.titulo.lowercase().contains(searchQuery))
        } ?: emptyList()

        adsAdapter.updateList(filteredList.toMutableList())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_add_ad -> { navigateToPostAd(); true }
            R.id.menu_my_ads -> { navigateToMyAds(); true }
            R.id.menu_finished_ads -> { navigateToFinishedAds(); true }
            R.id.menu_messages -> { navigateToMessages(); true }
            R.id.menu_profile -> { navigateToProfile(); true }
            R.id.menu_logout -> { logoutUser(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToPostAd() {
        startActivity(Intent(this, PostAdActivity::class.java))
    }

    private fun navigateToMyAds() {
        startActivity(Intent(this, MyAdsActivity::class.java))
    }

    private fun navigateToFinishedAds() {
        startActivity(Intent(this, FinishedAdsActivity::class.java))
    }

    private fun navigateToMessages() {
        startActivity(Intent(this, MessagesActivity::class.java))
    }

    private fun navigateToProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }

    private fun logoutUser() {
        viewModel.logout()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
