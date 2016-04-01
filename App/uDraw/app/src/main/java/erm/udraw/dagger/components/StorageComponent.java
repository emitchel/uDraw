package erm.udraw.dagger.components;

import javax.inject.Singleton;

import dagger.Component;
import erm.udraw.activities.BaseActivity;
import erm.udraw.dagger.modules.StorageModule;
import erm.udraw.fragments.BaseFragment;

/**
 * Specifying where this singleton is injected
 */
@Singleton
@Component(modules = StorageModule.class) //could have multiple modules for different "storage" classes
public interface StorageComponent {
    void inject(BaseFragment baseFragment);
    void inject(BaseActivity baseActivity);

}
