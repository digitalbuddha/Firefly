package social.androiddev.firefly.encryption

import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.InvalidKeyException
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.util.KeyHelper

object DeviceKeyBundleUtils {
    @JvmStatic
    @Throws(InvalidKeyException::class)
    fun generateDeviceKeyBundle(
        address: SignalProtocolAddress,
        signedPreKeyId: Int,
        preKeyCount: Int,
        identityKeyPair: IdentityKeyPair
    ): DeviceKeyBundle {
        return DeviceKeyBundle(
            KeyHelper.generateRegistrationId(false),
            SignalProtocolAddress(address.name, address.deviceId),
            identityKeyPair,
            KeyHelper.generatePreKeys(1, preKeyCount),
            KeyHelper.generateSignedPreKey(identityKeyPair, signedPreKeyId)
        )
    }
}