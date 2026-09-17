package com.example.haptics

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticManager(context: Context) {
    var isVibrationEnabled: Boolean = true

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun vibrateTap() {
        if (!isVibrationEnabled || vibrator?.hasVibrator() != true) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(20)
            }
        } catch (_: Exception) {}
    }

    fun vibrateEscape() {
        if (!isVibrationEnabled || vibrator?.hasVibrator() != true) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(45)
            }
        } catch (_: Exception) {}
    }

    fun vibrateBlocked() {
        if (!isVibrationEnabled || vibrator?.hasVibrator() != true) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 40, 40, 60)
                val amplitudes = intArrayOf(0, 180, 0, 240)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(80)
            }
        } catch (_: Exception) {}
    }

    fun vibrateComplete() {
        if (!isVibrationEnabled || vibrator?.hasVibrator() != true) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 60, 50, 80, 50, 120)
                val amplitudes = intArrayOf(0, 150, 0, 200, 0, 255)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(150)
            }
        } catch (_: Exception) {}
    }
}
