package com.template.states

import com.template.contracts.PokerContract
import com.template.model.Card
import com.template.model.RankingEnum
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@BelongsToContract(PokerContract::class)
@CordaSerializable
data class PlayerState(
        override val linearId: UniqueIdentifier = UniqueIdentifier(),
        val party: Party,
        val dealer: Party,
        var myCards: List<Card> = emptyList<Card>(),
        var rankingEnum: RankingEnum = RankingEnum.HIGH_CARD,
        var highCard: Card? = null,
        var highCardRankingList: List<Card> = emptyList<Card>()
) : LinearState {
    override val participants: List<Party>
        get() = listOf(party, dealer)
}