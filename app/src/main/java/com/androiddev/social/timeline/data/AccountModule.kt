package com.androiddev.social.timeline.data

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.androiddev.social.AppScope
import com.androiddev.social.SingleIn
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides

private val Context.dataStore by preferencesDataStore("account_preferences")

@ContributesTo(AppScope::class)
@Module
class AccountModule {
    @Provides
    @SingleIn(AppScope::class)
    fun profideDataStore(
        context: Application,
    ): DataStore<Preferences> {
       return context.dataStore
    }
}