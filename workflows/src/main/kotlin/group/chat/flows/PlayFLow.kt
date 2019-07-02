package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.PokerContract
//import com.template.contracts.templateContract
import com.template.model.Card
import com.template.model.RoundEnum
import com.template.model.RoundEnum.*
import com.template.states.Deck
import com.template.states.GroupChatState
import com.template.states.PlayerState
import com.template.util.GameUtil
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.util.*

// *********
// * Flows *
// * Dealer initiates the flow
// * Dealer constructs the flow with game id and action
// *  the permitted actions are deal, flop, river, turn, evaluate winner and close
// *********
@InitiatingFlow
@StartableByRPC
class PlayFLow(val gameID: String, val round: String) : FlowLogic<Unit>() {
    /**
     * Tracks progress throughout the flows call execution.
     */
    override val progressTracker: ProgressTracker
        get() {
            return ProgressTracker(
                    VALIDATING,
                    BUILDING,
                    SIGNING,
                    COLLECTING,
                    FINALISING
            )
        }

    companion object {
        object VALIDATING : ProgressTracker.Step("Performing initial steps - get game state and round enum player and check if they are valid")
        object BUILDING : ProgressTracker.Step("Building and verifying transaction. Do the Play and update the cards")
        object SIGNING : ProgressTracker.Step("Dealer Signing transaction.")
        object COLLECTING : ProgressTracker.Step("Collecting signatures from the dealer and other players.") {
            override fun childProgressTracker() = CollectSignaturesFlow.tracker()
        }

        object FINALISING : ProgressTracker.Step("Finalising transaction. - Full Final signature on the vault") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(VALIDATING, BUILDING, SIGNING, COLLECTING, FINALISING)
    }

    @Suspendable
    override fun call() {
        // Initiator flow logic goes here.
        // Step 1. Validation.
        progressTracker.currentStep = VALIDATING
        val notary = this.serviceHub.networkMapCache.notaryIdentities.first()
        val oldGameStateRef = this.serviceHub.vaultService.queryBy(GroupChatState::class.java, QueryCriteria.LinearStateQueryCriteria(linearId = listOf(UniqueIdentifier(id = UUID.fromString(gameID))))).states.first()
        val oldGroupChatState: GroupChatState = oldGameStateRef.state.data
        val oldPlayerStateRefList = mutableListOf<StateAndRef<PlayerState>>()
        val players = oldGroupChatState.players
        val roundEnum = RoundEnum.valueOf(round)
        val oldDeckStateRef = this.serviceHub.vaultService.queryBy(Deck::class.java, QueryCriteria.LinearStateQueryCriteria(linearId = listOf(oldGroupChatState.deckIdentifier))).states.first()
        val newDeckState = oldDeckStateRef.state.data.copy()
        var newGameState = oldGroupChatState.copy(rounds = roundEnum)
        val newPlayerStates = mutableListOf<PlayerState>()
        val oldPlayerStates = mutableListOf<PlayerState>()
        val txBuilder = TransactionBuilder(notary)

        // Step 2. Building.
        progressTracker.currentStep = BUILDING
        //Create a new copy of PlayerState state for every player state
        players.forEach {
            val oldPlayerStateRef = this.serviceHub.vaultService.queryBy(PlayerState::class.java).states.filter { playerstateref -> playerstateref.state.data.party == it }.first()
            val oldPlayerState = oldPlayerStateRef.state.data
            oldPlayerStateRefList.add(oldPlayerStateRef)
            oldPlayerStates.add(oldPlayerState)
            newPlayerStates.add(oldPlayerState.copy())
        }
        when (roundEnum) {
            Dealt -> {
                GameUtil.deal(newPlayerStates, mutableListOf<Card>(), newDeckState)
                txBuilder.addCommand(Command(PokerContract.Commands.DEALT(), newGameState.participants.map { it.owningKey }))
            }
            Flopped -> {
                val tableCards = newGameState.tableCards.toMutableList()
                GameUtil.callFlop(newPlayerStates, tableCards, newDeckState)
                newGameState = newGameState.copy(tableCards = tableCards.toList())
                txBuilder.addCommand(Command(PokerContract.Commands.FLOPPED(), newGameState.participants.map { it.owningKey }))
            }
            Turned -> {
                val tableCards = newGameState.tableCards.toMutableList()
                GameUtil.betTurn(newPlayerStates, tableCards, newDeckState)
                newGameState = newGameState.copy(tableCards = tableCards.toList())
                txBuilder.addCommand(Command(PokerContract.Commands.TURNED(), newGameState.participants.map { it.owningKey }))
            }
            Rivered -> {
                val tableCards = newGameState.tableCards.toMutableList()
                GameUtil.betRiver(newPlayerStates, tableCards, newDeckState)
                newGameState = newGameState.copy(tableCards = tableCards.toList())
                txBuilder.addCommand(Command(PokerContract.Commands.RIVERED(), newGameState.participants.map { it.owningKey }))
            }
            Winner -> {
                val tableCards = newGameState.tableCards.toMutableList()
                val winnerList = GameUtil.getWinner(newPlayerStates, tableCards)
                newGameState.winner = winnerList.get(0).party
                println("Table cards are ")
                println("\t "+ tableCards)
                println("Winner List in order is " )
                winnerList.forEach{
                    println( it.party.name)
                    println( "\t\t with cards" + it.myCards)
                }
                println("The winning amount of " + newGameState.betAmount + " goes to " + winnerList.get(0).party.name)
                txBuilder.addCommand(Command(PokerContract.Commands.WINNER(), newGameState.participants.map { it.owningKey }))
            }
            else ->
                throw RuntimeException("This should not happen")

        }
        txBuilder.addInputState(oldGameStateRef)
                .addInputState(oldDeckStateRef)
        oldPlayerStateRefList.forEach { txBuilder.addInputState(it) }

        newPlayerStates.forEach {  txBuilder.addOutputState(it) }
        txBuilder
                .addOutputState(newGameState)
                .addOutputState(newDeckState)
                .verify(serviceHub)

        // Step 3. Sign the transaction.
        progressTracker.currentStep = SIGNING
        val dealerSignedTx = serviceHub.signInitialTransaction(txBuilder)

        // Step 4. Get the counter-party (Players) signature.
        progressTracker.currentStep = COLLECTING
        val otherPartySessions = newGameState.players.map { initiateFlow(it) }
        val fullySignedTx = subFlow(CollectSignaturesFlow(dealerSignedTx, otherPartySessions.toSet()))

        // Step 6. Finalise the transaction.
        progressTracker.currentStep = FINALISING
        subFlow(FinalityFlow(fullySignedTx, otherPartySessions.toSet()))
    }

}

@InitiatedBy(PlayFLow::class)
class PlayFlowResponder(val otherPartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(otherPartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                //TODO
            }
        }
        val txId = subFlow(signTransactionFlow).id

        return subFlow(ReceiveFinalityFlow(otherPartySession, expectedTxId = txId))
    }
}