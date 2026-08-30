package com.musicwave.di

import android.content.Context
import com.musicwave.data.api.*
import com.musicwave.data.repository.*
import com.musicwave.playback.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import okhttp3.*
import java.net.Proxy
import java.util.concurrent.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        allowSpecialFloatingPointValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun providePlaybackAuthState(): PlaybackAuthState = PlaybackAuthState.EMPTY

    @Provides
    @Singleton
    fun provideTokenRepository(): TokenRepository = InMemoryTokenRepository()

    @Provides
    @Singleton
    fun provideHttpClient(
        authState: PlaybackAuthState
    ): HttpClient {
        return HttpClient(OkHttp) {
            engine {
                config {
                    connectTimeout(15, TimeUnit.SECONDS)
                    readTimeout(45, TimeUnit.SECONDS)
                    writeTimeout(15, TimeUnit.SECONDS)
                    retryOnConnectionFailure(true)
                }
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                })
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
    }

    @Provides
    @Singleton
    fun provideInnerTubeApi(
        httpClient: HttpClient,
        authState: PlaybackAuthState
    ): InnerTubeApi = InnerTubeApiImpl(httpClient, authState)

    @Provides
    @Singleton
    fun provideExtractorApi(
        tokenRepository: TokenRepository
    ): ExtractorApi = ExtractorApiFactory.create(tokenRepository)

    @Provides
    @Singleton
    fun provideSearchRepository(
        innerTubeApi: InnerTubeApi,
        json: Json
    ): SearchRepository = SearchRepositoryImpl(innerTubeApi, json)

    @Provides
    @Singleton
    fun provideLibraryRepository(
        innerTubeApi: InnerTubeApi,
        json: Json
    ): LibraryRepository = LibraryRepositoryImpl(innerTubeApi, json)

    @Provides
    @Singleton
    fun providePlaybackRepository(
        innerTubeApi: InnerTubeApi,
        extractorApi: ExtractorApi,
        json: Json
    ): PlaybackRepository = PlaybackRepositoryImpl(innerTubeApi, extractorApi, json)
}

interface SearchRepository {
    suspend fun search(query: String, filter: String? = null): Result<List<YTItem>>
    suspend fun searchSuggestions(query: String): Result<List<String>>
    suspend fun searchSummary(query: String): Result<SearchSummaryPage>
    suspend fun home(): Result<HomePage>
    suspend fun homeContinuation(continuation: String): Result<HomePage>
    suspend fun album(browseId: String): Result<AlbumPage>
    suspend fun artist(browseId: String): Result<ArtistPage>
    suspend fun artistItems(endpoint: BrowseEndpoint): Result<ArtistItemsPage>
    suspend fun artistItemsContinuation(continuation: String): Result<ArtistItemsContinuationPage>
    suspend fun playlist(playlistId: String): Result<PlaylistPage>
    suspend fun playlistContinuation(continuation: String, playlistId: String? = null): Result<PlaylistContinuationPage>
    suspend fun explore(): Result<ExplorePage>
    suspend fun moodAndGenres(): Result<List<MoodAndGenres>>
    suspend fun browse(browseId: String, params: String? = null): Result<BrowseResult>
    suspend fun related(endpoint: BrowseEndpoint): Result<RelatedPage>
    suspend fun charts(): Result<ChartsPage>
    suspend fun musicHistory(): Result<HistoryPage>
}

