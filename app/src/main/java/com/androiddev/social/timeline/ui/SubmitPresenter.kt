package com.androiddev.social.timeline.ui

import android.content.res.AssetFileDescriptor
import android.net.Uri
import android.webkit.MimeTypeMap
import com.androiddev.social.AuthRequiredScope
import com.androiddev.social.SingleIn
import com.androiddev.social.auth.data.OauthRepository
import com.androiddev.social.shared.UserApi
import com.androiddev.social.timeline.data.AccountRepository
import com.androiddev.social.timeline.data.FeedType
import com.androiddev.social.timeline.data.NewPoll
import com.androiddev.social.timeline.data.NewStatus
import com.androiddev.social.timeline.data.Poll
import com.androiddev.social.timeline.data.Status
import com.androiddev.social.timeline.data.StatusDao
import com.androiddev.social.timeline.data.toStatusDb
import com.androiddev.social.timeline.data.updateOldStatus
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
        val uris: Set<Uri>,
        val pollOptions: List<String>? = null,
        val pollExpiresIn: Int,
        val pollMultipleChoices: Boolean = false,
        val pollHideTotals: Boolean = false,
    ) : SubmitEvent

    data class BoostMessage(
        val statusId: String, val feedType: FeedType, val boosted: Boolean,
    ) : SubmitEvent

    data class Follow(val accountId: String, val unfollow: Boolean = false) :
        SubmitEvent

    data class FollowTag(val tagName: String, val unfollow: Boolean = false) :
        SubmitEvent

    data class FavoriteMessage(
        val statusId: String, val feedType: FeedType, val favourited: Boolean,
    ) : SubmitEvent

    data class BookmarkMessage(val statusId: String, val feedType: FeedType) :
        SubmitEvent

    data class SubmitModel(
        val statuses: Map<String, List<Status>>
    )

    sealed interface SubmitEffect

    data class VotePoll(val statusId: String, val pollId: String, val choices: List<Int>) : SubmitEvent

    data class DeleteStatus(val statusId: String) : SubmitEvent

    data class MuteAccount(val accountId: String, val mute: Boolean) : SubmitEvent

    data class BlockAccount(val accountId: String, val block: Boolean) : SubmitEvent
}

