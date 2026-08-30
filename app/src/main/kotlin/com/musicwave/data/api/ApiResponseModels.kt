package com.musicwave.data.api

import com.musicwave.data.model.*
import kotlinx.serialization.Serializable

@Serializable
data class BrowseResponse(
    val header: BrowseHeader? = null,
    val contents: BrowseContents? = null,
    val continuationContents: ContinuationContents? = null,
    val onResponseReceivedActions: List<OnResponseReceivedAction>? = null
)

@Serializable
data class BrowseHeader(
    val musicImmersiveHeaderRenderer: MusicImmersiveHeaderRenderer? = null,
    val musicVisualHeaderRenderer: MusicVisualHeaderRenderer? = null,
    val musicDetailHeaderRenderer: MusicDetailHeaderRenderer? = null,
    val musicHeaderRenderer: MusicHeaderRenderer? = null,
    val musicEditablePlaylistDetailHeaderRenderer: MusicEditablePlaylistDetailHeaderRenderer? = null
)

@Serializable
data class MusicImmersiveHeaderRenderer(
    val title: TitleRuns? = null,
    val thumbnail: MusicThumbnailRenderer? = null,
    val description: DescriptionRuns? = null,
    val playButton: ButtonRenderer? = null,
    val startRadioButton: ButtonRenderer? = null,
    val subscriptionButton: SubscriptionButton? = null,
    val monthlyListenerCount: TitleRuns? = null,
    val playlistItemData: PlaylistItemData? = null
)

@Serializable
data class MusicVisualHeaderRenderer(
    val title: TitleRuns? = null,
    val foregroundThumbnail: MusicThumbnailRenderer? = null,
    val straplineThumbnail: MusicThumbnailRenderer? = null
)

@Serializable
data class MusicDetailHeaderRenderer(
    val title: TitleRuns? = null,
    val description: DescriptionRuns? = null,
    val thumbnail: MusicThumbnailRenderer? = null
)

@Serializable
data class MusicHeaderRenderer(
    val title: TitleRuns? = null,
    val subtitle: TitleRuns? = null,
    val straplineTextOne: TitleRuns? = null,
    val secondSubtitle: TitleRuns? = null,
    val thumbnail: MusicThumbnailRenderer? = null,
    val buttons: List<ButtonRenderer>? = null
)

@Serializable
data class MusicEditablePlaylistDetailHeaderRenderer(
    val header: MusicResponsiveHeaderRenderer? = null
)

@Serializable
data class MusicResponsiveHeaderRenderer(
    val title: TitleRuns? = null,
    val straplineTextOne: TitleRuns? = null,
    val secondSubtitle: TitleRuns? = null,
    val thumbnail: MusicThumbnailRenderer? = null,
    val buttons: List<ButtonRenderer>? = null
)

@Serializable
data class BrowseContents(
    val singleColumnBrowseResultsRenderer: SingleColumnBrowseResultsRenderer? = null,
    val twoColumnBrowseResultsRenderer: TwoColumnBrowseResultsRenderer? = null,
    val sectionListRenderer: SectionListRenderer? = null,
    val tabbedSearchResultsRenderer: TabbedSearchResultsRenderer? = null
)

@Serializable
data class SingleColumnBrowseResultsRenderer(
    val tabs: List<TabRenderer>? = null,
    val header: SectionListHeader? = null
)

@Serializable
data class TwoColumnBrowseResultsRenderer(
    val tabs: List<TabRenderer>? = null,
    val primaryContents: SectionListRenderer? = null,
    val secondaryContents: SectionListRenderer? = null
)

@Serializable
data class SectionListRenderer(
    val contents: List<SectionListContent>? = null,
    val header: SectionListHeader? = null,
    val continuations: List<Continuation>? = null
)

@Serializable
data class SectionListContent(
    val musicCarouselShelfRenderer: MusicCarouselShelfRenderer? = null,
    val musicShelfRenderer: MusicShelfRenderer? = null,
    val musicPlaylistShelfRenderer: MusicPlaylistShelfRenderer? = null,
    val musicResponsiveHeaderRenderer: MusicResponsiveHeaderRenderer? = null,
    val musicEditablePlaylistDetailHeaderRenderer: MusicEditablePlaylistDetailHeaderRenderer? = null,
    val musicDescriptionShelfRenderer: MusicDescriptionShelfRenderer? = null,
    val itemSectionRenderer: ItemSectionRenderer? = null,
    val gridRenderer: GridRenderer? = null,
    val playlistPanelRenderer: PlaylistPanelRenderer? = null,
    val musicQueueRenderer: MusicQueueRenderer? = null,
    val playlistSongContents: List<PlaylistSongContent>? = null
)

