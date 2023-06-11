package com.androiddev.social.search

import com.androiddev.social.SingleIn
import com.androiddev.social.UserScope
import com.androiddev.social.auth.data.OauthRepository
import com.androiddev.social.shared.UserApi
import com.androiddev.social.timeline.data.Account
import com.androiddev.social.timeline.data.Status
import com.androiddev.social.timeline.data.Tag
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.StoreRequest
import org.mobilenativefoundation.store.store5.StoreResponse
import javax.inject.Inject
@ContributesBinding(UserScope::class)
@SingleIn(UserScope::class)
class RealSearchRepository @Inject constructor(
    private val userApi: UserApi,
    private val oauthRepository: OauthRepository

) : SearchRepository {

    val store = StoreBuilder.from(
        Fetcher.of { searchTerm: String ->
            userApi.search(
                authHeader = oauthRepository.getAuthHeader(),
                searchTerm = searchTerm
            )
        }
    ).build()


    override fun data(searchTerm: String): Flow<StoreResponse<SearchResult>> {
        // return data from memory or disk cache AND refresh from network
        // think of this as the classic "double tap"
        // NOTE: if network is not available, return cached data only
        return store.stream(StoreRequest.cached(searchTerm, refresh = true))
    }
}

interface SearchRepository {

    fun data(searchTerm: String): Flow<StoreResponse<SearchResult>>
}

@Serializable
data class SearchResult(
    val accounts: List<Account>,
    val hashtags: List<Tag>,
    val statuses: List<Status>
)