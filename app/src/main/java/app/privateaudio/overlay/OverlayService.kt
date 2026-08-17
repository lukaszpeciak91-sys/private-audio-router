package app.privateaudio.overlay

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import app.privateaudio.R

/** Owns only the floating window. Routing remains exclusively service-owned elsewhere. */
class OverlayService : Service() {
    private val windowManager by lazy { getSystemService(WindowManager::class.java) }
    private var overlayView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW -> showOverlay()
            ACTION_HIDE -> {
                hideOverlay()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        hideOverlay()
        super.onDestroy()
    }

    private fun showOverlay() {
        if (overlayView != null || !Settings.canDrawOverlays(this)) return

        val density = resources.displayMetrics.density
        val surface = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding((16 * density).toInt(), (12 * density).toInt(), (16 * density).toInt(), (12 * density).toInt())
            setBackgroundColor(Color.rgb(38, 38, 38))
            addView(TextView(context).apply {
                setText(R.string.overlay_test_label)
                setTextColor(Color.WHITE)
            })
            addView(Button(context).apply {
                setText(R.string.overlay_close)
                setOnClickListener {
                    hideOverlay()
                    stopSelf()
                }
            })
        }
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = (16 * density).toInt()
            y = (96 * density).toInt()
        }
        try {
            windowManager.addView(surface, layoutParams)
            overlayView = surface
        } catch (_: SecurityException) {
            // The user can revoke the grant between the permission check and this call.
            stopSelf()
        }
    }

    private fun hideOverlay() {
        overlayView?.let(windowManager::removeView)
        overlayView = null
    }

    companion object {
        private const val ACTION_SHOW = "app.privateaudio.overlay.SHOW"
        private const val ACTION_HIDE = "app.privateaudio.overlay.HIDE"

        fun showIntent(context: Context) = Intent(context, OverlayService::class.java).setAction(ACTION_SHOW)
        fun hideIntent(context: Context) = Intent(context, OverlayService::class.java).setAction(ACTION_HIDE)
    }
}
