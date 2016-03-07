package erm.udraw.fragments;

import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by ellio on 3/6/2016.
 *
 * Base fragment to inherit common functionality
 */
public abstract class BaseFragment extends Fragment {


    public void log(String msg){
        Log.i(getTag(), msg);
    }

    public void log(String title, String msg){
        Log.i(title,msg);
    }
}
