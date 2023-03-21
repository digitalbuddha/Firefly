package social.androiddev.firefly.encryption

import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.state.SignedPreKeyRecord

data class DeviceKeyBundle(
    val registrationId: Int,
    val address: SignalProtocolAddress,
    val identityKeyPair: IdentityKeyPair,
    val preKeys: List<PreKeyRecord>,
    val signedPreKey: SignedPreKeyRecord
)