package com.musicwave.playback

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.session.MediaButtonReceiver
import com.musicwave.MainActivity
import com.musicwave.data.model.SongItem
import dagger.hilt.android.AndroidEntryPoint
import android.app.Service
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import androidx.media3.common.*
import androidx.media3.exoplayer.*
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.CopyOnWriteArrayList

@AndroidEntryPoint
class MusicPlaybackService : MediaSessionService(), AudioManager.OnAudioFocusChangeListener {

    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val queueManager = QueueManager()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentMediaItem = MutableStateFlow<MediaItem?>(null)
    val currentMediaItem: StateFlow<MediaItem?> = _currentMediaItem.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _shuffleMode = MutableStateFlow(false)
    val shuffleMode: StateFlow<Boolean> = _shuffleMode.asStateFlow()
    
    private val _repeatMode = MutableStateFlow(REPEAT_MODE_NONE)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val positionUpdateJob = serviceScope.launch {
        while (isActive) {
            mediaSession?.controller?.playbackState?.currentPosition?.let { position ->
                _currentPosition.value = position
            }
            delay(1000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        
        val sessionActivityPendingIntent = packageManager
            .getLaunchIntentForPackage(packageName)
            ?.let { sessionIntent ->
                PendingIntent.getActivity(
                    this,
                    0,
                    sessionIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }

        mediaSession = MediaSession.Builder(this)
            .setSessionActivity(sessionActivityPendingIntent)
            .setCallback(MediaSessionCallback())
            .build()
            
        setupAudioFocus()
    }

    private fun setupAudioFocus() {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setOnAudioFocusChangeListener(this)
            .build()
        
        audioManager.requestAudioFocus(audioFocusRequest)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession?.controller?.sessionBinder?.asBinder()?.let { null }, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                mediaSession?.player?.playWhenReady = false
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                mediaSession?.player?.playWhenReady = false
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mediaSession?.player?.volume = 0.3f
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaSession?.player?.playWhenReady = true
                mediaSession?.player?.volume = 1f
            }
        }
    }

    inner class MediaSessionCallback : MediaSession.Callback {
        override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
            val mediaButtonIntent = MediaButtonReceiver.handleIntent(null, mediaButtonIntent)
            return super.onMediaButtonEvent(mediaButtonIntent)
        }
    }

    companion object {
        const val ACTION_PLAY = "com.musicwave.action.PLAY"
        const val ACTION_PAUSE = "com.musicwave.action.PAUSE"
        const val ACTION_NEXT = "com.musicwave.action.NEXT"
        const val ACTION_PREVIOUS = "com.musicwave.action.PREVIOUS"
        const val ACTION_SHUFFLE = "com.musicwave.action.SHUFFLE"
        const val ACTION_REPEAT = "com.musicwave.action.REPEAT"
        const val ACTION_SEEK_TO = "com.musicwave.action.SEEK_TO"
        const val EXTRA_MEDIA_ITEMS = "com.musicwave.extra.MEDIA_ITEMS"
        const val EXTRA_CURRENT_INDEX = "com.musicwave.extra.CURRENT_INDEX"
        const val EXTRA_SEEK_POSITION = "com.musicwave.extra.SEEK_POSITION"
    }
}

class QueueManager {
    private val queue = CopyOnWriteArrayList<SongItem>()
    private var currentIndex = -1
    
    val currentSong: SongItem?
        get() = queue.getOrNull(currentIndex)
    
    val queueSize: Int
        get() = queue.size
    
    fun setQueue(items: List<SongItem>, startIndex: Int = 0) {
        queue.clear()
        queue.addAll(items)
        currentIndex = startIndex
    }
    
    fun addToQueue(item: SongItem) {
        queue.add(item)
    }
    
    fun addToQueue(items: List<SongItem>) {
        queue.addAll(items)
    }
    
    fun removeFromQueue(index: Int) {
        if (index in queue.indices) {
            queue.removeAt(index)
            if (index < currentIndex) {
                currentIndex--
            } else if (index == currentIndex) {
                currentIndex = currentIndex.coerceAtMost(queue.size - 1)
            }
        }
    }
    
    fun clear() {
        queue.clear()
        currentIndex = -1
    }
    
    fun hasNext(): Boolean = currentIndex < queue.size - 1
    
    fun hasPrevious(): Boolean = currentIndex > 0
    
    fun next(): SongItem? {
        if (currentIndex < queue.size - 1) {
            currentIndex++
            return currentSong
        }
        return null
    }
    
    fun previous(): SongItem? {
        if (currentIndex > 0) {
            currentIndex--
            return currentSong
        }
        return null
    }
    
    fun setCurrentIndex(index: Int) {
        if (index in queue.indices) {
            currentIndex = index
        }
    }
    
    fun getQueue(): List<SongItem> = queue.toList()
    
    fun getCurrentIndex(): Int = currentIndex
}