@Serializable
data class SectionListHeader(
    val chipCloudRenderer: ChipCloudRenderer? = null,
    val musicCarouselShelfBasicHeaderRenderer: MusicCarouselShelfBasicHeaderRenderer? = null
)

@Serializable
data class ChipCloudRenderer(
    val chips: List<ChipCloudChipRenderer>? = null
)

@Serializable
data class ChipCloudChipRenderer(
    val text: TitleRuns? = null,
    val navigationEndpoint: NavigationEndpoint? = null
)

@Serializable
data class MusicCarouselShelfBasicHeaderRenderer(
    val title: TitleRuns? = null,
    val moreContentButton: ButtonRenderer? = null
)

@Serializable
data class TabRenderer(
    val tabRenderer: TabContent? = null
)

@Serializable
data class TabContent(
    val content: SectionListRenderer? = null,
    val endpoint: BrowseEndpoint? = null
)

@Serializable
data class Continuation(
    val nextContinuationData: NextContinuationData? = null
)

@Serializable
data class NextContinuationData(
    val continuation: String? = null,
    val clickTrackingParams: String? = null
)

@Serializable
data class MusicCarouselShelfRenderer(
    val header: MusicCarouselShelfBasicHeaderRenderer? = null,
    val contents: List<MusicCarouselShelfContent>? = null,
    val continuations: List<Continuation>? = null
)

@Serializable
data class MusicCarouselShelfContent(
    val musicTwoRowItemRenderer: MusicTwoRowItemRenderer? = null,
    val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer? = null,
    val musicNavigationButtonRenderer: MusicNavigationButtonRenderer? = null
)

@Serializable
data class MusicShelfRenderer(
    val title: TitleRuns? = null,
    val contents: List<MusicShelfContent>? = null,
    val continuations: List<Continuation>? = null
)

@Serializable
data class MusicShelfContent(
    val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer? = null
)

@Serializable
data class MusicPlaylistShelfRenderer(
    val title: TitleRuns? = null,
    val contents: List<MusicShelfContent>? = null,
    val continuations: List<Continuation>? = null
)

@Serializable
data class MusicDescriptionShelfRenderer(
    val description: DescriptionRuns? = null
)

@Serializable
data class ItemSectionRenderer(
    val contents: List<ItemSectionContent>? = null
)

@Serializable
data class ItemSectionContent(
    val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer? = null,
    val musicShelfRenderer: MusicShelfRenderer? = null,
    val gridRenderer: GridRenderer? = null
)

@Serializable
data class GridRenderer(
    val header: GridHeaderRenderer? = null,
    val items: List<GridItem>? = null,
    val continuations: List<Continuation>? = null
)

@Serializable
data class GridHeaderRenderer(
    val gridHeaderRenderer: GridHeaderContent? = null
)

@Serializable
data class GridHeaderContent(
    val title: TitleRuns? = null
)

@Serializable
data class GridItem(
    val musicTwoRowItemRenderer: MusicTwoRowItemRenderer? = null
)

@Serializable
data class PlaylistPanelRenderer(
    val contents: List<PlaylistPanelContent>? = null,
    val header: MusicQueueHeaderRenderer? = null,
    val continuations: List<Continuation>? = null
)

@Serializable
data class PlaylistPanelContent(
    val playlistPanelVideoRenderer: PlaylistPanelVideoRenderer? = null,
    val continuationItemRenderer: ContinuationItemRenderer? = null,
    val automixPreviewVideoRenderer: AutomixPreviewVideoRenderer? = null
)

@Serializable
data class MusicQueueRenderer(
    val header: MusicQueueHeaderRenderer? = null,
    val content: PlaylistPanelRenderer? = null
)

@Serializable
data class MusicQueueHeaderRenderer(
    val subtitle: TitleRuns? = null
)

