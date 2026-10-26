package com.laarasoft.moepay.core.domain.connectivity

import kotlinx.coroutines.flow.StateFlow

/** Whether the device has a usable network path, from the platform's own monitor. */
interface ConnectivityObserver {
    val isOnline: StateFlow<Boolean>
}
