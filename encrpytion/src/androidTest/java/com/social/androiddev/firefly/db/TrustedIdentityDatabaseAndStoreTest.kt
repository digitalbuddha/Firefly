package com.social.androiddev.firefly.db

import androidx.room.Room.inMemoryDatabaseBuilder
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import social.androiddev.firefly.encryption.db.EncryptionDatabase

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.InvalidKeyException
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.state.IdentityKeyStore
import org.whispersystems.libsignal.util.KeyHelper
import social.androiddev.firefly.encryption.db.LocalIdentityEntity
import social.androiddev.firefly.encryption.db.LocalIdentityKeyStore
import social.androiddev.firefly.encryption.db.TrustedKeyEntity
import social.androiddev.firefly.encryption.db.dao.LocalIdentityDao
import social.androiddev.firefly.encryption.db.dao.TrustedKeyDao

@RunWith(AndroidJUnit4::class)
class TrustedIdentityDatabaseAndStoreTest {
    private var db: social.androiddev.firefly.encryption.db.EncryptionDatabase? = null
    private var mLocalIdentityDao: LocalIdentityDao? = null
    private var mTrustedKeyDao: TrustedKeyDao? = null
    private var mLocalIdentityKeyStore: LocalIdentityKeyStore? = null
    @Before
    fun start() {
        val context = InstrumentationRegistry.getTargetContext()
        db = inMemoryDatabaseBuilder(context, social.androiddev.firefly.encryption.db.EncryptionDatabase::class.java).build()
        mLocalIdentityDao = db!!.localIdentityDao
        mTrustedKeyDao = db!!.trustedKeyDao
        mLocalIdentityKeyStore = LocalIdentityKeyStore(mTrustedKeyDao!!, mLocalIdentityDao!!)
    }

    @After
    fun tearDown() {
        db!!.close()
    }

    @Test
    @Throws(InvalidKeyException::class)
    fun localIdentityDaoTest() {
        val regId = KeyHelper.generateRegistrationId(false)
        val identityKeyPair = KeyHelper.generateIdentityKeyPair()
        val idEnt = LocalIdentityEntity(
            0, regId, "fake name", identityKeyPair.serialize()
        )
        mLocalIdentityDao!!.setLocalIdentity(idEnt)
        val queryRegId = mLocalIdentityDao!!.queryRegistrationId()
        Assert.assertEquals(regId.toLong(), queryRegId.toLong())
        val queryIdEnt = mLocalIdentityDao!!.query()
        Assert.assertEquals(idEnt.registrationId.toLong(), queryIdEnt!!.registrationId.toLong())
        Assert.assertArrayEquals(idEnt.identityKeyPair, queryIdEnt.identityKeyPair)
        val queryIdentityKeyPair = IdentityKeyPair(queryIdEnt.identityKeyPair)
        Assert.assertArrayEquals(identityKeyPair.serialize(), queryIdentityKeyPair.serialize())
    }

    @Test
    @Throws(InvalidKeyException::class)
    fun trustedKeyDaoTest() {
        val ik1 = IdentityKey(Curve.generateKeyPair().publicKey)
        val trust1 = TrustedKeyEntity("+6281", 1, ik1.serialize())
        mTrustedKeyDao!!.insert(trust1)
        var count = mTrustedKeyDao!!.countByNameAndDeviceId("+6281", 1)
        Assert.assertEquals(1, count.toLong())
        count = mTrustedKeyDao!!.countByNameAndDeviceId("+6288", 1)
        Assert.assertEquals(0, count.toLong())
        var queryTrust = mTrustedKeyDao!!.queryByNameAndDeviceId("+6281", 1)
        val queryIk = IdentityKey(queryTrust!!.identityKey, 0)
        Assert.assertNotNull(queryTrust)
        Assert.assertEquals(trust1.addressName, queryTrust.addressName)
        Assert.assertEquals(trust1.deviceId.toLong(), queryTrust.deviceId.toLong())
        Assert.assertArrayEquals(trust1.identityKey, queryTrust.identityKey)
        Assert.assertEquals(ik1, queryIk)
        queryTrust = mTrustedKeyDao!!.queryByNameAndDeviceId("no", 1)
        Assert.assertNull(queryTrust)
    }

    @Test
    fun localIdentityStoreTest() {
        val ik1 = IdentityKey(Curve.generateKeyPair().publicKey)
        val addr1 = SignalProtocolAddress("alice", 1)
        val saved = mLocalIdentityKeyStore!!.saveIdentity(addr1, ik1)
        Assert.assertTrue(saved)
        val queryIk = mLocalIdentityKeyStore!!.getIdentity(addr1)
        Assert.assertArrayEquals(ik1.serialize(), queryIk!!.serialize())
        var trusted = mLocalIdentityKeyStore!!.isTrustedIdentity(
            addr1,
            ik1,
            IdentityKeyStore.Direction.RECEIVING
        )
        Assert.assertTrue(trusted)
        trusted = mLocalIdentityKeyStore!!.isTrustedIdentity(
            SignalProtocolAddress("bob", 1),
            ik1,
            IdentityKeyStore.Direction.RECEIVING
        )
        Assert.assertTrue(trusted)
    }
}