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
import android.util.SparseArray
import android.view.View
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
        private val sDisplayMode: SparseArray<String> = SparseArray()

        init {
            sDisplayMode.put(DISPLAY_MODE_NORMAL, "NORMAL")
            sDisplayMode.put(DISPLAY_MODE_GLASS, "GLASS")
        }

        private val sInteractiveMode = SparseArray<String>()

        init {
            sInteractiveMode.put(INTERACTIVE_MODE_MOTION, "MOTION")
            sInteractiveMode.put(INTERACTIVE_MODE_TOUCH, "TOUCH")
            sInteractiveMode.put(INTERACTIVE_MODE_MOTION_WITH_TOUCH, "M & T")
            sInteractiveMode.put(INTERACTIVE_MODE_CARDBORAD_MOTION, "CARDBOARD M")
            sInteractiveMode.put(INTERACTIVE_MODE_CARDBORAD_MOTION_WITH_TOUCH, "CARDBOARD M&T")
        }

        private val sProjectionMode = SparseArray<String>()

        init {
            sProjectionMode.put(PROJECTION_MODE_SPHERE, "SPHERE")
            sProjectionMode.put(PROJECTION_MODE_DOME180, "DOME 180")
            sProjectionMode.put(PROJECTION_MODE_DOME230, "DOME 230")
            sProjectionMode.put(PROJECTION_MODE_DOME180_UPPER, "DOME 180 UPPER")
            sProjectionMode.put(PROJECTION_MODE_DOME230_UPPER, "DOME 230 UPPER")
            sProjectionMode.put(PROJECTION_MODE_STEREO_SPHERE_HORIZONTAL, "STEREO H SPHERE")
            sProjectionMode.put(PROJECTION_MODE_STEREO_SPHERE_VERTICAL, "STEREO V SPHERE")
            sProjectionMode.put(PROJECTION_MODE_PLANE_FIT, "PLANE FIT")
            sProjectionMode.put(PROJECTION_MODE_PLANE_CROP, "PLANE CROP")
            sProjectionMode.put(PROJECTION_MODE_PLANE_FULL, "PLANE FULL")
            sProjectionMode.put(
                PROJECTION_MODE_MULTI_FISH_EYE_HORIZONTAL, "MULTI FISH EYE HORIZONTAL"
            )
            sProjectionMode.put(PROJECTION_MODE_MULTI_FISH_EYE_VERTICAL, "MULTI FISH EYE VERTICAL")
            sProjectionMode.put(
                CustomProjectionFactory.CUSTOM_PROJECTION_FISH_EYE_RADIUS_VERTICAL,
                "CUSTOM MULTI FISH EYE"
            )
        }

        private val sAntiDistortion = SparseArray<String>()

        init {
            sAntiDistortion.put(1, "ANTI-ENABLE")
            sAntiDistortion.put(0, "ANTI-DISABLE")
        }

        private val sPitchFilter = SparseArray<String>()

        init {
            sPitchFilter.put(1, "FILTER PITCH")
            sPitchFilter.put(0, "FILTER NOP")
        }

        private val sFlingEnabled = SparseArray<String>()

        init {
            sFlingEnabled.put(1, "FLING ENABLED")
            sFlingEnabled.put(0, "FLING DISABLED")
        }

        fun startVideo(context: Context, uri: Uri) {
            val i = Intent(context, VideoPlayerActivity::class.java)
            i.data = uri
            context.startActivity(i)
        }
    }

    private var mVRLibrary: MDVRLibrary? = null

    private val plugins: MutableList<MDAbsPlugin> = LinkedList()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        hideSystemUi()
        super.onCreate(savedInstanceState)
        // set content view
        setContentView(R.layout.activity_base_video_player)

        // init VR Library
        mVRLibrary = createVRLibrary()
        val activity: Activity = this
        val hotspotPoints: MutableList<View> = LinkedList()
        hotspotPoints.add(findViewById(R.id.hotspot_point1))
        hotspotPoints.add(findViewById(R.id.hotspot_point2))
        SpinnerHelper.with(this).setData(sDisplayMode).setDefault(mVRLibrary!!.displayMode)
            .setClickHandler(object : SpinnerHelper.ClickHandler {
                override fun onSpinnerClicked(index: Int, key: Int, value: String?) {
                    mVRLibrary!!.switchDisplayMode(this@BaseVideoPlayerActivity, key)
                    val size = if (key == DISPLAY_MODE_GLASS) 2 else 1
                    for ((i, point) in hotspotPoints.withIndex()) {
                        point.visibility = if (i < size) View.VISIBLE else View.GONE
                    }
                }
            }).init(R.id.spinner_display)
        SpinnerHelper.with(this).setData(sInteractiveMode).setDefault(mVRLibrary!!.interactiveMode)
            .setClickHandler(object : SpinnerHelper.ClickHandler {
                override fun onSpinnerClicked(index: Int, key: Int, value: String?) {
                    mVRLibrary!!.switchInteractiveMode(this@BaseVideoPlayerActivity, key)
                }
            }).init(R.id.spinner_interactive)
        SpinnerHelper.with(this).setData(sProjectionMode).setDefault(mVRLibrary!!.projectionMode)
            .setClickHandler(object : SpinnerHelper.ClickHandler {
                override fun onSpinnerClicked(index: Int, key: Int, value: String?) {
                    mVRLibrary!!.switchProjectionMode(this@BaseVideoPlayerActivity, key)
                }
            }).init(R.id.spinner_projection)
        SpinnerHelper.with(this).setData(sAntiDistortion)
            .setDefault(if (mVRLibrary!!.isAntiDistortionEnabled) 1 else 0)
            .setClickHandler(object : SpinnerHelper.ClickHandler {
                override fun onSpinnerClicked(index: Int, key: Int, value: String?) {
                    mVRLibrary!!.isAntiDistortionEnabled = key != 0
                }
            }).init(R.id.spinner_distortion)
        findViewById<View>(R.id.button_remove_plugin).setOnClickListener {
            if (plugins.size > 0) {
                val plugin = plugins.removeAt(plugins.size - 1)
                getVRLibrary()!!.removePlugin(plugin)
            }
        }
        findViewById<View>(R.id.button_remove_plugins).setOnClickListener {
            plugins.clear()
            getVRLibrary()!!.removePlugins()
        }
        findViewById<View>(R.id.button_rotate_to_camera_plugin).setOnClickListener {
            val hotspot = getVRLibrary()!!.findHotspotByTag("tag-front")
            hotspot?.rotateToCamera()
        }
        findViewById<View>(R.id.button_add_md_view).setOnClickListener {
            val textView = TextView(activity)
            textView.setBackgroundColor(0x55FFCC11)
            textView.text = "Hello world."
            val builder =
                MDViewBuilder.create().provider(textView, 400 /*view width*/, 100 /*view height*/)
                    .size(4f, 1f).position(MDPosition.newInstance().setZ(-12.0f)).title("md view")
                    .tag("tag-md-text-view")
            val mdView: MDAbsView = MDView(builder)
            plugins.add(mdView)
            getVRLibrary()!!.addPlugin(mdView)
        }
        findViewById<View>(R.id.button_update_md_view).setOnClickListener {
            val mdView = getVRLibrary()!!.findViewByTag("tag-md-text-view")
            if (mdView != null) {
                val textView = mdView.castAttachedView(TextView::class.java)
                textView.text = "Cheer up!"
                textView.setBackgroundColor(-0x77ff0100)
                mdView.invalidate()
            }
        }
        val hotspotText = findViewById<View>(R.id.hotspot_text) as TextView
        val directorBriefText = findViewById<View>(R.id.director_brief_text) as TextView
        getVRLibrary()!!.setEyePickChangedListener { hitEvent ->
            val hotspot = hitEvent.hotspot
            val hitTimestamp = hitEvent.timestamp
            val text = if (hotspot == null) "nop" else String.format(
                Locale.CHINESE,
                "%s  %fs",
                hotspot.title,
                (System.currentTimeMillis() - hitTimestamp) / 1000.0f
            )
            hotspotText.text = text
            val brief = getVRLibrary()!!.directorBrief.toString()
            directorBriefText.text = brief
            if (System.currentTimeMillis() - hitTimestamp > 5000) {
                getVRLibrary()!!.resetEyePick()
            }
        }
        findViewById<View>(R.id.button_camera_little_planet).setOnClickListener {
            val cameraUpdate = getVRLibrary()!!.updateCamera()
            val near = PropertyValuesHolder.ofFloat("near", cameraUpdate.nearScale, -0.5f)
            val eyeZ = PropertyValuesHolder.ofFloat("eyeZ", cameraUpdate.eyeZ, 18f)
            val pitch = PropertyValuesHolder.ofFloat("pitch", cameraUpdate.pitch, 90f)
            val yaw = PropertyValuesHolder.ofFloat("yaw", cameraUpdate.yaw, 90f)
            val roll = PropertyValuesHolder.ofFloat("roll", cameraUpdate.roll, 0f)
            startCameraAnimation(cameraUpdate, near, eyeZ, pitch, yaw, roll)
        }
        findViewById<View>(R.id.button_camera_to_normal).setOnClickListener {
            val cameraUpdate = getVRLibrary()!!.updateCamera()
            val near = PropertyValuesHolder.ofFloat("near", cameraUpdate.nearScale, 0f)
            val eyeZ = PropertyValuesHolder.ofFloat("eyeZ", cameraUpdate.eyeZ, 0f)
            val pitch = PropertyValuesHolder.ofFloat("pitch", cameraUpdate.pitch, 0f)
            val yaw = PropertyValuesHolder.ofFloat("yaw", cameraUpdate.yaw, 0f)
            val roll = PropertyValuesHolder.ofFloat("roll", cameraUpdate.roll, 0f)
            startCameraAnimation(cameraUpdate, near, eyeZ, pitch, yaw, roll)
        }
        SpinnerHelper.with(this).setData(sPitchFilter).setDefault(0)
            .setClickHandler(object : SpinnerHelper.ClickHandler {
                override fun onSpinnerClicked(index: Int, key: Int, value: String?) {
                    val filter: IDirectorFilter? =
                        if (key == 0) null else object : DirectorFilterAdatper() {
                            override fun onFilterPitch(input: Float): Float {
                                if (input > 70) {
                                    return 70F
                                }
                                return if (input < -70) {
                                    (-70).toFloat()
                                } else input
                            }
                        }
                    getVRLibrary()!!.setDirectorFilter(filter)
                }
            }).init(R.id.spinner_pitch_filter)
        SpinnerHelper.with(this).setData(sFlingEnabled)
            .setDefault(if (getVRLibrary()!!.isFlingEnabled) 1 else 0)
            .setClickHandler(object : SpinnerHelper.ClickHandler {
                override fun onSpinnerClicked(index: Int, key: Int, value: String?) {
                    getVRLibrary()!!.isFlingEnabled = key == 1
                }
            }).init(R.id.spinner_fling_enable)
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

    private var animator: ValueAnimator? = null

    private fun startCameraAnimation(
        cameraUpdate: MDDirectorCamUpdate, vararg values: PropertyValuesHolder
    ) {
        if (animator != null) {
            animator!!.cancel()
        }
        animator = ValueAnimator.ofPropertyValuesHolder(*values).setDuration(2000)
        animator?.addUpdateListener { animation ->
            val near = animation.getAnimatedValue("near") as Float
            val eyeZ = animation.getAnimatedValue("eyeZ") as Float
            val pitch = animation.getAnimatedValue("pitch") as Float
            val yaw = animation.getAnimatedValue("yaw") as Float
            val roll = animation.getAnimatedValue("roll") as Float
            cameraUpdate.setEyeZ(eyeZ).setNearScale(near).setPitch(pitch).setYaw(yaw).roll = roll
        }
        animator?.start()
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