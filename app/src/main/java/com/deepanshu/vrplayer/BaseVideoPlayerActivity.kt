package com.deepanshu.vrplayer

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.asha.vrlib.MDDirectorCamUpdate
import com.asha.vrlib.MDVRLibrary
import com.asha.vrlib.MDVRLibrary.*
import com.asha.vrlib.model.MDPosition
import com.asha.vrlib.model.MDViewBuilder
import com.asha.vrlib.plugins.MDAbsPlugin
import com.asha.vrlib.plugins.hotspot.MDAbsView
import com.asha.vrlib.plugins.hotspot.MDView
import java.util.*

abstract class BaseVideoPlayerActivity : AppCompatActivity() {
    companion object {
        fun startVideo(context: Context, uri: Uri) {
            val i = Intent(context, VideoPlayerActivity::class.java)
            i.data = uri
            context.startActivity(i)
        }
    }

    private var mVRLibrary: MDVRLibrary? = null

    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        hideSystemUi()
        super.onCreate(savedInstanceState)
        // set content view
        setContentView(R.layout.activity_base_video_player)

        // init VR Library
        mVRLibrary = createVRLibrary()
        //mVRLibrary!!.isAntiDistortionEnabled = false
        //mVRLibrary!!.switchDisplayMode(this@BaseVideoPlayerActivity, MDVRLibrary.DISPLAY_MODE_NORMAL)
        //mVRLibrary!!.switchInteractiveMode(this@BaseVideoPlayerActivity, MDVRLibrary.INTERACTIVE_MODE_MOTION_WITH_TOUCH)
        mVRLibrary!!.switchProjectionMode(this@BaseVideoPlayerActivity, MDVRLibrary.PROJECTION_MODE_SPHERE)

        val crossBtn = findViewById<ImageView>(R.id.ivCross)
        val glassBtn = findViewById<ImageView>(R.id.ivCardBoard)
        val camBtn = findViewById<ImageView>(R.id.ivCameras)
        crossBtn.setOnClickListener {
            glassBtn.visibility = View.VISIBLE
            camBtn.visibility = View.VISIBLE
            crossBtn.visibility = View.GONE
            mVRLibrary!!.switchDisplayMode(this@BaseVideoPlayerActivity, MDVRLibrary.DISPLAY_MODE_NORMAL)
            mVRLibrary!!.isAntiDistortionEnabled = false
        }

        glassBtn.setOnClickListener {
            crossBtn.visibility = View.VISIBLE
            glassBtn.visibility = View.GONE
            camBtn.visibility = View.GONE
            mVRLibrary!!.switchDisplayMode(this@BaseVideoPlayerActivity, MDVRLibrary.DISPLAY_MODE_GLASS)
            mVRLibrary!!.isAntiDistortionEnabled = true
        }

    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, findViewById(R.id.gl_view)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    protected abstract fun createVRLibrary(): MDVRLibrary?

    fun getVRLibrary(): MDVRLibrary? {
        return mVRLibrary
    }

    override fun onResume() {
        super.onResume()
        mVRLibrary!!.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        mVRLibrary!!.onPause(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mVRLibrary!!.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mVRLibrary!!.onOrientationChanged(this)
    }

    protected fun getUri(): Uri? {
        val i = intent
        return if (i == null || i.data == null) {
            null
        } else i.data
    }

    fun cancelBusy() {
        findViewById<View>(R.id.progress).visibility = View.GONE
    }

}