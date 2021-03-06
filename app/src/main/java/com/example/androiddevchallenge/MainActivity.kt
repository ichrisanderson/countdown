/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.os.Bundle
import android.text.format.DateUtils
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.androiddevchallenge.ui.theme.MyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

enum class TimerState {
    IDLE,
    STARTED,
    STOPPED;
}

data class Timer(
    val duration: Int,
    val progress: Float,
    val reset: Boolean,
    val timerState: TimerState
)
private const val timeFormat = "%02d"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TimerScreen()
        }
    }
}

@Composable
fun TimerScreen() {
    val timer = mutableStateOf(
        Timer(
            duration = TimeUnit.SECONDS.toMillis(30).toInt(),
            progress = 0f,
            reset = false,
            timerState = TimerState.IDLE
        )
    )
    val scope = rememberCoroutineScope()
    MyTheme {
        Column {
            TimerCircle(
                timer.value,
                onProgress = {
                    timer.value = timer.value.copy(progress = it)
                },
                onReset = {
                    timer.value = timer.value.copy(progress = 0f, reset = true)
                    scope.launch {
                        delay(300)
                        timer.value = timer.value.copy(reset = false)
                    }
                },
                modifier = Modifier.padding(64.dp)
            )
            Spacer(Modifier.weight(1.0f))
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Button(
                    onClick = {
                        when (timer.value.timerState) {
                            TimerState.STARTED -> {
                                timer.value = timer.value.copy(
                                    timerState = TimerState.STOPPED
                                )
                            }
                            else -> {
                                timer.value = timer.value.copy(
                                    timerState = TimerState.STARTED
                                )
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.secondary
                    ),
                    shape = CircleShape
                ) {
                    val iconAsset: ImageVector =
                        when (timer.value.timerState) {
                            TimerState.STARTED -> Icons.Default.Pause
                            else -> Icons.Default.PlayArrow
                        }
                    Icon(
                        imageVector = iconAsset,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Composable
fun TimerCircle(
    timer: Timer,
    onProgress: (progress: Float) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {

    ConstraintLayout(modifier) {
        val (clock, text, button) = createRefs()
        val circularProgressModifier = Modifier
            .aspectRatio(1.0f)
            .constrainAs(clock) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(parent.top)
            }
        val remainingTime = (timer.duration - (timer.duration * timer.progress).toInt()).toLong()
        when (timer.timerState) {
            TimerState.STARTED -> {
                AnimatingCircularProgress(
                    timer = timer,
                    onProgress = onProgress,
                    modifier = circularProgressModifier
                )
            }
            else -> {
                CircularProgress(
                    timer.progress,
                    modifier = circularProgressModifier
                )
            }
        }

        val hours = (remainingTime / DateUtils.HOUR_IN_MILLIS).toInt()
        var remainder = (remainingTime % DateUtils.HOUR_IN_MILLIS).toInt()
        val minutes = (remainder / DateUtils.MINUTE_IN_MILLIS).toInt()
        remainder = (remainder % DateUtils.MINUTE_IN_MILLIS).toInt()
        val seconds = (remainingTime / DateUtils.SECOND_IN_MILLIS).toInt()
        remainder = (remainder % DateUtils.SECOND_IN_MILLIS).toInt()
        Text(
            text = "${timeFormat.format(minutes)}:${timeFormat.format(seconds)}",
            style = MaterialTheme.typography.h2.copy(color = MaterialTheme.colors.secondary),
            modifier = Modifier
                .constrainAs(text) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top, margin = 64.dp)
                }
        )
        Button(
            onClick = onReset,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Transparent
            ),
            modifier = Modifier
                .constrainAs(button) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(parent.bottom, margin = (-80).dp)
                }
        ) {
            Text(text = "Reset", color = MaterialTheme.colors.secondary)
        }
    }
}

@Composable
fun AnimatingCircularProgress(
    timer: Timer,
    onProgress: (progress: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(timer.progress) }
    if (timer.reset) {
        scope.launch {
            progress.snapTo(0f)
        }
    } else {
        scope.launch {
            progress.animateTo(
                1f,
                animationSpec = tween(
                    durationMillis = timer.duration - (timer.duration * progress.value).toInt(),
                    easing = LinearEasing
                )
            )
        }
    }
    onProgress(progress.value)
    CircularProgress(progress.value, modifier)
}

@Composable
fun CircularProgress(
    currentProgress: Float,
    modifier: Modifier = Modifier
) {
    val secondaryColor = MaterialTheme.colors.secondary
    Canvas(modifier = modifier) {
        val xCenter = size.width / 2
        val yCenter = size.height / 2
        val radius = min(xCenter, yCenter)
        val middle = Offset(xCenter, yCenter)
        val dotAngleDegrees = (270 - (360 * currentProgress)) % 360
        val dotAngleRadians = Math.toRadians(dotAngleDegrees.toDouble())
        val dotX = xCenter + (radius * cos(dotAngleRadians)).toFloat()
        val dotY = yCenter + (radius * sin(dotAngleRadians)).toFloat()
        drawCircle(
            color = Color.Black,
            center = middle,
            radius = radius,
            style = Stroke(8.dp.toPx())
        )
        drawCircle(
            color = Color.White,
            center = middle,
            radius = radius,
            style = Stroke(4.dp.toPx())
        )
        drawArc(
            startAngle = 270f,
            sweepAngle = dotAngleDegrees - 270f,
            useCenter = false,
            style = Stroke(4.dp.toPx()),
            color = secondaryColor
        )
        drawCircle(
            color = secondaryColor,
            center = Offset(dotX, dotY),
            radius = 16f,
            style = Fill
        )
    }
}
