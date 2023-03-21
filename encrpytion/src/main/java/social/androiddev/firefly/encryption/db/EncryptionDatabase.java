package social.androiddev.firefly.encryption.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import social.androiddev.firefly.encryption.db.dao.LocalIdentityDao;
import social.androiddev.firefly.encryption.db.dao.PreKeyDao;
import social.androiddev.firefly.encryption.db.dao.SessionDao;
import social.androiddev.firefly.encryption.db.dao.SignedPreKeyDao;
import social.androiddev.firefly.encryption.db.dao.TrustedKeyDao;

@Database(entities = {
        PreKeyEntity.class,
        LocalIdentityEntity.class,
        TrustedKeyEntity.class,
        SignedPreKeyEntity.class,
        SessionEntity.class}, version = 1)
public abstract class EncryptionDatabase extends RoomDatabase {

    public abstract PreKeyDao getPreKeyDao();

    public abstract LocalIdentityDao getLocalIdentityDao();

    public abstract TrustedKeyDao getTrustedKeyDao();

    public abstract SignedPreKeyDao getSignedPreKeyDao();

    public abstract SessionDao getSessionDao();
}