@Serializable
data class PlaylistPanelVideoRenderer(
    val title: TitleRuns? = null,
    val subtitle: SubtitleRuns? = null,
    val thumbnailRenderer: ThumbnailRenderer? = null,
    val navigationEndpoint: NavigationEndpoint? = null,
    val overlay: Overlay? = null,
    val playlistItemData: PlaylistItemData? = null,
    val badges: List<Badge>? = null,
    val selected: Boolean = false
)

@Serializable
data class ContinuationItemRenderer(
    val continuationEndpoint: ContinuationEndpoint? = null
)

@Serializable
data class ContinuationEndpoint(
    val continuationCommand: ContinuationCommand? = null
)

@Serializable
data class ContinuationCommand(
    val token: String? = null
)

@Serializable
data class AutomixPreviewVideoRenderer(
    val content: AutomixPlaylistVideoRenderer? = null
)

@Serializable
data class AutomixPlaylistVideoRenderer(
    val navigationEndpoint: NavigationEndpoint? = null
)

@Serializable
data class PlaylistSongContent(
    val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer? = null
)

@Serializable
data class MusicTwoRowItemRenderer(
    val title: TitleRuns? = null,
    val subtitle: SubtitleRuns? = null,
    val thumbnailRenderer: ThumbnailRenderer? = null,
    val navigationEndpoint: NavigationEndpoint? = null,
    val subtitleBadges: List<Badge>? = null,
    val thumbnailOverlay: ThumbnailOverlay? = null,
    val isSong: Boolean = false,
    val isAlbum: Boolean = false
)

@Serializable
data class MusicResponsiveListItemRenderer(
    val title: TitleRuns? = null,
    val flexColumns: List<FlexColumn>? = null,
    val thumbnail: MusicThumbnailRenderer? = null,
    val overlay: Overlay? = null,
    val playlistItemData: PlaylistItemData? = null,
    val badges: List<Badge>? = null,
    val navigationEndpoint: NavigationEndpoint? = null
)

@Serializable
data class MusicNavigationButtonRenderer(
    val title: TitleRuns? = null,
    val thumbnail: MusicThumbnailRenderer? = null,
    val navigationEndpoint: NavigationEndpoint? = null
)

@Serializable
data class FlexColumn(
    val musicResponsiveListItemFlexColumnRenderer: FlexColumnRenderer? = null
)

@Serializable
data class FlexColumnRenderer(
    val text: TitleRuns? = null
)

@Serializable
data class TitleRuns(
    val runs: List<Run>? = null
)

@Serializable
data class SubtitleRuns(
    val runs: List<Run>? = null
)

@Serializable
data class DescriptionRuns(
    val runs: List<Run>? = null
)

@Serializable
data class Run(
    val text: String? = null,
    val navigationEndpoint: NavigationEndpoint? = null
)

@Serializable
data class MusicThumbnailRenderer(
    val thumbnails: List<Thumbnail>? = null
)

@Serializable
data class ThumbnailRenderer(
    val musicThumbnailRenderer: MusicThumbnailRenderer? = null
)

@Serializable
data class Thumbnail(
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null
)

@Serializable
data class NavigationEndpoint(
    val watchEndpoint: WatchEndpoint? = null,
    val watchPlaylistEndpoint: WatchEndpoint? = null,
    val browseEndpoint: BrowseEndpoint? = null
)

@Serializable
data class ButtonRenderer(
    val buttonRenderer: ButtonContent? = null,
    val musicPlayButtonRenderer: MusicPlayButtonRenderer? = null,
    val menuRenderer: MenuRenderer? = null,
    val navigationEndpoint: NavigationEndpoint? = null
)

@Serializable
data class ButtonContent(
    val navigationEndpoint: NavigationEndpoint? = null
)

@Serializable
data class MusicPlayButtonRenderer(
    val playNavigationEndpoint: NavigationEndpoint? = null
)

@Serializable
data class MenuRenderer(
    val items: List<MenuItem>? = null
)

@Serializable
data class MenuItem(
    val menuNavigationItemRenderer: MenuNavigationItemRenderer? = null
)

@Serializable
data class MenuNavigationItemRenderer(
    val icon: Icon? = null,
    val navigationEndpoint: NavigationEndpoint? = null
)

@Serializable
data class Icon(
    val iconType: String? = null
)

@Serializable
data class SubscriptionButton(
    val subscribeButtonRenderer: SubscribeButtonRenderer? = null
)

@Serializable
data class SubscribeButtonRenderer(
    val channelId: String? = null,
    val subscriberCountText: TitleRuns? = null,
    val subscriberCountWithSubscribeText: TitleRuns? = null
)

