package erm.udraw.dagger.modules;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import erm.udraw.applicaiton.DrawApp;

/**
 * Knows how to create the object
 */
@Module
public class StorageModule {
    private final DrawApp application;

    public StorageModule(DrawApp application){this.application = application;}

    @Singleton
    @Provides
    SharedPreferences provideSharedPreferences(){
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

}
