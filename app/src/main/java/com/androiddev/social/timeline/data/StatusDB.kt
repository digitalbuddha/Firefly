package com.androiddev.social.timeline.data

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Entity(tableName = "status", primaryKeys = ["type", "originalId"])
data class StatusDB(
    val type: String,
    val isDirectMessage: Boolean,
    val remoteId: String,
    val originalId: String,
    val uri: String,
    val createdAt: Long,
    val content: String,
    val accountId: String?,
    val visibility: String,
    val spoilerText: String,
    val avatarUrl: String,
    val imageUrl: String?,
    val accountAddress: String,
    val applicationName: String,
    val userName: String,
    val displayName: String,
    val repliesCount: Int?,
    val favouritesCount: Int?,
    val reblogsCount: Int?,
    val emoji: List<Emoji>,
    val accountEmojis: List<Emoji>,
    val boostedEmojis: List<Emoji>,
    val mentions: List<Mention>,
    val tags: List<Tag>,
    val boostedBy: String?, //displayName
    val boostedAvatar: String?, //displayName
    val favorited: Boolean = false,
    val boosted: Boolean = false,
    val inReplyTo: String?,
    val boostedById: String?,
    val bookmarked: Boolean,
    val attachments: List<Attachment>,
    val poll: Poll?,
//    var uid: Int = 0,
)

@Dao
interface StatusDao {
    @Query("SELECT * FROM status WHERE type = :type ORDER BY originalId Desc")
    fun getTimeline(type: String): PagingSource<Int, StatusDB>

    @Query("SELECT * FROM status WHERE type = :type AND accountId = :accountId ORDER BY remoteId Desc")
    fun getUserTimeline(type: String, accountId: String): PagingSource<Int, StatusDB>


    @Query("SELECT * FROM status ORDER BY remoteId Asc Limit 1")
    fun getHomeTimelineLast(): StatusDB?

    @Query("SELECT * FROM status WHERE remoteId = :remoteId ORDER BY remoteId Asc Limit 1")
    fun getStatusBy(remoteId: String): Flow<StatusDB>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(statuses: List<StatusDB>)

    @Query(
        """UPDATE status 
            SET boosted = :boosted, reblogsCount = :replyCount, boostedAvatar = :boostedAvatar, boostedBy = :boostedName, boostedById = :boostedId
        WHERE  (originalId = :statusId OR remoteId = :statusId)"""
    )
    fun setBoosted(
        replyCount: Int,
        statusId: String,
        boosted: Boolean,
        boostedId: String,
        boostedName: String,
        boostedAvatar: String
    )

    @Query(
        """UPDATE status
            SET
              type= :type,
              isDirectMessage= :isDirectMessage,
              uri= :uri,
              createdAt= :createdAt,
              content= :content,
              accountId= :accountId,
              visibility= :visibility,
              spoilerText= :spoilerText,
              avatarUrl= :avatarUrl,
              imageUrl= :imageUrl,
              accountAddress= :accountAddress,
              applicationName= :applicationName,
              userName= :userName,
              displayName= :displayName,
              repliesCount= :repliesCount,
              favouritesCount= :favouritesCount,
              reblogsCount= :reblogsCount,
              emoji= :emoji,
              accountEmojis= :accountEmojis,
              boostedEmojis= :boostedEmojis,
              mentions= :mentions,
              tags= :tags,
              boostedBy= :boostedBy,
              boostedAvatar= :boostedAvatar,
              favorited= :favorited,
              boosted= :boosted,
              inReplyTo= :inReplyTo,
              boostedById= :boostedById,
              bookmarked= :bookmarked,
              attachments= :attachments,
              poll= :poll
        WHERE  (originalId = :statusId OR remoteId = :statusId)"""
    )
    fun updateStatus(
        statusId: String,
        type: String,
        isDirectMessage: Boolean,
        uri: String,
        createdAt: Long,
        content: String,
        accountId: String?,
        visibility: String,
        spoilerText: String,
        avatarUrl: String,
        imageUrl: String?,
        accountAddress: String,
        applicationName: String,
        userName: String,
        displayName: String,
        repliesCount: Int?,
        favouritesCount: Int?,
        reblogsCount: Int?,
        emoji: List<Emoji>,
        accountEmojis: List<Emoji>,
        boostedEmojis: List<Emoji>,
        mentions: List<Mention>,
        tags: List<Tag>,
        boostedBy: String?,
        boostedAvatar: String?,
        favorited: Boolean,
        boosted: Boolean,
        inReplyTo: String?,
        boostedById: String?,
        bookmarked: Boolean,
        attachments: List<Attachment>,
        poll: Poll?,
    )

