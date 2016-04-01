package erm.udraw.applicaiton;

import android.app.Application;

import erm.udraw.dagger.components.DaggerStorageComponent;
import erm.udraw.dagger.components.StorageComponent;
import erm.udraw.dagger.modules.StorageModule;

/**
 * Created by elliot-mitchell on 3/31/2016.
 */
public class DrawApp extends Application {
    StorageComponent sharedPreferencesStorageComponent;
    @Override
    public void onCreate(){
        super.onCreate();
        sharedPreferencesStorageComponent = DaggerStorageComponent.builder().storageModule(new StorageModule(this)).build();
    }

    public StorageComponent getStorageComponent(){return sharedPreferencesStorageComponent;}
}
