package pl.charlie.repertuar

import android.app.Application
import android.content.Context
import androidx.work.*
import coil3.ImageLoader
import coil3.request.ErrorResult
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.request.ImageRequest
import coil3.size.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Path.Companion.toOkioPath
import pl.charlie.repertuar.data.*
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

fun cinemaToday(): LocalDate = LocalDate.now(ZoneId.of("Europe/Warsaw"))

class CharlieApplication : Application(), SingletonImageLoader.Factory {
    val repository by lazy { ScheduleRepository(File(filesDir, "schedule"), CharlieSource()) }
    override fun onCreate() {
        super.onCreate()
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true).build()
        val work = PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints).setInitialDelay(20, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("charlie-schedule", ExistingPeriodicWorkPolicy.KEEP, work)
    }

    override fun newImageLoader(context: Context): ImageLoader = ImageLoader.Builder(context)
        .diskCache { DiskCache.Builder().directory(File(filesDir, "posters").toOkioPath()).maxSizeBytes(80L * 1024 * 1024).build() }
        .build()

    fun queueAssets() {
        WorkManager.getInstance(this).enqueueUniqueWork("charlie-assets", ExistingWorkPolicy.APPEND_OR_REPLACE,
            OneTimeWorkRequestBuilder<AssetsWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build())
    }

    suspend fun cacheAssets() = withContext(Dispatchers.IO) {
        repository.enrichPosters()
        repository.enrichFilms()
        for (film in repository.library.value.films.values) {
            val loader = SingletonImageLoader.get(this@CharlieApplication)
            for (url in film.posterCandidates) {
                val result = loader.execute(ImageRequest.Builder(this@CharlieApplication).data(url).size(Size(400, 600)).build())
                if (result !is ErrorResult) break
            }
        }
    }
}

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as CharlieApplication
        app.repository.load()
        val result = app.repository.refresh(cinemaToday(), 14)
        if (result.updatedDays == 0) return Result.retry()
        app.cacheAssets()
        return if (result.failedDays > 0) Result.retry() else Result.success()
    }
}

class AssetsWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as CharlieApplication
        app.repository.load()
        app.cacheAssets()
        return Result.success()
    }
}
