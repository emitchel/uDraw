package erm.udraw.dagger.modules;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import dagger.Module;
import dagger.Provides;
import erm.udraw.applicaiton.DrawApp;
import javax.inject.Singleton;

@Module
public class UDrawModule {
  private final DrawApp application;

  public UDrawModule(DrawApp application) {
    this.application = application;
  }

  @Singleton
  @Provides
  SharedPreferences provideSharedPreferences() {
    return PreferenceManager.getDefaultSharedPreferences(application);
  }
}
