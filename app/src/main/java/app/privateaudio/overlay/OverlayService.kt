package app.privateaudio.overlay

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.os.IBinder
import android.os.ResultReceiver
import android.provider.Settings
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.text.TextUtils
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import app.privateaudio.MainActivity
import app.privateaudio.PrivateAudioService
import app.privateaudio.PrivateAudioState
import app.privateaudio.R
import kotlin.math.hypot
import kotlin.math.min

internal enum class MiniControl { NONE, POWER, EXPAND, CLOSE }

internal class MiniGestureArbitrator(private val touchSlop: Float) {
    var touchedControl = MiniControl.NONE
        private set
    var dragging = false
        private set

    fun down(control: MiniControl) {
        touchedControl = control
        dragging = false
    }

    fun move(dx: Float, dy: Float): Boolean {
        if (!dragging && hypot(dx, dy) > touchSlop) {
            dragging = true
            touchedControl = MiniControl.NONE
        }
        return dragging
    }

    fun up(releasedControl: MiniControl): MiniControl {
        val activatedControl = if (!dragging && touchedControl == releasedControl) {
            touchedControl
        } else {
            MiniControl.NONE
        }
        clear()
        return activatedControl
    }

    fun cancel() = clear()

    private fun clear() {
        touchedControl = MiniControl.NONE
        dragging = false
    }
}