@Serializable
data class PlaylistItemData(
    val videoId: String? = null,
    val playlistId: String? = null,
    val setVideoId: String? = null
)

@Serializable
data class Badge(
    val musicInlineBadgeRenderer: MusicInlineBadgeRenderer? = null
)

@Serializable
data class MusicInlineBadgeRenderer(
    val icon: Icon? = null
)

@Serializable
data class Overlay(
    val musicItemThumbnailOverlayRenderer: MusicItemThumbnailOverlayRenderer? = null
)

@Serializable
data class MusicItemThumbnailOverlayRenderer(
    val content: MusicPlayButtonRenderer? = null
)

@Serializable
data class ThumbnailOverlay(
    val musicItemThumbnailOverlayRenderer: MusicItemThumbnailOverlayRenderer? = null
)

@Serializable
data class ContinuationContents(
    val sectionListContinuation: SectionListContinuation? = null,
    val musicShelfContinuation: MusicShelfContinuation? = null,
    val musicPlaylistShelfContinuation: MusicPlaylistShelfContinuation? = null,
    val gridContinuation: GridContinuation? = null,
    val playlistPanelContinuation: PlaylistPanelContinuation? = null
)

@Serializable
data class SectionListContinuation(
    val contents: List<SectionListContent>? = null,
    val continuations: List<Continuation>? = null
)

@Serializable
data class MusicShelfContinuation(
    val contents: List<MusicShelfContent>? = null,
    val continuations: List<Continuation>? = null
)

@Serializable
data class MusicPlaylistShelfContinuation(
    val contents: List<MusicShelfContent>? = null,
    val continuations: List<Continuation>? = null
)

@Serializable
data class GridContinuation(
    val items: List<GridItem>? = null,
    val continuations: List<Continuation>? = null
)

@Serializable
data class PlaylistPanelContinuation(
    val contents: List<PlaylistPanelContent>? = null,
    val continuations: List<Continuation>? = null
)

@Serializable
data class OnResponseReceivedAction(
    val appendContinuationItemsAction: AppendContinuationItemsAction? = null
)

@Serializable
data class AppendContinuationItemsAction(
    val continuationItems: List<ContinuationItem>? = null
)

@Serializable
data class ContinuationItem(
    val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer? = null,
    val continuationItemRenderer: ContinuationItemRenderer? = null
)

@Serializable
data class TabbedSearchResultsRenderer(
    val tabs: List<TabRenderer>? = null
)

@Serializable
data class SearchResponse(
    val contents: BrowseContents? = null
)

@Serializable
data class GetSearchSuggestionsResponse(
    val contents: List<SearchSuggestionsSectionRenderer>? = null
)

@Serializable
data class SearchSuggestionsSectionRenderer(
    val contents: List<SearchSuggestionContent>? = null
)

@Serializable
data class SearchSuggestionContent(
    val searchSuggestionRenderer: SearchSuggestionRenderer? = null,
    val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer? = null
)

@Serializable
data class SearchSuggestionRenderer(
    val suggestion: TitleRuns? = null
)

@Serializable
data class NextResponse(
    val contents: NextContents? = null,
    val continuationContents: ContinuationContents? = null
)

@Serializable
data class NextContents(
    val singleColumnMusicWatchNextResultsRenderer: SingleColumnMusicWatchNextResultsRenderer? = null
)

@Serializable
data class SingleColumnMusicWatchNextResultsRenderer(
    val tabbedRenderer: TabbedRenderer? = null
)

@Serializable
data class TabbedRenderer(
    val watchNextTabbedResultsRenderer: WatchNextTabbedResultsRenderer? = null
)

@Serializable
data class WatchNextTabbedResultsRenderer(
    val tabs: List<TabRenderer>? = null
)

@Serializable
data class PlayerResponse(
    val streamingData: StreamingData? = null,
    val playabilityStatus: PlayabilityStatus? = null,
    val videoDetails: VideoDetails? = null,
    val trackingParams: String? = null
)

@Serializable
data class StreamingData(
    val formats: List<Format>? = null,
    val adaptiveFormats: List<Format>? = null,
    val hlsManifestUrl: String? = null,
    val dashManifestUrl: String? = null
)

