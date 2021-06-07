package tooling

import kotlin.math.pow

class LinearRegression(private val xs: List<Double>, private val ys: List<Double>) {
    private var slope: Double = 0.0
    private var yIntercept: Double = 0.0

    init {
        val covariance = calculateCovariance(xs, ys)
        val variance = calculateVariance(xs)
        slope = calculateSlope(covariance, variance)
        yIntercept = calculateYIntercept(ys, slope, xs)
    }

    fun predict(independentVariable: Double) = slope * independentVariable + yIntercept

    fun calculateRSquared(): Double {
        val sst = ys.sumByDouble { y -> (y - ys.average()).pow(2) }
        val ssr = xs.zip(ys) { x, y -> (y - predict(x.toDouble())).pow(2) }.sum()
        return (sst - ssr) / sst
    }

    private fun calculateYIntercept(ys: List<Double>, slope: Double, xs: List<Double>) =
        ys.average() - slope * xs.average()

    private fun calculateSlope(covariance: Double, variance: Double) = covariance / variance

    private fun calculateCovariance(xs: List<Double>, ys: List<Double>) =
        xs.zip(ys) { x, y -> (x - xs.average()) * (y - ys.average()) }.sum()

    private fun calculateVariance(xs: List<Double>) = xs.sumByDouble { x -> (x - xs.average()).pow(2) }
}