package org.ruben.classifier

import org.ruben.Card
import org.ruben.Classification
import org.ruben.nn.NeuralNetworkSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

@Component
class CombinedClassifier {

    @Autowired
    private lateinit var neuralNetworkSerializer: NeuralNetworkSerializer
    private lateinit var ngClassifier: NgClassifier
    private lateinit var classClassifier: ClassClassifier

    fun load() {
        ngClassifier = neuralNetworkSerializer.loadNgClassifier()
        classClassifier = neuralNetworkSerializer.loadClassClassifier()
    }

    fun classifyCard(unbalancedCard: Card): Result {
        val card = unbalancedCard.stabilize()
        val warnings = ArrayList<String>()
        if (card.amountOfMatches() < 20) {
            warnings.add("Weinig matches gespeeld, de voorspelling is pas accuraat vanaf 20 matches")
        }
        if (card.current.ordinal < Classification.C0.ordinal) {
            warnings.add("Huidig klassement is hoger dan C0, de voorspelling werkt het beste vanaf C0 tot NG")
        }

        if (card.current == Classification.NG) {
            val (higherThanNg, higherThanNgCertainty) = ngClassifier.isHigherThanNg(card)
            if (higherThanNg) {
                val (normalClassification, normalClassificationCertainty) = classifyNormalClassification(card)

                return Result(normalClassification, Math.min(higherThanNgCertainty, normalClassificationCertainty), warnings)
            }

            return Result(Classification.NG, higherThanNgCertainty, warnings)
        } else {
            val (classification, certainty) = classifyNormalClassification(card)
            if (classification.ordinal < Classification.C0.ordinal) {
                warnings.add("Voorspelde klassement is hoger dan C0, de voorspelling werkt het beste vanaf C0 tot NG")
            }
            return Result(classification, certainty, warnings)
        }
    }

    private fun classifyNormalClassification(card: Card): Pair<Classification, Int> {
        return classClassifier.getClassification(card)
    }

    data class Result(val classification: Classification, val certain: Int, val warnings: List<String>)

}