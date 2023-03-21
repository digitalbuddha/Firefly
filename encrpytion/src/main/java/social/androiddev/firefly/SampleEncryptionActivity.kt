package social.androiddev.firefly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.room.Room.inMemoryDatabaseBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.state.impl.InMemorySignalProtocolStore
import org.whispersystems.libsignal.util.KeyHelper
import social.androiddev.firefly.encryption.DeviceKeyBundleUtils
import social.androiddev.firefly.encryption.Kryptonium
import social.androiddev.firefly.encryption.PublicKeys
import social.androiddev.firefly.encryption.db.LocalSignalProtocolStore
import social.androiddev.firefly.encryption.db.SignalProtocolStoreFactory
import kotlin.math.pow

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val db = inMemoryDatabaseBuilder(
            this,
            social.androiddev.firefly.encryption.db.EncryptionDatabase::class.java
        ).build()
//        doWork(db)


        setContent {
            val accountId = "1"
            var myKryptonium: Kryptonium? by remember { mutableStateOf(null) }
            var myStore: LocalSignalProtocolStore? by remember { mutableStateOf(null) }
            var remoteKrypt: Kryptonium? by remember { mutableStateOf(null) }
            var bobAsRemote: PublicKeys? by remember { mutableStateOf(null) }
            LaunchedEffect(key1 = Unit) {
                withContext(Dispatchers.IO) {
                    onLogin(db, accountId)
                    val (krypto: Kryptonium, store: LocalSignalProtocolStore) = onAppStart(db)
                    myKryptonium = krypto
                    myStore = store
                    val BOB_ADDR = SignalProtocolAddress("+622222_bob", 1)
                    val (registrationId1, address1, identityKeyPair1, preKeys1, signedPreKey1) = DeviceKeyBundleUtils.generateDeviceKeyBundle(
                        BOB_ADDR,
                        22,
                        2,
                        KeyHelper.generateIdentityKeyPair()
                    )
                    val BOB_STORE = InMemorySignalProtocolStore(identityKeyPair1, registrationId1)
                    BOB_STORE.storePreKey(preKeys1[0].id, preKeys1[0])
                    BOB_STORE.storeSignedPreKey(signedPreKey1.id, signedPreKey1)


                    bobAsRemote = PublicKeys(
                        address1,
                        registrationId1,
                        preKeys1[0].id,
                        preKeys1[0].keyPair.publicKey,
                        signedPreKey1.id,
                        signedPreKey1.keyPair.publicKey,
                        signedPreKey1.signature,
                        identityKeyPair1.publicKey
                    )
//                val bobJson = RemoteDeviceKeysParser.toJson(bobAsRemote).length
                    remoteKrypt = Kryptonium(BOB_STORE)
                }

            }
            Column {
                val scope = rememberCoroutineScope()
                Text(

                    text = "Local",
                    style = MaterialTheme.typography.headlineLarge
                )
                Button(onClick = {
                    scope.launch(Dispatchers.IO) {
                        remoteKrypt!!.storeRemoteDeviceKeys(
                            myRemoteDeviceKeys(
                                myKryptonium!!,
                                myStore!!,
                                accountId
                            )
                        )
                        myKryptonium!!.storeRemoteDeviceKeys(bobAsRemote!!)
                    }

                }) {
                    Text(text = "Trust Remote")
                }
                var result by remember { mutableStateOf("") }

                var text by remember { mutableStateOf("") }
                var remoteText by remember { mutableStateOf("") }

                TextField(value = text, onValueChange = { text = it })
                Button(onClick = {
                    scope.launch(Dispatchers.IO) {
                        val message = myKryptonium!!.encryptFor(
                            text.toByteArray(),
                            SignalProtocolAddress("+622222_bob", 1)
                        )
                        result += String(
                            remoteKrypt!!.decryptFrom(
                                message,
                                SignalProtocolAddress(accountId, 1)
                            )!!
                        )
                    }
                }) {
                    Text(text = "Send Encrypted Message To Remote")
                }

                Text(

                    text = "Remote",
                    style = MaterialTheme.typography.headlineLarge
                )
                TextField(value = remoteText, onValueChange = { remoteText = it })
                Button(onClick = {
                    scope.launch(Dispatchers.IO) {
                        val message = remoteKrypt!!.encryptFor(
                            remoteText.toByteArray(),
                            SignalProtocolAddress(accountId, 1)
                        )
                        result += String(
                            myKryptonium!!.decryptFrom(
                                message,
                                SignalProtocolAddress("+622222_bob", 1)
                            )!!
                        )
                    }
                }) {
                    Text(text = "Send Encrypted Message from Remote")
                }
                Text(

                    text = result,
                    style = MaterialTheme.typography.headlineLarge
                )
            }

        }
    }

    private fun myRemoteDeviceKeys(
        krypto: Kryptonium,
        store: LocalSignalProtocolStore,
        accountId: String
    ): PublicKeys {
        val identity = krypto.getLocalIdentity()!!
        val identityKeyPair = store.identityKeyPair
        val preKey = store.getNextPreKey()
        val signedPreKey =
            store.loadSignedPreKey(social.androiddev.firefly.hash(accountId.toLong()))
        return PublicKeys(
            SignalProtocolAddress(identity.name, 1),
            identity.registrationId,
            preKey.preKeyId,
            PreKeyRecord(preKey.preKeyRecord).keyPair.publicKey,
            signedPreKey.id,
            signedPreKey.keyPair.publicKey,
            signedPreKey.signature,
            identityKeyPair.publicKey
        )
    }

    private fun onAppStart(db: social.androiddev.firefly.encryption.db.EncryptionDatabase): Pair<Kryptonium, LocalSignalProtocolStore> {
        val store = SignalProtocolStoreFactory.createLocalSignalProtocolStore(
            db
        )
        return Kryptonium(store) to store
    }

    private fun onLogin(
        db: social.androiddev.firefly.encryption.db.EncryptionDatabase,
        accountId: String
    ): LocalSignalProtocolStore {
        val signalAddress = SignalProtocolAddress(accountId, 1)
        val (registrationId, address, identityKeyPair, preKeys, signedPreKey) = DeviceKeyBundleUtils.generateDeviceKeyBundle(
            signalAddress,
            social.androiddev.firefly.hash(accountId.toLong()),
            100,
            KeyHelper.generateIdentityKeyPair()

        )

        val encryptionStore = SignalProtocolStoreFactory.createLocalSignalProtocolStore(
            db
        )
        encryptionStore.setLocalIdentity(identityKeyPair, registrationId, signalAddress.name)
        preKeys.forEach {
            encryptionStore.storePreKey(it.id, it)

        }
        encryptionStore.storeSignedPreKey(signedPreKey.id, signedPreKey)
        //
        return encryptionStore
    }
}

fun hash(i: Long) = (i * 2654435761 % 2.toDouble().pow(32)).toInt()
