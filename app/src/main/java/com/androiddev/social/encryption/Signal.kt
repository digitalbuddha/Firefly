package com.androiddev.social.encryption

import android.util.Base64
import kotlinx.serialization.Serializable
import org.whispersystems.libsignal.DuplicateMessageException
import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.InvalidKeyException
import org.whispersystems.libsignal.InvalidKeyIdException
import org.whispersystems.libsignal.InvalidMessageException
import org.whispersystems.libsignal.LegacyMessageException
import org.whispersystems.libsignal.NoSessionException
import org.whispersystems.libsignal.SessionBuilder
import org.whispersystems.libsignal.SessionCipher
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.UntrustedIdentityException
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.ecc.ECPublicKey
import org.whispersystems.libsignal.protocol.PreKeySignalMessage
import org.whispersystems.libsignal.state.PreKeyBundle
import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.state.SignedPreKeyRecord
import org.whispersystems.libsignal.state.impl.InMemorySignalProtocolStore
import org.whispersystems.libsignal.util.KeyHelper


class Person(val name: String, deviceId: Int, signedPreKeyId: Int) {
    var identityKeyPair: IdentityKeyPair
    var registrationId: Int
    var preKeys: List<PreKeyRecord>
    var signedPreKey: SignedPreKeyRecord
    var address: SignalProtocolAddress

    init {
        identityKeyPair = KeyHelper.generateIdentityKeyPair()
        registrationId = KeyHelper.generateRegistrationId(false)
        preKeys = KeyHelper.generatePreKeys(1, 100)
        signedPreKey = KeyHelper.generateSignedPreKey(identityKeyPair, signedPreKeyId)
        address = SignalProtocolAddress(name, deviceId)
    }
}

@Serializable
data class PublicKey(
    val registrationId: Int,
    val devideId: Int,
    val prekeyId: Int,
    val preKeyPublicKey: String,
    val signedPreKeyId: Int,
    val signedPreKeyPublicKey: String,
    val signedPreKeySignature: String,
    val identityKeyParPublicKey:String,
    val name:String

)

@Throws(UntrustedIdentityException::class, InvalidKeyException::class)
fun encrypt(sender: Person, recipient: PublicKey): SessionCipher {
    val protocolStore =
        InMemorySignalProtocolStore(sender.identityKeyPair, sender.registrationId)
    val signalProtocolAddress = SignalProtocolAddress(recipient.name, recipient.devideId)
    val sessionBuilder = SessionBuilder(protocolStore, signalProtocolAddress)
    val preKeyBundle = PreKeyBundle(
        recipient.registrationId,
        recipient.devideId,
        recipient.prekeyId,
        Curve.decodePoint(decode(recipient.preKeyPublicKey),0),
        recipient.signedPreKeyId,
        Curve.decodePoint(decode(recipient.signedPreKeyPublicKey),0),
                decode(recipient.signedPreKeySignature),
       IdentityKey(Curve.decodePoint(decode(recipient.identityKeyParPublicKey),0)
    ))
    sessionBuilder.process(preKeyBundle)
    val cipher = SessionCipher(protocolStore,signalProtocolAddress)
    return cipher

}

private fun decode(result: String): ByteArray? = Base64.decode(result, Base64.DEFAULT)

 fun encode2(publicKey: ECPublicKey) =
    String(Base64.encode(publicKey.serialize(), Base64.DEFAULT))


@Throws(
    UntrustedIdentityException::class,
    InvalidKeyException::class,
    NoSessionException::class,
    DuplicateMessageException::class,
    InvalidMessageException::class,
    LegacyMessageException::class,
    InvalidKeyIdException::class
)
fun decrypt(sender: Person, recipient: Person, message: PreKeySignalMessage?): ByteArray {
    val protocolStore =
        InMemorySignalProtocolStore(recipient.identityKeyPair, recipient.registrationId)
    protocolStore.storePreKey(recipient.preKeys[0].id, recipient.preKeys[0])
    protocolStore.storeSignedPreKey(recipient.signedPreKey.id, recipient.signedPreKey)
    val sessionBuilder = SessionBuilder(protocolStore, sender.address)
    val preKeyBundle = PreKeyBundle(
        sender.registrationId,
        sender.address.deviceId,
        sender.preKeys[0].id,
        sender.preKeys[0].keyPair.publicKey,
        sender.signedPreKey.id,
        sender.signedPreKey.keyPair.publicKey,
        sender.signedPreKey.signature,
        sender.identityKeyPair.publicKey
    )
    sessionBuilder.process(preKeyBundle)
    val cipher = SessionCipher(protocolStore, sender.address)
    return cipher.decrypt(message)
}