/** Owns only the floating window. Routing remains exclusively service-owned elsewhere. */
class OverlayService : Service() {
    private val windowManager by lazy { getSystemService(WindowManager::class.java) }
    private var overlayView: FloatingControllerView? = null
    private var privateAudioService: PrivateAudioService? = null
    private var isBound = false
    private var overlayPosition: OverlayPosition? = null
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
            ACTION_SHOW -> showOverlay(intent.getParcelableExtra(EXTRA_SHOW_RESULT))
            ACTION_HIDE -> closeOverlay()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        hideOverlay()
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        overlayView?.refreshLocalizedPresentation()
    }

    private fun showOverlay(resultReceiver: ResultReceiver?) {
        if (overlayView != null) {
            resultReceiver?.send(SHOW_SUCCEEDED, null)
            return
        }
        if (!Settings.canDrawOverlays(this)) return
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
            gravity = Gravity.TOP or Gravity.LEFT
        }
        val bounds = windowManager.currentWindowMetrics.bounds
        val initialPosition = overlayPosition ?: OverlayPosition(
            x = (bounds.width() - layoutParams.width) / 2,
            y = (bounds.height() - layoutParams.height) / 2,
        )
        clampOverlayPosition(initialPosition, layoutParams)
        try {
            windowManager.addView(surface, layoutParams)
            overlayView = surface
            surface.beginStateObservation()
            resultReceiver?.send(SHOW_SUCCEEDED, null)
        } catch (_: SecurityException) {
            // The user can revoke the grant between the permission check and this call.
            unbindControllerService()
            stopSelf()
        }
    }

    private fun moveOverlay(layoutParams: WindowManager.LayoutParams, x: Int, y: Int) {
        val previousX = layoutParams.x
        val previousY = layoutParams.y
        val position = clampOverlayPosition(OverlayPosition(x, y), layoutParams)
        if (position.x == previousX && position.y == previousY) return
        overlayView?.let { windowManager.updateViewLayout(it, layoutParams) }
    }

    private fun clampOverlayPosition(
        position: OverlayPosition,
        layoutParams: WindowManager.LayoutParams,
    ): OverlayPosition {
        val bounds = windowManager.currentWindowMetrics.bounds
        return clampOverlayPosition(
            x = position.x,
            y = position.y,
            screenWidth = bounds.width(),
            screenHeight = bounds.height(),
            overlayWidth = layoutParams.width,
            overlayHeight = layoutParams.height,
        ).also {
            layoutParams.x = it.x
            layoutParams.y = it.y
            overlayPosition = it
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
        closeOverlay()
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
        private val statusTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(238, 238, 240)
            textSize = 16f
            typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
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

        fun refreshLocalizedPresentation() {
            contentDescription = stateDescription(state)
            invalidate()
        }

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

            paint.style = Paint.Style.FILL
            paint.color = statusColor(state)
            canvas.drawCircle(directionalX(STATUS_DOT_X), 31f, 5.5f, paint)
            drawStatusLabel(canvas, stateLabel(state))

            drawPower(canvas, powerColor(state))
            drawExpand(canvas)
            drawClose(canvas)
            canvas.restore()
        }

        private fun drawStatusLabel(canvas: Canvas, label: String) {
            val isRtl = isRtlLayout()
            val layout = StaticLayout.Builder.obtain(label, 0, label.length, statusTextPaint, STATUS_TEXT_WIDTH)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setTextDirection(
                    if (isRtl) TextDirectionHeuristics.FIRSTSTRONG_RTL
                    else TextDirectionHeuristics.FIRSTSTRONG_LTR,
                )
                .setIncludePad(false)
                .setMaxLines(1)
                .setEllipsize(TextUtils.TruncateAt.END)
                .setEllipsizedWidth(STATUS_TEXT_WIDTH)
                .build()
            canvas.save()
            val textLeft = if (isRtl) directionalX(STATUS_TEXT_RIGHT) else STATUS_TEXT_LEFT
            canvas.translate(textLeft, STATUS_TEXT_BASELINE + statusTextPaint.fontMetrics.ascent)
            layout.draw(canvas)
            canvas.restore()
        }

        private fun drawPower(canvas: Canvas, color: Int) {
            paint.color = color
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3.4f
            canvas.drawArc(RectF(134f, 15f, 166f, 47f), -48f, 276f, false, paint)
            canvas.drawLine(150f, 10.5f, 150f, 28f, paint)
        }

        private fun drawExpand(canvas: Canvas) {
            paint.color = Color.rgb(238, 238, 240)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            canvas.drawRoundRect(directionalRect(196f, 26f, 213f, 43f), 2.5f, 2.5f, paint)
            expandPath.reset()
            expandPath.moveTo(directionalX(207f), 26f)
            expandPath.lineTo(directionalX(221f), 12f)
            expandPath.moveTo(directionalX(211f), 12f)
            expandPath.lineTo(directionalX(221f), 12f)
            expandPath.lineTo(directionalX(221f), 22f)
            canvas.drawPath(expandPath, paint)
        }

        private fun drawClose(canvas: Canvas) {
            paint.color = Color.rgb(238, 238, 240)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2.2f
            canvas.drawLine(directionalX(263f), 21f, directionalX(283f), 41f, paint)
            canvas.drawLine(directionalX(283f), 21f, directionalX(263f), 41f, paint)
        }

        private fun isRtlLayout() =
            resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

        private fun directionalX(ltrX: Float) =
            if (isRtlLayout()) DESIGN_WIDTH - ltrX else ltrX

        private fun directionalRect(left: Float, top: Float, right: Float, bottom: Float): RectF =
            RectF(
                minOf(directionalX(left), directionalX(right)),
                top,
                maxOf(directionalX(left), directionalX(right)),
                bottom,
            )

        private val gesture = MiniGestureArbitrator(
            ViewConfiguration.get(context).scaledTouchSlop.toFloat(),
        )
        private var downRawX = 0f
        private var downRawY = 0f
        private var startWindowX = 0
        private var startWindowY = 0

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val layoutParams = layoutParams as? WindowManager.LayoutParams ?: return false
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downRawX = event.rawX
                    downRawY = event.rawY
                    startWindowX = layoutParams.x
                    startWindowY = layoutParams.y
                    gesture.down(controlAt(event.x))
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downRawX
                    val dy = event.rawY - downRawY
                    if (gesture.move(dx, dy)) {
                        moveOverlay(layoutParams, startWindowX + dx.toInt(), startWindowY + dy.toInt())
                    }
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    when (gesture.up(controlAt(event.x))) {
                        MiniControl.NONE -> Unit
                        MiniControl.POWER -> {
                            performClick()
                            togglePower()
                        }
                        MiniControl.EXPAND -> {
                            performClick()
                            expandMain()
                        }
                        MiniControl.CLOSE -> {
                            performClick()
                            closeOverlay()
                        }
                    }
                    return true
                }
                MotionEvent.ACTION_CANCEL -> {
                    gesture.cancel()
                    return true
                }
            }
            return super.onTouchEvent(event)
        }

        private fun controlAt(x: Float): MiniControl {
            val directionalTouchX = if (isRtlLayout()) width - x else x
            return when {
                directionalTouchX >= width * CLOSE_START_FRACTION -> MiniControl.CLOSE
                directionalTouchX >= width * EXPAND_START_FRACTION -> MiniControl.EXPAND
                directionalTouchX >= width * POWER_START_FRACTION &&
                    directionalTouchX < width * POWER_END_FRACTION -> MiniControl.POWER
                else -> MiniControl.NONE
            }
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
        private const val EXTRA_SHOW_RESULT = "app.privateaudio.overlay.SHOW_RESULT"
        const val SHOW_SUCCEEDED = 1
        private const val STATE_REFRESH_MILLIS = 200L
        private const val DESIGN_WIDTH = 300f
        private const val STATUS_DOT_X = 20f
        private const val STATUS_TEXT_LEFT = 34f
        private const val STATUS_TEXT_RIGHT = 126f
        private const val STATUS_TEXT_WIDTH = 92
        private const val STATUS_TEXT_BASELINE = 36.5f
        private const val POWER_START_FRACTION = 0.40f
        private const val POWER_END_FRACTION = 0.60f
        private const val EXPAND_START_FRACTION = 0.60f
        private const val CLOSE_START_FRACTION = 0.80f

        fun showIntent(context: Context, resultReceiver: ResultReceiver? = null) =
            Intent(context, OverlayService::class.java).setAction(ACTION_SHOW).apply {
                putExtra(EXTRA_SHOW_RESULT, resultReceiver)
            }
        fun hideIntent(context: Context) = Intent(context, OverlayService::class.java).setAction(ACTION_HIDE)
    }
}
