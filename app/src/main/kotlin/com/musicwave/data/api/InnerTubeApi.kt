package com.musicwave.data.api

import com.musicwave.data.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import okhttp3.*
import okhttp3.dnsoverhttps.*
import java.net.*
import java.util.concurrent.*

interface InnerTubeApi {
    suspend fun browse(request: BrowseRequest): HttpResponse
    suspend fun search(request: SearchRequest): HttpResponse
    suspend fun next(request: NextRequest): HttpResponse
    suspend fun player(request: PlayerRequest): HttpResponse
    suspend fun getQueue(request: GetQueueRequest): HttpResponse
    suspend fun getTranscript(request: TranscriptRequest): HttpResponse
    suspend fun accountMenu(client: YouTubeClient): HttpResponse
    suspend fun accountChannels(client: YouTubeClient): HttpResponse
    suspend fun addToPlaylist(request: AddToPlaylistRequest): HttpResponse
    suspend fun addSongsToPlaylist(request: AddSongsToPlaylistRequest): HttpResponse
    suspend fun removeFromPlaylist(request: RemoveFromPlaylistRequest): HttpResponse
    suspend fun moveSongPlaylist(request: MoveSongPlaylistRequest): HttpResponse
    suspend fun createPlaylist(request: CreatePlaylistRequest): HttpResponse
    suspend fun renamePlaylist(request: RenamePlaylistRequest): HttpResponse
    suspend fun startPlaylistCoverUpload(request: StartPlaylistCoverUploadRequest): HttpResponse
    suspend fun uploadPlaylistCover(request: UploadPlaylistCoverRequest): HttpResponse
    suspend fun setPlaylistCustomCover(request: SetPlaylistCustomCoverRequest): HttpResponse
    suspend fun removePlaylistCustomCover(request: RemovePlaylistCustomCoverRequest): HttpResponse
    suspend fun deletePlaylist(request: DeletePlaylistRequest): HttpResponse
    suspend fun likeVideo(request: LikeVideoRequest): HttpResponse
    suspend fun unlikeVideo(request: UnlikeVideoRequest): HttpResponse
    suspend fun likePlaylist(request: LikePlaylistRequest): HttpResponse
    suspend fun unlikePlaylist(request: UnlikePlaylistRequest): HttpResponse
    suspend fun subscribeChannel(request: SubscribeChannelRequest): HttpResponse
    suspend fun unsubscribeChannel(request: UnsubscribeChannelRequest): HttpResponse
    suspend fun registerPlayback(request: RegisterPlaybackRequest): HttpResponse
    suspend fun getSwJsData(): HttpResponse
}

@Serializable
data class BrowseRequest(
    val context: Context,
    val browseId: String? = null,
    val continuation: String? = null,
    val params: String? = null
)

@Serializable
data class SearchRequest(
    val context: Context,
    val query: String? = null,
    val continuation: String? = null,
    val params: String? = null
)

@Serializable
data class NextRequest(
    val context: Context,
    val videoId: String,
    val playlistId: String? = null,
    val playlistSetVideoId: String? = null,
    val index: Int? = null,
    val params: String? = null,
    val continuation: String? = null
)

@Serializable
data class PlayerRequest(
    val context: Context,
    val videoId: String,
    val playlistId: String? = null,
    val signatureTimestamp: Int? = null,
    val poToken: String? = null
)

@Serializable
data class GetQueueRequest(
    val context: Context,
    val videoIds: List<String>? = null,
    val playlistId: String? = null
)

@Serializable
data class TranscriptRequest(
    val context: Context,
    val videoId: String,
    val poToken: String? = null
)

@Serializable
data class AddToPlaylistRequest(
    val context: Context,
    val playlistId: String,
    val videoId: String
)

@Serializable
data class AddSongsToPlaylistRequest(
    val context: Context,
    val playlistId: String,
    val videoIds: List<String>
)

@Serializable
data class RemoveFromPlaylistRequest(
    val context: Context,
    val playlistId: String,
    val videoId: String,
    val setVideoId: String
)

@Serializable
data class MoveSongPlaylistRequest(
    val context: Context,
    val playlistId: String,
    val setVideoId: String,
    val successorSetVideoId: String? = null
)

@Serializable
data class CreatePlaylistRequest(
    val context: Context,
    val title: String,
    val videoIds: List<String> = emptyList(),
    val privacyStatus: String = "PRIVATE"
)

@Serializable
data class RenamePlaylistRequest(
    val context: Context,
    val playlistId: String,
    val title: String
)

@Serializable
data class StartPlaylistCoverUploadRequest(
    val context: Context,
    val playlistId: String,
    val fileSize: Long
)

@Serializable
data class UploadPlaylistCoverRequest(
    val context: Context,
    val playlistId: String,
    val uploadId: String,
    val fileSize: Long
)

@Serializable
data class SetPlaylistCustomCoverRequest(
    val context: Context,
    val playlistId: String,
    val encryptedBlobId: String
)