class SearchRepositoryImpl(
    private val api: InnerTubeApi,
    private val json: Json
) : SearchRepository {

    override suspend fun search(query: String, filter: String?): Result<List<YTItem>> = runCatching {
        val request = SearchRequest(
            context = buildContext(),
            query = query,
            params = filter
        )
        val response = api.search(request)
        val responseBody = response.bodyAsText()
        val searchResponse = json.decodeFromString<SearchResponse>(responseBody)
        parseSearchResults(searchResponse)
    }

    override suspend fun searchSuggestions(query: String): Result<List<String>> = runCatching {
        val request = SearchRequest(
            context = buildContext(),
            query = query
        )
        val response = api.search(request)
        val responseBody = response.bodyAsText()
        val searchResponse = json.decodeFromString<GetSearchSuggestionsResponse>(responseBody)
        searchResponse.contents?.flatMap { section ->
            section.contents?.mapNotNull { content ->
                content.searchSuggestionRenderer?.suggestion?.runs?.map { it.text.orEmpty() }?.joinToString("")
            } ?: emptyList()
        } ?: emptyList()
    }

    override suspend fun searchSummary(query: String): Result<SearchSummaryPage> = runCatching {
        val request = SearchRequest(
            context = buildContext(),
            query = query
        )
        val response = api.search(request)
        val responseBody = response.bodyAsText()
        val searchResponse = json.decodeFromString<SearchResponse>(responseBody)
        parseSearchSummary(searchResponse)
    }

    override suspend fun home(): Result<HomePage> = runCatching {
        val request = BrowseRequest(
            context = buildContext(),
            browseId = "FEmusic_home"
        )
        val response = api.browse(request)
        val responseBody = response.bodyAsText()
        val browseResponse = json.decodeFromString<BrowseResponse>(responseBody)
        parseHomePage(browseResponse)
    }

    override suspend fun homeContinuation(continuation: String): Result<HomePage> = runCatching {
        val request = BrowseRequest(
            context = buildContext(),
            continuation = continuation
        )
        val response = api.browse(request)
        val responseBody = response.bodyAsText()
        val browseResponse = json.decodeFromString<BrowseResponse>(responseBody)
        parseHomePage(browseResponse)
    }

    override suspend fun album(browseId: String): Result<AlbumPage> = runCatching {
        val request = BrowseRequest(
            context = buildContext(),
            browseId = browseId
        )
        val response = api.browse(request)
        val responseBody = response.bodyAsText()
        val browseResponse = json.decodeFromString<BrowseResponse>(responseBody)
        parseAlbumPage(browseResponse)
    }

    override suspend fun artist(browseId: String): Result<ArtistPage> = runCatching {
        val request = BrowseRequest(
            context = buildContext(),
            browseId = browseId
        )
        val response = api.browse(request)
        val responseBody = response.bodyAsText()
        val browseResponse = json.decodeFromString<BrowseResponse>(responseBody)
        parseArtistPage(browseResponse)
    }

    override suspend fun artistItems(endpoint: BrowseEndpoint): Result<ArtistItemsPage> = runCatching {
        val request = BrowseRequest(
            context = buildContext(),
            browseId = endpoint.browseId,
            params = endpoint.params
        )
        val response = api.browse(request)
        val responseBody = response.bodyAsText()
        val browseResponse = json.decodeFromString<BrowseResponse>(responseBody)
        parseArtistItemsPage(browseResponse)
    }

    override suspend fun artistItemsContinuation(continuation: String): Result<ArtistItemsContinuationPage> = runCatching {
        val request = BrowseRequest(
            context = buildContext(),
            continuation = continuation
        )
        val response = api.browse(request)
        val responseBody = response.bodyAsText()
        val browseResponse = json.decodeFromString<BrowseResponse>(responseBody)
        parseArtistItemsContinuationPage(browseResponse)
    }

    override suspend fun playlist(playlistId: String): Result<PlaylistPage> = runCatching {
        val request = BrowseRequest(
            context = buildContext(),
            browseId = "VL$playlistId"
        )
        val response = api.browse(request)
        val responseBody = response.bodyAsText()
        val browseResponse = json.decodeFromString<BrowseResponse>(responseBody)
        parsePlaylistPage(browseResponse, playlistId)
    }

    override suspend fun playlistContinuation(continuation: String, playlistId: String?): Result<PlaylistContinuationPage> = runCatching {
        val request = BrowseRequest(
            context = buildContext(),
            continuation = continuation
        )
        val response = api.browse(request)
        val responseBody = response.bodyAsText()
        val browseResponse = json.decodeFromString<BrowseResponse>(responseBody)
        parsePlaylistContinuationPage(browseResponse)
    }

    override suspend fun explore(): Result<ExplorePage> = runCatching {
        val request = BrowseRequest(
            context = buildContext(),
            browseId = "FEmusic_explore"
        )
        val response = api.browse(request)
        val responseBody = response.bodyAsText()
        val browseResponse = json.decodeFromString<BrowseResponse>(responseBody)
        parseExplorePage(browseResponse)
    }

    override suspend fun moodAndGenres(): Result<List<MoodAndGenres>> = runCatching {
        val request = BrowseRequest(
            context = buildContext(),
            browseId = "FEmusic_moods_and_genres"
        )
        val response = api.browse(request)
        val responseBody = response.bodyAsText()
        val browseResponse = json.decodeFromString<BrowseResponse>(responseBody)
        parseMoodAndGenres(browseResponse)
    }

    override suspend fun browse(browseId: String, params: String?): Result<BrowseResult> = runCatching {
        val request = BrowseRequest(
            context = buildContext(),
            browseId = browseId,
            params = params
        )
        val response = api.browse(request)
        val responseBody = response.bodyAsText()
        val browseResponse = json.decodeFromString<BrowseResponse>(responseBody)
        parseBrowseResult(browseResponse)
    }

    override suspend fun related(endpoint: BrowseEndpoint): Result<RelatedPage> = runCatching {
        val request = BrowseRequest(
            context = buildContext(),
            browseId = endpoint.browseId,
            params = endpoint.params
        )
        val response = api.browse(request)
        val responseBody = response.bodyAsText()
        val browseResponse = json.decodeFromString<BrowseResponse>(responseBody)
        parseRelatedPage(browseResponse)
    }

    override suspend fun charts(): Result<ChartsPage> = runCatching {
        val request = BrowseRequest(
            context = buildContext(),
            browseId = "FEmusic_charts",
            params = "ggMGCgQIgAQ%3D"
        )
        val response = api.browse(request)
        val responseBody = response.bodyAsText()
        val browseResponse = json.decodeFromString<BrowseResponse>(responseBody)
        parseChartsPage(browseResponse)
    }

    override suspend fun musicHistory(): Result<HistoryPage> = runCatching {
        val request = BrowseRequest(
            context = buildContext(),
            browseId = "FEmusic_history"
        )
        val response = api.browse(request)
        val responseBody = response.bodyAsText()
        val browseResponse = json.decodeFromString<BrowseResponse>(responseBody)
        parseHistoryPage(browseResponse)
    }

    private fun buildContext(): Context = Context(
        client = ClientContext(
            clientName = "WEB_REMIX",
            clientVersion = "1.20240918.01.00"
        )
    )

    private fun parseSearchResults(response: SearchResponse): List<YTItem> {
        val items = mutableListOf<YTItem>()
        response.contents?.tabbedSearchResultsRenderer?.tabs?.forEach { tab ->
            tab.tabRenderer?.content?.sectionListRenderer?.contents?.forEach { content ->
                content.musicShelfRenderer?.contents?.forEach { item ->
                    item.musicResponsiveListItemRenderer?.let { renderer ->
                        parseMusicResponsiveListItemRenderer(renderer)?.let { items.add(it) }
                    }
                }
                content.itemSectionRenderer?.contents?.forEach { itemSection ->
                    itemSection.musicShelfRenderer?.contents?.forEach { item ->
                        item.musicResponsiveListItemRenderer?.let { renderer ->
                            parseMusicResponsiveListItemRenderer(renderer)?.let { items.add(it) }
                        }
                    }
                }
            }
        }
        return items.distinctBy { it.id }
    }

    private fun parseSearchSummary(response: SearchResponse): SearchSummaryPage {
        val summaries = mutableListOf<SearchSummary>()
        response.contents?.tabbedSearchResultsRenderer?.tabs?.forEach { tab ->
            tab.tabRenderer?.content?.sectionListRenderer?.contents?.forEach { content ->
                content.musicCardShelfRenderer?.let { shelf ->
                    val title = shelf.header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs
                        ?.joinToString("") { it.text.orEmpty() } ?: "Other"
                    val items = shelf.contents?.mapNotNull { item ->
                        item.musicResponsiveListItemRenderer?.let { parseMusicResponsiveListItemRenderer(it) }
                    }?.distinctBy { it.id } ?: emptyList()
                    if (items.isNotEmpty()) {
                        summaries.add(SearchSummary(title = title, items = items))
                    }
                }
                content.musicShelfRenderer?.let { shelf ->
                    val title = shelf.title?.runs?.joinToString("") { it.text.orEmpty() } ?: "Other"
                    val items = shelf.contents?.mapNotNull { item ->
                        item.musicResponsiveListItemRenderer?.let { parseMusicResponsiveListItemRenderer(it) }
                    }?.distinctBy { it.id } ?: emptyList()
                    if (items.isNotEmpty()) {
                        summaries.add(SearchSummary(title = title, items = items))
                    }
                }
            }
        }
        return SearchSummaryPage(summaries = summaries)
    }

    private fun parseHomePage(response: BrowseResponse): HomePage {
        val sections = mutableListOf<HomeSection>()
        val chips = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer?.header?.chipCloudRenderer?.chips?.mapNotNull { chip ->
                HomeChip(
                    text = chip.text?.runs?.joinToString("") { it.text.orEmpty() } ?: "",
                    endpoint = chip.navigationEndpoint?.browseEndpoint
                )
            }
        response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer?.contents?.forEach { content ->
                content.musicCarouselShelfRenderer?.let { shelf ->
                    val title = shelf.header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs
                        ?.joinToString("") { it.text.orEmpty() } ?: ""
                    val items = shelf.contents?.mapNotNull { item ->
                        item.musicTwoRowItemRenderer?.let { parseMusicTwoRowItemRenderer(it) }
                    }?.distinctBy { it.id } ?: emptyList()
                    if (items.isNotEmpty()) {
                        sections.add(HomeSection(title = title, items = items))
                    }
                }
            }
        val continuation = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer?.continuations?.firstOrNull()
            ?.nextContinuationData?.continuation
        return HomePage(chips = chips, sections = sections, continuation = continuation)
    }

    private fun parseAlbumPage(response: BrowseResponse): AlbumPage {
        val header = response.header
        val album = header?.musicImmersiveHeaderRenderer?.let { renderer ->
            AlbumItem(
                browseId = "",
                playlistId = "",
                title = renderer.title?.runs?.joinToString("") { it.text.orEmpty() } ?: "",
                artists = emptyList(),
                year = null,
                thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.thumbnails?.lastOrNull()?.url
            )
        } ?: header?.musicDetailHeaderRenderer?.let { renderer ->
            AlbumItem(
                browseId = "",
                playlistId = "",
                title = renderer.title?.runs?.joinToString("") { it.text.orEmpty() } ?: "",
                artists = emptyList(),
                year = null,
                thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.thumbnails?.lastOrNull()?.url
            )
        } ?: AlbumItem(
            browseId = "",
            playlistId = "",
            title = "",
            artists = emptyList(),
            year = null,
            thumbnail = null
        )
        val songs = mutableListOf<SongItem>()
        response.contents?.twoColumnBrowseResultsRenderer?.tabs?.forEach { tab ->
            tab.tabRenderer?.content?.sectionListRenderer?.contents?.forEach { content ->
                content.musicShelfRenderer?.contents?.forEach { item ->
                    item.musicResponsiveListItemRenderer?.let { renderer ->
                        parseMusicResponsiveListItemRenderer(renderer)?.let { songs.add(it as SongItem) }
                    }
                }
            }
        }
        return AlbumPage(album = album, songs = songs)
    }

    private fun parseArtistPage(response: BrowseResponse): ArtistPage {
        val header = response.header
        val artist = header?.musicImmersiveHeaderRenderer?.let { renderer ->
            ArtistItem(
                browseId = "",
                title = renderer.title?.runs?.joinToString("") { it.text.orEmpty() } ?: "",
                artists = emptyList(),
                thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.thumbnails?.lastOrNull()?.url
            )
        } ?: ArtistItem(
            browseId = "",
            title = "",
            artists = emptyList(),
            thumbnail = null
        )
        val sections = mutableListOf<ArtistSection>()
        return ArtistPage(artist = artist, sections = sections)
    }

    private fun parseArtistItemsPage(response: BrowseResponse): ArtistItemsPage {
        val title = response.header?.musicHeaderRenderer?.title?.runs?.joinToString("") { it.text.orEmpty() } ?: ""
        val items = mutableListOf<YTItem>()
        response.contents?.singleColumnBrowseResultsRenderer?.tabs?.forEach { tab ->
            tab.tabRenderer?.content?.sectionListRenderer?.contents?.forEach { content ->
                content.musicShelfRenderer?.contents?.forEach { item ->
                    item.musicResponsiveListItemRenderer?.let { renderer ->
                        parseMusicResponsiveListItemRenderer(renderer)?.let { items.add(it) }
                    }
                }
            }
        }
        return ArtistItemsPage(title = title, items = items)
    }

    private fun parseArtistItemsContinuationPage(response: BrowseResponse): ArtistItemsContinuationPage {
        val items = mutableListOf<YTItem>()
        response.continuationContents?.sectionListContinuation?.contents?.forEach { content ->
            content.musicShelfRenderer?.contents?.forEach { item ->
                item.musicResponsiveListItemRenderer?.let { renderer ->
                    parseMusicResponsiveListItemRenderer(renderer)?.let { items.add(it) }
                }
            }
        }
        val continuation = response.continuationContents?.sectionListContinuation?.continuations?.firstOrNull()
            ?.nextContinuationData?.continuation
        return ArtistItemsContinuationPage(items = items, continuation = continuation)
    }

    private fun parsePlaylistPage(response: BrowseResponse, playlistId: String): PlaylistPage {
        val header = response.header
        val playlist = header?.musicResponsiveHeaderRenderer?.let { renderer ->
            PlaylistItem(
                playlistId = playlistId,
                title = renderer.title?.runs?.joinToString("") { it.text.orEmpty() } ?: "",
                artists = renderer.straplineTextOne?.runs?.map { run ->
                    Artist(
                        name = run.text.orEmpty(),
                        id = run.navigationEndpoint?.browseEndpoint?.browseId
                    )
                } ?: emptyList(),
                songCountText = renderer.secondSubtitle?.runs?.firstOrNull()?.text,
                thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.thumbnails?.lastOrNull()?.url,
                description = null,
                playEndpoint = null
            )
        } ?: PlaylistItem(
            playlistId = playlistId,
            title = "",
            artists = emptyList(),
            songCountText = null,
            thumbnail = null,
            description = null,
            playEndpoint = null
        )
        val songs = mutableListOf<SongItem>()
        response.contents?.twoColumnBrowseResultsRenderer?.secondaryContents?.sectionListRenderer?.contents?.forEach { content ->
            content.musicShelfRenderer?.contents?.forEach { item ->
                item.musicResponsiveListItemRenderer?.let { renderer ->
                    parseMusicResponsiveListItemRenderer(renderer)?.let { songs.add(it as SongItem) }
                }
            }
        }
        val songsContinuation = response.contents?.twoColumnBrowseResultsRenderer?.secondaryContents?.sectionListRenderer?.continuations?.firstOrNull()
            ?.nextContinuationData?.continuation
        return PlaylistPage(playlist = playlist, songs = songs, songsContinuation = songsContinuation)
    }

    private fun parsePlaylistContinuationPage(response: BrowseResponse): PlaylistContinuationPage {
        val songs = mutableListOf<SongItem>()
        response.continuationContents?.musicPlaylistShelfContinuation?.contents?.forEach { item ->
            item.musicResponsiveListItemRenderer?.let { renderer ->
                parseMusicResponsiveListItemRenderer(renderer)?.let { songs.add(it as SongItem) }
            }
        }
        val continuation = response.continuationContents?.musicPlaylistShelfContinuation?.continuations?.firstOrNull()
            ?.nextContinuationData?.continuation
        return PlaylistContinuationPage(songs = songs, continuation = continuation)
    }

    private fun parseExplorePage(response: BrowseResponse): ExplorePage {
        val newReleaseAlbums = mutableListOf<AlbumItem>()
        val moodAndGenres = mutableListOf<MoodAndGenres>()
        response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer?.contents?.forEach { content ->
                content.musicCarouselShelfRenderer?.let { shelf ->
                    val title = shelf.header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs
                        ?.joinToString("") { it.text.orEmpty() } ?: ""
                    shelf.contents?.forEach { item ->
                        item.musicTwoRowItemRenderer?.let { renderer ->
                            parseMusicTwoRowItemRenderer(renderer)?.let { ytItem ->
                                if (ytItem is AlbumItem) newReleaseAlbums.add(ytItem)
                            }
                        }
                    }
                }
            }
        return ExplorePage(newReleaseAlbums = newReleaseAlbums, moodAndGenres = moodAndGenres)
    }

    private fun parseMoodAndGenres(response: BrowseResponse): List<MoodAndGenres> {
        val items = mutableListOf<MoodAndGenres>()
        response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer?.contents?.forEach { content ->
                content.musicCarouselShelfRenderer?.contents?.forEach { item ->
                    item.musicNavigationButtonRenderer?.let { renderer ->
                        MoodAndGenres(
                            title = renderer.title?.runs?.joinToString("") { it.text.orEmpty() } ?: "",
                            endpoint = renderer.navigationEndpoint?.browseEndpoint,
                            thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.thumbnails?.lastOrNull()?.url
                        )
                    }?.let { items.add(it) }
                }
            }
        return items
    }

    private fun parseBrowseResult(response: BrowseResponse): BrowseResult {
        val title = response.header?.musicHeaderRenderer?.title?.runs?.joinToString("") { it.text.orEmpty() }
        val thumbnail = response.header?.musicHeaderRenderer?.thumbnail?.musicThumbnailRenderer?.thumbnails?.lastOrNull()?.url
        val items = mutableListOf<BrowseResult.Item>()
        return BrowseResult(title = title, thumbnail = thumbnail, items = items)
    }

    private fun parseRelatedPage(response: BrowseResponse): RelatedPage {
        val songs = mutableListOf<SongItem>()
        val albums = mutableListOf<AlbumItem>()
        val artists = mutableListOf<ArtistItem>()
        val playlists = mutableListOf<PlaylistItem>()
        response.contents?.sectionListRenderer?.contents?.forEach { content ->
            content.musicCarouselShelfRenderer?.contents?.forEach { item ->
                item.musicResponsiveListItemRenderer?.let { renderer ->
                    parseMusicResponsiveListItemRenderer(renderer)?.let { ytItem ->
                        when (ytItem) {
                            is SongItem -> songs.add(ytItem)
                            is AlbumItem -> albums.add(ytItem)
                            is ArtistItem -> artists.add(ytItem)
                            is PlaylistItem -> playlists.add(ytItem)
                        }
                    }
                }
                item.musicTwoRowItemRenderer?.let { renderer ->
                    parseMusicTwoRowItemRenderer(renderer)?.let { ytItem ->
                        when (ytItem) {
                            is SongItem -> songs.add(ytItem)
                            is AlbumItem -> albums.add(ytItem)
                            is ArtistItem -> artists.add(ytItem)
                            is PlaylistItem -> playlists.add(ytItem)
                        }
                    }
                }
            }
        }
        return RelatedPage(songs = songs, albums = albums, artists = artists, playlists = playlists)
    }

    private fun parseChartsPage(response: BrowseResponse): ChartsPage {
        val sections = mutableListOf<ChartsSection>()
        response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer?.contents?.forEach { content ->
                content.musicCarouselShelfRenderer?.let { shelf ->
                    val title = shelf.header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs
                        ?.joinToString("") { it.text.orEmpty() } ?: ""
                    val items = shelf.contents?.mapNotNull { item ->
                        item.musicResponsiveListItemRenderer?.let { parseMusicResponsiveListItemRenderer(it) }
                            ?: item.musicTwoRowItemRenderer?.let { parseMusicTwoRowItemRenderer(it) }
                    }?.distinctBy { it.id } ?: emptyList()
                    if (items.isNotEmpty()) {
                        sections.add(ChartsSection(title = title, items = items))
                    }
                }
            }
        return ChartsPage(sections = sections)
    }

    private fun parseHistoryPage(response: BrowseResponse): HistoryPage {
        val sections = mutableListOf<HomeSection>()
        response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer?.contents?.forEach { content ->
                content.musicCarouselShelfRenderer?.let { shelf ->
                    val title = shelf.header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs
                        ?.joinToString("") { it.text.orEmpty() } ?: ""
                    val items = shelf.contents?.mapNotNull { item ->
                        item.musicTwoRowItemRenderer?.let { parseMusicTwoRowItemRenderer(it) }
                    }?.distinctBy { it.id } ?: emptyList()
                    if (items.isNotEmpty()) {
                        sections.add(HomeSection(title = title, items = items))
                    }
                }
            }
        return HistoryPage(sections = sections)
    }

    private fun parseMusicResponsiveListItemRenderer(renderer: MusicResponsiveListItemRenderer): YTItem? {
        val title = renderer.title?.runs?.joinToString("") { it.text.orEmpty() } ?: return null
        val videoId = renderer.playlistItemData?.videoId ?: return null
        val thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.thumbnails?.lastOrNull()?.url
        val explicit = renderer.badges?.any { badge ->
            badge.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
        } == true
        return SongItem(
            id = videoId,
            title = title,
            artists = renderer.flexColumns?.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.map { run ->
                Artist(
                    name = run.text.orEmpty(),
                    id = run.navigationEndpoint?.browseEndpoint?.browseId
                )
            } ?: emptyList(),
            thumbnail = thumbnail,
            explicit = explicit
        )
    }

    private fun parseMusicTwoRowItemRenderer(renderer: MusicTwoRowItemRenderer): YTItem? {
        val title = renderer.title?.runs?.joinToString("") { it.text.orEmpty() } ?: return null
        val thumbnail = renderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnails?.lastOrNull()?.url
        val explicit = renderer.subtitleBadges?.any { badge ->
            badge.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
        } == true

        return when {
            renderer.navigationEndpoint?.watchEndpoint?.videoId != null -> {
                SongItem(
                    id = renderer.navigationEndpoint.watchEndpoint.videoId,
                    title = title,
                    artists = renderer.subtitle?.runs?.filter { it.navigationEndpoint?.browseEndpoint?.browseId != null }?.map { run ->
                        Artist(
                            name = run.text.orEmpty(),
                            id = run.navigationEndpoint?.browseEndpoint?.browseId
                        )
                    } ?: emptyList(),
                    thumbnail = thumbnail,
                    explicit = explicit
                )
            }
            renderer.navigationEndpoint?.browseEndpoint?.browseId != null -> {
                val browseId = renderer.navigationEndpoint.browseEndpoint.browseId
                val subtitle = renderer.subtitle?.runs?.joinToString("") { it.text.orEmpty() }
                when {
                    browseId.startsWith("UC") -> {
                        ArtistItem(
                            browseId = browseId,
                            title = title,
                            artists = emptyList(),
                            thumbnail = thumbnail
                        )
                    }
                    browseId.startsWith("VL") -> {
                        val playlistId = browseId.removePrefix("VL")
                        PlaylistItem(
                            playlistId = playlistId,
                            title = title,
                            artists = emptyList(),
                            thumbnail = thumbnail,
                            description = subtitle
                        )
                    }
                    else -> {
                        AlbumItem(
                            browseId = browseId,
                            playlistId = browseId,
                            title = title,
                            artists = renderer.subtitle?.runs?.filter { it.navigationEndpoint?.browseEndpoint?.browseId != null }?.map { run ->
                                Artist(
                                    name = run.text.orEmpty(),
                                    id = run.navigationEndpoint?.browseEndpoint?.browseId
                                )
                            } ?: emptyList(),
                            year = subtitle?.toIntOrNull(),
                            thumbnail = thumbnail,
                            explicit = explicit
                        )
                    }
                }
            }
            else -> null
        }
    }
}

