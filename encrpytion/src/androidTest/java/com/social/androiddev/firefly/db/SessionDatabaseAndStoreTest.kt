package com.social.androiddev.firefly.db

import androidx.room.Room.inMemoryDatabaseBuilder
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.state.SessionRecord
import social.androiddev.firefly.encryption.db.LocalSessionStore
import social.androiddev.firefly.encryption.db.SessionEntity
import social.androiddev.firefly.encryption.db.dao.SessionDao

@RunWith(AndroidJUnit4::class)
class SessionDatabaseAndStoreTest {
    private var db: social.androiddev.firefly.encryption.db.EncryptionDatabase? = null
    private var mSessionDao: SessionDao? = null
    private var mLocalSessionStore: LocalSessionStore? = null
    @Before
    fun start() {
        val context = InstrumentationRegistry.getTargetContext()
        db = inMemoryDatabaseBuilder(context, social.androiddev.firefly.encryption.db.EncryptionDatabase::class.java).build()
        mSessionDao = db!!.sessionDao
        mLocalSessionStore = LocalSessionStore(mSessionDao!!)
    }

    @After
    fun tearDown() {
        db!!.close()
    }

    @Test
    fun sessionDaoTest() {
        val sessEmpty = SessionRecord()
        val sessEmptyEntity = SessionEntity(
            "alice", 2,
            sessEmpty.serialize()
        )
        mSessionDao!!.insert(sessEmptyEntity)
        var count = mSessionDao!!.queryCountByAddress("alice", 2)
        Assert.assertEquals(1, count.toLong())
        count = mSessionDao!!.queryDeviceIds("alice", 1).size
        Assert.assertEquals(1, count.toLong())
        count = mSessionDao!!.queryDeviceIds("alice", 2).size
        Assert.assertEquals(0, count.toLong())
        var queryEntity = mSessionDao!!.queryByAddress("alice", 1)
        Assert.assertNull(queryEntity)
        queryEntity = mSessionDao!!.queryByAddress("alice", 2)
        Assert.assertNotNull(queryEntity)
        Assert.assertEquals(sessEmptyEntity.protocolAddressName, queryEntity!!.protocolAddressName)
        Assert.assertEquals(sessEmptyEntity.deviceId.toLong(), queryEntity.deviceId.toLong())
        Assert.assertArrayEquals(sessEmptyEntity.session, queryEntity.session)
        mSessionDao!!.deleteByAddress("alice", 2)
        count = mSessionDao!!.queryCountByAddress("alice", 2)
        Assert.assertEquals(0, count.toLong())
        mSessionDao!!.insert(sessEmptyEntity)
        mSessionDao!!.deleteByAddressName("alice")
        count = mSessionDao!!.queryCountByAddress("alice", 2)
        Assert.assertEquals(0, count.toLong())
    }

    @Test
    fun localSessionStoreTest() {
        val sessEmpty = SessionRecord()
        val bobAddr = SignalProtocolAddress("bob", 2)
        var exist = mLocalSessionStore!!.containsSession(bobAddr)
        Assert.assertFalse(exist)
        mLocalSessionStore!!.storeSession(bobAddr, sessEmpty)
        mLocalSessionStore!!.storeSession(bobAddr, sessEmpty)
        exist = mLocalSessionStore!!.containsSession(bobAddr)
        Assert.assertTrue(exist)
        val querySess = mLocalSessionStore!!.loadSession(bobAddr)
        Assert.assertArrayEquals(sessEmpty.serialize(), querySess.serialize())
        var count = mLocalSessionStore!!.getSubDeviceSessions(bobAddr.name).size
        Assert.assertEquals(1, count.toLong())
        mLocalSessionStore!!.deleteSession(bobAddr)
        exist = mLocalSessionStore!!.containsSession(bobAddr)
        Assert.assertFalse(exist)
        mLocalSessionStore!!.storeSession(bobAddr, sessEmpty)
        mLocalSessionStore!!.deleteAllSessions(bobAddr.name)
        count = mLocalSessionStore!!.getSubDeviceSessions(bobAddr.name).size
        Assert.assertEquals(0, count.toLong())
    }
}