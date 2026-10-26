package com.laarasoft.moepay.core.data.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.laarasoft.moepay.core.domain.connectivity.ConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Follows the default network with `ConnectivityManager`; app-lifetime singleton. */
internal class AndroidConnectivityObserver(context: Context) : ConnectivityObserver {

    private val manager = context.getSystemService(ConnectivityManager::class.java)
    private val _isOnline = MutableStateFlow(manager.activeNetwork?.let(::hasInternet) ?: false)
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    init {
        manager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                _isOnline.value = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }

            override fun onLost(network: Network) {
                _isOnline.value = false
            }

            override fun onUnavailable() {
                _isOnline.value = false
            }
        })
    }

    private fun hasInternet(network: Network): Boolean =
        manager.getNetworkCapabilities(network)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
}
