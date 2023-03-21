package social.androiddev.firefly.encryption.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import social.androiddev.firefly.encryption.db.LocalIdentityEntity

@Dao
interface LocalIdentityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setLocalIdentity(entity: LocalIdentityEntity?)

    @Query("SELECT * FROM local_identity")
    fun query(): LocalIdentityEntity?

    @Query("SELECT reg_id FROM local_identity")
    fun queryRegistrationId(): Int
}