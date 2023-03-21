package social.androiddev.firefly.encryption.db

import android.util.Log
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.state.SessionRecord
import org.whispersystems.libsignal.state.SessionStore
import social.androiddev.firefly.encryption.db.dao.SessionDao
import java.io.IOException

class LocalSessionStore(private val mSessionDao: SessionDao) : SessionStore {
    override fun loadSession(address: SignalProtocolAddress): SessionRecord {
        val entity = mSessionDao.queryByAddress(address.name, address.deviceId)
        if (entity?.session != null) {
            try {
                return SessionRecord(entity.session)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return SessionRecord()
    }

    override fun getSubDeviceSessions(name: String): List<Int> {
        return mSessionDao.queryDeviceIds(name, 1)
    }

    override fun storeSession(address: SignalProtocolAddress, record: SessionRecord) {
        Log.d("TestEncrypt", "store session...")
        //        if (containsSession(address)) return;
        Log.d("TestEncrypt", "\t session not exist store new one")
        mSessionDao.insert(
            SessionEntity(
                address.name,
                address.deviceId,
                record.serialize()
            )
        )
    }

    override fun containsSession(address: SignalProtocolAddress): Boolean {
        val count = mSessionDao.queryCountByAddress(address.name, address.deviceId)
        return count > 0
    }

    override fun deleteSession(address: SignalProtocolAddress) {
        mSessionDao.deleteByAddress(address.name, address.deviceId)
    }

    override fun deleteAllSessions(name: String) {
        mSessionDao.deleteByAddressName(name)
    }
}