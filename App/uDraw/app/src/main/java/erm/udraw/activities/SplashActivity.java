package erm.udraw.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import erm.udraw.R;
import erm.udraw.utils.Constants;

public class SplashActivity extends BaseActivity {

  private TextView title;
  private TextView signature;
  private ImageView image;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);
    gatherViews();
  }

  @Override
  public void onStart() {
    super.onStart();
    animateUI();
  }

  private void animateUI() {
    //Fade in image
    image.animate().alpha(1f).setDuration(Constants.Durations.LONG_DURATION).setListener(null);
    //Fade in Title
    title.animate()
        .alpha(1f)
        .setDuration(Constants.Durations.LONG_DURATION)
        .setListener(new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            //Fade in signature
            signature.animate()
                .alpha(1f)
                .setDuration(Constants.Durations.LONG_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                  @Override
                  public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    //Send off to main activity after 1s
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                      @Override
                      public void run() {
                        goToHomeActivity();
                      }
                    }, Constants.Durations.SUPER_LONG_DURATION);
                  }
                });
          }
        });
  }

  private void goToHomeActivity() {
    log("Going to HomeActivity Activity");
    Intent intent = new Intent(this, HomeActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    startActivity(intent);
  }

  private void gatherViews() {
    //TODO: import butterknife or convert to kotlin to remove the need for this
    title = (TextView) findViewById(R.id.title);
    signature = (TextView) findViewById(R.id.signature);
    image = (ImageView) findViewById(R.id.launcher);
  }

  @Override
  public String getTag() {
    return "Splash Activity";
  }
}
