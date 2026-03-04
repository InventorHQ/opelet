package io.inventor.opelet.network

import io.inventor.opelet.model.GitHubAsset
import io.inventor.opelet.model.GitHubRelease
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class GitHubApi {

    companion object {
        private const val BASE_URL = "https://api.github.com"
        private const val CONNECT_TIMEOUT = 10_000
        private const val READ_TIMEOUT = 30_000
    }

    suspend fun getReleases(owner: String, repo: String): Result<List<GitHubRelease>> =
        withContext(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/repos/$owner/$repo/releases")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = CONNECT_TIMEOUT
                    readTimeout = READ_TIMEOUT
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                }

                val code = connection.responseCode
                if (code != 200) {
                    val errorBody = connection.errorStream?.bufferedReader()?.readText() ?: ""
                    connection.disconnect()
                    return@withContext Result.failure(
                        ApiException(code, "GitHub API returned $code: $errorBody")
                    )
                }

                val body = connection.inputStream.bufferedReader().readText()
                connection.disconnect()

                val releases = parseReleases(JSONArray(body))
                Result.success(releases)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Downloads a file from the given URL, writing to the destination.
     * Returns the file on success. Calls [onProgress] with bytes downloaded so far and total bytes
     * (-1 if unknown).
     */
    suspend fun downloadFile(
        url: String,
        destination: File,
        onProgress: (downloaded: Long, total: Long) -> Unit = { _, _ -> },
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = CONNECT_TIMEOUT
                readTimeout = READ_TIMEOUT
                instanceFollowRedirects = true
            }

            val code = connection.responseCode
            if (code != 200) {
                connection.disconnect()
                return@withContext Result.failure(
                    ApiException(code, "Download failed with HTTP $code")
                )
            }

            val total = connection.contentLengthLong
            destination.parentFile?.mkdirs()

            connection.inputStream.use { input ->
                FileOutputStream(destination).use { output ->
                    val buffer = ByteArray(8192)
                    var downloaded = 0L
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloaded += read
                        onProgress(downloaded, total)
                    }
                }
            }
            connection.disconnect()

            Result.success(destination)
        } catch (e: Exception) {
            destination.delete()
            Result.failure(e)
        }
    }

    private fun parseReleases(json: JSONArray): List<GitHubRelease> {
        return (0 until json.length()).map { i ->
            val obj = json.getJSONObject(i)
            GitHubRelease(
                id = obj.getLong("id"),
                tagName = obj.getString("tag_name"),
                name = obj.optString("name", obj.getString("tag_name")),
                body = obj.optString("body", ""),
                prerelease = obj.getBoolean("prerelease"),
                publishedAt = obj.optString("published_at", ""),
                assets = parseAssets(obj.getJSONArray("assets")),
            )
        }
    }

    private fun parseAssets(json: JSONArray): List<GitHubAsset> {
        return (0 until json.length()).map { i ->
            val obj = json.getJSONObject(i)
            GitHubAsset(
                id = obj.getLong("id"),
                name = obj.getString("name"),
                size = obj.getLong("size"),
                downloadUrl = obj.getString("browser_download_url"),
            )
        }
    }
}

class ApiException(val code: Int, message: String) : Exception(message)
