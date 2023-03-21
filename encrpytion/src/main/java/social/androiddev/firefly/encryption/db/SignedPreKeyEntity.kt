package social.androiddev.firefly.encryption.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "signed_prekeys")
data class SignedPreKeyEntity(
    @ColumnInfo(name = "id") @PrimaryKey val id: Int,
    @ColumnInfo(name = "signed_prekey") val signedPreKeyRecord: ByteArray
)