package erm.udraw.activities;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import javax.inject.Inject;

import erm.udraw.R;
import erm.udraw.applicaiton.DrawApp;

/**
 * Created by ellio on 3/6/2016.
 * <p/>
 * Base class to inherit common functionality
 */
public abstract class BaseActivity extends AppCompatActivity {
  public boolean errorPopupOpen;

  public abstract String getTag();

  @Inject
  public SharedPreferences preferences;

  public void log(String msg) {
    Log.i(getTag(), msg);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ((DrawApp) getApplication()).getComponent().inject(this);
    log(getTag() + " was created");
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      Window w = getWindow(); // in Activity's onCreate() for instance
      w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
          WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
      w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
          WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }
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

  public void errorPopup(String title, String message, String buttonText) {
    new AlertDialog.Builder(this)
        .setTitle(title)
        .setIcon(R.drawable.ic_error_black_36dp)
        .setMessage(message)
        .setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            errorPopupOpen = false;
            //do nothing
          }
        })
        .show();
    errorPopupOpen = true;
  }
}
