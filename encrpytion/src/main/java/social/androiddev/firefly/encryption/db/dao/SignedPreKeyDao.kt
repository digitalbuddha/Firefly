package social.androiddev.firefly.encryption.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import social.androiddev.firefly.encryption.db.SignedPreKeyEntity

@Dao
interface SignedPreKeyDao {
    @Query("SELECT * FROM signed_prekeys")
    fun queryAll(): List<SignedPreKeyEntity>

    @Query("SELECT * FROM signed_prekeys WHERE id = :signedPreKeyId")
    fun queryById(signedPreKeyId: Int): SignedPreKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(signedPreKeyEntity: SignedPreKeyEntity)

    @Query("DELETE FROM signed_prekeys WHERE id = :signedPreKeyId")
    fun deleteById(signedPreKeyId: Int)

    @Query("SELECT COUNT(*) FROM signed_prekeys WHERE id = :signedPreKeyId")
    fun queryCountById(signedPreKeyId: Int): Int
}