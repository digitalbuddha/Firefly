package com.androiddev.social.auth.data

import com.androiddev.social.*
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.anvil.annotations.ContributesTo
import org.mobilenativefoundation.store.cache5.CacheBuilder
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject


interface UserManager {
    fun userComponentFor(accessTokenRequest: AccessTokenRequest): UserComponent
    fun userComponentFor(code: String): UserComponent
}

@ContributesTo(AppScope::class)
interface UserManagerProvider {
    fun getUserManager(): UserManager
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class RealUserManager @Inject constructor(val app: FireflyApp) : UserManager {
    val cache = CacheBuilder<String, UserComponent>()
        .build()
    val atomicReference = AtomicReference<UserComponent>()


    override fun userComponentFor(accessTokenRequest: AccessTokenRequest): UserComponent {
        return cache.getOrPut(accessTokenRequest.code) {
            (app.component as UserParentComponent).createUserComponent()
                .userComponent(accessTokenRequest = accessTokenRequest)
        }.also {
            atomicReference.set(it)
        }
    }

    override fun userComponentFor(code: String): UserComponent {
        return cache.getIfPresent(code)!!
    }
}