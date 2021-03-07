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
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
    STOPPED,
    COMPLETE;
}

data class Timer(
    val duration: Int,
    val progress: Float,
    val reset: Boolean,
    val timerState: TimerState
)

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
            duration = TimeUnit.SECONDS.toMillis(5).toInt(),
            progress = 0f,
            reset = false,
            timerState = TimerState.IDLE
        )
    )
    val scope = rememberCoroutineScope()
    MyTheme {
        ConstraintLayout(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            val (countdown, bottomRow) = createRefs()
            Countdown(
                timer.value,
                onProgress = {
                    if (it == 1.0f) {
                        timer.value = timer.value.copy(
                            progress = it,
                            timerState = TimerState.COMPLETE
                        )
                    } else {
                        timer.value = timer.value.copy(progress = it)
                    }
                },
                onReset = {
                    timer.value = timer.value.copy(progress = 0f, reset = true)
                    scope.launch {
                        delay(300)
                        when (timer.value.timerState) {
                            TimerState.STOPPED,
                            TimerState.COMPLETE -> {
                                timer.value = timer.value.copy(
                                    reset = false,
                                    timerState = TimerState.IDLE
                                )
                            }
                            else -> {
                                timer.value = timer.value.copy(reset = false)
                            }
                        }
                    }
                },
                modifier = Modifier
                    .constrainAs(countdown) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(bottomRow.top)
                    }
                    .padding(64.dp)
            )
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .constrainAs(bottomRow) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    }
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
                            TimerState.COMPLETE -> {
                                timer.value = timer.value.copy(
                                    progress = 0.0f,
                                    timerState = TimerState.STARTED
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
                            TimerState.COMPLETE -> Icons.Default.RestartAlt
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
fun Countdown(
    timer: Timer,
    onProgress: (progress: Float) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    ConstraintLayout(modifier) {
        val (progress, timeText, timeUp, button) = createRefs()
        val circularProgressModifier = Modifier
            .aspectRatio(1.0f)
            .constrainAs(progress) {
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
        val infiniteTransition = rememberInfiniteTransition()
        val color = when (timer.timerState) {
            TimerState.COMPLETE,
            TimerState.STOPPED -> {
                val color by infiniteTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = 0.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(700, 350, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )
                color
            }
            else -> {
                1.0f
            }
        }
        Text(
            text = Utils.timeString(remainingTime),
            style = MaterialTheme.typography.h2.copy(
                color = MaterialTheme.colors.secondary
            ),
            modifier = Modifier
                .alpha(color)
                .constrainAs(timeText) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top, margin = 64.dp)
                }
        )
        if (timer.timerState == TimerState.COMPLETE) {
            Text(
                text = "Time Up!",
                style = MaterialTheme.typography.h6.copy(
                    color = MaterialTheme.colors.secondary
                ),
                modifier = Modifier
                    .alpha(color)
                    .constrainAs(timeUp) {
                        start.linkTo(timeText.start)
                        end.linkTo(timeText.end)
                        top.linkTo(timeText.bottom)
                    }
            )
        }
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
                    top.linkTo(parent.bottom, margin = (-56).dp)
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
    modifier: Modifier
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
    modifier: Modifier
) {
    val progressColor = MaterialTheme.colors.secondary
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
            color = progressColor
        )
        drawCircle(
            color = progressColor,
            center = Offset(dotX, dotY),
            radius = 16f,
            style = Fill
        )
    }
}
