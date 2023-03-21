package social.androiddev.firefly.encryption.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import social.androiddev.firefly.encryption.db.PreKeyEntity

@Dao
interface PreKeyDao {
    @Query("SELECT * FROM prekeys WHERE prekey_id = :preKeyId")
    fun queryByPreKeyId(preKeyId: Int): PreKeyEntity?

    @Query("SELECT * FROM prekeys ORDER BY prekey_id DESC")
    fun getNextPreKey(): PreKeyEntity?

    @Query("UPDATE prekeys SET used = :update where prekey_id = :preKeyId")
    fun update(update: Boolean, preKeyId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPreKey(preKeyEntity: PreKeyEntity?)

    @Query("SELECT COUNT(prekey_id) FROM prekeys WHERE prekey_id = :preKeyId")
    fun countByPreKeyId(preKeyId: Int): Int

    @Query("DELETE FROM prekeys WHERE prekey_id = :preKeyId")
    fun deleteByPreKeyId(preKeyId: Int)
}