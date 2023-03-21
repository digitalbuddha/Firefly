package social.androiddev.firefly.encryption.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trusted_identities")
class TrustedKeyEntity(
    @ColumnInfo(name = "address_name") @PrimaryKey val addressName: String,
    @ColumnInfo(name = "device_id") val deviceId: Int,
    @ColumnInfo(name = "identity_key") val identityKey: ByteArray
)