package com.laarasoft.moepay.core.data.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Lives as long as the app process: realtime connection and cache refreshes run here. */
class ApplicationScope : CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.Default)
