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
            app.repository.load()
            if (System.currentTimeMillis() - library.value.lastUpdated > 6 * 60 * 60 * 1000L ||
                cinemaToday() !in library.value.days) refresh()
        }
    }

    fun refresh() {
        if (mutableRefreshing.value) return
        mutableRefreshing.value = true
        mutableError.value = null
        viewModelScope.launch {
            try {
                val result = app.repository.refresh(cinemaToday(), 14)
                mutableError.value = when {
                    result.updatedDays == 0 -> "Nie udało się odświeżyć repertuaru. Sprawdź połączenie i spróbuj ponownie."
                    result.failedDays > 0 -> "Część dni czeka na odświeżenie. Zachowano poprzedni repertuar."
                    else -> null
                }
                if (result.updatedDays > 0) app.queueAssets()
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { mutableError.value = "Nie udało się zapisać repertuaru. Spróbuj ponownie." }
            finally { mutableRefreshing.value = false }
        }
    }

    fun save(id: String) { viewModelScope.launch {
        try { app.repository.toggleSaved(id) }
        catch (_: Exception) { mutableError.value = "Nie udało się zapisać filmu. Spróbuj ponownie." }
    } }

    fun browserError() { mutableError.value = "Brak przeglądarki. Zainstaluj przeglądarkę, aby otworzyć stronę kina." }
    override fun onCleared() { connectivity.unregisterNetworkCallback(callback) }
}
