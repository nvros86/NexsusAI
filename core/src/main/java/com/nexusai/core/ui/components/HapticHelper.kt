package com.nexusai.core.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object HapticHelper {
    fun lightTap(context: Context) {
        vibrate(context, VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun mediumTap(context: Context) {
        vibrate(context, VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun heavyTap(context: Context) {
        vibrate(context, VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun success(context: Context) {
        vibrate(
            context,
            VibrationEffect.createWaveform(longArrayOf(0, 30, 50, 30), intArrayOf(0, 128, 0, 200), -1)
        )
    }

    fun error(context: Context) {
        vibrate(
            context,
            VibrationEffect.createWaveform(longArrayOf(0, 50, 50, 50, 50, 50), intArrayOf(0, 200, 0, 200, 0, 200), -1)
        )
    }

    private fun vibrate(context: Context, effect: VibrationEffect) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.vibrate(effect)
    }
}
