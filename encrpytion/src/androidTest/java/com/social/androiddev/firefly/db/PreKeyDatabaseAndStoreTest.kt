package com.social.androiddev.firefly.db

import androidx.room.Room.inMemoryDatabaseBuilder
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.whispersystems.libsignal.InvalidKeyIdException
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.util.KeyHelper
import social.androiddev.firefly.encryption.db.LocalPreKeyStore
import social.androiddev.firefly.encryption.db.PreKeyEntity
import social.androiddev.firefly.encryption.db.dao.PreKeyDao
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class PreKeyDatabaseAndStoreTest {
    private var db: social.androiddev.firefly.encryption.db.EncryptionDatabase? = null
    private var prekeyDao: PreKeyDao? = null
    private var prekeyStore: LocalPreKeyStore? = null
    @Before
    fun start() {
        val context = InstrumentationRegistry.getTargetContext()
        db = inMemoryDatabaseBuilder(context, social.androiddev.firefly.encryption.db.EncryptionDatabase::class.java).build()
        prekeyDao = db!!.preKeyDao
        prekeyStore = LocalPreKeyStore(prekeyDao!!)
    }

    @After
    fun tearDown() {
        db!!.close()
    }

    @Test
    fun insertMoreThanOne() {
        val keyPair = Curve.generateKeyPair()
        val preKeyRecord = PreKeyRecord(1, keyPair)
        val keyPair1 = Curve.generateKeyPair()
        val preKeyRecord1 = PreKeyRecord(2, keyPair1)
        val entity = PreKeyEntity(preKeyRecord.id, preKeyRecord.serialize(), false)
        val entity1 = PreKeyEntity(preKeyRecord1.id, preKeyRecord1.serialize(), false)
        prekeyDao!!.insertPreKey(entity)
        prekeyDao!!.insertPreKey(entity)
        var count = prekeyDao!!.countByPreKeyId(entity.preKeyId)
        Assert.assertEquals(1, count.toLong())
        prekeyDao!!.insertPreKey(entity1)
        count = prekeyDao!!.countByPreKeyId(entity1.preKeyId)
        Assert.assertEquals(1, count.toLong())
        prekeyDao!!.deleteByPreKeyId(entity.preKeyId)
        prekeyDao!!.deleteByPreKeyId(entity1.preKeyId)
        count = prekeyDao!!.countByPreKeyId(entity.preKeyId)
        Assert.assertEquals(0, count.toLong())
        count = prekeyDao!!.countByPreKeyId(entity1.preKeyId)
        Assert.assertEquals(0, count.toLong())
    }

    @Test
    @Throws(IOException::class)
    fun allOperationTest() {
        val keyPair = Curve.generateKeyPair()
        val preKeyRecord = PreKeyRecord(1, keyPair)
        val entity = PreKeyEntity(preKeyRecord.id, preKeyRecord.serialize(), false)
        prekeyDao!!.insertPreKey(entity)
        val queryEntity = prekeyDao!!.queryByPreKeyId(preKeyRecord.id)
        val queryPreKeyRecord = PreKeyRecord(queryEntity!!.preKeyRecord)
        Assert.assertEquals(preKeyRecord.id.toLong(), queryEntity.preKeyId.toLong())
        Assert.assertEquals(preKeyRecord.id.toLong(), queryPreKeyRecord.id.toLong())
        Assert.assertEquals(preKeyRecord.keyPair.publicKey, queryPreKeyRecord.keyPair.publicKey)
        Assert.assertArrayEquals(
            preKeyRecord.keyPair.privateKey.serialize(),
            queryPreKeyRecord.keyPair.privateKey.serialize()
        )
        var count = prekeyDao!!.countByPreKeyId(preKeyRecord.id)
        Assert.assertEquals(1, count.toLong())
        prekeyDao!!.deleteByPreKeyId(preKeyRecord.id)
        count = prekeyDao!!.countByPreKeyId(preKeyRecord.id)
        Assert.assertEquals(0, count.toLong())
    }

    @Test
    @Throws(InvalidKeyIdException::class)
    fun localPreKeyStoreAllTest() {
        val preKeys = KeyHelper.generatePreKeys(1, 3)
        for (preKey in preKeys) {
            prekeyStore!!.storePreKey(preKey.id, preKey)
            var exist = prekeyStore!!.containsPreKey(preKey.id)
            Assert.assertTrue(exist)
            val preKeyRecord = prekeyStore!!.loadPreKey(preKey.id)
            Assert.assertEquals(preKey.id.toLong(), preKeyRecord.id.toLong())
            Assert.assertEquals(preKey.keyPair.publicKey, preKeyRecord.keyPair.publicKey)
            Assert.assertArrayEquals(
                preKey.keyPair.privateKey.serialize(),
                preKeyRecord.keyPair.privateKey.serialize()
            )
            prekeyStore!!.removePreKey(preKey.id)
            exist = prekeyStore!!.containsPreKey(preKey.id)
            Assert.assertFalse(exist)
        }
    }
}