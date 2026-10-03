package com.example.core.audio

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import android.util.Log

/**
 * Generates synthetic PC speaker beeps, BIOS POST chimes, and kernel boot sounds.
 */
object KernelSoundManager {
    private const val TAG = "KernelSoundManager"
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_SYSTEM, 90)
        } catch (e: Exception) {
            Log.w(TAG, "ToneGenerator initialization failed, audio may be muted: ${e.message}")
        }
    }

    /**
     * Plays the classic single short SeaBIOS / IBM PC 8254 PIT POST Beep (~1000Hz, 120ms).
     * Triggered when the terminal simulation initializes the kernel boot sequence.
     */
    fun playBiosPostBeep() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to play POST beep: ${e.message}")
        }
    }

    /**
     * Plays a two-tone rising chime (e.g. 440Hz -> 880Hz) to signal successful Protected Mode / Kernel Handshake.
     */
    fun playKernelBootChime() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 150)
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 200)
                } catch (ignored: Exception) {}
            }, 160)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to play kernel chime: ${e.message}")
        }
    }

    /**
     * Plays a double error beep for Kernel Panic or MBR missing magic signature.
     */
    fun playKernelPanicBeep() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 250)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to play panic beep: ${e.message}")
        }
    }
}
