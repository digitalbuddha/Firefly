package com.androiddev.social.timeline.data

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.androiddev.social.AppScope
import com.androiddev.social.SingleIn
import com.androiddev.social.auth.data.LoggedInAccounts
import com.androiddev.social.auth.data.LoggedInAccountsSerializer
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides

 val Context.dataStore by dataStore("account_preferences", LoggedInAccountsSerializer)

@ContributesTo(AppScope::class)
@Module
class AccountModule {
    @Provides
    @SingleIn(AppScope::class)
    fun provide(
        context: Application,
    ): DataStore<LoggedInAccounts> {
        return context.dataStore
    }
}

