package erm.udraw.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import javax.inject.Inject;

import erm.udraw.applicaiton.DrawApp;

/**
 * Created by ellio on 3/6/2016.
 *
 * Base fragment to inherit common functionality
 */
public abstract class BaseFragment extends Fragment {

    @Inject
    public SharedPreferences mPreferences;

    public void log(String msg){
        Log.i(getTag(), msg);
    }

    public void log(String title, String msg){
        Log.i(title,msg);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        ((DrawApp)getActivity().getApplication()).getStorageComponent().inject(this);
    }

    public SharedPreferences getSharedPreferences(){
        return this.mPreferences;
    }
}