    @Query(
        """UPDATE status
             SET poll = :poll
           WHERE  (originalId = :statusId OR remoteId = :statusId)
           """
    )
    fun updatePoll(
        statusId: String,
        poll: Poll,
    )

    @Query("UPDATE status SET repliesCount=:replyCount WHERE remoteId = :statusId")
    fun update(replyCount: Int, statusId: String)

    @Query("DELETE FROM status")
    fun delete()
}

fun StatusDao.updateOldStatus(newStatus: StatusDB) = with(newStatus) {
    updateStatus(
        statusId = remoteId,
        type= type,
        isDirectMessage= isDirectMessage,
        uri= uri,
        createdAt= createdAt,
        content= content,
        accountId= accountId,
        visibility= visibility,
        spoilerText= spoilerText,
        avatarUrl= avatarUrl,
        imageUrl= imageUrl,
        accountAddress= accountAddress,
        applicationName= applicationName,
        userName= userName,
        displayName= displayName,
        repliesCount= repliesCount,
        favouritesCount= favouritesCount,
        reblogsCount= reblogsCount,
        emoji= emoji,
        accountEmojis= accountEmojis,
        boostedEmojis= boostedEmojis,
        mentions= mentions,
        tags= tags,
        boostedBy= boostedBy,
        boostedAvatar= boostedAvatar,
        favorited= favorited,
        boosted= boosted,
        inReplyTo= inReplyTo,
        boostedById= boostedById,
        bookmarked= bookmarked,
        attachments= attachments,
        poll= poll
    )
}

@Database(entities = [StatusDB::class], version = 17)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun statusDao(): StatusDao
}

class Converters {
    @TypeConverter
    fun toEmoji(value: String): List<Emoji> {
        return Json.decodeFromString(ListSerializer(Emoji.serializer()), value)
    }

    @TypeConverter
    fun fromEmoji(emoji: List<Emoji>): String {
        return Json.encodeToString(ListSerializer(Emoji.serializer()), emoji)
    }

    @TypeConverter
    fun fromAttachment(value: String): List<Attachment> {
        return Json.decodeFromString(ListSerializer(Attachment.serializer()), value)
    }

    @TypeConverter
    fun toAttachment(attachment: List<Attachment>): String {
        return Json.encodeToString(ListSerializer(Attachment.serializer()), attachment)
    }


    @TypeConverter
    fun toTag(value: String): List<Tag> {
        return Json.decodeFromString(ListSerializer(Tag.serializer()), value)
    }

    @TypeConverter
    fun fromTag(tag: List<Tag>): String {
        return Json.encodeToString(ListSerializer(Tag.serializer()), tag)
    }

    @TypeConverter
    fun toMention(value: String): List<Mention> {
        return Json.decodeFromString(ListSerializer(Mention.serializer()), value)
    }

    @TypeConverter
    fun fromMention(mention: List<Mention>): String {
        return Json.encodeToString(ListSerializer(Mention.serializer()), mention)
    }

    @TypeConverter
    fun toPoll(value: String?): Poll? {
        return value?.let { p -> Json.decodeFromString(Poll.serializer(), p) }
    }

    @TypeConverter
    fun fromPoll(poll: Poll?): String? {
        return poll?.let { p -> Json.encodeToString(Poll.serializer(), p) }
    }
}