package org.ruben.training

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.apache.log4j.Logger
import org.ruben.Card
import org.ruben.Classification
import org.ruben.classifier.ClassClassifier
import org.ruben.classifier.NgClassifier
import org.ruben.database.LocalPlayerCardService
import org.ruben.nn.Activator
import org.ruben.nn.NeuralNetwork
import org.ruben.nn.NeuralNetworkSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct

@Component
class TrainingCardService {

    @Autowired
    private lateinit var localPlayerCardService: LocalPlayerCardService
    @Autowired
    private lateinit var neuralNetworkSerializer: NeuralNetworkSerializer

    private val log = Logger.getInstance("TrainingCardService")
    private val amountOfVotes = 50

    private val ngTrainingTime = 1000 * 5L;
    private val classTrainingTime = 1000 * 60 * 60L

    fun train() {
        val allCards = localPlayerCardService.loadCards()
        val cards = allCards
                .filter { it.result.ordinal >= Classification.C0.ordinal }
                .filter { it.current.ordinal >= Classification.C0.ordinal }
                .filter { it.amountOfMatches() >= 20 }
                .map { it.stabilize() }

        trainNgNNs(cards)
        trainNormalNNs(cards)

        localPlayerCardService.serializeCards(allCards)
    }

    fun trainNgNNs(allCards: List<Card>) {
        val ngClassifier = NgClassifier()
        ngClassifier.train(amountOfVotes, allCards, ngTrainingTime)
        neuralNetworkSerializer.serializeNgClassifier(ngClassifier)
    }


    fun trainNormalNNs(allCards: List<Card>) {
        val classClassifier = ClassClassifier()
        classClassifier.train(amountOfVotes, allCards, classTrainingTime)
        neuralNetworkSerializer.serializeClassClassifier(classClassifier)
    }

}