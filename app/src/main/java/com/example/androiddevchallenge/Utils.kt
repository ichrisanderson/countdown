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

import android.text.format.DateUtils

object Utils {

    private const val timeFormat = "%02d"

    fun timeString(remainingTime: Long): String {
        val hours = (remainingTime / DateUtils.HOUR_IN_MILLIS).toInt()
        var remainder = (remainingTime % DateUtils.HOUR_IN_MILLIS).toInt()
        val minutes = (remainder / DateUtils.MINUTE_IN_MILLIS).toInt()
        remainder = (remainder % DateUtils.MINUTE_IN_MILLIS).toInt()
        val seconds = (remainingTime / DateUtils.SECOND_IN_MILLIS).toInt()
        remainder = (remainder % DateUtils.SECOND_IN_MILLIS).toInt()

        return "${timeFormat.format(minutes)}:${timeFormat.format(seconds)}"
    }
}
