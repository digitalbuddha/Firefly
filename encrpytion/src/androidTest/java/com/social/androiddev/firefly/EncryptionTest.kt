package com.social.androiddev.firefly

import android.util.Log
import androidx.room.Room.inMemoryDatabaseBuilder
import androidx.test.InstrumentationRegistry
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.whispersystems.libsignal.DuplicateMessageException
import org.whispersystems.libsignal.InvalidKeyException
import org.whispersystems.libsignal.InvalidKeyIdException
import org.whispersystems.libsignal.InvalidMessageException
import org.whispersystems.libsignal.LegacyMessageException
import org.whispersystems.libsignal.NoSessionException
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.UntrustedIdentityException
import org.whispersystems.libsignal.state.impl.InMemorySignalProtocolStore
import org.whispersystems.libsignal.util.KeyHelper
import social.androiddev.firefly.encryption.DeviceKeyBundleUtils.generateDeviceKeyBundle
import social.androiddev.firefly.encryption.Kryptonium
import social.androiddev.firefly.encryption.PublicKeys
import social.androiddev.firefly.encryption.db.SignalProtocolStoreFactory.createLocalSignalProtocolStore

class EncryptionTest {
    private var db: social.androiddev.firefly.encryption.db.EncryptionDatabase? = null
    @Before
    fun start() {
        val context = InstrumentationRegistry.getTargetContext()
        db = inMemoryDatabaseBuilder(context, social.androiddev.firefly.encryption.db.EncryptionDatabase::class.java).build()
    }

    @After
    fun tearDown() {
        db!!.close()
    }

    @Test
    @Throws(
        InvalidKeyException::class,
        UntrustedIdentityException::class,
        InvalidMessageException::class,
        DuplicateMessageException::class,
        LegacyMessageException::class,
        InvalidKeyIdException::class,
        NoSessionException::class
    )
    fun encryptWithLocalSignalProtocolStoreTest() {
        val ALICE_ADDR = SignalProtocolAddress("+621111_alc", 1)
        val BOB_ADDR = SignalProtocolAddress("+622222_bob", 1)
        val (registrationId, address, identityKeyPair, preKeys, signedPreKey) = generateDeviceKeyBundle(
            org.whispersystems.libsignal.SignalProtocolAddress(ALICE_ADDR.name, 1),
            BOB_ADDR.deviceId,
            1,
            KeyHelper.generateIdentityKeyPair()
        )
        val (registrationId1, address1, identityKeyPair1, preKeys1, signedPreKey1) = generateDeviceKeyBundle(
            org.whispersystems.libsignal.SignalProtocolAddress(BOB_ADDR.name,1),
            BOB_ADDR.deviceId,
            22,
            KeyHelper.generateIdentityKeyPair()
        )
        val ALICE_STORE = createLocalSignalProtocolStore(
            db!!
        )
        ALICE_STORE.setLocalIdentity(identityKeyPair, registrationId, ALICE_ADDR.name)
        ALICE_STORE.storePreKey(preKeys[0].id, preKeys[0])
        ALICE_STORE.storeSignedPreKey(signedPreKey.id, signedPreKey)
        val BOB_STORE = InMemorySignalProtocolStore(identityKeyPair1, registrationId1)
        BOB_STORE.storePreKey(preKeys1[0].id, preKeys1[0])
        BOB_STORE.storeSignedPreKey(signedPreKey1.id, signedPreKey1)

        //
        val ALICE_AS_REMOTE = PublicKeys(
            address,
            registrationId,
            preKeys[0].id,
            preKeys[0].keyPair.publicKey,
            signedPreKey.id,
            signedPreKey.keyPair.publicKey,
            signedPreKey.signature,
            identityKeyPair.publicKey
        )
        val BOB_AS_REMOTE = PublicKeys(
            address1,
            registrationId1,
            preKeys1[0].id,
            preKeys1[0].keyPair.publicKey,
            signedPreKey1.id,
            signedPreKey1.keyPair.publicKey,
            signedPreKey1.signature,
            identityKeyPair1.publicKey
        )
        val ALICE_CRYPT = Kryptonium(ALICE_STORE)
        val BOB_CRYPT = Kryptonium(BOB_STORE)
        ALICE_CRYPT.storeRemoteDeviceKeys(BOB_AS_REMOTE)
        BOB_CRYPT.storeRemoteDeviceKeys(ALICE_AS_REMOTE)
        for (i in 0..999) {
            //
            val ALICE_ORIG_MSG = "Hello I'm Alice"
            var msg = ALICE_CRYPT.encryptFor(
                ALICE_ORIG_MSG.toByteArray(),
                SignalProtocolAddress("+622222_bob", 1)
            )
            Log.d("TestEncrypt", "ALICE --to--> BOB: type: " + msg.type)
            var decryptMsg = BOB_CRYPT.decryptFrom(msg, SignalProtocolAddress("+621111_alc", 1))
            Assert.assertEquals(ALICE_ORIG_MSG, String(decryptMsg!!))
            Log.d("TestEncrypt", String(decryptMsg!!))

            //
            val BOB_ORIG_MSG = "Hi I'm Bob"
            val msg2 = BOB_CRYPT.encryptFor(
                BOB_ORIG_MSG.toByteArray(),
                SignalProtocolAddress("+621111_alc", 1)
            )
            Log.d("TestEncrypt", "BOB --to--> ALICE: type: " + msg2.type)
            val decryptMsg2 = ALICE_CRYPT.decryptFrom(msg2, SignalProtocolAddress("+622222_bob", 1))
            Assert.assertEquals(BOB_ORIG_MSG, String(decryptMsg2!!))
            Log.d("TestEncrypt", String(decryptMsg2!!))

            //
            msg = BOB_CRYPT.encryptFor(
                "teeest".toByteArray(),
                SignalProtocolAddress("+621111_alc", 1)
            )
            Log.d("TestEncrypt", "BOB --to--> ALICE: type: " + msg.type)
            decryptMsg = ALICE_CRYPT.decryptFrom(msg, SignalProtocolAddress("+622222_bob", 1))
            Assert.assertEquals("teeest", String(decryptMsg!!))
            Log.d("TestEncrypt", String(decryptMsg))

            //
            msg = ALICE_CRYPT.encryptFor(
                "I'm alice!!!!".toByteArray(),
                SignalProtocolAddress("+622222_bob", 1)
            )
            Log.d("TestEncrypt", "ALICE --to--> BOB: type: " + msg.type)
            decryptMsg = BOB_CRYPT.decryptFrom(msg, SignalProtocolAddress("+621111_alc", 1))
            Assert.assertEquals("I'm alice!!!!", String(decryptMsg!!))
            Log.d("TestEncrypt", String(decryptMsg))
        }
    }
}