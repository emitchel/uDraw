package erm.udraw.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by ellio on 3/6/2016.
 * <p/>
 * Base class to inherit common functionality
 */
public abstract class BaseActivity extends AppCompatActivity {
    public abstract String getTag();

    /**
     * @param intent
     * @param extras
     * @param enterAnim
     * @param exitAnim
     */
    public void goToActivity(Intent intent, Bundle extras, int enterAnim, int exitAnim) {
        if (extras != null)
            intent.putExtras(extras);

        if (enterAnim <=0)
            enterAnim = android.R.anim.fade_in;

        if(exitAnim <=0)
            exitAnim = android.R.anim.fade_out;

        startActivity(intent);
        overridePendingTransition(enterAnim,exitAnim);
    }

    public void log(String msg) {
        Log.i(getTag(), msg);
    }

    public void log(String title, String msg) {
        Log.i(title, msg);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log(getTag() + " was created");

    }

    @Override
    public void onStart() {
        super.onStart();
        log(getTag() + " was started");
    }

    @Override
    public void onPause() {
        super.onPause();
        log(getTag() + " was paused");
    }

    @Override
    public void onStop() {
        super.onStop();
        log(getTag() + " was stopped");
    }
}
