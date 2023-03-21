package social.androiddev.firefly.encryption

import org.whispersystems.libsignal.DuplicateMessageException
import org.whispersystems.libsignal.InvalidKeyException
import org.whispersystems.libsignal.InvalidKeyIdException
import org.whispersystems.libsignal.InvalidMessageException
import org.whispersystems.libsignal.LegacyMessageException
import org.whispersystems.libsignal.NoSessionException
import org.whispersystems.libsignal.SessionBuilder
import org.whispersystems.libsignal.SessionCipher
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.UntrustedIdentityException
import org.whispersystems.libsignal.protocol.CiphertextMessage
import org.whispersystems.libsignal.protocol.PreKeySignalMessage
import org.whispersystems.libsignal.protocol.SignalMessage
import org.whispersystems.libsignal.state.PreKeyBundle
import org.whispersystems.libsignal.state.SignalProtocolStore
import social.androiddev.firefly.encryption.db.LocalSignalProtocolStore

class Kryptonium(private val signalProtocolStore: SignalProtocolStore) {

    @Throws(UntrustedIdentityException::class, InvalidKeyException::class)
    fun storeRemoteDeviceKeys(remoteDeviceKeys: PublicKeys) {
        val sessionBuilder = SessionBuilder(signalProtocolStore, remoteDeviceKeys.address)
        val preKeyBundle = PreKeyBundle(
            remoteDeviceKeys.registrationId,
            remoteDeviceKeys.address.deviceId,
            remoteDeviceKeys.preKeyId,
            remoteDeviceKeys.preKeyPublic,
            remoteDeviceKeys.signPreKeyId,
            remoteDeviceKeys.signedPreKeyPublic,
            remoteDeviceKeys.signedPreKeySignature,
            remoteDeviceKeys.identityKey
        )
        sessionBuilder.process(preKeyBundle)
    }

    fun getLocalIdentity() = (signalProtocolStore as LocalSignalProtocolStore).getLocalIdentity()

    @Throws(UntrustedIdentityException::class)
    fun encryptFor(data: ByteArray?, address: SignalProtocolAddress?): CiphertextMessage {
        val sessionCipher = SessionCipher(signalProtocolStore, address)
        return sessionCipher.encrypt(data)
    }

    @Throws(
        InvalidKeyException::class,
        LegacyMessageException::class,
        InvalidMessageException::class,
        DuplicateMessageException::class,
        InvalidKeyIdException::class,
        UntrustedIdentityException::class
    )
    fun decryptFromPre(
        data: PreKeySignalMessage?,
        remoteAddress: SignalProtocolAddress?
    ): ByteArray {
        val sessionCipher = SessionCipher(signalProtocolStore, remoteAddress)
        return sessionCipher.decrypt(data)
    }

    @Throws(
        NoSessionException::class,
        DuplicateMessageException::class,
        InvalidMessageException::class,
        UntrustedIdentityException::class,
        LegacyMessageException::class
    )
    fun decryptFromSignal(data: SignalMessage?, remoteAddress: SignalProtocolAddress?): ByteArray {
        val sessionCipher = SessionCipher(signalProtocolStore, remoteAddress)
        return sessionCipher.decrypt(data)
    }

    @Throws(
        DuplicateMessageException::class,
        InvalidMessageException::class,
        UntrustedIdentityException::class,
        LegacyMessageException::class,
        InvalidKeyException::class,
        InvalidKeyIdException::class,
        NoSessionException::class
    )
    fun decryptFrom(data: CiphertextMessage, remoteAddress: SignalProtocolAddress?): ByteArray? {
        if (data.type == CiphertextMessage.PREKEY_TYPE) {
            return decryptFromPre(data as PreKeySignalMessage, remoteAddress)
        } else if (data.type == CiphertextMessage.WHISPER_TYPE) {
            return decryptFromSignal(data as SignalMessage, remoteAddress)
        }
        return null
    }
}