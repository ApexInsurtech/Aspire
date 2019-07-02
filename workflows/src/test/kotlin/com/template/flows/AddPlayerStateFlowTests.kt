package com.template.flows

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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AddPlayerStateFlowTests {
    companion object {
        val log = loggerFor<AddPlayerStateFlowTests>()
    }

    private val network = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
            TestCordapp.findCordapp("com.poker.contracts"),
            TestCordapp.findCordapp("com.poker.flows")
    )))
    private val dealer = network.createNode()
    private val playerA = network.createNode()

    init {
        listOf(playerA, dealer).forEach {
            it.registerInitiatedFlow(AddPlayerAcceptor::class.java)
        }
    }

    @Before
    fun setup() = network.runNetwork()

    @After
    fun tearDown() = network.stopNodes()

    @Test
    fun `Add Player should return a UID and all its state values are initialized`() {
        val notaryNode = network.defaultNotaryNode.info.legalIdentities.first()
        val startGameFlow = dealer.startFlow(StartGroupChat(notaryNode)).toCompletableFuture()
        network.runNetwork()
        val gameUID = startGameFlow.getOrThrow()
        log.info("game id: $gameUID")
        assertNotNull(gameUID.id)

        val addPlayerFlow = dealer.startFlow(AddPlayerFlow(gameUID.id.toString(), playerA.info.legalIdentities.first())).toCompletableFuture()
        network.runNetwork()
        val playerUID = addPlayerFlow.getOrThrow()
        assertNotNull(playerUID.id)
        val playerVault = playerA.services.vaultService.queryBy<PlayerState>()
        assertTrue(playerVault.states.size == 1)
        val stateAndRef = playerVault.states.first()
        assertTrue(stateAndRef.state.notary == notaryNode)
        val playerState = stateAndRef.state.data
        assertTrue(playerState.myCards.isEmpty())
        assertTrue(playerState.participants.size == 2)
    }
}