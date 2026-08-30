package com.musicwave.data.api

import kotlinx.serialization.Serializable

@Serializable
data class PlaybackAuthState(
    val visitorData: String? = null,
    val dataSyncId: String? = null,
    val cookie: String? = null,
    val poToken: String? = null,
    val poTokenPlayer: String? = null,
    val poTokenGvs: String? = null,
    val poTokenGvsSession: String? = null,
    val poTokenGvsVideoId: String? = null,
    val webClientPoTokenEnabled: Boolean = false
) {
    val hasLoginCookie: Boolean
        get() = cookie != null || poToken != null

    val hasPlaybackLoginContext: Boolean
        get() = (cookie != null && dataSyncId != null) || poToken != null

    val fingerprint: String
        get() = "$cookie|$dataSyncId|$poToken|$poTokenPlayer|$poTokenGvs|$poTokenGvsSession|$poTokenGvsVideoId"

    fun normalized(): PlaybackAuthState = copy(
        poToken = poToken?.trim().takeIf { it.isNotEmpty() },
        poTokenPlayer = poTokenPlayer?.trim().takeIf { it.isNotEmpty() },
        poTokenGvs = poTokenGvs?.trim().takeIf { it.isNotEmpty() },
        poTokenGvsSession = poTokenGvsSession?.trim().takeIf { it.isNotEmpty() },
        poTokenGvsVideoId = poTokenGvsVideoId?.trim().takeIf { it.isNotEmpty() }
    )

    fun resolvePlayerPoToken(
        client: YouTubeClient,
        explicitPoToken: String?,
        videoId: String
    ): String? {
        if (explicitPoToken != null && explicitPoToken.isNotEmpty()) return explicitPoToken
        if (!hasPlaybackLoginContext) return null
        if (client.loginSupported) return poTokenPlayer
        return poToken
    }

    fun resolveGvsPoToken(
        client: YouTubeClient? = null,
        videoId: String? = null
    ): String? {
        val token = poTokenGvs ?: poTokenGvsSession
        if (token == null) return null
        if (videoId != null && poTokenGvsVideoId != null && poTokenGvsVideoId != videoId) return null
        return token
    }

    fun resolveSubsPoToken(client: YouTubeClient, videoId: String): String? {
        return if (client.loginSupported) poToken else poTokenPlayer
    }

    companion object {
        val EMPTY = PlaybackAuthState()
    }
}