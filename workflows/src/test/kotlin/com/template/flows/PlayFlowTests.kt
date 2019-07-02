package com.template.flows

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.template.model.RoundEnum
import com.template.states.GroupChatState
import com.template.states.PlayerState
import net.corda.core.node.services.queryBy
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Before
import org.junit.Test

class PlayFlowTests {
    companion object {
        val log = loggerFor<PlayFlowTests>()
    }

    private val network = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
            TestCordapp.findCordapp("com.poker.contracts"),
            TestCordapp.findCordapp("com.poker.flows")
    )))
    private val dealer = network.createNode()
    private val playerA = network.createNode()
    private val playerB = network.createNode()

    init {
        listOf(playerA, playerB, dealer).forEach {
            it.registerInitiatedFlow(AddPlayerAcceptor::class.java)
            it.registerInitiatedFlow(PlayFlowResponder::class.java)
        }
    }

    @Before
    fun setup() = network.runNetwork()

    @After
    fun tearDown() = network.stopNodes()

    @Test
    fun `Deal and the players receive two cards`() {
        val notaryNode = network.defaultNotaryNode.info.legalIdentities.first()
        val startGameFlow = dealer.startFlow(StartGroupChat(notaryNode)).toCompletableFuture()
        network.runNetwork()
        val gameUID = startGameFlow.getOrThrow()


        dealer.startFlow(AddPlayerFlow(gameUID.id.toString(), playerA.info.legalIdentities.first())).toCompletableFuture()
        network.runNetwork()
        dealer.startFlow(AddPlayerFlow(gameUID.id.toString(), playerB.info.legalIdentities.first())).toCompletableFuture()

        network.runNetwork()

        dealer.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Dealt.name)).toCompletableFuture()
        network.runNetwork()
        val playerAVault = playerA.services.vaultService.queryBy<PlayerState>()
        val playerAstateAndRef = playerAVault.states.first()
        val playerAState = playerAstateAndRef.state.data
        assertThat(playerAState.myCards.size).isEqualTo(2)

        val playerBVault = playerB.services.vaultService.queryBy<PlayerState>()
        val playerBstateAndRef = playerBVault.states.first()
        val playerBState = playerBstateAndRef.state.data
        assertThat(playerBState.myCards.size).isEqualTo(2)
    }

    @Test
    fun `Flop and the table cards are three cards`() {
        val notaryNode = network.defaultNotaryNode.info.legalIdentities.first()
        val startGameFlow = dealer.startFlow(StartGroupChat(notaryNode)).toCompletableFuture()
        network.runNetwork()
        val gameUID = startGameFlow.getOrThrow()

        dealer.startFlow(AddPlayerFlow(gameUID.id.toString(), playerA.info.legalIdentities.first())).toCompletableFuture()
        network.runNetwork()
        dealer.startFlow(AddPlayerFlow(gameUID.id.toString(), playerB.info.legalIdentities.first())).toCompletableFuture()
        network.runNetwork()
        dealer.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Dealt.name)).toCompletableFuture()
        network.runNetwork()
        dealer.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Flopped.name)).toCompletableFuture()
        network.runNetwork()

        val dealerAVault = dealer.services.vaultService.queryBy<GroupChatState>()
        val dealerStateAndRef = dealerAVault.states.first()
        val gameState = dealerStateAndRef.state.data
        assertThat(gameState.tableCards.size).isEqualTo(3)
    }

    @Test
    fun `call River after deal+flop and the table cards are four cards`() {
        val notaryNode = network.defaultNotaryNode.info.legalIdentities.first()
        val startGameFlow = dealer.startFlow(StartGroupChat(notaryNode)).toCompletableFuture()
        network.runNetwork()
        val gameUID = startGameFlow.getOrThrow()

        dealer.startFlow(AddPlayerFlow(gameUID.id.toString(), playerA.info.legalIdentities.first())).toCompletableFuture()
        network.runNetwork()
        dealer.startFlow(AddPlayerFlow(gameUID.id.toString(), playerB.info.legalIdentities.first())).toCompletableFuture()
        network.runNetwork()
        dealer.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Dealt.name)).toCompletableFuture()
        network.runNetwork()
        dealer.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Flopped.name)).toCompletableFuture()
        network.runNetwork()
        dealer.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Rivered.name)).toCompletableFuture()
        network.runNetwork()

        val dealerAVault = dealer.services.vaultService.queryBy<GroupChatState>()
        val dealerStateAndRef = dealerAVault.states.first()
        val gameState = dealerStateAndRef.state.data
        assertThat(gameState.tableCards.size).isEqualTo(4)
    }

    @Test
    fun `call Turn after deal+flop+river and the table cards are five cards`() {
        val notaryNode = network.defaultNotaryNode.info.legalIdentities.first()
        val startGameFlow = dealer.startFlow(StartGroupChat(notaryNode)).toCompletableFuture()
        network.runNetwork()
        val gameUID = startGameFlow.getOrThrow()

        dealer.startFlow(AddPlayerFlow(gameUID.id.toString(), playerA.info.legalIdentities.first())).toCompletableFuture()
        network.runNetwork()
        dealer.startFlow(AddPlayerFlow(gameUID.id.toString(), playerB.info.legalIdentities.first())).toCompletableFuture()
        network.runNetwork()
        dealer.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Dealt.name)).toCompletableFuture()
        network.runNetwork()
        dealer.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Flopped.name)).toCompletableFuture()
        network.runNetwork()
        dealer.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Rivered.name)).toCompletableFuture()
        network.runNetwork()
        dealer.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Turned.name)).toCompletableFuture()
        network.runNetwork()

        val dealerAVault = dealer.services.vaultService.queryBy<GroupChatState>()
        val dealerStateAndRef = dealerAVault.states.first()
        val gameState = dealerStateAndRef.state.data
        assertThat(gameState.tableCards.size).isEqualTo(5)
    }

    @Test
    fun `Decide winner after deal+flop+river+turn`() {
        val notaryNode = network.defaultNotaryNode.info.legalIdentities.first()
        val startGameFlow = dealer.startFlow(StartGroupChat(notaryNode)).toCompletableFuture()
        network.runNetwork()
        val gameUID = startGameFlow.getOrThrow()

        dealer.startFlow(AddPlayerFlow(gameUID.id.toString(), playerA.info.legalIdentities.first())).toCompletableFuture()
        network.runNetwork()
        dealer.startFlow(AddPlayerFlow(gameUID.id.toString(), playerB.info.legalIdentities.first())).toCompletableFuture()
        network.runNetwork()
        dealer.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Dealt.name)).toCompletableFuture()
        network.runNetwork()
        dealer.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Flopped.name)).toCompletableFuture()
        network.runNetwork()
        dealer.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Rivered.name)).toCompletableFuture()
        network.runNetwork()
        dealer.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Turned.name)).toCompletableFuture()
        network.runNetwork()
        dealer.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Winner.name)).toCompletableFuture()
        network.runNetwork()

        val dealerAVault = dealer.services.vaultService.queryBy<GroupChatState>()
        val dealerStateAndRef = dealerAVault.states.first()
        val gameState = dealerStateAndRef.state.data
        assertThat(gameState.winner).isNotNull()
    }
}