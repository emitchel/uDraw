package erm.udraw.applicaiton;

import android.app.Application;
import erm.udraw.dagger.components.DaggerUDrawComponent;
import erm.udraw.dagger.components.UDrawComponent;
import erm.udraw.dagger.modules.UDrawModule;

/**
 * Created by elliot-mitchell on 3/31/2016.
 */
public class DrawApp extends Application {
  private UDrawComponent injectionComponent;

  @Override
  public void onCreate() {
    super.onCreate();
    injectionComponent = DaggerUDrawComponent.builder().uDrawModule(new UDrawModule(this)).build();
  }

  public UDrawComponent getComponent() {
    return injectionComponent;
  }
}
