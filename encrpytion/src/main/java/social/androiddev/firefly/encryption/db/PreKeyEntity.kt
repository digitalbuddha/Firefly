package social.androiddev.firefly.encryption.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prekeys")
data class PreKeyEntity(
    @ColumnInfo(name = "prekey_id")
    @PrimaryKey val preKeyId: Int,
    @ColumnInfo(name = "prekey") val preKeyRecord: ByteArray,
    @ColumnInfo(name = "used") val used: Boolean = false
)