@Serializable
data class RemovePlaylistCustomCoverRequest(
    val context: Context,
    val playlistId: String
)

@Serializable
data class DeletePlaylistRequest(
    val context: Context,
    val playlistId: String
)

@Serializable
data class LikeVideoRequest(
    val context: Context,
    val videoId: String
)

@Serializable
data class UnlikeVideoRequest(
    val context: Context,
    val videoId: String
)

@Serializable
data class LikePlaylistRequest(
    val context: Context,
    val playlistId: String
)

@Serializable
data class UnlikePlaylistRequest(
    val context: Context,
    val playlistId: String
)

@Serializable
data class SubscribeChannelRequest(
    val context: Context,
    val channelId: String
)

@Serializable
data class UnsubscribeChannelRequest(
    val context: Context,
    val channelId: String
)

@Serializable
data class RegisterPlaybackRequest(
    val context: Context,
    val url: String,
    val playlistId: String? = null,
    val cpn: String
)

@Serializable
data class Context(
    val client: ClientContext,
    val user: UserContext? = null,
    val request: RequestContext? = null,
    val clickTracking: ClickTrackingContext? = null,
    val adSignalsInfo: AdSignalsInfoContext? = null
)

@Serializable
data class ClientContext(
    val clientName: String,
    val clientVersion: String,
    val osName: String = "Android",
    val osVersion: String = "14",
    val platform: String = "MOBILE",
    val userAgent: String = "com.google.android.youtube/19.09.37 (Linux; U; Android 14) gzip",
    val userInterfaceTheme: String = "USER_INTERFACE_THEME_DARK",
    val deviceMake: String = "Google",
    val deviceModel: String = "Pixel 8",
    val screenWidthPoints: Int = 1080,
    val screenHeightPoints: Int = 2400,
    val screenPixelDensity: Float = 2.75f,
    val screenDensityFloat: Float = 2.75f,
    val utcOffsetMinutes: Int = 0,
    val timeZone: String = "UTC",
    val mainAppWebInfo: MainAppWebInfo? = null
)

@Serializable
data class MainAppWebInfo(
    val graftUrl: String = "/",
    val webDisplayMode: String = "WEB_DISPLAY_MODE_BROWSER",
    val isWebNativeShareAvailable: Boolean = true
)

@Serializable
data class UserContext(
    val lockedSafetyMode: Boolean = false
)

@Serializable
data class RequestContext(
    val useSsl: Boolean = true,
    val internalExperimentFlags: List<String> = emptyList(),
    val consistencyTokenJars: List<ConsistencyTokenJar> = emptyList()
)

@Serializable
data class ConsistencyTokenJar(
    val token: String,
    val jarType: String = "CONSISTENCY_TOKEN_JAR_TYPE_GENERIC"
)

@Serializable
data class ClickTrackingContext(
    val clickTrackingParams: String = ""
)

@Serializable
data class AdSignalsInfoContext(
    val params: List<AdSignalParam> = emptyList()
)

@Serializable
data class AdSignalParam(
    val key: String,
    val value: String
)

enum class YouTubeClient(
    val clientName: String,
    val clientVersion: String,
    val supportsCookieAuthentication: Boolean = false,
    val loginSupported: Boolean = false
) {
    WEB("WEB", "2.20240918.01.00"),
    WEB_REMIX("WEB_REMIX", "1.20240918.01.00", loginSupported = true),
    ANDROID("ANDROID", "19.09.37", loginSupported = true),
    ANDROID_MUSIC("ANDROID_MUSIC", "6.41.51", loginSupported = true),
    IOS("IOS", "19.09.37", loginSupported = true),
    IOS_MUSIC("IOS_MUSIC", "6.41.51", loginSupported = true),
    TV("TV", "7.20240918.01.00"),
    TV_EMBEDDED("TV_EMBEDDED", "7.20240918.01.00")
}

