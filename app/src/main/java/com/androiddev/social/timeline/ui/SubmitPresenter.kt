package com.androiddev.social.timeline.ui

import android.content.res.AssetFileDescriptor
import android.net.Uri
import android.webkit.MimeTypeMap
import com.androiddev.social.AuthRequiredScope
import com.androiddev.social.SingleIn
import com.androiddev.social.auth.data.OauthRepository
import com.androiddev.social.shared.UserApi
import com.androiddev.social.timeline.data.FeedType
import com.androiddev.social.timeline.data.NewStatus
import com.androiddev.social.timeline.data.Status
import com.androiddev.social.timeline.data.StatusDao
import com.androiddev.social.timeline.data.toStatusDb
import com.androiddev.social.ui.util.Presenter
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import social.androiddev.firefly.R
import java.io.IOException
import java.io.InputStream
import java.util.Date
import javax.inject.Inject


abstract class SubmitPresenter :
    Presenter<SubmitPresenter.SubmitEvent, SubmitPresenter.SubmitModel, SubmitPresenter.SubmitEffect>(
        SubmitModel(emptyMap())
    ) {
    sealed interface SubmitEvent
    data class PostMessage(
        val content: String,
        val visibility: String,
        val replyStatusId: String? = null,
        val replyCount: Int = 0,
        val uris: Set<Uri>
    ) : SubmitEvent


    data class BoostMessage(val statusId: String, val feedType: FeedType, val boosted: Boolean) :
        SubmitEvent

    data class Follow(val accountId: String, val unfollow: Boolean = false) :
        SubmitEvent

    data class FollowTag(val tagName: String, val unfollow: Boolean = false) :
        SubmitEvent

    data class FavoriteMessage(val statusId: String, val feedType: FeedType, val favourited: Boolean) :
        SubmitEvent

    data class BookmarkMessage(val statusId: String) :
        SubmitEvent

    data class SubmitModel(
        val statuses: Map<String, List<Status>>
    )

    sealed interface SubmitEffect
}

