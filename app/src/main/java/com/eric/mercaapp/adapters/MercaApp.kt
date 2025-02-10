package com.eric.mercaapp.adapters

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore

class MercaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseFirestore.getInstance()
    }
}
