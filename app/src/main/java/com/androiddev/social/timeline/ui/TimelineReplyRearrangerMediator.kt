package com.androiddev.social.timeline.ui

import com.androiddev.social.UserScope
import com.androiddev.social.timeline.data.StatusDB
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import javax.inject.Inject

interface TimelineReplyRearrangerMediator {

    fun rearrangeTimeline(
        statuses: List<StatusDB>,
    ): Flow<StatusDB>
}

@ContributesBinding(UserScope::class, boundType = TimelineReplyRearrangerMediator::class)
class RealTimelineReplyRearrangerMediator @Inject constructor() : TimelineReplyRearrangerMediator {

    @OptIn(FlowPreview::class)
    private fun addIndentionToDescendantStatus(
        after: List<StatusDB>,
        repliesGraph: Map<String, List<StatusDB>>,
        levelCounter: Int,
        alreadyEmittedSet: MutableSet<String>,
    ): Flow<StatusDB> = after.asFlow().flatMapConcat { status ->
        flow {
            if (repliesGraph.containsKey(status.remoteId)) {
                emitAll(
                    addIndentionToDescendantStatus(
                        repliesGraph[status.remoteId]!!,
                        repliesGraph, levelCounter + 1,
                        alreadyEmittedSet
                    )
                )
                emitStatus(status, levelCounter, alreadyEmittedSet)
            } else {
                emitStatus(status, levelCounter, alreadyEmittedSet)
            }
        }
    }

    @OptIn(FlowPreview::class)
    override fun rearrangeTimeline(
        statuses: List<StatusDB>,
    ): Flow<StatusDB> {
        val wrappedStatues = statuses.sortedBy { it.dbOrder }.asSequence()

        val repliesGraph = wrappedStatues
            .filter { !it.inReplyTo.isNullOrEmpty() }
            .groupBy { it.inReplyTo!! }

        val associateStatuses = wrappedStatues.associateBy { it.remoteId }
        val orderQueue = wrappedStatues.map { it.dbOrder }.toMutableList()
        orderQueue.reverse()
        val alreadyEmittedSet = mutableSetOf<String>()

        return wrappedStatues.asFlow()
            .flatMapConcat { status ->
                var inReplyTo = status.inReplyTo
                val ascendants = mutableListOf<StatusDB>()
                while (!inReplyTo.isNullOrEmpty() && currentCoroutineContext().isActive) {
                    val parent = associateStatuses[inReplyTo]
                    parent?.let { ascendants.add(it) }
                    inReplyTo = parent?.inReplyTo
                }

                flow {
                    var levelCounter = kotlin.math.max(0, ascendants.size - 1)
                    if (repliesGraph.containsKey(status.remoteId)) {
                        val replies = repliesGraph[status.remoteId]!!
                        emitAll(
                            addIndentionToDescendantStatus(
                                replies,
                                repliesGraph,
                                levelCounter + 1,
                                alreadyEmittedSet,
                            )
                        )
                        emitStatus(status, levelCounter, alreadyEmittedSet)
                    } else {
                        emitStatus(status, levelCounter, alreadyEmittedSet)
                    }
                    ascendants.forEach { parent ->
                        emitStatus(parent, levelCounter, alreadyEmittedSet)
                        levelCounter--
                    }
                }
            }
            .map {
                it.copy(dbOrder = orderQueue.removeLast())
            }
            .flowOn(Dispatchers.Default)
    }

    private suspend fun FlowCollector<StatusDB>.emitStatus(
        status: StatusDB,
        levelCounter: Int,
        alreadyEmittedSet: MutableSet<String>,
    ) {
        if (!alreadyEmittedSet.contains(status.remoteId)) {
            alreadyEmittedSet.add(status.remoteId)
            emit(status.copy(replyIndention = levelCounter))
        }
    }
}
