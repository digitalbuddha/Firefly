package social.androiddev.firefly.encryption.db

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "sessions", primaryKeys = ["address_name", "device_id"])
data class SessionEntity(
    @ColumnInfo(name = "address_name") val protocolAddressName: String,
    @ColumnInfo(name = "device_id") val deviceId: Int,
    @ColumnInfo(name = "session") val session: ByteArray
)