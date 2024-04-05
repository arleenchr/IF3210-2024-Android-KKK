package com.example.bondoman.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.NetworkRequest
import android.widget.Toast


class NetworkUtils(private val context: Context) {

    private var connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    fun isOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                super.onLost(network)
                if (!isOnline()) {
                    Toast.makeText(context, "Internet connection lost", Toast.LENGTH_SHORT).show()
                }
            }
        }

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
    }

    fun unregisterNetworkCallback() {
        if (networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback!!)
            networkCallback = null
        }
    }
}
