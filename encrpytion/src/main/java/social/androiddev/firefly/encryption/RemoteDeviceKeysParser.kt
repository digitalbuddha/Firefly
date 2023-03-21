package social.androiddev.firefly.encryption

import android.util.Base64
import org.json.JSONException
import org.json.JSONObject
import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.InvalidKeyException
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.ecc.Curve

object RemoteDeviceKeysParser {
    private const val K_NAME = "name"
    private const val K_DEV_ID = "dev_id"
    private const val K_REG_ID = "reg_id"
    private const val K_PREKEY_ID = "pk_id"
    private const val K_PREKEY_PUBLIC = "pk_pub"
    private const val K_SIGNED_PREKEY_ID = "spk_id"
    private const val K_SIGNED_PREKEY_PUBLIC = "spk_pub"
    private const val K_SIGNED_PREKEY_SIG = "spk_sig"
    private const val K_ID_KEY = "id_key"

    @JvmStatic
    @Throws(JSONException::class)
    fun toJson(rdk: PublicKeys): String {
        val json = JSONObject()
        json.put(K_NAME, rdk.address.name)
        json.put(K_DEV_ID, rdk.address.deviceId)
        json.put(K_REG_ID, rdk.registrationId)
        json.put(K_PREKEY_ID, rdk.preKeyId)
        json.put(
            K_PREKEY_PUBLIC,
            String(Base64.encode(rdk.preKeyPublic.serialize(), Base64.DEFAULT))
        )
        json.put(K_SIGNED_PREKEY_ID, rdk.signPreKeyId)
        json.put(
            K_SIGNED_PREKEY_PUBLIC,
            String(Base64.encode(rdk.signedPreKeyPublic.serialize(), Base64.DEFAULT))
        )
        json.put(
            K_SIGNED_PREKEY_SIG,
            String(Base64.encode(rdk.signedPreKeySignature, Base64.DEFAULT))
        )
        json.put(
            K_ID_KEY,
            String(Base64.encode(rdk.identityKey.serialize(), Base64.DEFAULT))
        )
        return json.toString()
    }

    @JvmStatic
    @Throws(InvalidKeyException::class, JSONException::class)
    fun fromJson(jsonString: String?): PublicKeys {
        val json = JSONObject(jsonString)
        val name = json.getString(K_NAME)
        val deviceId = json.getInt(K_DEV_ID)
        val registrationId = json.getInt(K_REG_ID)
        val preKeyId = json.getInt(K_PREKEY_ID)
        val preKeyPublic = Base64.decode(json.getString(K_PREKEY_PUBLIC), Base64.DEFAULT)
        val signedPreKeyId = json.getInt(K_SIGNED_PREKEY_ID)
        val signedPreKeyPublic =
            Base64.decode(json.getString(K_SIGNED_PREKEY_PUBLIC), Base64.DEFAULT)
        val signedPreKeySignature =
            Base64.decode(json.getString(K_SIGNED_PREKEY_SIG), Base64.DEFAULT)
        val identityKey = Base64.decode(json.getString(K_ID_KEY), Base64.DEFAULT)
        return PublicKeys(
            SignalProtocolAddress(name, deviceId),
            registrationId,
            preKeyId,
            Curve.decodePoint(preKeyPublic, 0),
            signedPreKeyId,
            Curve.decodePoint(signedPreKeyPublic, 0),
            signedPreKeySignature,
            IdentityKey(identityKey, 0)
        )
    }
}