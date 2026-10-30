package pl.charlie.repertuar.updates

import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The injectable boundary is the public HTTP response, never an internal collaborator. */
class GitHubReleaseSource(private val readResponse: suspend () -> String = { fetchLatest() }) {
    suspend fun latest(): AppRelease = withContext(Dispatchers.IO) {
        val json = JSONObject(readResponse())
        require(!json.getBoolean("draft") && !json.getBoolean("prerelease"))
        val release = AppRelease(AppVersion.parse(json.getString("tag_name").removePrefix("v")))
        require(json.getString("tag_name") == release.tag && json.getString("html_url") == release.pageUrl)
        val assets = json.getJSONArray("assets")
        require((0 until assets.length()).any { index ->
            val asset = assets.getJSONObject(index)
            asset.optString("name") == release.filename && asset.optString("state") == "uploaded" &&
                asset.optLong("size") > 0L && asset.optString("browser_download_url") == release.downloadUrl
        }) { "Release has no official APK" }
        release
    }

    companion object {
        private fun fetchLatest(): String {
            val connection = URL("https://api.github.com/repos/${AppRelease.Repository}/releases/latest")
                .openConnection() as HttpURLConnection
            connection.connectTimeout = 8_000
            connection.readTimeout = 8_000
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("X-GitHub-Api-Version", "2026-03-10")
            connection.setRequestProperty("User-Agent", "CharlieRepertuar-Android")
            try {
                check(connection.responseCode == 200) { "Release HTTP ${connection.responseCode}" }
                return connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } finally { connection.disconnect() }
        }
    }
}
