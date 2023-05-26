package com.codexcollab.contactbackup.ui.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NetworkManager(private val context: Context) : ConnectivityManager.NetworkCallback() {

    private val networkStatus = MutableLiveData<Boolean>()
    private lateinit var connectivityManager: ConnectivityManager

    fun register(): LiveData<Boolean> {
        connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(this)
        } else {
            val builder = NetworkRequest.Builder()
            connectivityManager.registerNetworkCallback(builder.build(), this)
        }
        var isConnected = false
        connectivityManager.allNetworks.forEach { network ->
            val networkCapability = connectivityManager.getNetworkCapabilities(network)
            networkCapability?.let {
                if (it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    isConnected = true
                    return@forEach
                }
            }
        }
        networkStatus.postValue(isConnected)
        return networkStatus
    }

    fun unRegister() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (this::connectivityManager.isInitialized)
                connectivityManager.unregisterNetworkCallback(this)
        }
    }

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        networkStatus.postValue(true)
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        networkStatus.postValue(false)
    }

    override fun onUnavailable() {
        super.onUnavailable()
        if (networkStatus.value != false) networkStatus.postValue(false)
    }
}