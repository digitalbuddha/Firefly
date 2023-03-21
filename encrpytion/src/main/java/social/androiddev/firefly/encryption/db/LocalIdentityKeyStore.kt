package social.androiddev.firefly.encryption.db

import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.InvalidKeyException
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.state.IdentityKeyStore
import social.androiddev.firefly.encryption.db.dao.LocalIdentityDao
import social.androiddev.firefly.encryption.db.dao.TrustedKeyDao
import java.util.Arrays

class LocalIdentityKeyStore(
    private val mTrustedKeyDao: TrustedKeyDao,
    private val mLocalIdentityDao: LocalIdentityDao
) : IdentityKeyStore {
    fun setLocalIdentity(identityKeyPair: IdentityKeyPair, registrationId: Int, name: String) {
        mLocalIdentityDao.setLocalIdentity(
            LocalIdentityEntity(
                0, registrationId,
                name,
                identityKeyPair.serialize(),
            )
        )
    }

    fun getLocalIdentity() =
        mLocalIdentityDao.query()


    override fun getIdentityKeyPair(): IdentityKeyPair? {
        val entity = mLocalIdentityDao.query()
        if (entity?.identityKeyPair != null) {
            try {
                return IdentityKeyPair(entity.identityKeyPair)
            } catch (e: InvalidKeyException) {
                e.printStackTrace()
            }
        }
        return null
    }

    override fun getLocalRegistrationId(): Int {
        return mLocalIdentityDao.queryRegistrationId()
    }

    override fun saveIdentity(address: SignalProtocolAddress, identityKey: IdentityKey): Boolean {
        mTrustedKeyDao.insert(
            TrustedKeyEntity(
                address.name,
                address.deviceId,
                identityKey.serialize()
            )
        )
        return true
    }

    override fun isTrustedIdentity(
        address: SignalProtocolAddress,
        identityKey: IdentityKey,
        direction: IdentityKeyStore.Direction
    ): Boolean {
        val entity = mTrustedKeyDao.queryByNameAndDeviceId(address.name, address.deviceId)
        return entity == null || Arrays.equals(entity.identityKey, identityKey.serialize())
    }

    override fun getIdentity(address: SignalProtocolAddress): IdentityKey? {
        val entity = mTrustedKeyDao.queryByNameAndDeviceId(address.name, address.deviceId)
        if (entity != null) {
            try {
                return IdentityKey(entity.identityKey, 0)
            } catch (e: InvalidKeyException) {
                e.printStackTrace()
            }
        }
        return null
    }


}