package social.androiddev.firefly.encryption.db

object SignalProtocolStoreFactory {
    @JvmStatic
    fun createLocalSignalProtocolStore(db: social.androiddev.firefly.encryption.db.EncryptionDatabase): LocalSignalProtocolStore {
        val identityKeyStore = LocalIdentityKeyStore(db.trustedKeyDao, db.localIdentityDao)
        val preKeyStore = LocalPreKeyStore(db.preKeyDao)
        val signedPreKeyStore = LocalSignedPreKeyStore(db.signedPreKeyDao)
        val sessionStore = LocalSessionStore(db.sessionDao)
        return LocalSignalProtocolStore(
            identityKeyStore,
            preKeyStore,
            signedPreKeyStore,
            sessionStore
        )
    }
}