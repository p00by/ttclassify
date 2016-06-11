package org.ruben.classifier

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.apache.log4j.Logger
import org.ruben.Card
import org.ruben.Classification
import org.ruben.nn.NeuralNetwork
import java.util.*

class ClassClassifier() : Classifier() {

    public lateinit var nns: List<NeuralNetwork>

    val log = Logger.getLogger(ClassClassifier::class.java)

    fun train(amountOfVotes: Int, allCards: List<Card>, trainingTime: Long) {
        val cards = allCards
                .filter { it.current != Classification.NG }

        val trainingCards = cards.filterIndexed { i, card -> i % 5 != 0 }
        val evaluateCards = cards.filterIndexed { i, card -> i % 5 == 0 }

        var testInput = Classification.values().flatMap { classification ->
            trainingCards.map { card ->
                Pair(generateInput(card, classification), generateExpectedOutput(card.result, classification)) }
        }

        //We want to have an equal amount of higher, lower and hit classifications
        for (i in 1..4) {
            testInput += cards.map { card ->
                Pair(generateInput(card, card.result), generateExpectedOutput(card.result, card.result))
            }
        }

        val testNumbers = testInput.flatMap { it.first }
        val statistics = DescriptiveStatistics(testNumbers.toDoubleArray())

        val mean = statistics.mean
        val stdev = statistics.standardDeviation

        log.info("Starting to train NNs")
        val nns = (1..amountOfVotes).map {
            NeuralNetwork(
                    nrInputs = 22,
                    nrHidden = Arrays.asList(32, 32),
                    nrOutputs = 3,
                    learningRate = 0.01,
                    mean = mean,
                    stdev = stdev)
        }

        val currentTime = System.currentTimeMillis()
        while (currentTime + trainingTime > System.currentTimeMillis()) {
            nns.forEachIndexed { i, nn ->
                train(testInput, nn, 1000);
            }
            evaluate(evaluateCards, nns)
        }
    }

    private fun generateInput(card: Card, offset: Classification) : List<Double> {
        val input = ArrayList<Double>()

        for (i in 1..22) input.add(0.0)

        for (classification in Classification.values()) {
            val boundedOffset = Math.max(-5 ,Math.min(5, classification.ordinal - offset.ordinal)) + 5;
            input[boundedOffset * 2] = input[boundedOffset * 2] + card.getWins(classification);
            input[boundedOffset * 2 + 1] = input[boundedOffset * 2 + 1] + card.getLosses(classification);
        }

        return input
    }

    private fun generateExpectedOutput(expectedClassification: Classification, classification: Classification): List<Double> {
        if (expectedClassification.ordinal - classification.ordinal >= 1) {
            return Arrays.asList(0.0,1.0,0.0)
        } else if (expectedClassification.ordinal - classification.ordinal == 0) {
            return Arrays.asList(1.0,0.0,0.0)
        } else if (expectedClassification.ordinal - classification.ordinal <= -1) {
            return Arrays.asList(0.0,0.0,1.0)
        }
        throw IllegalArgumentException()
    }

    private fun evaluate(cards: List<Card>, nets: List<NeuralNetwork>): Unit {
        var hits = 0
        var realCards = 0
        var tooHigh = 0
        var tooLow = 0

        cards.forEach { card ->
            val guess = getClassification(card).first

            if (guess != Classification.NG) {
                if (guess == card.result) {
                    hits++
                }
                realCards++
            }
        }

        val percentage = Math.round(hits * 100.0 / realCards)
        log.info("Hit $hits/$realCards = ($percentage%)")
    }

    fun getClassification(card: Card) : Pair<Classification, Int> {
        val guesses = nns.map { nn ->
            val classifications = Classification.values().map { classification ->
                val input = generateInput(card, classification)
                val values = nn.evaluate(input)
                values
            }

            val errors = classifications.map { el -> -(el[0]) }

            val guesses = errors.withIndex()
                    .sortedBy { it.component2() }
                    .map { Pair(it.component2(), Classification.values()[it.component1()]) }

            if (guesses.first().second == Classification.NG) {
                Classification.E6
            } else {
                guesses.first().second
            }
        }

        val bestGuess = Classification.values().asList().map { classification -> {
            val count = guesses.filter { it == classification }.size
            val el = Pair(classification, count)
            el
        } }.sortedBy {  it.invoke().second }.last().invoke()

        return Pair(bestGuess.first, bestGuess.second * 100 / nns.size)
    }


}