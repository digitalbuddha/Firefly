package com.social.androiddev.firefly

import android.util.Log
import org.json.JSONException
import org.junit.Assert
import org.junit.Test
import org.whispersystems.libsignal.InvalidKeyException
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.util.KeyHelper
import social.androiddev.firefly.encryption.DeviceKeyBundleUtils.generateDeviceKeyBundle
import social.androiddev.firefly.encryption.PublicKeys
import social.androiddev.firefly.encryption.RemoteDeviceKeysParser.fromJson
import social.androiddev.firefly.encryption.RemoteDeviceKeysParser.toJson

class DeviceKeysTest {
    @Test
    @Throws(InvalidKeyException::class)
    fun generateDeviceKeyBundleTest() {
        val dkb = generateDeviceKeyBundle(
            SignalProtocolAddress("+6281111111111", 11228),
            1,
            100,
            KeyHelper.generateIdentityKeyPair()
        )
        Assert.assertNotNull(dkb)
        Assert.assertEquals("+6281111111111", dkb.address.name)
        Assert.assertEquals(11228, dkb.address.deviceId.toLong())
        Assert.assertEquals(1, dkb.signedPreKey.id.toLong())
        Assert.assertEquals(dkb.preKeys.size.toLong(), 100)
    }

    @Test
    @Throws(InvalidKeyException::class, JSONException::class)
    fun preKeyBundleSerializationDeserializationTest() {
        val (registrationId, address, identityKeyPair, preKeys, signedPreKey) = generateDeviceKeyBundle(
            SignalProtocolAddress("+6281111111111", 1), 1, 22, KeyHelper.generateIdentityKeyPair()
        )
        val rdk = PublicKeys(
            address,
            registrationId,
            preKeys[0].id,
            preKeys[0].keyPair.publicKey,
            signedPreKey.id,
            signedPreKey.keyPair.publicKey,
            signedPreKey.signature,
            identityKeyPair.publicKey
        )
        val keyJson1 = toJson(rdk)
        Assert.assertNotNull(keyJson1)
        val rdk2 = fromJson(keyJson1)
        Assert.assertNotNull(rdk2)
        Assert.assertEquals(rdk.registrationId.toLong(), rdk2.registrationId.toLong())
        Assert.assertEquals(rdk.address, rdk2.address)
        Assert.assertEquals(rdk.preKeyId.toLong(), rdk2.preKeyId.toLong())
        Assert.assertEquals(rdk.preKeyPublic, rdk2.preKeyPublic)
        Assert.assertEquals(rdk.signPreKeyId.toLong(), rdk2.signPreKeyId.toLong())
        Assert.assertEquals(rdk.signedPreKeyPublic, rdk2.signedPreKeyPublic)
        Assert.assertArrayEquals(rdk.signedPreKeySignature, rdk2.signedPreKeySignature)
        Assert.assertEquals(rdk.identityKey, rdk2.identityKey)
        val keyJson2 = toJson(rdk2)
        Assert.assertNotNull(keyJson2)
        Assert.assertEquals(keyJson1, keyJson2)
        Log.d("DeviceKeysTest", keyJson1)
        Log.d("DeviceKeysTest", keyJson2)
    }
}