@Serializable
data class Format(
    val url: String? = null,
    val mimeType: String? = null,
    val bitrate: Long? = null,
    val audioQuality: String? = null,
    val approxDurationMs: String? = null,
    val signatureCipher: String? = null,
    val signature: String? = null
)

@Serializable
data class PlayabilityStatus(
    val status: String? = null,
    val reason: String? = null,
    val errorScreen: ErrorScreen? = null
)

@Serializable
data class ErrorScreen(
    val playerErrorMessageRenderer: PlayerErrorMessageRenderer? = null
)

@Serializable
data class PlayerErrorMessageRenderer(
    val reason: TitleRuns? = null
)

@Serializable
data class VideoDetails(
    val videoId: String? = null,
    val title: String? = null,
    val author: String? = null,
    val lengthSeconds: String? = null,
    val thumbnail: MusicThumbnailRenderer? = null,
    val isLiveContent: Boolean = false
)

@Serializable
data class GetQueueResponse(
    val queueDatas: List<QueueData>? = null
)

@Serializable
data class QueueData(
    val content: QueueContent? = null
)

@Serializable
data class QueueContent(
    val playlistPanelVideoRenderer: PlaylistPanelVideoRenderer? = null
)

@Serializable
data class GetTranscriptResponse(
    val actions: List<TranscriptAction>? = null
)

@Serializable
data class TranscriptAction(
    val updateEngagementPanelAction: UpdateEngagementPanelAction? = null
)

@Serializable
data class UpdateEngagementPanelAction(
    val content: TranscriptContent? = null
)

@Serializable
data class TranscriptContent(
    val transcriptRenderer: TranscriptRenderer? = null
)

@Serializable
data class TranscriptRenderer(
    val body: TranscriptBodyRenderer? = null
)

@Serializable
data class TranscriptBodyRenderer(
    val cueGroups: List<CueGroup>? = null
)

@Serializable
data class CueGroup(
    val transcriptCueGroupRenderer: TranscriptCueGroupRenderer? = null
)

@Serializable
data class TranscriptCueGroupRenderer(
    val cues: List<Cue>? = null
)

@Serializable
data class Cue(
    val transcriptCueRenderer: TranscriptCueRenderer? = null
)

@Serializable
data class TranscriptCueRenderer(
    val startOffsetMs: Long? = null,
    val cue: CueContent? = null
)

@Serializable
data class CueContent(
    val simpleText: String? = null
)

@Serializable
data class AccountMenuResponse(
    val actions: List<AccountAction>? = null
)

@Serializable
data class AccountAction(
    val openPopupAction: OpenPopupAction? = null
)

@Serializable
data class OpenPopupAction(
    val popup: Popup? = null
)

@Serializable
data class Popup(
    val multiPageMenuRenderer: MultiPageMenuRenderer? = null
)

@Serializable
data class MultiPageMenuRenderer(
    val header: MenuHeader? = null
)

@Serializable
data class MenuHeader(
    val activeAccountHeaderRenderer: ActiveAccountHeaderRenderer? = null
)

@Serializable
data class ActiveAccountHeaderRenderer(
    val accountName: TitleRuns? = null,
    val email: TitleRuns? = null,
    val accountPhoto: MusicThumbnailRenderer? = null,
    val dataSyncId: String? = null,
    val channelId: String? = null
)

@Serializable
data class AddItemYouTubePlaylistResponse(
    val status: String? = null,
    val playlistEditResults: List<PlaylistEditResult>? = null
)

@Serializable
data class PlaylistEditResult(
    val playlistEditVideoAddedResultData: PlaylistEditVideoAddedResultData? = null
)

@Serializable
data class PlaylistEditVideoAddedResultData(
    val videoId: String? = null,
    val setVideoId: String? = null
)

@Serializable
data class CreatePlaylistResponse(
    val playlistId: String? = null
)

@Serializable
data class EditPlaylistResponse(
    val newHeader: MusicEditablePlaylistDetailHeaderRenderer? = null
)

@Serializable
data class ImageUploadResponse(
    val encryptedBlobId: String? = null
)

@Serializable
data class NextResultResponse(
    val title: String? = null,
    val items: List<SongItem>? = null,
    val currentIndex: Int? = null,
    val lyricsEndpoint: BrowseEndpoint? = null,
    val relatedEndpoint: BrowseEndpoint? = null,
    val continuation: String? = null,
    val endpoint: WatchEndpoint? = null
)