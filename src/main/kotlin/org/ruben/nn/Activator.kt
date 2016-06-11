package org.ruben.nn

import java.util.*

val rg = Random()

interface Activator {

    fun eval(x: Double): Double

    fun derivative(x: Double): Double

    fun initialWeights(n: Int): List<Double>

    fun initialBias(): Double

    object Sigmoid: Activator {

        override fun eval(x: Double): Double {
            return 1.0 / (1.0 + Math.exp(-x))
        }

        override fun derivative(x: Double): Double {
            return x * (1- x)
        }

        override fun initialWeights(n: Int): List<Double> {
            return (1..n).map {(rg.nextDouble() * 2) - 1}
        }

        override fun initialBias(): Double {
            return 0.0;
        }
    }

    object Relu: Activator {

        override fun eval(x: Double): Double {
            return Math.max(0.0, x)
        }

        override fun derivative(x: Double): Double {
            return if (x > 0) 1.0 else 0.0
        }

        override fun initialWeights(n: Int): List<Double> {
            return (1..n).map {rg.nextDouble() / n}
        }

        override fun initialBias(): Double {
            return 0.0;
        }
    }

}