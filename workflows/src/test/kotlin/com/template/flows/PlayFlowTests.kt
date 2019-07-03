package com.template.flows

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.template.model.RoundEnum
import com.template.states.GroupChatState
import com.template.states.MemberState
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
    private val moderator = network.createNode()
    private val memberA = network.createNode()
    private val memberB = network.createNode()

    init {
        listOf(memberA, memberB, moderator).forEach {
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
        val startGameFlow = moderator.startFlow(StartGroupChat(notaryNode)).toCompletableFuture()
        network.runNetwork()
        val gameUID = startGameFlow.getOrThrow()


        moderator.startFlow(AddGroupMemberFlow(gameUID.id.toString(), memberA.info.legalIdentities.first())).toCompletableFuture()
        network.runNetwork()
        moderator.startFlow(AddGroupMemberFlow(gameUID.id.toString(), memberB.info.legalIdentities.first())).toCompletableFuture()

        network.runNetwork()

        moderator.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Dealt.name)).toCompletableFuture()
        network.runNetwork()
        val memberAVault = memberA.services.vaultService.queryBy<MemberState>()
        val memberAstateAndRef = memberAVault.states.first()
        val memberAState = memberAstateAndRef.state.data
        assertThat(memberAState.myCards.size).isEqualTo(2)

        val memberBVault = memberB.services.vaultService.queryBy<MemberState>()
        val memberBstateAndRef = memberBVault.states.first()
        val memberBState = memberBstateAndRef.state.data
        assertThat(memberBState.myCards.size).isEqualTo(2)
    }

    @Test
    fun `Flop and the table cards are three cards`() {
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
        moderator.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Flopped.name)).toCompletableFuture()
        network.runNetwork()

        val moderatorAVault = moderator.services.vaultService.queryBy<GroupChatState>()
        val moderatorStateAndRef = moderatorAVault.states.first()
        val gameState = moderatorStateAndRef.state.data
        assertThat(gameState.tableCards.size).isEqualTo(3)
    }

    @Test
    fun `call River after deal+flop and the table cards are four cards`() {
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
        moderator.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Flopped.name)).toCompletableFuture()
        network.runNetwork()
        moderator.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Rivered.name)).toCompletableFuture()
        network.runNetwork()

        val moderatorAVault = moderator.services.vaultService.queryBy<GroupChatState>()
        val moderatorStateAndRef = moderatorAVault.states.first()
        val gameState = moderatorStateAndRef.state.data
        assertThat(gameState.tableCards.size).isEqualTo(4)
    }

    @Test
    fun `call Turn after deal+flop+river and the table cards are five cards`() {
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
        moderator.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Flopped.name)).toCompletableFuture()
        network.runNetwork()
        moderator.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Rivered.name)).toCompletableFuture()
        network.runNetwork()
        moderator.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Turned.name)).toCompletableFuture()
        network.runNetwork()

        val moderatorAVault = moderator.services.vaultService.queryBy<GroupChatState>()
        val moderatorStateAndRef = moderatorAVault.states.first()
        val gameState = moderatorStateAndRef.state.data
        assertThat(gameState.tableCards.size).isEqualTo(5)
    }

    @Test
    fun `Decide winner after deal+flop+river+turn`() {
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
        moderator.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Flopped.name)).toCompletableFuture()
        network.runNetwork()
        moderator.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Rivered.name)).toCompletableFuture()
        network.runNetwork()
        moderator.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Turned.name)).toCompletableFuture()
        network.runNetwork()
        moderator.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Winner.name)).toCompletableFuture()
        network.runNetwork()

        val moderatorAVault = moderator.services.vaultService.queryBy<GroupChatState>()
        val moderatorStateAndRef = moderatorAVault.states.first()
        val gameState = moderatorStateAndRef.state.data
        assertThat(gameState.winner).isNotNull()
    }
}