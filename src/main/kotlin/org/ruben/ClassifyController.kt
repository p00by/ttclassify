package org.ruben

import org.apache.log4j.Logger
import org.ruben.classifier.CombinedClassifier
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.util.concurrent.atomic.AtomicLong

@RestController
class ClassifyController {

    private val log = Logger.getLogger(ClassifyController::class.java)

    @Autowired
    private lateinit var combinedClassifier: CombinedClassifier

    @RequestMapping(value = "/classify", method = arrayOf(RequestMethod.POST))
    fun greeting(@RequestBody card: Card): CombinedClassifier.Result {
        log.info("Got request to classify $card")

        return combinedClassifier.classifyCard(card)
    }

}