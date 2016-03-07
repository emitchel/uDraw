package erm.udraw.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import erm.udraw.R;
import erm.udraw.objects.Constants;

public class SplashActivity extends BaseActivity {

    TextView mTitle, mSignature;

    Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setUpObjects();
        gatherViews();

    }

    public void setUpObjects() {
        this.mContext = this;
    }

    @Override
    public void onStart() {
        super.onStart();
        animateUI();
    }

    private void animateUI() {
        //Fade in Title
        mTitle.animate().alpha(1f).setDuration(Constants.LONG_DURATION).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //Fade in siganature
                mSignature.animate().alpha(1f).setDuration(Constants.LONG_DURATION).setListener(new AnimatorListenerAdapter() {
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
                        }, Constants.LONG_DURATION);
                    }
                });
            }
        });
    }

    private void goToHomeActivity() {
        log("Going to Home Activity");
        Intent intent = new Intent(this, Home.class);
        goToActivity(intent, null,0,0);
    }

    private void gatherViews() {
        mTitle = (TextView) findViewById(R.id.title);
        mSignature = (TextView) findViewById(R.id.signature);
    }

    @Override
    public String getTag() {
        return "Splash Activity";
    }
}
