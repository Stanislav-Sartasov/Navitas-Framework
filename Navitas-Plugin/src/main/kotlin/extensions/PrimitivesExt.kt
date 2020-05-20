package extensions

import kotlin.math.pow
import kotlin.math.round

fun Float.roundWithAccuracy(accuracy: Int): Float {
    val delim = 10.0.pow(accuracy)
    return (round(this * (delim.toInt())) / delim).toFloat()
}