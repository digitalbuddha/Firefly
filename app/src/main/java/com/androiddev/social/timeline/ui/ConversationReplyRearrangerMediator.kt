package com.androiddev.social.timeline.ui

import com.androiddev.social.AuthRequiredScope
import com.androiddev.social.timeline.data.StatusDB
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

interface ConversationReplyRearrangerMediator {

    fun rearrangeConversations(
        after: List<StatusDB>,
        parentStatusId: String
    ): List<StatusDB>
}

@ContributesBinding(
    AuthRequiredScope::class,
    boundType = ConversationReplyRearrangerMediator::class
)
class RealConversationReplyRearrangerMediator @Inject constructor() :
    ConversationReplyRearrangerMediator {

    private fun addIndentionToAfterStatus(
        after: List<StatusDB>,
        repliesGraph: Map<String, List<StatusDB>>,
        levelCounter: Int,
    ): Sequence<StatusDB> = after.asSequence().flatMap {
        if (repliesGraph.containsKey(it.remoteId)) {
            sequence {
                yield(it.copy(replyIndention = levelCounter))
                yieldAll(
                    addIndentionToAfterStatus(
                        repliesGraph[it.remoteId]!!, repliesGraph, levelCounter + 1
                    )
                )
            }
        } else {
            sequenceOf(it.copy(replyIndention = levelCounter))
        }
    }

    override fun rearrangeConversations(
        after: List<StatusDB>,
        parentStatusId: String
    ): List<StatusDB> {
        val repliesGraph = after.groupBy { it.inReplyTo ?: parentStatusId }

        val statusDBS = repliesGraph[parentStatusId]?.let {
            addIndentionToAfterStatus(it, repliesGraph, 0).toList()
        } ?: emptyList()
        return statusDBS
    }
}
