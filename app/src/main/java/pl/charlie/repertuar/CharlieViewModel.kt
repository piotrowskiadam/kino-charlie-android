package pl.charlie.repertuar

import android.app.Application
import android.content.Context
import android.net.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CharlieViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as CharlieApplication
    val library = app.repository.library
    val updates = app.updates.state
    private val mutableToday = MutableStateFlow(cinemaToday())
    val today = mutableToday.asStateFlow()
    private val mutableStartup = MutableStateFlow(StartupPhase.CheckingCache)
    val startup = mutableStartup.asStateFlow()
    private var hasRequestedAssets = false
    private fun requestAssets() { hasRequestedAssets = true; app.queueAssets() }
    private val mutableRefreshing = MutableStateFlow(false)
    val refreshing = mutableRefreshing.asStateFlow()
    private val mutableError = MutableStateFlow<String?>(null)
    val error = mutableError.asStateFlow()
    private val connectivity = app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private fun connected(): Boolean = connectivity.getNetworkCapabilities(connectivity.activeNetwork)
        ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
    private val mutableOnline = MutableStateFlow(connected())
    val online = mutableOnline.asStateFlow()
    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            mutableOnline.value = connected()
        }
        override fun onLost(network: Network) { mutableOnline.value = connected() }
    }

    init {
        connectivity.registerDefaultNetworkCallback(callback)
        viewModelScope.launch {
            library.collect { current ->
                if (mutableStartup.value == StartupPhase.Preparing && today.value in current.days) {
                    mutableStartup.value = StartupPhase.Ready
                    requestAssets()
                }
            }
        }
        viewModelScope.launch {
            online.collect { available ->
                if (available && mutableStartup.value == StartupPhase.Failed) refresh()
                if (available) app.updates.check()
            }
        }
    }

    fun onResume() {
        viewModelScope.launch {
            app.updates.load()
            if (connected()) app.updates.check()
        }
        viewModelScope.launch {
            app.repository.load()
            val currentDate = cinemaToday()
            mutableToday.value = currentDate
            if (mutableStartup.value == StartupPhase.CheckingCache) {
                mutableStartup.value = if (library.value.days.isNotEmpty()) StartupPhase.Ready else StartupPhase.Preparing
            }
            if (System.currentTimeMillis() - library.value.lastUpdated > 6 * 60 * 60 * 1000L ||
                currentDate !in library.value.days) refresh()
            else if (!hasRequestedAssets && library.value.days.values.any(library.value::needsPosters)) requestAssets()
        }
    }

    fun checkUpdates() { if (connected()) viewModelScope.launch { app.updates.check(force = true) } }

    fun refresh() {
        if (mutableRefreshing.value) return
        mutableRefreshing.value = true
        mutableError.value = null
        if (mutableStartup.value != StartupPhase.Ready) mutableStartup.value = StartupPhase.Preparing
        viewModelScope.launch {
            try {
                if (!connected()) {
                    mutableOnline.value = false
                    mutableError.value = "Brak połączenia z internetem. Połącz się i spróbuj ponownie."
                    return@launch
                }
                val result = app.repository.refresh(cinemaToday(), 14)
                mutableError.value = when {
                    result.updatedDays == 0 -> "Nie udało się odświeżyć repertuaru. Sprawdź połączenie i spróbuj ponownie."
                    result.failedDays > 0 -> "Część dni czeka na odświeżenie. Zachowano poprzedni repertuar."
                    else -> null
                }
                if (result.updatedDays > 0) requestAssets()
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { mutableError.value = "Nie udało się zapisać repertuaru. Spróbuj ponownie." }
            finally {
                if (mutableStartup.value != StartupPhase.Ready) {
                    mutableStartup.value = if (today.value in library.value.days) StartupPhase.Ready else StartupPhase.Failed
                }
                mutableRefreshing.value = false
            }
        }
    }

    fun save(id: String) { viewModelScope.launch {
        try { app.repository.toggleSaved(id) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { mutableError.value = "Nie udało się zapisać filmu. Spróbuj ponownie." }
    } }

    fun browserError() { mutableError.value = "Brak przeglądarki. Zainstaluj przeglądarkę, aby otworzyć stronę kina." }
    override fun onCleared() { connectivity.unregisterNetworkCallback(callback) }
}
