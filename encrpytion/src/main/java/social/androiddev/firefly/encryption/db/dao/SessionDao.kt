package social.androiddev.firefly.encryption.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import social.androiddev.firefly.encryption.db.SessionEntity

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE address_name = :addressName AND device_id = :deviceId")
    fun queryByAddress(addressName: String, deviceId: Int): SessionEntity?

    @Query("SELECT device_id FROM sessions WHERE address_name = :addressName AND device_id != :notEqualDeviceId")
    fun queryDeviceIds(addressName: String, notEqualDeviceId: Int): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: SessionEntity): Long

    @Query("SELECT COUNT(*) FROM sessions WHERE address_name = :addressName AND device_id = :deviceId")
    fun queryCountByAddress(addressName: String, deviceId: Int): Int

    @Query("DELETE FROM sessions WHERE address_name = :addressName AND device_id = :deviceId")
    fun deleteByAddress(addressName: String, deviceId: Int)

    @Query("DELETE FROM sessions WHERE address_name = :addressName")
    fun deleteByAddressName(addressName: String)
}