package com.template.util

import com.template.model.Card
import com.template.model.CardRankEnum
import com.template.model.CardRankEnum.*
import com.template.model.RankingEnum
import com.template.model.RankingEnum.*
import com.template.states.PlayerState
import java.util.*

/*
ROYAL_FLUSH,
STRAIGHT_FLUSH,
FOUR_OF_A_KIND,
FULL_HOUSE,
FLUSH,
STRAIGHT,
THREE_OF_A_KIND,
TWO_PAIR,
ONE_PAIR,
HIGH_CARD
*/
object RankingUtil {

    fun getRankingToInt(playerState: PlayerState): Int {
        return playerState.rankingEnum.ordinal
    }

    fun checkRanking(playerState: PlayerState, tableCards: List<Card>) {

        //HIGH_CARD
        val highCard = getHighCard(playerState, tableCards)
        playerState.highCard = highCard

        //ROYAL_FLUSH
        var rankingList = getRoyalFlush(playerState, tableCards)
        if (rankingList != null) {
            setRankingEnumAndList(playerState, ROYAL_FLUSH, rankingList)
            return
        }
        //STRAIGHT_FLUSH
        rankingList = getStraightFlush(playerState, tableCards)
        if (rankingList != null) {
            setRankingEnumAndList(playerState, STRAIGHT_FLUSH,
                    rankingList)
            return
        }
        //FOUR_OF_A_KIND
        rankingList = getFourOfAKind(playerState, tableCards)
        if (rankingList != null) {
            setRankingEnumAndList(playerState, FOUR_OF_A_KIND,
                    rankingList)
            return
        }
        //FULL_HOUSE
        rankingList = getFullHouse(playerState, tableCards)
        if (rankingList != null) {
            setRankingEnumAndList(playerState, FULL_HOUSE, rankingList)
            return
        }
        //FLUSH
        rankingList = getFlush(playerState, tableCards)
        if (rankingList != null) {
            setRankingEnumAndList(playerState, FLUSH, rankingList)
            return
        }
        //STRAIGHT
        rankingList = getStraight(playerState, tableCards)
        if (rankingList != null) {
            setRankingEnumAndList(playerState, STRAIGHT, rankingList)
            return
        }
        //THREE_OF_A_KIND
        rankingList = getThreeOfAKind(playerState, tableCards)
        if (rankingList != null) {
            setRankingEnumAndList(playerState, THREE_OF_A_KIND,
                    rankingList)
            return
        }
        //TWO_PAIR
        rankingList = getTwoPair(playerState, tableCards)
        if (rankingList != null) {
            setRankingEnumAndList(playerState, TWO_PAIR, rankingList)
            return
        }
        //ONE_PAIR
        rankingList = getOnePair(playerState, tableCards)
        if (rankingList != null) {
            setRankingEnumAndList(playerState, ONE_PAIR, rankingList)
            return
        }
        //HIGH_CARD
        playerState.rankingEnum = HIGH_CARD
        val highCardRankingList = ArrayList<Card>()
        highCardRankingList.add(highCard)
        playerState.highCardRankingList = highCardRankingList
        return
    }

    fun getRoyalFlush(playerState: PlayerState, tableCards: List<Card>): List<Card>? {
        if (!isSameSuit(playerState, tableCards)) {
            return null
        }

        val rankEnumList = toRankEnumList(playerState, tableCards)

        return if (rankEnumList.contains(CARD_10)
                && rankEnumList.contains(JACK)
                && rankEnumList.contains(QUEEN)
                && rankEnumList.contains(KING)
                && rankEnumList.contains(ACE)) {

            getMergedCardList(playerState, tableCards)
        } else null

    }

    fun getStraightFlush(playerState: PlayerState,
                         tableCards: List<Card>): List<Card>? {
        return getSequence(playerState, tableCards, 5, true)
    }

    fun getFourOfAKind(playerState: PlayerState,
                       tableCards: List<Card>): List<Card>? {
        val mergedList = getMergedCardList(playerState, tableCards)
        return checkPair(mergedList, 4)
    }

    fun getFullHouse(playerState: PlayerState, tableCards: List<Card>): List<Card>? {
        val mergedList = getMergedCardList(playerState, tableCards)
        val threeList = checkPair(mergedList, 3)
        if (threeList != null) {
            mergedList.removeAll(threeList)
            val twoList = checkPair(mergedList, 2)
            if (twoList != null) {
                threeList.addAll(twoList)
                return threeList
            }
        }
        return null
    }

    fun getFlush(playerState: PlayerState, tableCards: List<Card>): List<Card>? {
        val mergedList = getMergedCardList(playerState, tableCards)
        val flushList = ArrayList<Card>()

        for (card1 in mergedList) {
            for (card2 in mergedList) {
                if (card1.suit.equals(card2.suit)) {
                    if (!flushList.contains(card1)) {
                        flushList.add(card1)
                    }
                    if (!flushList.contains(card2)) {
                        flushList.add(card2)
                    }
                }
            }
            if (flushList.size == 5) {
                return flushList
            }
            flushList.clear()
        }
        return null
    }

