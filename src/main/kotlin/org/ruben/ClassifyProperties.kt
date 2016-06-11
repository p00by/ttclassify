package org.ruben

import org.springframework.stereotype.Component
import java.util.*

@Component
class ClassifyProperties {

    val properties: Properties;

    init {
        properties = Properties()
        properties.load(ClassifyProperties::class.java.getResourceAsStream("/ttclassify.properties"))
    }

    fun getCardsLocation(): String {
        return properties.get("cardStorage").toString()
    }

    fun getTTApiUser(): String {
        return properties.get("ttApiUser").toString()
    }

    fun getTTApiPassword(): String {
        return properties.get("ttApiPassword").toString()
    }

    fun getNewClassifications(): String {
        return properties.get("newClassifications").toString()
    }

    fun downloadCards(): String {
        return properties.get("downloadCards").toString()
    }

    fun trainNets(): String {
        return properties.get("trainNets").toString()
    }

    fun classNnStorage(): String {
        return properties.get("classNnStorage").toString()
    }

    fun ngNnStorage(): String {
        return properties.get("ngNnStorage").toString()
    }

}