@ContributesBinding(AuthRequiredScope::class, boundType = SubmitPresenter::class)
@SingleIn(AuthRequiredScope::class)
class RealSubmitPresenter @Inject constructor(
    val statusDao: StatusDao,
    val api: UserApi,
    val oauthRepository: OauthRepository,
    val context: android.app.Application
) : SubmitPresenter() {

    override suspend fun eventHandler(event: SubmitEvent, coroutineScope: CoroutineScope): Unit =
        withContext(Dispatchers.IO) {
            when (event) {
                is PostMessage -> {
                    val ids: List<String> = event.uris.map { uri ->
                        var mimeType = context.contentResolver.getType(uri)
                        val stream = context.contentResolver.openInputStream(uri)

                        val map = MimeTypeMap.getSingleton()
                        val fileExtension = map.getExtensionFromMimeType(mimeType)
                        val filename = "%s_%s.%s".format(
                            context.getString(R.string.app_name),
                            Date().time.toString(),
                            fileExtension
                        )


                        if (mimeType == null) mimeType = "multipart/form-data"

                        val fileDescriptor: AssetFileDescriptor? =
                            context.getContentResolver()
                                .openAssetFileDescriptor(uri, "r")
                        val fileSize = fileDescriptor?.length
                        val fileBody = ProgressRequestBody(
                            content = stream!!,
                            contentLength = fileSize!!,
                            mediaType = mimeType.toMediaTypeOrNull()!!
                        )

                        val body: MultipartBody.Part =
                            MultipartBody.Part.createFormData("file", filename, fileBody)

                        val uploadResult = kotlin.runCatching {
                            api.upload(
                                authHeader = authHeader(),
                                file = body
                            )
                        }
                        return@map uploadResult.getOrNull()
                    }.filterNotNull().map { it.id }

                    val result = kotlin.runCatching {
                        val status = NewStatus(
                            mediaIds = ids,
                            status = event.content,
                            visibility = event.visibility.toLowerCase(),
                            replyStatusId = event.replyStatusId,
                        )
                        api.newStatus(
                            authHeader = authHeader(),
                            status = status
                        )
                    }
                    when {
                        result.isSuccess -> {
                            withContext(Dispatchers.IO) {
                                event.replyStatusId?.let {
                                    statusDao.update(
                                        event.replyCount + 1, it
                                    )
                                }
                            }
                        }
                    }
                }

                is FavoriteMessage -> {
                    val result = kotlin.runCatching {
                        if (event.favourited) {
                            api.unfavouriteStatus(
                                authHeader = authHeader(),
                                id = event.statusId
                            )
                        } else {
                            api.favouriteStatus(
                                authHeader = authHeader(),
                                id = event.statusId
                            )
                        }
                    }
                    when {
                        result.isSuccess -> {
                            withContext(Dispatchers.IO) {
                                statusDao.insertAll(
                                    listOf(result.getOrThrow().toStatusDb(event.feedType))
                                )
                            }
                        }
                    }
                }

                is BookmarkMessage -> {
                    val result = kotlin.runCatching {
                        api.bookmarkStatus(
                            authHeader = authHeader(),
                            id = event.statusId
                        )
                    }
                    when {
                        result.isSuccess -> {
                            withContext(Dispatchers.IO) {
//                            statusDao.insertAll(
//                                listOf(result.getOrThrow().toStatusDb(event.feedType))
//                            )
                            }
                        }
                    }
                }

                is BoostMessage -> {
                    val result = kotlin.runCatching {
                        if (event.boosted) {
                            api.unBoostStatus(
                                authHeader = authHeader(),
                                id = event.statusId
                            )
                        } else {
                            api.boostStatus(
                                authHeader = authHeader(),
                                id = event.statusId
                            )
                        }
                    }
                    when {
                        result.isSuccess -> {
                            withContext(Dispatchers.IO) {
                                result.getOrThrow().reblog?.let {
                                    statusDao.insertAll(listOf(it.toStatusDb(event.feedType)))
                                }
                                val newStatus = result.getOrThrow()
                                statusDao.setBoosted(
                                    replyCount = newStatus.reblog!!.reblogsCount ?: 0,
                                    statusId = newStatus.reblog.id,
                                    boosted = newStatus.reblogged ?: false,
                                    boostedId = newStatus.account!!.id,
                                    boostedAvatar = newStatus.account.avatar,
                                    boostedName = newStatus.account.displayName
                                )
                            }
                        }
                    }
                }

                is Follow -> {
                    val result =
                        if (event.unfollow) {
                            kotlin.runCatching {
                                api.unfollowAccount(
                                    authHeader = authHeader(),
                                    event.accountId
                                )
                            }
                        } else {
                            kotlin.runCatching {
                                api.followAccount(
                                    authHeader = authHeader(),
                                    accountId = event.accountId
                                )
                            }

                        }
                    when {
                        result.isSuccess -> {
                            withContext(Dispatchers.IO) {

                            }
                        }
                    }
                }

                is FollowTag -> {
                    val result =
                        if (event.unfollow) {
                            kotlin.runCatching {
                                api.unfollowTag(
                                    authHeader = authHeader(),
                                    name = event.tagName
                                )
                            }
                        } else {
                            kotlin.runCatching {
                                api.followTag(
                                    authHeader = authHeader(),
                                    name = event.tagName
                                )
                            }
                        }



                    when {
                        result.isSuccess -> {
                            withContext(Dispatchers.IO) {

                            }
                        }
                    }
                }
            }
        }

    private suspend fun authHeader() = " Bearer ${oauthRepository.getCurrent()}"
}


class ProgressRequestBody(
    private val content: InputStream,
    private val contentLength: Long,
    private val mediaType: MediaType
) :
    RequestBody() {
    interface UploadCallback {
        fun onProgressUpdate(percentage: Int)
    }

    override fun contentType(): MediaType? {
        return mediaType
    }

    override fun contentLength(): Long {
        return contentLength
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var uploaded: Long = 0
        try {
            var read: Int
            while (content.read(buffer).also { read = it } != -1) {
                uploaded += read.toLong()
                sink.write(buffer, 0, read)
            }
        } finally {
            content.close()
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048
    }
}
