package com.androiddev.social.timeline.ui

import com.androiddev.social.AuthRequiredScope
import com.androiddev.social.timeline.ui.model.UI
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

interface ReplyIndentionLogic {
    fun addIndentionToStatus(
        after: List<UI>,
        repliesGraph: Map<String, List<UI>>,
        levelCounter: Int,
    ): Sequence<UI>
}

@ContributesBinding(AuthRequiredScope::class, boundType = ReplyIndentionLogic::class)
class RealReplyIndentionLogic @Inject constructor() : ReplyIndentionLogic {

    override fun addIndentionToStatus(
        after: List<UI>,
        repliesGraph: Map<String, List<UI>>,
        levelCounter: Int,
    ): Sequence<UI> = after.asSequence().flatMap {
        if (repliesGraph.containsKey(it.remoteId)) {
            sequence {
                yield(it.copy(replyIndention = levelCounter))
                yieldAll(
                    addIndentionToStatus(
                        repliesGraph[it.remoteId]!!, repliesGraph, levelCounter + 1
                    )
                )
            }
        } else {
            sequenceOf(it.copy(replyIndention = levelCounter))
        }
    }
}