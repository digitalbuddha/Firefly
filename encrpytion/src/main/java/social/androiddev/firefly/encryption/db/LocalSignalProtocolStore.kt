package social.androiddev.firefly.encryption.db

import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.InvalidKeyIdException
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.state.IdentityKeyStore
import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.state.SessionRecord
import org.whispersystems.libsignal.state.SignalProtocolStore
import org.whispersystems.libsignal.state.SignedPreKeyRecord

class LocalSignalProtocolStore(
    private val mIdentityKeyStore: LocalIdentityKeyStore,
    private val mPreKeyStore: LocalPreKeyStore,
    private val mSignedPreKeyStore: LocalSignedPreKeyStore,
    private val mSessionStore: LocalSessionStore
) : SignalProtocolStore {
    fun setLocalIdentity(identityKeyPair: IdentityKeyPair?, registrationId: Int, name: String) {
        mIdentityKeyStore.setLocalIdentity(identityKeyPair!!, registrationId, name)
    }

    fun getLocalIdentity() =
        mIdentityKeyStore.getLocalIdentity()

    //TODO add logic to add more prekeys when non left
    fun getNextPreKey(): PreKeyEntity = run {
        val mPreKeyDao = mPreKeyStore.mPreKeyDao
        mPreKeyDao.getNextPreKey()!!.also {
            mPreKeyDao.update(preKeyId = it.preKeyId, update = true)
        }
    }


    // IdentityKey
    override fun getIdentityKeyPair(): IdentityKeyPair {
        return mIdentityKeyStore.identityKeyPair!!
    }

    override fun getLocalRegistrationId(): Int {
        return mIdentityKeyStore.localRegistrationId
    }

    override fun saveIdentity(address: SignalProtocolAddress, identityKey: IdentityKey): Boolean {
        return mIdentityKeyStore.saveIdentity(address, identityKey)
    }

    override fun isTrustedIdentity(
        address: SignalProtocolAddress,
        identityKey: IdentityKey,
        direction: IdentityKeyStore.Direction
    ): Boolean {
        return mIdentityKeyStore.isTrustedIdentity(address, identityKey, direction)
    }

    override fun getIdentity(address: SignalProtocolAddress): IdentityKey {
        return mIdentityKeyStore.getIdentity(address)!!
    }

    // PreKey
    @Throws(InvalidKeyIdException::class)
    override fun loadPreKey(preKeyId: Int): PreKeyRecord {
        return mPreKeyStore.loadPreKey(preKeyId)
    }

    override fun storePreKey(preKeyId: Int, record: PreKeyRecord) {
        mPreKeyStore.storePreKey(preKeyId, record)
    }

    override fun containsPreKey(preKeyId: Int): Boolean {
        return mPreKeyStore.containsPreKey(preKeyId)
    }

    override fun removePreKey(preKeyId: Int) {
        mPreKeyStore.removePreKey(preKeyId)
    }

    // Signed PreKey
    @Throws(InvalidKeyIdException::class)
    override fun loadSignedPreKey(signedPreKeyId: Int): SignedPreKeyRecord {
        return mSignedPreKeyStore.loadSignedPreKey(signedPreKeyId)
    }

    override fun loadSignedPreKeys(): List<SignedPreKeyRecord> {
        return mSignedPreKeyStore.loadSignedPreKeys()
    }

    override fun storeSignedPreKey(signedPreKeyId: Int, record: SignedPreKeyRecord) {
        mSignedPreKeyStore.storeSignedPreKey(signedPreKeyId, record)
    }

    override fun containsSignedPreKey(signedPreKeyId: Int): Boolean {
        return mSignedPreKeyStore.containsSignedPreKey(signedPreKeyId)
    }

    override fun removeSignedPreKey(signedPreKeyId: Int) {
        mSignedPreKeyStore.removeSignedPreKey(signedPreKeyId)
    }

    // SessionStore
    override fun loadSession(address: SignalProtocolAddress): SessionRecord {
        return mSessionStore.loadSession(address)
    }

    override fun getSubDeviceSessions(name: String): List<Int> {
        return mSessionStore.getSubDeviceSessions(name)
    }

    override fun storeSession(address: SignalProtocolAddress, record: SessionRecord) {
        mSessionStore.storeSession(address, record)
    }

    override fun containsSession(address: SignalProtocolAddress): Boolean {
        return mSessionStore.containsSession(address)
    }

    override fun deleteSession(address: SignalProtocolAddress) {
        mSessionStore.deleteSession(address)
    }

    override fun deleteAllSessions(name: String) {
        mSessionStore.deleteAllSessions(name)
    }
}