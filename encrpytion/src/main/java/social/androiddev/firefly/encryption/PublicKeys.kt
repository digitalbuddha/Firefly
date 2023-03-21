package social.androiddev.firefly.encryption

import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.ecc.ECPublicKey

data class PublicKeys(
    val address: SignalProtocolAddress,
    val registrationId: Int,
    val preKeyId: Int,
    val preKeyPublic: ECPublicKey,
    val signPreKeyId: Int,
    val signedPreKeyPublic: ECPublicKey,
    val signedPreKeySignature: ByteArray,
    val identityKey: IdentityKey
)