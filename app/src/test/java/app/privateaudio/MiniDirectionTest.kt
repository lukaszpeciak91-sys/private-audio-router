package app.privateaudio

import app.privateaudio.overlay.MiniControl
import app.privateaudio.overlay.miniControlAt
import app.privateaudio.overlay.miniDirectionalX
import org.junit.Assert.assertEquals
import org.junit.Test

class MiniDirectionTest {
    @Test
    fun geometryMirrorsStatusAndCloseAroundTheMiniWidth() {
        assertEquals(20f, miniDirectionalX(20f, 300f, rtl = false))
        assertEquals(280f, miniDirectionalX(20f, 300f, rtl = true))
        assertEquals(263f, miniDirectionalX(263f, 300f, rtl = false))
        assertEquals(37f, miniDirectionalX(263f, 300f, rtl = true))
    }

    @Test
    fun mirroredCloseAndExpandVisualRegionsKeepTheirActions() {
        assertEquals(MiniControl.CLOSE, miniControlAt(270f, 300f, rtl = false))
        assertEquals(MiniControl.EXPAND, miniControlAt(210f, 300f, rtl = false))
        assertEquals(MiniControl.CLOSE, miniControlAt(30f, 300f, rtl = true))
        assertEquals(MiniControl.EXPAND, miniControlAt(90f, 300f, rtl = true))
    }
}
