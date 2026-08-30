package com.musicwave.data.model

interface YTItem {
    val id: String
    val title: String
    val artists: List<Artist>
    val thumbnail: String?
    val thumbnailWidth: Int?
    val thumbnailHeight: Int?
    val explicit: Boolean
    val duration: String?
    val durationMs: Long?
}

data class SongItem(
    override val id: String,
    override val title: String,
    override val artists: List<Artist>,
    override val thumbnail: String?,
    override val thumbnailWidth: Int? = null,
    override val thumbnailHeight: Int? = null,
    override val explicit: Boolean = false,
    val album: AlbumItem? = null,
    val playlistId: String? = null,
    val setVideoId: String? = null,
    val isMusicVideo: Boolean = false,
    override val duration: String? = null,
    override val durationMs: Long? = null,
    val endpoint: WatchEndpoint? = null
) : YTItem

data class AlbumItem(
    val browseId: String,
    val playlistId: String,
    override val id: String = browseId,
    override val title: String,
    override val artists: List<Artist>,
    val year: Int? = null,
    override val thumbnail: String?,
    override val thumbnailWidth: Int? = null,
    override val thumbnailHeight: Int? = null,
    override val explicit: Boolean = false
) : YTItem

data class ArtistItem(
    val browseId: String,
    override val id: String = browseId,
    override val title: String,
    override val artists: List<Artist> = emptyList(),
    override val thumbnail: String?,
    override val thumbnailWidth: Int? = null,
    override val thumbnailHeight: Int? = null,
    val channelId: String? = null,
    val playEndpoint: WatchEndpoint? = null,
    val shuffleEndpoint: WatchEndpoint? = null,
    val radioEndpoint: WatchEndpoint? = null,
    val subscriberCountText: String? = null,
    val monthlyListenerCountText: String? = null,
    val description: String? = null
) : YTItem

data class PlaylistItem(
    val playlistId: String,
    override val id: String = playlistId,
    override val title: String,
    override val artists: List<Artist> = emptyList(),
    val songCountText: String? = null,
    override val thumbnail: String?,
    override val thumbnailWidth: Int? = null,
    override val thumbnailHeight: Int? = null,
    val description: String? = null,
    val playEndpoint: WatchEndpoint? = null,
    val shuffleEndpoint: WatchEndpoint? = null,
    val radioEndpoint: WatchEndpoint? = null,
    val isEditable: Boolean = false
) : YTItem

data class Artist(
    val name: String,
    val id: String? = null
)

data class WatchEndpoint(
    val videoId: String,
    val playlistId: String? = null,
    val playlistSetVideoId: String? = null,
    val index: Int? = null,
    val params: String? = null
)

data class BrowseEndpoint(
    val browseId: String,
    val params: String? = null
)

data class SearchResult(
    val items: List<YTItem> = emptyList(),
    val continuation: String? = null
)

data class SearchSuggestions(
    val queries: List<String> = emptyList(),
    val recommendedItems: List<SearchSuggestionPage> = emptyList()
)

data class SearchSuggestionPage(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val thumbnail: String? = null,
    val endpoint: WatchEndpoint? = null
)

data class SearchSummaryPage(
    val summaries: List<SearchSummary> = emptyList()
)

data class SearchSummary(
    val title: String,
    val items: List<YTItem> = emptyList()
)

data class HomePage(
    val chips: List<HomeChip>? = null,
    val sections: List<HomeSection> = emptyList(),
    val continuation: String? = null
)

data class HomeChip(
    val text: String,
    val endpoint: BrowseEndpoint? = null
)

data class HomeSection(
    val title: String,
    val items: List<YTItem> = emptyList(),
    val endpoint: BrowseEndpoint? = null,
    val continuation: String? = null
)

data class NextResult(
    val title: String? = null,
    val items: List<SongItem> = emptyList(),
    val currentIndex: Int? = null,
    val lyricsEndpoint: BrowseEndpoint? = null,
    val relatedEndpoint: BrowseEndpoint? = null,
    val continuation: String? = null,
    val endpoint: WatchEndpoint
)

data class AlbumPage(
    val album: AlbumItem,
    val songs: List<SongItem> = emptyList(),
    val otherVersions: List<AlbumItem> = emptyList()
)

data class ArtistPage(
    val artist: ArtistItem,
    val sections: List<ArtistSection> = emptyList(),
    val description: String? = null
)

data class ArtistSection(
    val title: String,
    val items: List<YTItem> = emptyList(),
    val continuation: String? = null,
    val layout: String = "LIST"
)

data class ArtistItemsPage(
    val title: String,
    val items: List<YTItem> = emptyList(),
    val continuation: String? = null,
    val layout: String = "LIST"
)

data class PlaylistPage(
    val playlist: PlaylistItem,
    val songs: List<SongItem> = emptyList(),
    val songsContinuation: String? = null,
    val continuation: String? = null
)

data class PlaylistContinuationPage(
    val songs: List<SongItem> = emptyList(),
    val continuation: String? = null
)

data class LibraryPage(
    val items: List<YTItem> = emptyList(),
    val continuation: String? = null
)

data class LibraryContinuationPage(
    val items: List<YTItem> = emptyList(),
    val continuation: String? = null
)

data class ExplorePage(
    val newReleaseAlbums: List<AlbumItem> = emptyList(),
    val moodAndGenres: List<MoodAndGenres> = emptyList()
)

data class MoodAndGenres(
    val title: String,
    val endpoint: BrowseEndpoint? = null,
    val thumbnail: String? = null
)

data class ChartsPage(
    val sections: List<ChartsSection> = emptyList(),
    val continuation: String? = null
)

data class ChartsSection(
    val title: String,
    val items: List<YTItem> = emptyList(),
    val chartType: String = "GENRE"
)

data class RelatedPage(
    val songs: List<SongItem> = emptyList(),
    val albums: List<AlbumItem> = emptyList(),
    val artists: List<ArtistItem> = emptyList(),
    val playlists: List<PlaylistItem> = emptyList()
)

data class AccountInfo(
    val name: String,
    val email: String,
    val photoUrl: String? = null,
    val dataSyncId: String? = null,
    val channelId: String? = null
)

data class AccountChannel(
    val name: String,
    val id: String,
    val dataSyncId: String? = null,
    val photoUrl: String? = null,
    val isSelected: Boolean = false
)

data class ExtractedAudio(
    val success: Boolean,
    val valid: Boolean,
    val cached: Boolean,
    val serverVersion: String,
    val title: String? = null,
    val thumbnail: String? = null,
    val streamUrl: String,
    val streamPath: String,
    val streamExpiresAt: Long,
    val formatId: String? = null,
    val ext: String? = null,
    val acodec: String? = null,
    val mimeType: String? = null,
    val error: String? = null
)