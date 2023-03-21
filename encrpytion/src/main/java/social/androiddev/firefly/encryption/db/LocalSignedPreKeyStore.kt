package social.androiddev.firefly.encryption.db

import org.whispersystems.libsignal.InvalidKeyIdException
import org.whispersystems.libsignal.state.SignedPreKeyRecord
import org.whispersystems.libsignal.state.SignedPreKeyStore
import social.androiddev.firefly.encryption.db.dao.SignedPreKeyDao
import java.io.IOException

class LocalSignedPreKeyStore(private val mSignedPreKeyDao: SignedPreKeyDao) : SignedPreKeyStore {
    @Throws(InvalidKeyIdException::class)
    override fun loadSignedPreKey(signedPreKeyId: Int): SignedPreKeyRecord {
        val entity = mSignedPreKeyDao.queryById(signedPreKeyId)
        if (entity != null) {
            try {
                return SignedPreKeyRecord(entity.signedPreKeyRecord)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        throw InvalidKeyIdException("No such signedprekey!")
    }

    override fun loadSignedPreKeys(): List<SignedPreKeyRecord> {
        val signedPreKeyRecords: MutableList<SignedPreKeyRecord> = ArrayList()
        for (entity in mSignedPreKeyDao.queryAll()) {
            try {
                signedPreKeyRecords.add(SignedPreKeyRecord(entity.signedPreKeyRecord))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return signedPreKeyRecords
    }

    override fun storeSignedPreKey(signedPreKeyId: Int, record: SignedPreKeyRecord) {
        mSignedPreKeyDao.insert(SignedPreKeyEntity(signedPreKeyId, record.serialize()))
    }

    override fun containsSignedPreKey(signedPreKeyId: Int): Boolean {
        val count = mSignedPreKeyDao.queryCountById(signedPreKeyId)
        return count > 0
    }

    override fun removeSignedPreKey(signedPreKeyId: Int) {
        mSignedPreKeyDao.deleteById(signedPreKeyId)
    }
}