@ContributesBinding(AuthRequiredScope::class, boundType = SubmitPresenter::class)
@SingleIn(AuthRequiredScope::class)
class RealSubmitPresenter @Inject constructor(
    val statusDao: StatusDao,
    val api: UserApi,
    val oauthRepository: OauthRepository,
    val context: android.app.Application,
    val accountRepository: AccountRepository,
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
                                authHeader = oauthRepository.getAuthHeader(),
                                file = body
                            )
                        }
                        return@map uploadResult.getOrNull()
                    }.filterNotNull().map { it.id }

                    val poll = event.pollOptions?.takeIf {
                        ids.isEmpty() && it.isNotEmpty()
                    }?.let { options ->
                        NewPoll(
                            options = options,
                            expiresIn = event.pollExpiresIn,
                            multiple = event.pollMultipleChoices,
                            hideTotals = event.pollHideTotals,
                        )
                    }

                    val result = kotlin.runCatching {
                        val status = NewStatus(
                            mediaIds = ids,
                            status = event.content,
                            visibility = event.visibility.toLowerCase(),
                            replyStatusId = event.replyStatusId,
                            poll = poll,
                        )
                        api.newStatus(
                            authHeader = oauthRepository.getAuthHeader(),
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
                                authHeader = oauthRepository.getAuthHeader(),
                                id = event.statusId
                            )
                        } else {
                            api.favouriteStatus(
                                authHeader = oauthRepository.getAuthHeader(),
                                id = event.statusId
                            )
                        }
                    }
                    when {
                        result.isSuccess -> {
                            withContext(Dispatchers.IO) {
                                result.getOrThrow().let { newStatus ->
                                    statusDao.updateOldStatus(newStatus.toStatusDb(event.feedType))
                                }
                            }
                        }
                    }
                }

                is BookmarkMessage -> {
                    val result = kotlin.runCatching {
                        api.bookmarkStatus(
                            authHeader = oauthRepository.getAuthHeader(),
                            id = event.statusId
                        )
                    }
                    when {
                        result.isSuccess -> {
                            withContext(Dispatchers.IO) {
                                result.getOrThrow().let { newStatus ->
                                    statusDao.updateOldStatus(newStatus.toStatusDb(event.feedType))
                                }
                            }
                        }
                    }
                }

                is BoostMessage -> {
                    val result = kotlin.runCatching {
                        if (event.boosted) {
                            api.unBoostStatus(
                                authHeader = oauthRepository.getAuthHeader(),
                                id = event.statusId
                            )
                        } else {
                            api.boostStatus(
                                authHeader = oauthRepository.getAuthHeader(),
                                id = event.statusId
                            )
                        }
                    }
                    when {
                        result.isSuccess -> {
                            withContext(Dispatchers.IO) {
                                result.getOrThrow().let { newStatus ->
                                    statusDao.updateOldStatus(newStatus.toStatusDb())
                                }
                            }
                        }
                    }
                }

                is Follow -> {
                    val result =
                        if (event.unfollow) {
                            kotlin.runCatching {
                                api.unfollowAccount(
                                    authHeader = oauthRepository.getAuthHeader(),
                                    event.accountId
                                )
                            }
                        } else {
                            kotlin.runCatching {
                                api.followAccount(
                                    authHeader = oauthRepository.getAuthHeader(),
                                    accountId = event.accountId
                                )
                            }

                        }
                    when {
                        result.isSuccess -> {
                            withContext(Dispatchers.IO) {
                                accountRepository.fresh(event.accountId)
                            }
                        }
                    }
                }

                is FollowTag -> {
                    val result =
                        if (event.unfollow) {
                            kotlin.runCatching {
                                api.unfollowTag(
                                    authHeader = oauthRepository.getAuthHeader(),
                                    name = event.tagName
                                )
                            }
                        } else {
                            kotlin.runCatching {
                                api.followTag(
                                    authHeader = oauthRepository.getAuthHeader(),
                                    name = event.tagName
                                )
                            }
                        }
                }

                is VotePoll -> {
                    val result = kotlin.runCatching {
                        api.votePoll(
                            authHeader = oauthRepository.getAuthHeader(),
                            id = event.pollId,
                            choices = event.choices,
                        )
                    }
                    when {
                        result.isSuccess -> {
                            withContext(Dispatchers.IO) {
                                statusDao.updatePoll(event.statusId, result.getOrThrow())
                            }
                        }
                    }
                }

                is DeleteStatus -> {
                    val result = kotlin.runCatching {
                        api.deleteStatus(
                            authHeader = oauthRepository.getAuthHeader(),
                            id = event.statusId,
                        )
                    }
                    when {
                        result.isSuccess -> {
                            withContext(Dispatchers.IO) {
                                statusDao.delete(event.statusId)
                            }
                        }
                    }
                }

                is BlockAccount -> {
                    val result = kotlin.runCatching {
                        if (event.block) {
                            api.blockAccount(
                                authHeader = oauthRepository.getAuthHeader(),
                                id = event.accountId,
                            )
                        } else {
                            api.unblockAccount(
                                authHeader = oauthRepository.getAuthHeader(),
                                id = event.accountId,
                            )
                        }
                    }
                    when {
                        result.isSuccess -> {
                            withContext(Dispatchers.IO) {
                                accountRepository.clear(event.accountId)
                                accountRepository.get(event.accountId)
                            }
                        }
                    }
                }
                is MuteAccount -> {
                    val result = kotlin.runCatching {
                        if (event.mute) {
                            api.muteAccount(
                                authHeader = oauthRepository.getAuthHeader(),
                                id = event.accountId,
                            )
                        } else {
                            api.unMuteAccount(
                                authHeader = oauthRepository.getAuthHeader(),
                                id = event.accountId,
                            )
                        }
                    }
                    when {
                        result.isSuccess -> {
                            withContext(Dispatchers.IO) {
                                accountRepository.clear(event.accountId)
                                accountRepository.get(event.accountId)
                            }
                        }
                    }
                }
            }
        }
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
