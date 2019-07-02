package com.template.util

import com.template.model.Card
import com.template.states.Deck
import com.template.states.PlayerState
import java.util.*

object GameUtil {

    fun deal(playerStates: MutableList<PlayerState>, tableCards: MutableList<Card>, deck: Deck) {
        for (player in playerStates) {
            player.myCards = listOf(deck.pop(), deck.pop())
        }
        checkPlayersRanking(playerStates, tableCards)
    }

    fun betTurn(playerStates: MutableList<PlayerState>, tableCards: MutableList<Card>, deck: Deck) {
        //deck.pop()
        tableCards.add(deck.pop())
        checkPlayersRanking(playerStates, tableCards)
    }

    fun betRiver(playerStates: MutableList<PlayerState>, tableCards: MutableList<Card>, deck: Deck) {
        //deck.pop()
        tableCards.add(deck.pop())
        checkPlayersRanking(playerStates, tableCards)
    }

    /**
     * double initial bet
     */
    fun callFlop(playerStates: MutableList<PlayerState>, tableCards: MutableList<Card>, deck: Deck) {
        //deck.pop()
        tableCards.add(deck.pop())
        tableCards.add(deck.pop())
        tableCards.add(deck.pop())
        checkPlayersRanking(playerStates, tableCards)
    }

    fun getWinner(playerStates: MutableList<PlayerState>, tableCards: MutableList<Card>): List<PlayerState> {
        checkPlayersRanking(playerStates, tableCards)
        val winnerList = ArrayList<PlayerState>()
        var winner = playerStates.get(0)
        var winnerRank = RankingUtil.getRankingToInt(winner)
        winnerList.add(winner)
        for (i in 1 until playerStates.size) {
            val player = playerStates.get(i)
            val playerRank = RankingUtil.getRankingToInt(player)
            //Draw game
            if (winnerRank == playerRank) {
                var highHandPlayer = checkHighSequence(winner, player)
                //Draw checkHighSequence
                if (highHandPlayer == null) {
                    highHandPlayer = checkHighCardWinner(winner, player)
                }
                //Not draw in checkHighSequence or checkHighCardWinner
                if (highHandPlayer != null && !winner.equals(highHandPlayer)) {
                    winner = highHandPlayer
                    winnerList.clear()
                    winnerList.add(winner)
                } else if (highHandPlayer == null) {
                    //Draw in checkHighSequence and checkHighCardWinner
                    winnerList.add(winner)
                }
            } else if (winnerRank < playerRank) {
                winner = player
                winnerList.clear()
                winnerList.add(winner)
            }
            winnerRank = RankingUtil.getRankingToInt(winner)
        }

        return winnerList
    }

    private fun checkHighSequence(playerState1: PlayerState, playerState2: PlayerState): PlayerState? {
        val player1Rank = sumRankingList(playerState1)
        val player2Rank = sumRankingList(playerState2)
        if (player1Rank > player2Rank) {
            return playerState1
        } else if (player1Rank < player2Rank) {
            return playerState2
        }
        return null
    }

    private fun checkHighCardWinner(playerState1: PlayerState, playerState2: PlayerState): PlayerState? {
        var winner = compareHighCard(playerState1, playerState1.highCard!!,
                playerState2, playerState2.highCard!!)
        if (winner == null) {
            var player1Card = RankingUtil.getHighCard(playerState1,
                    emptyList<Card>())
            var player2Card = RankingUtil.getHighCard(playerState2,
                    emptyList<Card>())
            winner = compareHighCard(playerState1, player1Card, playerState2, player2Card)
            if (winner != null) {
                playerState1.highCard = player1Card
                playerState2.highCard = player2Card
            } else {
                player1Card = getSecondHighCard(playerState1, player1Card)
                player2Card = getSecondHighCard(playerState2, player2Card)
                winner = compareHighCard(playerState1, player1Card, playerState2,
                        player2Card)
                if (winner != null) {
                    playerState1.highCard = player1Card
                    playerState2.highCard = player2Card
                }
            }
        }
        return winner
    }

    private fun checkPlayersRanking(playerStates: MutableList<PlayerState>, tableCards: MutableList<Card>) {
        for (player in playerStates) {
            RankingUtil.checkRanking(player, tableCards)
        }
    }

    /*
	 * TODO This method must be moved to RankingUtil
	 */
    private fun sumRankingList(playerState: PlayerState): Int {
        var sum: Int = 0
        for (card in playerState.highCardRankingList) {
            sum += card.getRankToInt()
        }
        return sum
    }

    /*
	 * TODO This method must be moved to RankingUtil
	 */
    private fun getSecondHighCard(playerState: PlayerState, card: Card): Card {
        return if (playerState.myCards[0].equals(card)) {
            playerState.myCards[1]
        } else playerState.myCards[0]
    }

    private fun compareHighCard(playerState1: PlayerState, player1HighCard: Card,
                                playerState2: PlayerState, player2HighCard: Card): PlayerState? {
        if (player1HighCard.getRankToInt() > player2HighCard.getRankToInt()) {
            return playerState1
        } else if (player1HighCard.getRankToInt() < player2HighCard
                        .getRankToInt()) {
            return playerState2
        }
        return null
    }


}