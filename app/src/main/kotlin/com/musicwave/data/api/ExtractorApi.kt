package com.musicwave.data.api

import com.musicwave.data.model.ExtractedAudio
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.net.*
import java.util.Base64
import java.util.concurrent.*

interface ExtractorApi {
    suspend fun extractAudio(
        videoUrl: String,
        poToken: String? = null,
        gvsToken: String? = null,
        cookies: String? = null
    ): ExtractedAudio

    suspend fun checkStreamStatus(
        streamId: String,
        signature: String,
        expiry: Long
    ): StreamStatusResponse

    fun close()
}

@Serializable
data class StreamStatusResponse(
    val success: Boolean,
    val valid: Boolean,
    val cached: Boolean,
    val serverVersion: String,
    val title: String? = null,
    val thumbnail: String? = null,
    val streamUrl: String? = null,
    val streamPath: String? = null,
    val streamExpiresAt: Long? = null,
    val formatId: String? = null,
    val ext: String? = null,
    val acodec: String? = null,
    val mimeType: String? = null,
    val error: String? = null
)

@Serializable
data class BackendExtractorResponse(
    val success: Boolean,
    val valid: Boolean,
    val cached: Boolean,
    val serverVersion: String? = null,
    val title: String? = null,
    val thumbnail: String? = null,
    val streamUrl: String? = null,
    val streamPath: String? = null,
    val streamExpiresAt: Long? = null,
    val formatId: String? = null,
    val ext: String? = null,
    val acodec: String? = null,
    val mimeType: String? = null,
    val error: String? = null
) {
    fun toExtractedAudio(baseUrl: String): ExtractedAudio {
        val normalizedStreamUrl = streamUrl?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw ExtractorException("Extractor returned no signed stream URL")
        val normalizedStreamPath = streamPath?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw ExtractorException("Extractor returned no signed stream path")
        val normalizedStreamExpiresAt = streamExpiresAt
            ?: throw ExtractorException("Extractor returned no stream expiry")
        val normalizedServerVersion = serverVersion?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw ExtractorException("Extractor returned no server version")

        val streamUri = normalizedStreamUrl.toUriOrNull()
            ?: throw ExtractorException("Extractor returned an invalid signed stream URL")
        
        val backendUri = baseUrl.trimEnd('/').toUriOrNull()
            ?: throw ExtractorException("Invalid base URL")

        if (streamUri.scheme?.lowercase() !in setOf("http", "https") ||
            !streamUri.isSameOriginAs(backendUri) ||
            streamUri.path?.startsWith("/api/play/") != true
        ) {
            throw ExtractorException("Extractor returned an untrusted signed stream URL")
        }

        val resolvedStreamPath = buildString {
            append(streamUri.rawPath)
            streamUri.rawQuery?.let { query -> append('?').append(query) }
        }
        
        if (normalizedStreamPath != resolvedStreamPath) {
            throw ExtractorException("Extractor returned inconsistent signed stream data")
        }

        val queryParameters = streamUri.rawQuery.orEmpty().split('&').mapNotNull { parameter ->
            val separator = parameter.indexOf('=')
            if (separator <= 0) null else parameter.substring(0, separator) to parameter.substring(separator + 1)
        }.toMap()
        
        val signature = queryParameters["sig"]
        val expiry = queryParameters["exp"]?.toLongOrNull()
        
        if (signature == null || !Regex("^[A-Fa-f0-9]{32,256}$").matches(signature) ||
            expiry == null || expiry != normalizedStreamExpiresAt
        ) {
            throw ExtractorException("Extractor returned an incomplete signed stream URL")
        }

        return ExtractedAudio(
            success = success,
            valid = valid,
            cached = cached,
            serverVersion = normalizedServerVersion,
            title = title,
            thumbnail = thumbnail,
            streamUrl = normalizedStreamUrl,
            streamPath = normalizedStreamPath,
            streamExpiresAt = normalizedStreamExpiresAt,
            formatId = formatId,
            ext = ext,
            acodec = acodec,
            mimeType = mimeType,
            error = error
        )
    }
}

@Serializable
private data class BackendErrorResponse(
    val error: String? = null
)

