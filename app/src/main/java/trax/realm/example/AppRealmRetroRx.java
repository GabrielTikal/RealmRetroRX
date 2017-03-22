package trax.realm.example;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Gabriel on 3/22/2017.
 */

public class AppRealmRetroRx  extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        super.onCreate();
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this)
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }

}
