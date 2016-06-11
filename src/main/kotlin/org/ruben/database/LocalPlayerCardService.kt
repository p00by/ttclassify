package org.ruben.database

import org.ruben.Card
import org.springframework.stereotype.Component
import java.io.FileInputStream
import java.io.FileOutputStream
import com.fasterxml.jackson.module.kotlin.*
import org.ruben.ClassifyProperties
import org.springframework.beans.factory.annotation.Autowired

@Component
class LocalPlayerCardService {

    @Autowired
    lateinit var classifyProperties: ClassifyProperties


    fun serializeCards(cards: List<Card>): Unit {
        val mapper = jacksonObjectMapper()

        mapper.writerWithDefaultPrettyPrinter()
            .writeValue(FileOutputStream(classifyProperties.getCardsLocation()), cards)
    }

    fun loadCards(): List<Card> {
        val mapper = jacksonObjectMapper();
        val cards = mapper.readValue<List<Card>>(FileInputStream(classifyProperties.getCardsLocation()))
        return cards;
    }


}