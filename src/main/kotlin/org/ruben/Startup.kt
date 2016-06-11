package org.ruben

import org.ruben.classifier.CombinedClassifier
import org.ruben.database.DownloadPlayerCardService
import org.ruben.database.LocalPlayerCardService
import org.ruben.nn.NeuralNetworkSerializer
import org.ruben.training.TrainingCardService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class Startup {

    @Autowired
    private lateinit var classifyProperties: ClassifyProperties
    @Autowired
    private lateinit var downloadPlayerCardService: DownloadPlayerCardService
    @Autowired
    private lateinit var trainingCardService: TrainingCardService
    @Autowired
    private lateinit var combinedClassifier: CombinedClassifier


    @PostConstruct
    fun start() {
        if (classifyProperties.downloadCards() == "true") {
            downloadPlayerCardService.downloadCards()
        }

        if (classifyProperties.trainNets() == "true") {
            trainingCardService.train()
        }

        combinedClassifier.load()
    }
}