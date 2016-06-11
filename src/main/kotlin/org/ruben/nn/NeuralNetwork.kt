package org.ruben.nn

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*

class NeuralNetwork(
        val nrInputs: Int,
        val nrHidden: List<Int>,
        val nrOutputs: Int,
        val learningRate: Double,
        val mean: Double,
        val stdev: Double) {

    val hiddenLayers: MutableList<MutableList<Neuron>>
    val outputLayer: MutableList<Neuron>
    @JsonIgnore
    val activator = Activator.Relu

    init {
        hiddenLayers = ArrayList()
        outputLayer = ArrayList()

        var hiddenInputs = nrInputs
        for (i in 0..nrHidden.size - 1) {
            val hiddenLayer = ArrayList<Neuron>()
            for (j in 0..nrHidden[i] - 1) {
                hiddenLayer.add(Neuron(hiddenInputs))
            }
            hiddenInputs = nrHidden[i]
            hiddenLayers.add(hiddenLayer)
        }


        val lastHidden = nrHidden.last()

        for (i in 1..nrOutputs) outputLayer.add(Neuron(lastHidden));
    }

    fun evaluate(inputUnscaled: List<Double>) : List<Double> {
        val input = scaleInput(inputUnscaled)

        val hidden = hiddenLayers.fold(input)
            {current, layer -> layer.map { node -> node.evaluate(current, activator) }}

        return outputLayer.map { el -> el.evaluate(hidden, activator) }
    }

    //https://mattmazur.com/2015/03/17/a-step-by-step-backpropagation-example/
    fun train(inputUnscaled: List<Double>, expectedOutput: List<Double>) {
        val input = scaleInput(inputUnscaled)
        val hiddenOutputs = ArrayList<List<Double>>()
        var current = input

        for (hiddenLayer in hiddenLayers) {
            current = hiddenLayer.map{
                it.evaluate(current, activator) }
            hiddenOutputs.add(current)
        }

        val output = outputLayer.map { el -> el.evaluate(current, activator) }

        var lastDeltas = ArrayList<Double>()
        var lastLayer = outputLayer

        for (oIndex in 0..outputLayer.size -1) {
            val oNeuron = outputLayer[oIndex]
            val oDelta =  -(expectedOutput[oIndex] - output[oIndex]) * activator.derivative (output[oIndex]);

            for (hIndex in 0..hiddenLayers.last().size -1) {
                val delta = oDelta * hiddenOutputs[hiddenLayers.size - 1][hIndex];

                oNeuron.newWeights[hIndex] = oNeuron.weights[hIndex] - (learningRate * delta);
            }

            oNeuron.newBias = oNeuron.bias  - learningRate * oDelta

            lastDeltas.add(oDelta)
        }

        for (hLayerIndexComplement in 1..hiddenLayers.size) {
            val hLayerIndex = hiddenLayers.size - hLayerIndexComplement
            val previousOutput = if (hLayerIndex == 0) input else hiddenOutputs[hLayerIndex - 1]

            val currentDeltas = ArrayList<Double>()
            val currentLayer = hiddenLayers[hLayerIndex]

            for (hIndex in 0..currentLayer.size - 1) {
                val hNeuron = currentLayer[hIndex]

                var sum = 0.0
                for (deltaIndex in 0..lastDeltas.size - 1) {
                    sum += lastDeltas[deltaIndex] * lastLayer[deltaIndex].weights[hIndex]
                }

                val deltaOut = activator.derivative(hiddenOutputs[hLayerIndex][hIndex])
                var hDelta = sum * deltaOut

                for (previousIndex in 0..previousOutput.size - 1) {

                    var delta = hDelta * previousOutput[previousIndex];

                    hNeuron.newWeights[previousIndex] = hNeuron.weights[previousIndex] - (learningRate * delta);
                }

                hNeuron.newBias = hNeuron.bias - learningRate * hDelta;

                currentDeltas.add(hDelta)
            }


            lastDeltas = currentDeltas
            lastLayer = currentLayer
        }

        hiddenLayers.forEach { it.forEach { el -> el.updateWeights() } }
        outputLayer.forEach { it.updateWeights() }
    }

    private fun scaleInput(input: List<Double>): List<Double> {
        return input.map{(it - mean)/stdev}
    }
}