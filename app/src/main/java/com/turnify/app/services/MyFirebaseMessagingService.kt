package com.turnify.app.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseMessagingService: FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "FCM"
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

    }

}