class InnerTubeApiImpl(
    private val httpClient: HttpClient,
    private val authState: PlaybackAuthState,
    private val baseUrl: String = "https://music.youtube.com/youtubei/v1"
) : InnerTubeApi {

    private val json = Json { ignoreUnknownKeys = true }

    private fun buildContext(client: YouTubeClient): Context {
        return Context(
            client = ClientContext(
                clientName = client.clientName,
                clientVersion = client.clientVersion
            ),
            user = UserContext(),
            request = RequestContext(),
            clickTracking = ClickTrackingContext(),
            adSignalsInfo = AdSignalsInfoContext()
        )
    }

    override suspend fun browse(request: BrowseRequest): HttpResponse = httpClient.post("$baseUrl/browse") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    override suspend fun search(request: SearchRequest): HttpResponse = httpClient.post("$baseUrl/search") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    override suspend fun next(request: NextRequest): HttpResponse = httpClient.post("$baseUrl/next") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    override suspend fun player(request: PlayerRequest): HttpResponse = httpClient.post("$baseUrl/player") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    override suspend fun getQueue(request: GetQueueRequest): HttpResponse = httpClient.post("$baseUrl/get_queue") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    override suspend fun getTranscript(request: TranscriptRequest): HttpResponse = httpClient.post("$baseUrl/get_transcript") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    override suspend fun accountMenu(client: YouTubeClient): HttpResponse = httpClient.post("$baseUrl/account_menu") {
        contentType(ContentType.Application.Json)
        setBody(Context(client = ClientContext(client.clientName, client.clientVersion)))
    }

    override suspend fun accountChannels(client: YouTubeClient): HttpResponse = httpClient.post("$baseUrl/account_channels") {
        contentType(ContentType.Application.Json)
        setBody(Context(client = ClientContext(client.clientName, client.clientVersion)))
    }

    override suspend fun addToPlaylist(request: AddToPlaylistRequest): HttpResponse = httpClient.post("$baseUrl/playlist/edit") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    override suspend fun addSongsToPlaylist(request: AddSongsToPlaylistRequest): HttpResponse = httpClient.post("$baseUrl/playlist/edit") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    override suspend fun removeFromPlaylist(request: RemoveFromPlaylistRequest): HttpResponse = httpClient.post("$baseUrl/playlist/edit") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    override suspend fun moveSongPlaylist(request: MoveSongPlaylistRequest): HttpResponse = httpClient.post("$baseUrl/playlist/edit") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    override suspend fun createPlaylist(request: CreatePlaylistRequest): HttpResponse = httpClient.post("$baseUrl/playlist/create") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    override suspend fun renamePlaylist(request: RenamePlaylistRequest): HttpResponse = httpClient.post("$baseUrl/playlist/edit") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    override suspend fun startPlaylistCoverUpload(request: StartPlaylistCoverUploadRequest): HttpResponse = httpClient.post("$baseUrl/playlist/upload_cover") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    override suspend fun uploadPlaylistCover(request: UploadPlaylistCoverRequest): HttpResponse = httpClient.post("$baseUrl/playlist/upload_cover") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    override suspend fun setPlaylistCustomCover(request: SetPlaylistCustomCoverRequest): HttpResponse = httpClient.post("$baseUrl/playlist/set_cover") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    override suspend fun removePlaylistCustomCover(request: RemovePlaylistCustomCoverRequest): HttpResponse = httpClient.post("$baseUrl/playlist/remove_cover") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    override suspend fun deletePlaylist(request: DeletePlaylistRequest): HttpResponse = httpClient.post("$baseUrl/playlist/delete") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    override suspend fun likeVideo(request: LikeVideoRequest): HttpResponse = httpClient.post("$baseUrl/like_video") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    override suspend fun unlikeVideo(request: UnlikeVideoRequest): HttpResponse = httpClient.post("$baseUrl/unlike_video") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    override suspend fun likePlaylist(request: LikePlaylistRequest): HttpResponse = httpClient.post("$baseUrl/like_playlist") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    override suspend fun unlikePlaylist(request: UnlikePlaylistRequest): HttpResponse = httpClient.post("$baseUrl/unlike_playlist") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    override suspend fun subscribeChannel(request: SubscribeChannelRequest): HttpResponse = httpClient.post("$baseUrl/subscribe_channel") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    override suspend fun unsubscribeChannel(request: UnsubscribeChannelRequest): HttpResponse = httpClient.post("$baseUrl/unsubscribe_channel") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    override suspend fun registerPlayback(request: RegisterPlaybackRequest): HttpResponse = httpClient.post("$baseUrl/register_playback") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    override suspend fun getSwJsData(): HttpResponse = httpClient.get("https://www.youtube.com/sw.js") {
        accept(ContentType.Text.Plain)
    }
}

class InnerTubeApiFactory {
    companion object {
        private const val BASE_URL = "https://music.youtube.com/youtubei/v1"
        private const val VISITOR_DATA_URL = "https://www.youtube.com/sw.js"

        fun create(
            authState: PlaybackAuthState,
            proxy: Proxy? = null,
            dns: Dns? = null
        ): InnerTubeApi {
            val okHttpBuilder = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(45, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .dns(dns ?: Dns.SYSTEM)

            proxy?.let { okHttpBuilder.proxy(it) }

            val httpClient = HttpClient(io.ktor.client.engine.okhttp.OkHttp) {
                engine {
                    preconfigured = okHttpBuilder.build()
                }
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                install(Logging) {
                    level = LogLevel.NONE
                }
                defaultRequest {
                    header("User-Agent", "com.google.android.youtube/19.09.37 (Linux; U; Android 14) gzip")
                    header("X-YouTube-Client-Name", "1")
                    header("X-YouTube-Client-Version", "19.09.37")
                    header("Origin", "https://music.youtube.com")
                    header("Referer", "https://music.youtube.com/")
                    authState.cookie?.let { header("Cookie", it) }
                    authState.visitorData?.let { header("X-Goog-Visitor-Id", it) }
                }
            }

            return InnerTubeApiImpl(httpClient, authState, BASE_URL)
        }
    }
}