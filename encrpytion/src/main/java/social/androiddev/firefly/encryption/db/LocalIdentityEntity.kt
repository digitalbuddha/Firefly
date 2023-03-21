package social.androiddev.firefly.encryption.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_identity")
data class LocalIdentityEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "reg_id") val registrationId: Int,
    @ColumnInfo(name = "signal_address_name") val name: String,
    @ColumnInfo(name = "id_key_pair") val identityKeyPair: ByteArray?
)