    //S‹o 5 cartas seguidas de naipes diferentes, caso empate ganha aquele com a maior sequ?ncia.
    fun getStraight(playerState: PlayerState, tableCards: List<Card>): List<Card>? {
        return getSequence(playerState, tableCards, 5, false)
    }

    fun getThreeOfAKind(playerState: PlayerState,
                        tableCards: List<Card>): List<Card>? {
        val mergedList = getMergedCardList(playerState, tableCards)
        return checkPair(mergedList, 3)
    }

    fun getTwoPair(playerState: PlayerState, tableCards: List<Card>): List<Card>? {
        val mergedList = getMergedCardList(playerState, tableCards)
        val twoPair1 = checkPair(mergedList, 2)
        if (twoPair1 != null) {
            mergedList.removeAll(twoPair1)
            val twoPair2 = checkPair(mergedList, 2)
            if (twoPair2 != null) {
                twoPair1.addAll(twoPair2)
                return twoPair1
            }
        }
        return null
    }

    fun getOnePair(playerState: PlayerState, tableCards: List<Card>): List<Card>? {
        val mergedList = getMergedCardList(playerState, tableCards)
        return checkPair(mergedList, 2)
    }

    fun getHighCard(playerState: PlayerState, tableCards: List<Card>): Card {
        val allCards = ArrayList<Card>()
        allCards.addAll(tableCards)
        allCards.add(playerState.myCards[0])
        allCards.add(playerState.myCards[1])

        var highCard = allCards[0]
        for (card in allCards) {
            if (card.getRankToInt() > highCard.getRankToInt()) {
                highCard = card
            }
        }
        return highCard
    }

    private fun getSequence(playerState: PlayerState,
                            tableCards: List<Card>, sequenceSize: Int?, compareSuit: Boolean): List<Card>? {
        val orderedList = getOrderedCardList(playerState, tableCards)
        val sequenceList = ArrayList<Card>()

        var cardPrevious: Card? = null
        for (card in orderedList) {
            if (cardPrevious != null) {
                if (card.getRankToInt() - cardPrevious.getRankToInt() == 1) {
                    if ((!compareSuit) || cardPrevious.suit.equals(card.suit)) {
                        if (sequenceList.size == 0) {
                            sequenceList.add(cardPrevious)
                        }
                        sequenceList.add(card)
                    }
                } else {
                    if (sequenceList.size == sequenceSize) {
                        return sequenceList
                    }
                    sequenceList.clear()
                }
            }
            cardPrevious = card
        }

        return if (sequenceList.size == sequenceSize) sequenceList else null
    }

    private fun getMergedCardList(playerState: PlayerState,
                                  tableCards: List<Card>): MutableList<Card> {
        val merged = ArrayList<Card>()
        merged.addAll(tableCards)
        merged.add(playerState.myCards[0])
        merged.add(playerState.myCards[1])
        return merged
    }

    private fun getOrderedCardList(playerState: PlayerState,
                                   tableCards: List<Card>): List<Card> {
        val ordered: MutableList<Card> = getMergedCardList(playerState, tableCards)
        Collections.sort(ordered, Comparator<Card> { c1: Card, c2: Card -> if (c1.getRankToInt() < c2.getRankToInt()) -1 else 1 })
        return ordered
    }

    private fun checkPair(mergedList: List<Card>, pairSize: Int?): MutableList<Card>? {
        val checkedPair = ArrayList<Card>()
        for (card1 in mergedList) {
            checkedPair.add(card1)
            for (card2 in mergedList) {
                if (!card1.equals(card2) && card1.rank.equals(card2.rank)) {
                    checkedPair.add(card2)
                }
            }
            if (checkedPair.size == pairSize) {
                return checkedPair
            }
            checkedPair.clear()
        }
        return null
    }

    private fun isSameSuit(playerState: PlayerState, tableCards: List<Card>): Boolean {
        val suit = playerState.myCards[0].suit

        if (!suit.equals(playerState.myCards[1].suit)) {
            return false
        }

        for (card in tableCards) {
            if (!card.suit.equals(suit)) {
                return false
            }
        }

        return true
    }

    private fun toRankEnumList(playerState: PlayerState,
                               tableCards: List<Card>): List<CardRankEnum> {
        val rankEnumList = ArrayList<CardRankEnum>()

        for (card in tableCards) {
            rankEnumList.add(card.rank)
        }

        rankEnumList.add(playerState.myCards[0].rank)
        rankEnumList.add(playerState.myCards[1].rank)

        return rankEnumList
    }

    private fun setRankingEnumAndList(playerState: PlayerState,
                                      rankingEnum: RankingEnum, rankingList: List<Card>) {
        playerState.rankingEnum = rankingEnum
        playerState.highCardRankingList = rankingList
    }
}
