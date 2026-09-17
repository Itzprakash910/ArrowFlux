package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SoundManager(private val scope: CoroutineScope) {
    var isSoundEnabled: Boolean = true
    var isMusicEnabled: Boolean = true

    private val sampleRate = 22050

    fun playTap() {
        if (!isSoundEnabled) return
        playTone(frequency = 600f, durationMs = 40, attack = 0.05f)
    }

    fun playMove() {
        if (!isSoundEnabled) return
        playFrequencySweep(startFreq = 420f, endFreq = 880f, durationMs = 120)
    }

    fun playBlocked() {
        if (!isSoundEnabled) return
        playTone(frequency = 180f, durationMs = 150, attack = 0.02f, waveType = 1)
    }

    fun playSuccess() {
        if (!isSoundEnabled) return
        scope.launch(Dispatchers.Default) {
            playToneSync(523.25f, 60) // C5
            kotlinx.coroutines.delay(40)
            playToneSync(659.25f, 60) // E5
            kotlinx.coroutines.delay(40)
            playToneSync(783.99f, 100) // G5
        }
    }

    fun playLevelComplete() {
        if (!isSoundEnabled) return
        scope.launch(Dispatchers.Default) {
            playToneSync(440f, 80) // A4
            kotlinx.coroutines.delay(60)
            playToneSync(554.37f, 80) // C#5
            kotlinx.coroutines.delay(60)
            playToneSync(659.25f, 80) // E5
            kotlinx.coroutines.delay(60)
            playToneSync(880f, 220) // A5
        }
    }

    fun playStar() {
        if (!isSoundEnabled) return
        playFrequencySweep(700f, 1200f, 100)
    }

    fun playCoin() {
        if (!isSoundEnabled) return
        scope.launch(Dispatchers.Default) {
            playToneSync(987.77f, 50) // B5
            kotlinx.coroutines.delay(30)
            playToneSync(1318.51f, 120) // E6
        }
    }

    fun playHint() {
        if (!isSoundEnabled) return
        playFrequencySweep(500f, 750f, 150)
    }

    fun playUndo() {
        if (!isSoundEnabled) return
        playFrequencySweep(600f, 320f, 100)
    }

    fun playButton() {
        if (!isSoundEnabled) return
        playTone(frequency = 500f, durationMs = 30)
    }

    private fun playTone(frequency: Float, durationMs: Int, attack: Float = 0.1f, waveType: Int = 0) {
        scope.launch(Dispatchers.Default) {
            playToneSync(frequency, durationMs, attack, waveType)
        }
    }

    private fun playToneSync(frequency: Float, durationMs: Int, attack: Float = 0.1f, waveType: Int = 0) {
        try {
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            val decay = 4.0 / numSamples

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val angle = 2.0 * PI * frequency * t
                val rawSample = if (waveType == 1) {
                    if (sin(angle) >= 0) 0.7 else -0.7
                } else {
                    sin(angle)
                }
                // Envelope
                val envelope = exp(-i * decay)
                val sample = (rawSample * envelope * Short.MAX_VALUE * 0.45).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            Thread.sleep(durationMs.toLong() + 20)
            audioTrack.release()
        } catch (_: Exception) {
            // Silently fallback if audio hardware is busy
        }
    }

    private fun playFrequencySweep(startFreq: Float, endFreq: Float, durationMs: Int) {
        scope.launch(Dispatchers.Default) {
            try {
                val numSamples = (sampleRate * durationMs / 1000)
                val buffer = ShortArray(numSamples)
                var currentPhase = 0.0

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val currentFreq = startFreq + (endFreq - startFreq) * progress
                    currentPhase += 2.0 * PI * currentFreq / sampleRate
                    val envelope = 1.0 - progress
                    val sample = (sin(currentPhase) * envelope * Short.MAX_VALUE * 0.4).toInt()
                    buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                Thread.sleep(durationMs.toLong() + 20)
                audioTrack.release()
            } catch (_: Exception) {
            }
        }
    }
}
