package org.ruben.nn

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.ruben.Card
import org.ruben.ClassifyProperties
import org.ruben.classifier.ClassClassifier
import org.ruben.classifier.NgClassifier
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.FileInputStream
import java.io.FileOutputStream

@Component
class NeuralNetworkSerializer {

    @Autowired
    private lateinit var classifyProperties: ClassifyProperties

    fun serializeNgClassifier(ngClassifier: NgClassifier) {
        serialize(classifyProperties.ngNnStorage(), ngClassifier.nns)
    }

    fun serializeClassClassifier(classClassifier: ClassClassifier) {
        serialize(classifyProperties.ngNnStorage(), classClassifier.nns)
    }

    fun loadNgClassifier(): NgClassifier {
        val ngClassifier = NgClassifier()
        ngClassifier.nns = loadNNs(classifyProperties.ngNnStorage())
        return ngClassifier
    }

    fun loadClassClassifier(): ClassClassifier {
        val classClassifier = ClassClassifier()
        classClassifier.nns = loadNNs(classifyProperties.classNnStorage())
        return classClassifier
    }

    private fun serialize(fileName: String, nns: List<NeuralNetwork>) {
        val mapper = jacksonObjectMapper()

        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(FileOutputStream(classifyProperties.ngNnStorage()), nns)
    }

   private fun loadNNs(fileName: String): List<NeuralNetwork> {
        val mapper = jacksonObjectMapper()

        return mapper.readValue<List<NeuralNetwork>>(FileInputStream(fileName))
    }

}