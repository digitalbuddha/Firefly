package com.androiddev.social.auth.data

import com.androiddev.social.*
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.anvil.annotations.ContributesTo
import org.mobilenativefoundation.store.cache5.CacheBuilder
import javax.inject.Inject


interface UserManager {
    fun userComponentFor(accessTokenRequest: AccessTokenRequest): UserComponent
}

@ContributesTo(AppScope::class)
interface UserManagerProvider {
    fun getUserManager(): UserManager
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class RealUserManager @Inject constructor(val app: EbonyApp) : UserManager {
    val cache = CacheBuilder<String, UserComponent>()
        .build()

    override fun userComponentFor(accessTokenRequest: AccessTokenRequest): UserComponent {
        return cache.getOrPut(accessTokenRequest.domain!!) {
            (app.component as UserParentComponent).createUserComponent()
                .userComponent(accessTokenRequest = accessTokenRequest)
        }
    }
}