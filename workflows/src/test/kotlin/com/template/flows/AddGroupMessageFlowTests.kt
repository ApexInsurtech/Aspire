package com.template.flows

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.template.model.RoundEnum
import com.template.states.GroupChatState
import group.chat.flows.AcceptGroupChatFlow
import group.chat.flows.AddGroupMessageFlow
import net.corda.core.node.services.queryBy
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Before
import org.junit.Test

class AddGroupMessageFlowTests {
    companion object {
        val log = loggerFor<AddGroupMessageFlowTests>()
    }

    private val network = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
            TestCordapp.findCordapp("com.poker.contracts"),
            TestCordapp.findCordapp("com.poker.flows")
    )))
    private val moderator = network.createNode()
    private val memberA = network.createNode()
    private val memberB = network.createNode()

    init {
        listOf(memberA, memberB, moderator).forEach {
            it.registerInitiatedFlow(AddPlayerAcceptor::class.java)
            it.registerInitiatedFlow(PlayFlowResponder::class.java)
            it.registerInitiatedFlow(AcceptGroupChatFlow::class.java)
        }
    }

    @Before
    fun setup() = network.runNetwork()

    @After
    fun tearDown() = network.stopNodes()

    @Test
    fun `Add a Betting amount should add the value in game state`() {
        val betAmount = ""
        val notaryNode = network.defaultNotaryNode.info.legalIdentities.first()
        val startGameFlow = moderator.startFlow(StartGroupChat(notaryNode)).toCompletableFuture()
        network.runNetwork()
        val gameUID = startGameFlow.getOrThrow()

        moderator.startFlow(AddGroupMemberFlow(gameUID.id.toString(), memberA.info.legalIdentities.first())).toCompletableFuture()
        network.runNetwork()
        moderator.startFlow(AddGroupMemberFlow(gameUID.id.toString(), memberB.info.legalIdentities.first())).toCompletableFuture()
        network.runNetwork()
        moderator.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Dealt.name)).toCompletableFuture()
        network.runNetwork()
        memberA.startFlow(AddGroupMessageFlow(gameUID.toString(), betAmount)).toCompletableFuture()
        network.runNetwork()

        val moderatorAVault = moderator.services.vaultService.queryBy<GroupChatState>()
        val moderatorStateAndRef = moderatorAVault.states.first()
        val gameState = moderatorStateAndRef.state.data
        assertThat(gameState.betAmount).isEqualTo(betAmount)
    }
}