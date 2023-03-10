package com.androiddev.social




import android.app.Application
import com.androiddev.social.auth.data.AccessTokenRequest
import com.androiddev.social.auth.data.OauthRepository
import com.androiddev.social.shared.UserApi
import com.squareup.anvil.annotations.ContributesSubcomponent
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.ExperimentalAnvilApi
import com.squareup.anvil.annotations.MergeComponent
import dagger.BindsInstance
import dagger.Component
import javax.inject.Scope
import kotlin.reflect.KClass

class FireflyApp : Application() {
    val component by lazy {
        (DaggerSkeletonComponent.factory().create(this as Application, this) as AppComponent.AppParentComponent).appComponent()
    }
    override fun onCreate() {
        super.onCreate()
    }
}

@MergeComponent(SkeletonScope::class)
@SingleIn(SkeletonScope::class)
interface SkeletonComponent  {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance app2: Application,
            @BindsInstance fireflyApp: FireflyApp,
        ): SkeletonComponent
    }
}

@ContributesSubcomponent(
    scope = AppScope::class,
    parentScope = SkeletonScope::class,
)
@SingleIn(AppScope::class)
@OptIn(ExperimentalAnvilApi::class)
interface AppComponent  {
    @OptIn(ExperimentalAnvilApi::class)
    @ContributesTo(SkeletonScope::class)
    interface AppParentComponent {
        fun appComponent(): AppComponent
    }
}

@OptIn(ExperimentalAnvilApi::class)
@ContributesSubcomponent(
    scope = UserScope::class,
    parentScope = AppScope::class
)
@SingleIn(UserScope::class)
interface UserComponent  {
    @OptIn(ExperimentalAnvilApi::class)
    @ContributesSubcomponent.Factory
    interface Factory {
        fun userComponent(
            @BindsInstance accessTokenRequest: AccessTokenRequest
        ): UserComponent
    }
    fun oauthRepository():OauthRepository
    fun api():UserApi
    fun request():AccessTokenRequest
}

@ContributesTo(AppScope::class)
interface UserParentComponent {
    @OptIn(ExperimentalAnvilApi::class)
    fun createUserComponent(): UserComponent.Factory
}

@OptIn(ExperimentalAnvilApi::class)
@ContributesSubcomponent(
    scope = AuthRequiredScope::class,
    parentScope = UserScope::class
)
@SingleIn(AuthRequiredScope::class)
interface AuthRequiredComponent {

    @ContributesTo(UserScope::class)
    interface ParentComponent {
        @OptIn(ExperimentalAnvilApi::class)

        fun createAuthRequiredComponent(): AuthRequiredComponent
    }
}

interface Injector {
//    abstract fun signInPresenter(): Any
}

@OptIn(ExperimentalAnvilApi::class)
@ContributesSubcomponent(
    scope = AuthOptionalScope::class,
    parentScope = AppScope::class
)
@SingleIn(AuthOptionalScope::class)
interface AuthOptionalComponent : Injector {
    @ContributesTo(AppScope::class)
    interface ParentComponent {
        fun createAuthOptionalComponent(): AuthOptionalComponent
    }
}

@OptIn(ExperimentalAnvilApi::class)
@ContributesSubcomponent(
    scope = AuthOptionalScreenScope::class,
    parentScope = AppScope::class
)
@SingleIn(AuthOptionalScreenScope::class)
interface AuthOptionalScreenComponent : Injector {
    @ContributesTo(AppScope::class)
    interface ScreenParentComponent : Injector {
        fun createAuthOptionalScreenComponent(): AuthOptionalScreenComponent
    }
}

@OptIn(ExperimentalAnvilApi::class)
@ContributesSubcomponent(
    scope = AuthRequiredScreenScope::class,
    parentScope = UserScope::class
)
@SingleIn(AuthRequiredScreenScope::class)
interface AuthRequiredScreenComponent : Injector {
    @ContributesTo(UserScope::class)
    interface ScreenParentComponent : Injector {
        fun createAuthRequiredScreenComponent(): AuthRequiredScreenComponent
    }
}

abstract class SkeletonScope private constructor()
abstract class AppScope private constructor()
abstract class UserScope private constructor()
abstract class AuthRequiredScope private constructor()
abstract class AuthOptionalScope private constructor()
abstract class AuthOptionalScreenScope private constructor()
abstract class AuthRequiredScreenScope private constructor()
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class SingleIn(val clazz: KClass<*>)