interface LibraryRepository {
    suspend fun getLibrary(): Result<LibraryPage>
    suspend fun getLibraryContinuation(continuation: String): Result<LibraryContinuationPage>
}

class LibraryRepositoryImpl(
    private val api: InnerTubeApi,
    private val json: Json
) : LibraryRepository {

    override suspend fun getLibrary(): Result<LibraryPage> = runCatching {
        val request = BrowseRequest(
            context = Context(client = ClientContext(clientName = "WEB_REMIX", clientVersion = "1.20240918.01.00")),
            browseId = "FEmusic_library"
        )
        val response = api.browse(request)
        val responseBody = response.bodyAsText()
        val browseResponse = json.decodeFromString<BrowseResponse>(responseBody)
        parseLibraryPage(browseResponse)
    }

    override suspend fun getLibraryContinuation(continuation: String): Result<LibraryContinuationPage> = runCatching {
        val request = BrowseRequest(
            context = Context(client = ClientContext(clientName = "WEB_REMIX", clientVersion = "1.20240918.01.00")),
            continuation = continuation
        )
        val response = api.browse(request)
        val responseBody = response.bodyAsText()
        val browseResponse = json.decodeFromString<BrowseResponse>(responseBody)
        parseLibraryContinuationPage(browseResponse)
    }

    private fun parseLibraryPage(response: BrowseResponse): LibraryPage {
        val items = mutableListOf<YTItem>()
        response.contents?.singleColumnBrowseResultsRenderer?.tabs?.forEach { tab ->
            tab.tabRenderer?.content?.sectionListRenderer?.contents?.forEach { content ->
                content.musicShelfRenderer?.contents?.forEach { item ->
                    item.musicResponsiveListItemRenderer?.let { renderer ->
                        parseMusicResponsiveListItemRenderer(renderer)?.let { items.add(it) }
                    }
                }
                content.gridRenderer?.items?.forEach { item ->
                    item.musicTwoRowItemRenderer?.let { renderer ->
                        parseMusicTwoRowItemRenderer(renderer)?.let { items.add(it) }
                    }
                }
            }
        }
        val continuation = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer?.continuations?.firstOrNull()
            ?.nextContinuationData?.continuation
        return LibraryPage(items = items, continuation = continuation)
    }

    private fun parseLibraryContinuationPage(response: BrowseResponse): LibraryContinuationPage {
        val items = mutableListOf<YTItem>()
        response.continuationContents?.sectionListContinuation?.contents?.forEach { content ->
            content.musicShelfRenderer?.contents?.forEach { item ->
                item.musicResponsiveListItemRenderer?.let { renderer ->
                    parseMusicResponsiveListItemRenderer(renderer)?.let { items.add(it) }
                }
            }
        }
        val continuation = response.continuationContents?.sectionListContinuation?.continuations?.firstOrNull()
            ?.nextContinuationData?.continuation
        return LibraryContinuationPage(items = items, continuation = continuation)
    }

    private fun parseMusicResponsiveListItemRenderer(renderer: MusicResponsiveListItemRenderer): YTItem? {
        val title = renderer.title?.runs?.joinToString("") { it.text.orEmpty() } ?: return null
        val videoId = renderer.playlistItemData?.videoId ?: return null
        val thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.thumbnails?.lastOrNull()?.url
        val explicit = renderer.badges?.any { badge ->
            badge.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
        } == true
        return SongItem(
            id = videoId,
            title = title,
            artists = renderer.flexColumns?.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.map { run ->
                Artist(
                    name = run.text.orEmpty(),
                    id = run.navigationEndpoint?.browseEndpoint?.browseId
                )
            } ?: emptyList(),
            thumbnail = thumbnail,
            explicit = explicit
        )
    }

    private fun parseMusicTwoRowItemRenderer(renderer: MusicTwoRowItemRenderer): YTItem? {
        val title = renderer.title?.runs?.joinToString("") { it.text.orEmpty() } ?: return null
        val thumbnail = renderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnails?.lastOrNull()?.url
        val explicit = renderer.subtitleBadges?.any { badge ->
            badge.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
        } == true
        return when {
            renderer.navigationEndpoint?.watchEndpoint?.videoId != null -> {
                SongItem(
                    id = renderer.navigationEndpoint.watchEndpoint.videoId,
                    title = title,
                    artists = renderer.subtitle?.runs?.filter { it.navigationEndpoint?.browseEndpoint?.browseId != null }?.map { run ->
                        Artist(
                            name = run.text.orEmpty(),
                            id = run.navigationEndpoint?.browseEndpoint?.browseId
                        )
                    } ?: emptyList(),
                    thumbnail = thumbnail,
                    explicit = explicit
                )
            }
            renderer.navigationEndpoint?.browseEndpoint?.browseId != null -> {
                val browseId = renderer.navigationEndpoint.browseEndpoint.browseId
                val subtitle = renderer.subtitle?.runs?.joinToString("") { it.text.orEmpty() }
                when {
                    browseId.startsWith("UC") -> {
                        ArtistItem(
                            browseId = browseId,
                            title = title,
                            artists = emptyList(),
                            thumbnail = thumbnail
                        )
                    }
                    browseId.startsWith("VL") -> {
                        val playlistId = browseId.removePrefix("VL")
                        PlaylistItem(
                            playlistId = playlistId,
                            title = title,
                            artists = emptyList(),
                            thumbnail = thumbnail,
                            description = subtitle
                        )
                    }
                    else -> {
                        AlbumItem(
                            browseId = browseId,
                            playlistId = browseId,
                            title = title,
                            artists = renderer.subtitle?.runs?.filter { it.navigationEndpoint?.browseEndpoint?.browseId != null }?.map { run ->
                                Artist(
                                    name = run.text.orEmpty(),
                                    id = run.navigationEndpoint?.browseEndpoint?.browseId
                                )
                            } ?: emptyList(),
                            year = subtitle?.toIntOrNull(),
                            thumbnail = thumbnail,
                            explicit = explicit
                        )
                    }
                }
            }
            else -> null
        }
    }
}

