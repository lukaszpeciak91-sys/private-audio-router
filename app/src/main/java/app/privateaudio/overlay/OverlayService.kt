package app.privateaudio.overlay

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import app.privateaudio.MainActivity
import app.privateaudio.PrivateAudioService
import app.privateaudio.PrivateAudioState
import app.privateaudio.R
import kotlin.math.min

/** Owns only the floating window. Routing remains exclusively service-owned elsewhere. */
class OverlayService : Service() {
    private val windowManager by lazy { getSystemService(WindowManager::class.java) }
    private var overlayView: FloatingControllerView? = null
    private var privateAudioService: PrivateAudioService? = null
    private var isBound = false
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            privateAudioService = (binder as PrivateAudioService.LocalBinder).service
            overlayView?.beginStateObservation()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            privateAudioService = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW -> showOverlay()
            ACTION_HIDE -> closeOverlay()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        hideOverlay()
        super.onDestroy()
    }

    private fun showOverlay() {
        if (overlayView != null || !Settings.canDrawOverlays(this)) return
        bindControllerService()
        val surface = FloatingControllerView(this)
        val density = resources.displayMetrics.density
        val layoutParams = WindowManager.LayoutParams(
            (300 * density).toInt(),
            (62 * density).toInt(),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.CENTER
        }
        try {
            windowManager.addView(surface, layoutParams)
            overlayView = surface
            surface.beginStateObservation()
        } catch (_: SecurityException) {
            // The user can revoke the grant between the permission check and this call.
            unbindControllerService()
            stopSelf()
        }
    }

    private fun bindControllerService() {
        if (!isBound) {
            isBound = bindService(
                Intent(this, PrivateAudioService::class.java),
                serviceConnection,
                Context.BIND_AUTO_CREATE,
            )
        }
    }

    private fun hideOverlay() {
        overlayView?.stopStateObservation()
        overlayView?.let(windowManager::removeView)
        overlayView = null
        unbindControllerService()
    }

    private fun closeOverlay() {
        hideOverlay()
        stopSelf()
    }

    private fun togglePower() {
        val controller = privateAudioService ?: return
        if (controller.privateAudioState == PrivateAudioState.READY) {
            startForegroundService(
                Intent(this, PrivateAudioService::class.java)
                    .setAction(PrivateAudioService.ACTION_ARM),
            )
        } else {
            controller.disarmAndStopStartedLifetime()
        }
    }

    private fun expandMain() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
        )
    }

    private fun unbindControllerService() {
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
        privateAudioService = null
    }

    private inner class FloatingControllerView(context: Context) : View(context) {
        private val density = resources.displayMetrics.density
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        private val expandPath = Path()
        private var state = PrivateAudioState.READY
        private val refreshState = object : Runnable {
            override fun run() {
                val latest = privateAudioService?.privateAudioState ?: PrivateAudioState.READY
                if (latest != state) {
                    state = latest
                    contentDescription = stateDescription(latest)
                    invalidate()
                }
                postDelayed(this, STATE_REFRESH_MILLIS)
            }
        }

        init {
            isClickable = true
            contentDescription = stateDescription(state)
        }

        fun beginStateObservation() {
            removeCallbacks(refreshState)
            post(refreshState)
        }

        fun stopStateObservation() = removeCallbacks(refreshState)

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat()
            val h = height.toFloat()
            val scale = min(w / 300f, h / 62f)
            canvas.save()
            canvas.scale(scale, scale)

            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(15, 15, 16)
            canvas.drawRoundRect(RectF(0.75f, 0.75f, 299.25f, 61.25f), 13f, 13f, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            paint.color = Color.rgb(91, 91, 94)
            canvas.drawRoundRect(RectF(0.75f, 0.75f, 299.25f, 61.25f), 13f, 13f, paint)

            drawPower(canvas, powerColor(state))
            paint.style = Paint.Style.FILL
            paint.color = statusColor(state)
            canvas.drawCircle(82f, 31f, 5.5f, paint)
            paint.color = Color.rgb(238, 238, 240)
            paint.textSize = 16f
            paint.typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
            canvas.drawText(stateLabel(state), 96f, 36.5f, paint)

            paint.color = Color.rgb(62, 62, 65)
            canvas.drawRect(170f, 13f, 171f, 49f, paint)
            drawExpand(canvas)
            drawClose(canvas)
            canvas.restore()
        }

        private fun drawPower(canvas: Canvas, color: Int) {
            paint.color = color
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3.4f
            canvas.drawArc(RectF(18f, 15f, 50f, 47f), -48f, 276f, false, paint)
            canvas.drawLine(34f, 10.5f, 34f, 28f, paint)
        }

        private fun drawExpand(canvas: Canvas) {
            paint.color = Color.rgb(238, 238, 240)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            canvas.drawRoundRect(RectF(196f, 26f, 213f, 43f), 2.5f, 2.5f, paint)
            expandPath.reset()
            expandPath.moveTo(207f, 26f)
            expandPath.lineTo(221f, 12f)
            expandPath.moveTo(211f, 12f)
            expandPath.lineTo(221f, 12f)
            expandPath.lineTo(221f, 22f)
            canvas.drawPath(expandPath, paint)
        }

        private fun drawClose(canvas: Canvas) {
            paint.color = Color.rgb(238, 238, 240)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2.2f
            canvas.drawLine(263f, 21f, 283f, 41f, paint)
            canvas.drawLine(283f, 21f, 263f, 41f, paint)
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (event.action != MotionEvent.ACTION_UP) return true
            performClick()
            when {
                event.x < width * POWER_END_FRACTION -> togglePower()
                event.x >= width * CLOSE_START_FRACTION -> closeOverlay()
                event.x >= width * EXPAND_START_FRACTION -> expandMain()
            }
            return true
        }

        override fun performClick(): Boolean {
            super.performClick()
            return true
        }

        private fun stateLabel(value: PrivateAudioState) = getString(
            when (value) {
                PrivateAudioState.READY -> R.string.state_ready
                PrivateAudioState.WAITING -> R.string.state_waiting
                PrivateAudioState.ACTIVE -> R.string.state_active
                PrivateAudioState.ERROR -> R.string.state_error
            },
        )

        private fun stateDescription(value: PrivateAudioState) = getString(
            R.string.overlay_controller_description,
            stateLabel(value),
        )

        private fun statusColor(value: PrivateAudioState) = when (value) {
            PrivateAudioState.READY, PrivateAudioState.ACTIVE -> Color.rgb(34, 218, 112)
            PrivateAudioState.WAITING -> Color.rgb(238, 172, 54)
            PrivateAudioState.ERROR -> Color.rgb(238, 75, 75)
        }

        private fun powerColor(value: PrivateAudioState) = when (value) {
            PrivateAudioState.READY -> Color.rgb(184, 184, 188)
            PrivateAudioState.WAITING -> Color.rgb(238, 172, 54)
            PrivateAudioState.ACTIVE -> Color.rgb(34, 218, 112)
            PrivateAudioState.ERROR -> Color.rgb(238, 75, 75)
        }
    }

    companion object {
        private const val ACTION_SHOW = "app.privateaudio.overlay.SHOW"
        private const val ACTION_HIDE = "app.privateaudio.overlay.HIDE"
        private const val STATE_REFRESH_MILLIS = 200L
        private const val POWER_END_FRACTION = 0.22f
        private const val EXPAND_START_FRACTION = 0.57f
        private const val CLOSE_START_FRACTION = 0.80f

        fun showIntent(context: Context) = Intent(context, OverlayService::class.java).setAction(ACTION_SHOW)
        fun hideIntent(context: Context) = Intent(context, OverlayService::class.java).setAction(ACTION_HIDE)
    }
}
