package org.ruben.database

import org.apache.commons.io.IOUtils
import org.apache.log4j.Logger
import org.ruben.Card
import org.ruben.CardSection
import org.ruben.Classification
import org.ruben.ClassifyProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.FileInputStream
import java.util.*
import javax.xml.soap.MessageFactory
import javax.xml.soap.SOAPConnectionFactory
import javax.xml.soap.SOAPMessage

@Component
class DownloadPlayerCardService {

    private val url = "http://api.vttl.be/0.7/index.php?s=vttl"
    private val log = Logger.getInstance("DownloadPlayerCardService")

    @Autowired
    private lateinit var localPlayerCardService: LocalPlayerCardService

    @Autowired
    private lateinit var classifyProperties: ClassifyProperties

    fun downloadCards() {
        val cards = loadCards(Arrays.asList("OVL001", "OVL012",
               "OVL018", "OVL020", "OVL032", "OVL039", "OVL046", "OVL047",
               "OVL052", "OVL053", "OVL054", "OVL056", "OVL059", "OVL061",
               "OVL080", "OVL082", "OVL088", "OVL092", "OVL093", "OVL095",
               "OVL096", "OVL101", "OVL102", "OVL103", "OVL106", "OVL107",
               "OVL108", "OVL115", "OVL119", "OVL123", "OVL125", "OVL131",
               "OVL133", "OVL134", "OVL138", "OVL142", "OVL144", "OVL145",
               "OVL146", "OVL148", "OVL149"))
           .filter { card -> card.classifications.find {
               section -> section.wins > 0 || section.losses > 0
           } != null }

       localPlayerCardService.serializeCards(cards)
    }

    //Did not get OVL039, OVL046 and OVL082
    private fun loadCards(clubs: List<String>) : List<Card> {
        return clubs.flatMap { club ->
            //Annoying but i have to sleep a bit, otherwise i exceed quota :'(
            Thread.sleep(60000*2)
            try {
                val cards = loadCards(club);
                cards
            } catch (e : Exception) {
                ArrayList<Card>()
            }
        }
    }

    //Blahblah ugly soap parsing code
    private fun loadCards(club: String): List<Card> {
        val newClassifications = loadNewClassifications();

        val connectionFactory = SOAPConnectionFactory.newInstance()
        val connection = connectionFactory.createConnection()

        val soapResponse = connection.call(createRequest(club), url);

        log.info("Loading for $club");
        val memberResponse = soapResponse.soapBody.getElementsByTagName("ns1:GetMembersResponse").item(0);

        val amountOfCards = Integer.parseInt( memberResponse.childNodes.item(0).textContent )
        log.info("Found $amountOfCards  elements");


        val cards = ArrayList<Card>();
        for (i in 1..amountOfCards) {
            try {
                val member = memberResponse.childNodes.item(i)
                var firstName = ""
                var lastName = ""
                var currentClassification = ""

                val wins = HashMap<Classification, Int>()
                val losses = HashMap<Classification, Int>()
                val encountered = HashSet<List<String>>()

                for (j in 0..member.childNodes.length - 1) {
                    val memberNode = member.childNodes.item(j)
                    if (memberNode.nodeName.equals("ns1:FirstName")) {
                        firstName = memberNode.textContent
                    } else if (memberNode.nodeName.equals("ns1:LastName")) {
                        lastName = memberNode.textContent
                    } else if (memberNode.nodeName.equals("ns1:Ranking")) {
                        currentClassification = memberNode.textContent
                    } else if (memberNode.nodeName.equals("ns1:ResultEntries")) {
                        var date = "";
                        var opponentIndex = "";
                        var opponentRanking = "";
                        var result = "";
                        for (z in 0..memberNode.childNodes.length - 1) {
                            val opponentNode = memberNode.childNodes.item(z)
                            if (opponentNode.nodeName.equals("ns1:Date")) {
                                date = opponentNode.textContent;
                            } else if (opponentNode.nodeName.equals("ns1:UniqueIndex")) {
                                opponentIndex = opponentNode.textContent;
                            } else if (opponentNode.nodeName.equals("ns1:Ranking")) {
                                opponentRanking = opponentNode.textContent;
                            } else if (opponentNode.nodeName.equals("ns1:Result")) {
                                result = opponentNode.textContent;
                            }
                        }

                        //avoid bug in tabt api - lots of results are mentioned more than once
                        //might have a slight bug if you really play against the same dude
                        //twice on the same day
                        val unicity = Arrays.asList(opponentIndex, result);
                        if (encountered.add(unicity)) {

                            val opponentClassification = Classification.valueOf(opponentRanking);
                            if (result.equals("V")) {
                                increase(wins, opponentClassification)
                            } else if (result.equals("D")) {
                                increase(losses, opponentClassification)
                            }
                        }
                    }
                }

                val newClassification = newClassifications.get(lastName + " " + firstName)
                if (newClassification != null) {

                    val card = Card(
                            Classification.values().map {
                                classification ->
                                CardSection(classification, wins[classification] ?: 0, losses[classification] ?: 0)
                            },
                            Classification.valueOf(currentClassification),
                            newClassification)

                    cards.add(card)
                }
            } catch (e: Exception) {
                log.info("Could not load a card ", e)
            }
        }

        return cards;
    }

    private fun loadNewClassifications() : Map<String, Classification> {
        val stream = FileInputStream(classifyProperties.getNewClassifications())
        val newClassifications = HashMap<String, Classification>();
        for (line in IOUtils.toString(stream, "UTF-8").split("\n")) {
            val split = line.split(",")
            newClassifications.put(split[0].trim(), Classification.valueOf(split[1].trim()))
        }
        return newClassifications;
    }

    private fun increase(map: MutableMap<Classification, Int>, classification: Classification) : Unit {
        val current =  map.get(classification)
        if (current != null) {
            map.put(classification, current + 1)
        } else {
            map.put(classification, 1)
        }
    }

    private fun createRequest(clubName: String): SOAPMessage {
        val messageFactory = MessageFactory.newInstance();
        val soapMessage = messageFactory.createMessage();
        val soapPart = soapMessage.soapPart;
        val envelope = soapPart.envelope;

        envelope.addNamespaceDeclaration("tab", "http://api.frenoy.net/TabTAPI")

        val soapBody = envelope.body;
        val getMembersRequest = soapBody.addChildElement("GetMembersRequest", "tab")
        val credentials = getMembersRequest.addChildElement("Credentials", "tab")
        val account = credentials.addChildElement("Account", "tab")
        account.addTextNode(classifyProperties.getTTApiUser())
        val password = credentials.addChildElement("Password", "tab")
        //  TODO make this a property somehow
        account.addTextNode(classifyProperties.getTTApiPassword())
        val club = getMembersRequest.addChildElement("Club", "tab")
        club.addTextNode(clubName)
        val season = getMembersRequest.addChildElement("Season", "tab")
        season.addTextNode("16")
        val rankingPointsInformation = getMembersRequest.addChildElement("RankingPointsInformation", "tab")
        rankingPointsInformation.addTextNode("true")
        val withResults = getMembersRequest.addChildElement("WithResults", "tab")
        withResults.addTextNode("true")

        soapMessage.saveChanges();

        return soapMessage
    }

}