package com.androiddev.social.search

import androidx.compose.material3.ColorScheme
import androidx.paging.ExperimentalPagingApi
import com.androiddev.social.AuthRequiredScope
import com.androiddev.social.SingleIn
import com.androiddev.social.auth.data.OauthRepository
import com.androiddev.social.shared.UserApi
import com.androiddev.social.timeline.data.Account
import com.androiddev.social.timeline.data.FeedType
import com.androiddev.social.timeline.data.Tag
import com.androiddev.social.timeline.data.mapStatus
import com.androiddev.social.timeline.data.toStatusDb
import com.androiddev.social.timeline.ui.model.UI
import com.androiddev.social.ui.util.Presenter
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import org.mobilenativefoundation.store.store5.ResponseOrigin
import org.mobilenativefoundation.store.store5.StoreResponse
import java.util.Locale
import javax.inject.Inject


abstract class SearchPresenter :
    Presenter<SearchPresenter.SearchEvent, SearchPresenter.SearchModel, SearchPresenter.SearchEffect>(
        SearchModel(emptyList())
    ) {


    sealed interface SearchEvent

    //    object Load : searchEvent
    data class Init(val colorScheme: ColorScheme) : SearchEvent


    data class SearchModel(
        val accounts: List<Account>? = null,
        val hashtags: List<Tag>? = null,
        val statuses: List<UI>? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    sealed interface SearchEffect

    abstract fun onQueryTextChange(searchTerm: String)
}


@ExperimentalPagingApi
@ContributesBinding(AuthRequiredScope::class, boundType = SearchPresenter::class)
@SingleIn(AuthRequiredScope::class)
class RealSearchPresenter @Inject constructor(
    private val searchRepository: SearchRepository,
    private val userApi: UserApi,
    private val oauthRepository: OauthRepository

) : SearchPresenter() {
    //Drop 1 keeps from emitting the initial value
    private val searchInput: MutableStateFlow<String> = MutableStateFlow("")

    override suspend fun eventHandler(event: SearchEvent, scope: CoroutineScope) {
        when (event) {
            is Init -> {
                mapSearchToResults(event.colorScheme)
            }

        }
    }

    suspend fun mapSearchToResults(colorScheme: ColorScheme) {
        searchInput
            .debounce(100)
            .filter { lengthGreaterThan1(it) }
            .flatMapLatest(searchRepository::data)
            .flowOn(Dispatchers.IO)
            .catch { model = SearchModel(error = it.localizedMessage) }
            .collect { results ->
                when (results) {
                    is StoreResponse.Data -> {
                        if (results.origin == ResponseOrigin.Fetcher && results.dataOrNull()
                            == null
                        )
                            model = SearchModel(error = "Sorry no results found")
                        else {
                            val authHeader = " Bearer ${oauthRepository.getCurrent()}"
//
                            val searchResults = results.requireData()
                            model = SearchModel(
                                accounts = searchResults.accounts,
                                hashtags = searchResults.hashtags,
                                statuses = searchResults.statuses.map {
                                    it.toStatusDb(FeedType.User).mapStatus(colorScheme)
                                })
                        }
                    }

                    is StoreResponse.Loading -> {
                        model = model.copy(isLoading = true)
                    }

                    else -> {

                        if (results is StoreResponse.Error.Message) {
                            model = SearchModel(error = results.errorMessageOrNull())
                        } else if (results is StoreResponse.Error) {
                            model = model.copy(isLoading = false)
                        }
                    }
                }
            }
    }

    private fun lengthGreaterThan1(it: String): Boolean {
        if (it.length <= 1) model = SearchModel()
        return it.length > 1
    }


    override fun onQueryTextChange(searchTerm: String) {
        searchInput.tryEmit(searchTerm.lowercase(Locale.getDefault()))
    }

}