package social.androiddev.firefly.encryption.db

import org.whispersystems.libsignal.InvalidKeyIdException
import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.state.PreKeyStore
import social.androiddev.firefly.encryption.db.dao.PreKeyDao
import java.io.IOException

class LocalPreKeyStore(val mPreKeyDao: PreKeyDao) : PreKeyStore {
    @Throws(InvalidKeyIdException::class)
    override fun loadPreKey(preKeyId: Int): PreKeyRecord {
        val preKeyEntity = mPreKeyDao.queryByPreKeyId(preKeyId)
        if (preKeyEntity?.preKeyRecord != null) {
            try {
                return PreKeyRecord(preKeyEntity.preKeyRecord)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        throw InvalidKeyIdException("No such prekeyrecord!")
    }

    override fun storePreKey(preKeyId: Int, record: PreKeyRecord) {
        mPreKeyDao.insertPreKey(PreKeyEntity(preKeyId, record.serialize()))
    }

    override fun containsPreKey(preKeyId: Int): Boolean {
        return mPreKeyDao.countByPreKeyId(preKeyId) > 0
    }

    override fun removePreKey(preKeyId: Int) {
        mPreKeyDao.deleteByPreKeyId(preKeyId)
    }
}