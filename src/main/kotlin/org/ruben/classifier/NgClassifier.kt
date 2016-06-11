package org.ruben.classifier

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.apache.log4j.Logger
import org.ruben.Card
import org.ruben.Classification
import org.ruben.nn.NeuralNetwork
import java.util.*

class NgClassifier (): Classifier() {

    public lateinit var nns: List<NeuralNetwork>

    val log = Logger.getLogger(NgClassifier::class.java)

    fun train(amountOfVotes: Int, allCards: List<Card>, trainingTime: Long) {
        val cards = allCards
                .filter { it.current == Classification.NG }

        val trainingCards = cards.filterIndexed { i, card -> i % 5 != 0 }
        val evaluateCards = cards.filterIndexed { i, card -> i % 5 == 0 }

        val testInput =  trainingCards.map { card ->
            Pair(createNgInput(card), createNgOutput(card)) }

        val testNumbers = testInput.flatMap { it.first }
        val statistics = DescriptiveStatistics(testNumbers.toDoubleArray())

        val mean = statistics.mean
        val stdev = statistics.standardDeviation

        nns = (1..amountOfVotes).map {
            NeuralNetwork(
                    nrInputs = 4,
                    nrHidden = Arrays.asList(8, 8),
                    nrOutputs = 2,
                    learningRate = 0.01,
                    mean = mean,
                    stdev = stdev)
        }

        val currentTime = System.currentTimeMillis()
        while (currentTime + trainingTime > System.currentTimeMillis()) {
            nns.forEachIndexed { i, nn ->
                train(testInput, nn, 1000);
            }

            evaluateNetworks(evaluateCards)
        }
    }

    private fun createNgInput(card: Card): List<Double> {
        val input = ArrayList<Double>()
        for (i in 1..4) input.add(0.0)

        input[0] = card.getWins(Classification.NG) * 1.0
        input[1] = card.getLosses(Classification.NG) * 1.0
        Classification.values().filter { it != Classification.NG }.forEach {
            input[2] += card.getWins(it) * 1.0
            input[3] += card.getLosses(it) * 1.0
        }

        return input;
    }

    private fun createNgOutput(card: Card): List<Double> {
        if (card.result == Classification.NG) {
            return Arrays.asList(1.0, 0.0)
        } else {
            return Arrays.asList(0.0, 1.0)
        }
    }

    private fun evaluateNetworks(cards: List<Card>) {
        var hits = 0

        cards.forEach { card ->
            val isHigherThanNg = isHigherThanNg(card).first

            if (isHigherThanNg && card.result != Classification.NG) {
                hits++
            } else if (!isHigherThanNg && card.result == Classification.NG) {
                hits++
            }
        }

        val percentage = Math.round(hits * 100.0 / cards.size)
        log.info("NG: Hit $hits/${cards.size} = ($percentage%)")
    }

    fun isHigherThanNg(card: Card): Pair<Boolean, Int> {
        val guesses = nns.map { nn ->
            val input = createNgInput(card)
            val output = nn.evaluate(input)
            output[0] < output[1]
        }

        val bestGuess = Arrays.asList(false, true).map { classification -> {
            val count = guesses.filter { it == classification }.size
            val el = Pair(classification, count)
            el
        } }.sortedBy {  it.invoke().second }.last().invoke()

        return bestGuess
    }


}