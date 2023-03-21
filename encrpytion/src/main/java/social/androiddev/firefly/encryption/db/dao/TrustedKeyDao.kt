package social.androiddev.firefly.encryption.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import social.androiddev.firefly.encryption.db.TrustedKeyEntity

@Dao
interface TrustedKeyDao {
    @Query("SELECT * FROM trusted_identities WHERE address_name = :name AND device_id = :deviceId")
    fun queryByNameAndDeviceId(name: String?, deviceId: Int): TrustedKeyEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(entity: TrustedKeyEntity?)

    @Query("SELECT COUNT(*) FROM trusted_identities WHERE address_name = :name AND device_id = :deviceId")
    fun countByNameAndDeviceId(name: String?, deviceId: Int): Int
}