class ExtractorApiImpl(
    private val baseUrl: String,
    private val tokenRepository: TokenRepository,
    private val authenticationCallback: () -> Unit = {}
) : ExtractorApi, AutoCloseable {

    private val normalizedBaseUrl = baseUrl.trimEnd('/')
    private val clientJson = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    
    private val httpClient = HttpClient(OkHttp) {
        engine {
            config {
                connectTimeout(15, TimeUnit.SECONDS)
                readTimeout(45, TimeUnit.SECONDS)
                writeTimeout(15, TimeUnit.SECONDS)
                retryOnConnectionFailure(true)
            }
        }
        install(ContentNegotiation) {
            json(clientJson)
        }
    }

    override suspend fun extractAudio(
        videoUrl: String,
        poToken: String?,
        gvsToken: String?,
        cookies: String?
    ): ExtractedAudio = withContext(Dispatchers.IO) {
        val normalizedVideoUrl = videoUrl.trim()
        requireHttpUrl(normalizedVideoUrl, "Video URL")
        
        val normalizedPoToken = poToken?.normalizeBase64UrlPoToken("player")
        val normalizedGvsToken = gvsToken?.normalizeBase64UrlPoToken("GVS")
        val normalizedCookies = cookies?.trim()?.takeIf { it.isNotEmpty() }

        val token = tokenRepository.getToken() ?: throw ExtractorAuthenticationException("Extractor token is missing")
        
        val response = httpClient.get("$normalizedBaseUrl/api/extract") {
            header("Authorization", "Bearer $token")
            parameter("url", normalizedVideoUrl)
            normalizedPoToken?.let { parameter("po_token", it) }
            normalizedGvsToken?.let { parameter("gvs_token", it) }
            normalizedCookies?.let { parameter("cookies", it) }
        }
        
        val raw = response.bodyAsText()
        validateHttpResponse(response, raw)
        
        val backendResponse = clientJson.decodeFromString<BackendExtractorResponse>(raw)
        backendResponse.toExtractedAudio(normalizedBaseUrl)
    }

    override suspend fun checkStreamStatus(
        streamId: String,
        signature: String,
        expiry: Long
    ): StreamStatusResponse = withContext(Dispatchers.IO) {
        val normalizedStreamId = streamId.trim()
        require(Regex("^[A-Za-z0-9_-]{1,128}$").matches(normalizedStreamId)) { "Stream ID is invalid" }
        val normalizedSignature = signature.trim()
        require(Regex("^[A-Fa-f0-9]{32,256}$").matches(normalizedSignature)) { "Stream signature is invalid" }
        require(expiry > 0L) { "Stream expiry must be positive" }

        val token = tokenRepository.getToken() ?: throw ExtractorAuthenticationException("Extractor token is missing")
        
        val response = httpClient.get("$normalizedBaseUrl/api/check-status/$normalizedStreamId") {
            header("Authorization", "Bearer $token")
            parameter("sig", normalizedSignature)
            parameter("exp", expiry)
        }
        
        val raw = response.bodyAsText()
        validateHttpResponse(response, raw)
        
        clientJson.decodeFromString<StreamStatusResponse>(raw)
    }

    override fun close() {
        httpClient.close()
    }

    private suspend fun validateHttpResponse(response: HttpResponse, raw: String) {
        if (response.status.value in 200..299) return

        val serverMessage = runCatching { clientJson.decodeFromString<BackendErrorResponse>(raw).error }
            .getOrNull()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
        
        val message = serverMessage ?: "Extractor returned HTTP ${response.status.value}"

        when (response.status) {
            HttpStatusCode.Unauthorized -> {
                tokenRepository.clearToken()
                val exception = ExtractorAuthenticationException(message)
                runCatching { authenticationCallback() }.exceptionOrNull()?.let { exception.addSuppressed(it) }
                throw exception
            }
            HttpStatusCode.Forbidden -> throw ExtractorForbiddenException(message)
            HttpStatusCode.Gone -> throw ExtractorStreamExpiredException(message)
            else -> throw ExtractorHttpException(response.status.value, message)
        }
    }

    private fun requireHttpUrl(value: String, fieldName: String) {
        val uri = value.toUriOrNull()
        if (uri == null || uri.scheme?.lowercase() !in setOf("http", "https") || uri.host.isNullOrBlank()) {
            throw IllegalArgumentException("$fieldName is invalid")
        }
    }

    private fun String?.normalizeBase64UrlPoToken(context: String): String? {
        val value = this?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val base64UrlValue = value
            .replace('+', '-')
            .replace('/', '_')
            .trimEnd('=')
        val paddingLength = (4 - base64UrlValue.length % 4) % 4
        val paddedValue = base64UrlValue + "=".repeat(paddingLength)
        val decoded = runCatching { Base64.getUrlDecoder().decode(paddedValue) }
            .getOrElse { cause ->
                throw ExtractorException("Invalid $context PO Token: expected a base64url-encoded value", cause)
            }
        if (decoded.isEmpty()) {
            throw ExtractorException("Invalid $context PO Token: expected a non-empty base64url-encoded value")
        }
        return Base64.getUrlEncoder().encodeToString(decoded)
    }
}

open class ExtractorException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

class ExtractorAuthenticationException(message: String) : ExtractorException(message)
class ExtractorForbiddenException(message: String) : ExtractorException(message)
class ExtractorStreamExpiredException(message: String) : ExtractorException(message)
class ExtractorHttpException(val statusCode: Int, message: String) : ExtractorException(message)

interface TokenRepository {
    fun getToken(): String?
    fun setToken(token: String)
    fun clearToken()
}

class InMemoryTokenRepository : TokenRepository {
    @Volatile
    private var token: String? = null
    
    override fun getToken(): String? = token
    
    override fun setToken(newToken: String) {
        token = newToken
    }
    
    override fun clearToken() {
        token = null
    }
}

class ExtractorApiFactory {
    companion object {
        private const val DEFAULT_BASE_URL = "https://moriextractor.koyeb.app"
        
        fun create(
            tokenRepository: TokenRepository,
            baseUrl: String = DEFAULT_BASE_URL,
            authenticationCallback: () -> Unit = {}
        ): ExtractorApi {
            return ExtractorApiImpl(baseUrl, tokenRepository, authenticationCallback)
        }
    }
}

private fun String?.toUriOrNull(): URI? = runCatching { URI(this!!) }.getOrNull()

private fun URI.isSameOriginAs(other: URI): Boolean =
    scheme.equals(other.scheme, ignoreCase = true) &&
        host.equals(other.host, ignoreCase = true) &&
        effectivePort() == other.effectivePort()

private fun URI.effectivePort(): Int =
    when {
        port >= 0 -> port
        scheme.equals("https", ignoreCase = true) -> 443
        else -> 80
    }