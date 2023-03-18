package com.androiddev.social.encryption

import android.util.Base64
import org.junit.Test
import org.whispersystems.libsignal.DuplicateMessageException
import org.whispersystems.libsignal.InvalidKeyIdException
import org.whispersystems.libsignal.InvalidMessageException
import org.whispersystems.libsignal.InvalidVersionException
import org.whispersystems.libsignal.LegacyMessageException
import org.whispersystems.libsignal.NoSessionException
import org.whispersystems.libsignal.UntrustedIdentityException
import org.whispersystems.libsignal.protocol.PreKeySignalMessage


class E3Test{
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
            signedPreKeySignature = String(Base64.encode(bob.signedPreKey.signature, Base64.DEFAULT)),
            identityKeyParPublicKey = encode2(publicKey = bob.identityKeyPair.publicKey.publicKey),
            name = bob.name
        )
        val ALICE_MSG = encrypt(ALICE, realBob)
        val serialize = ALICE_MSG.serialize()
        val msg = decrypt(ALICE, bob, PreKeySignalMessage(serialize))
        println("-------------- encrypted -------------")
        println(String(serialize))
        println("-------------- decrypted -------------")
        println(String(msg))
        // ----------------------------------
    }


}