package com.omismone.berryflow.ui

import kotlin.math.abs

// Orders by the numeric magnitude of the amount, largest first, regardless of
// whether it is income or an expense. Stable: equal amounts keep the order
// they had. Sorts on the real Double value, never on formatted text.
fun <T> Iterable<T>.sortedByMagnitudeDescending(amountOf: (T) -> Double): List<T> =
    sortedByDescending { abs(amountOf(it)) }