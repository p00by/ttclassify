package org.ruben.nn

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*

val random = Random()

class Neuron(val inputSize: Int) {

    var weights: MutableList<Double> = ArrayList()
    var bias: Double = 0.0

    @JsonIgnore
    var newWeights: MutableList<Double> = ArrayList()
    @JsonIgnore
    var newBias: Double = 0.0


    init {
        weights.addAll(Activator.Relu.initialWeights(inputSize))

        for (i in 1..inputSize) {
            newWeights.add(0.0)
        }

        bias = Activator.Relu.initialBias()
        newBias = bias;
    }

    fun evaluate(inputs: List<Double>, activator: Activator) : Double {
        var sum = 0.0;
        for (i in weights.indices) {
            sum += inputs[i] * weights[i]
        }

        return activator.eval(sum + bias)
    }

    fun updateWeights() : Unit {
        for (i in 0..weights.size -1) {
            weights[i] = newWeights[i]
        }
        bias = newBias;
    }

}
