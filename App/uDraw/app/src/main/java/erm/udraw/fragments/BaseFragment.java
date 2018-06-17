package erm.udraw.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import erm.udraw.applicaiton.DrawApp;
import javax.inject.Inject;

/**
 * Created by ellio on 3/6/2016.
 *
 * Base fragment to inherit common functionality
 */
public abstract class BaseFragment extends Fragment {

  @Inject
  public SharedPreferences prefrences;

  public void log(String msg) {
    Log.i(getTag(), msg);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ((DrawApp) getActivity().getApplication()).getComponent().inject(this);
  }
}
