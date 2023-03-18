package com.androiddev.social

import android.util.Base64
import com.androiddev.social.encryption.Person
import com.androiddev.social.encryption.PublicKey
import com.androiddev.social.encryption.decrypt
import com.androiddev.social.encryption.encode2
import com.androiddev.social.encryption.encrypt
import kotlinx.serialization.json.Json
import org.junit.Test
import org.whispersystems.libsignal.DuplicateMessageException
import org.whispersystems.libsignal.InvalidKeyIdException
import org.whispersystems.libsignal.InvalidMessageException
import org.whispersystems.libsignal.InvalidVersionException
import org.whispersystems.libsignal.LegacyMessageException
import org.whispersystems.libsignal.NoSessionException
import org.whispersystems.libsignal.UntrustedIdentityException
import org.whispersystems.libsignal.protocol.PreKeySignalMessage
import java.nio.charset.StandardCharsets


class E3Test {
    @Test
    @Throws(
        java.security.InvalidKeyException::class,
        UntrustedIdentityException::class,
        LegacyMessageException::class,
        InvalidMessageException::class,
        NoSessionException::class,
        DuplicateMessageException::class,
        InvalidVersionException::class,
        InvalidKeyIdException::class
    )
    fun test() {
        val ALICE = Person("+6281111111111", 111, 2)
        val bob = Person("+6282222222222", 112, 5)
        val realBob = PublicKey(
            registrationId = bob.registrationId,
            devideId = 0,
            prekeyId = bob.preKeys[0].id,
            preKeyPublicKey = encode2(publicKey = bob.preKeys[0].keyPair.publicKey),
            signedPreKeyId = bob.signedPreKey.id,
            signedPreKeyPublicKey = encode2(publicKey = bob.signedPreKey.keyPair.publicKey),
            signedPreKeySignature = String(
                Base64.encode(
                    bob.signedPreKey.signature,
                    Base64.DEFAULT
                )
            ),
            identityKeyParPublicKey = encode2(publicKey = bob.identityKeyPair.publicKey.publicKey),
            name = bob.name
        )
        val json = Json.encodeToString(serializer = PublicKey.serializer(), value = realBob)
        val ALICE_MSG = encrypt(ALICE, realBob).encrypt(
            "Hello world!, hello world!, hello world!, hello world!, hello world!, hello world! haha".toByteArray(
                StandardCharsets.UTF_8
            )
        )
        val serialize = ALICE_MSG.serialize()
        val msg = decrypt(ALICE, bob, PreKeySignalMessage(serialize))
        println("-------------- encrypted -------------")
        println(String(serialize))
        println(json + json.length + "is the size of the json")
        println("-------------- decrypted -------------")
        println(String(msg))
        // ----------------------------------
    }


}