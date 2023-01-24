package com.deepanshu.vrplayer

import android.net.Uri
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Surface
import android.widget.Toast
import com.asha.vrlib.MD360Director
import com.asha.vrlib.MD360DirectorFactory
import com.asha.vrlib.MDVRLibrary
import com.asha.vrlib.MDVRLibrary.DirectorFilterAdatper
import com.asha.vrlib.model.BarrelDistortionConfig
import com.asha.vrlib.model.MDPinchConfig
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.DefaultHlsDataSourceFactory
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource

class VideoPlayerActivity : BaseVideoPlayerActivity() {

    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isSupportedMultiWindow()) {
            initializePlayer()
        }
    }

    override fun createVRLibrary(): MDVRLibrary? {
        return MDVRLibrary.with(this).displayMode(MDVRLibrary.DISPLAY_MODE_NORMAL)
            .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_MOTION).asVideo { surface ->
                Handler(Looper.getMainLooper()).post { setSurface(surface) }
            }.ifNotSupport {
                val tip =
                    if (it == MDVRLibrary.INTERACTIVE_MODE_MOTION) "onNotSupport:MOTION" else "onNotSupport:$it"
                Toast.makeText(this@VideoPlayerActivity, tip, Toast.LENGTH_SHORT).show()
            }.pinchConfig(MDPinchConfig().setMin(1.0f).setMax(8.0f).setDefaultValue(0.1f))
            .pinchEnabled(true).directorFactory(object : MD360DirectorFactory() {
                override fun createDirector(p0: Int): MD360Director {
                    return MD360Director.builder().setPitch(90F).build()
                }
            }).projectionFactory(CustomProjectionFactory()).barrelDistortionConfig(
                BarrelDistortionConfig().setParamA(-0.068).setParamB(0.320000).setParamC(-0.2).setDefaultEnabled(false)
                    .setScale(0.80f)
            )
            .build(findViewById<GLSurfaceView>(R.id.gl_view))

        // ParamA is for distance in views.
        // ParamB is for shape or radius.
        // ParamC is for
    }

    override fun onResume() {
        super.onResume()
        if (player != null) {
            playVideo()
        } else if ((isSupportedMultiWindow() || player == null)) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (!isSupportedMultiWindow()) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (isSupportedMultiWindow()) {
            releasePlayer()
        }
    }

    private fun isSupportedMultiWindow() = Build.VERSION.SDK_INT > 23

    private fun mediaItem(uri: Uri?): MediaItem {
        return MediaItem.Builder().setUri(uri).build()
    }

    private fun mediaSource(uriTypeHls: Boolean): MediaSource.Factory {
        val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(this)
        return if (uriTypeHls)  DefaultMediaSourceFactory(this).setDataSourceFactory(dataSourceFactory)
        else ProgressiveMediaSource.Factory(dataSourceFactory)
    }

    private fun initializePlayer() {
        val uri = getUri()
        val urlTypeHls = uri?.path?.contains(".m3u8");
        if (player == null) {
            val builder = ExoPlayer.Builder(this).setMediaSourceFactory(mediaSource(urlTypeHls!!))
            player = builder.build()
        }
        player?.repeatMode = Player.REPEAT_MODE_ALL
        if (uri != null) {
            player?.setMediaItem(mediaItem(uri))
            if (getVRLibrary() != null) {
                getVRLibrary()!!.notifyPlayerChanged()
            }
        }
    }

    private fun setSurface(surface: Surface) {
        if (player != null) {
            player?.setVideoSurface(surface)
        }
    }

    private fun playVideo() {
        cancelBusy()
        player?.prepare()
        player?.play()
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }
}