package org.ruben.classifier

import org.ruben.nn.NeuralNetwork
import java.util.*

open class Classifier {
    val random = Random();

    protected fun train(cards: List<Pair<List<Double>, List<Double>>>, nn: NeuralNetwork, nrCards: Int): Unit {
        for (i in 1..nrCards) {
            val card = cards[random.nextInt(cards.size)]

            nn.train(card.first, card.second);
        }
    }
}