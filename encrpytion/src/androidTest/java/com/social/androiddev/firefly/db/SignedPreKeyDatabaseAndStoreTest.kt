package com.social.androiddev.firefly.db

import androidx.room.Room.inMemoryDatabaseBuilder
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.whispersystems.libsignal.InvalidKeyException
import org.whispersystems.libsignal.InvalidKeyIdException
import org.whispersystems.libsignal.state.SignedPreKeyRecord
import org.whispersystems.libsignal.util.KeyHelper
import social.androiddev.firefly.encryption.db.LocalSignedPreKeyStore
import social.androiddev.firefly.encryption.db.SignedPreKeyEntity
import social.androiddev.firefly.encryption.db.dao.SignedPreKeyDao
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class SignedPreKeyDatabaseAndStoreTest {
    private var db: social.androiddev.firefly.encryption.db.EncryptionDatabase? = null
    private var mSignedPreKeyDao: SignedPreKeyDao? = null
    private var mLocalSignedPreKeyStore: LocalSignedPreKeyStore? = null
    @Before
    fun start() {
        val context = InstrumentationRegistry.getTargetContext()
        db = inMemoryDatabaseBuilder(context, social.androiddev.firefly.encryption.db.EncryptionDatabase::class.java).build()
        mSignedPreKeyDao = db!!.signedPreKeyDao
        mLocalSignedPreKeyStore = LocalSignedPreKeyStore(mSignedPreKeyDao!!)
    }

    @After
    fun tearDown() {
        db!!.close()
    }

    @Test
    @Throws(InvalidKeyException::class, IOException::class)
    fun signedPreKeyDaoTest() {
        val spk = KeyHelper.generateSignedPreKey(KeyHelper.generateIdentityKeyPair(), 10)
        val entity = SignedPreKeyEntity(spk.id, spk.serialize())
        mSignedPreKeyDao!!.insert(entity)
        val qEntity = mSignedPreKeyDao!!.queryById(spk.id)
        val qSpk = SignedPreKeyRecord(qEntity!!.signedPreKeyRecord)
        Assert.assertEquals(spk.id.toLong(), qSpk.id.toLong())
        Assert.assertArrayEquals(spk.serialize(), qSpk.serialize())
        Assert.assertEquals(1, mSignedPreKeyDao!!.queryAll().size.toLong())
        mSignedPreKeyDao!!.deleteById(entity.id)
        Assert.assertEquals(0, mSignedPreKeyDao!!.queryCountById(entity.id).toLong())
        Assert.assertEquals(0, mSignedPreKeyDao!!.queryAll().size.toLong())
    }

    @Test
    @Throws(InvalidKeyException::class, InvalidKeyIdException::class)
    fun signedPreKeyStoreTest() {
        val spk = KeyHelper.generateSignedPreKey(KeyHelper.generateIdentityKeyPair(), 10)
        val (id) = SignedPreKeyEntity(spk.id, spk.serialize())
        mLocalSignedPreKeyStore!!.storeSignedPreKey(id, spk)
        val exist = mLocalSignedPreKeyStore!!.containsSignedPreKey(id)
        Assert.assertTrue(exist)
        val qSpk = mLocalSignedPreKeyStore!!.loadSignedPreKey(id)
        Assert.assertArrayEquals(spk.serialize(), qSpk.serialize())
        Assert.assertEquals(1, mLocalSignedPreKeyStore!!.loadSignedPreKeys().size.toLong())
        mLocalSignedPreKeyStore!!.removeSignedPreKey(id)
        Assert.assertFalse(mLocalSignedPreKeyStore!!.containsSignedPreKey(id))
        Assert.assertEquals(0, mLocalSignedPreKeyStore!!.loadSignedPreKeys().size.toLong())
    }
}