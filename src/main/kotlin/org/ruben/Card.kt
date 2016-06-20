package org.ruben

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

enum class Classification {
    B0, B2, B4, B6, C0, C2, C4, C6, D0, D2, D4, D6, E0, E2, E4, E6, NG
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class Card(val classifications: List<CardSection>, val current: Classification, val result: Classification = Classification.NG) {

    fun getWins(classification: Classification): Int {
        val el = classifications.find { el -> el.classification == classification }
        return el?.wins ?: 0
    }

    fun getLosses(classification: Classification): Int {
        val el = classifications.find { el -> el.classification == classification }
        return el?.losses ?: 0
    }

    fun stabilize(): Card {
        return Card(classifications.map{section ->
            val amountOfMatches = amountOfMatches();
            val additional = if (amountOfMatches >= 20) 0 else 20 - amountOfMatches;

            if (section.classification == current) {
                CardSection(section.classification, section.wins + 2 + additional / 2, section.losses + 2 + additional / 2)
            } else {
                section
            }
        }, current, result);
    }

    fun amountOfMatches(): Int {
        return classifications.map { it.wins + it.losses }.sum() ?: 0
    }

}

data class CardSection(val classification: Classification, val wins: Int, val losses: Int)