interface PlaybackRepository {
    suspend fun next(endpoint: WatchEndpoint): Result<NextResult>
    suspend fun player(videoId: String, playlistId: String?): Result<PlayerResponse>
    suspend fun getQueue(videoIds: List<String>? = null, playlistId: String? = null): Result<List<SongItem>>
    suspend fun getTranscript(videoId: String): Result<String>
}

class PlaybackRepositoryImpl(
    private val api: InnerTubeApi,
    private val extractorApi: ExtractorApi,
    private val json: Json
) : PlaybackRepository {

    override suspend fun next(endpoint: WatchEndpoint): Result<NextResult> = runCatching {
        val request = NextRequest(
            context = Context(client = ClientContext(clientName = "WEB_REMIX", clientVersion = "1.20240918.01.00")),
            videoId = endpoint.videoId,
            playlistId = endpoint.playlistId,
            playlistSetVideoId = endpoint.playlistSetVideoId,
            index = endpoint.index,
            params = endpoint.params
        )
        val response = api.next(request)
        val responseBody = response.bodyAsText()
        val nextResponse = json.decodeFromString<NextResponse>(responseBody)
        parseNextResponse(nextResponse, endpoint)
    }

    override suspend fun player(videoId: String, playlistId: String?): Result<PlayerResponse> = runCatching {
        val request = PlayerRequest(
            context = Context(client = ClientContext(clientName = "WEB_REMIX", clientVersion = "1.20240918.01.00")),
            videoId = videoId,
            playlistId = playlistId
        )
        val response = api.player(request)
        val responseBody = response.bodyAsText()
        json.decodeFromString<PlayerResponse>(responseBody)
    }

    override suspend fun getQueue(videoIds: List<String>?, playlistId: String?): Result<List<SongItem>> = runCatching {
        val request = GetQueueRequest(
            context = Context(client = ClientContext(clientName = "WEB_REMIX", clientVersion = "1.20240918.01.00")),
            videoIds = videoIds,
            playlistId = playlistId
        )
        val response = api.getQueue(request)
        val responseBody = response.bodyAsText()
        val queueResponse = json.decodeFromString<GetQueueResponse>(responseBody)
        queueResponse.queueDatas?.mapNotNull { data ->
            data.content?.playlistPanelVideoRenderer?.let { renderer ->
                val title = renderer.title?.runs?.joinToString("") { it.text.orEmpty() } ?: return@let null
                val videoId = renderer.navigationEndpoint?.watchEndpoint?.videoId ?: return@let null
                val thumbnail = renderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnails?.lastOrNull()?.url
                val artists = renderer.subtitle?.runs?.filter { it.navigationEndpoint?.browseEndpoint?.browseId != null }?.map { run ->
                    Artist(
                        name = run.text.orEmpty(),
                        id = run.navigationEndpoint?.browseEndpoint?.browseId
                    )
                } ?: emptyList()
                SongItem(
                    id = videoId,
                    title = title,
                    artists = artists,
                    thumbnail = thumbnail,
                    playlistId = renderer.playlistItemData?.playlistId,
                    setVideoId = renderer.playlistItemData?.setVideoId
                )
            }
        } ?: emptyList()
    }

    override suspend fun getTranscript(videoId: String): Result<String> = runCatching {
        val request = TranscriptRequest(
            context = Context(client = ClientContext(clientName = "WEB_REMIX", clientVersion = "1.20240918.01.00")),
            videoId = videoId
        )
        val response = api.getTranscript(request)
        val responseBody = response.bodyAsText()
        val transcriptResponse = json.decodeFromString<GetTranscriptResponse>(responseBody)
        transcriptResponse.actions?.firstOrNull()?.updateEngagementPanelAction?.content?.transcriptRenderer?.body?.transcriptBodyRenderer?.cueGroups?.joinToString("\n") { group ->
            val time = group.transcriptCueGroupRenderer?.cues?.firstOrNull()?.transcriptCueRenderer?.startOffsetMs ?: 0
            val text = group.transcriptCueGroupRenderer?.cues?.firstOrNull()?.transcriptCueRenderer?.cue?.simpleText?.trim('♪')?.trim(' ') ?: ""
            "[%02d:%02d.%03d]$text".format(time / 60000, (time / 1000) % 60, time % 1000)
        } ?: ""
    }

    private fun parseNextResponse(response: NextResultResponse, endpoint: WatchEndpoint): NextResult {
        val playlistPanelRenderer = response.contents?.singleColumnMusicWatchNextResultsRenderer?.tabbedRenderer
            ?.watchNextTabbedResultsRenderer?.tabs?.getOrNull(0)?.tabRenderer?.content?.musicQueueRenderer?.content
        val title = response.contents?.singleColumnMusicWatchNextResultsRenderer?.tabbedRenderer
            ?.watchNextTabbedResultsRenderer?.tabs?.getOrNull(0)?.tabRenderer?.content?.musicQueueRenderer?.header
            ?.musicQueueHeaderRenderer?.subtitle?.runs?.firstOrNull()?.text
        val songs = playlistPanelRenderer?.contents?.mapNotNull { content ->
            content.playlistPanelVideoRenderer?.let { renderer ->
                val songTitle = renderer.title?.runs?.joinToString("") { it.text.orEmpty() } ?: return@let null
                val videoId = renderer.navigationEndpoint?.watchEndpoint?.videoId ?: return@let null
                val thumbnail = renderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnails?.lastOrNull()?.url
                val artists = renderer.subtitle?.runs?.filter { it.navigationEndpoint?.browseEndpoint?.browseId != null }?.map { run ->
                    Artist(
                        name = run.text.orEmpty(),
                        id = run.navigationEndpoint?.browseEndpoint?.browseId
                    )
                } ?: emptyList()
                SongItem(
                    id = videoId,
                    title = songTitle,
                    artists = artists,
                    thumbnail = thumbnail,
                    playlistId = renderer.playlistItemData?.playlistId,
                    setVideoId = renderer.playlistItemData?.setVideoId,
                    endpoint = renderer.navigationEndpoint?.watchEndpoint
                )
            }
        }?.distinctBy { it.id } ?: emptyList()
        val currentIndex = songs.indexOfFirst { it.id == endpoint.videoId }.takeIf { it != -1 }
        val continuation = playlistPanelRenderer?.continuations?.firstOrNull()?.nextContinuationData?.continuation
        return NextResult(
            title = title,
            items = songs,
            currentIndex = currentIndex,
            continuation = continuation,
            endpoint = endpoint
        )
    }
}

data class BrowseResult(
    val title: String?,
    val thumbnail: String?,
    val items: List<Item>
) {
    data class Item(
        val title: String?,
        val items: List<YTItem>
    )
}

data class ArtistItemsContinuationPage(
    val items: List<YTItem>,
    val continuation: String?
)

data class HistoryPage(
    val sections: List<HomeSection>
)