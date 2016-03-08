package erm.udraw.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import erm.udraw.R;
import erm.udraw.objects.Constants;
import erm.udraw.views.CanvasView;

/**
 * A placeholder fragment containing a simple view.
 */
public class HomeFragment extends BaseFragment {
    CanvasView mCanvas;

    LinearLayout mActionWrapper;
    int mActionWrapperHeight = 0;

    boolean mAnimationBusy;

    RelativeLayout mLineWidthArea;
    boolean mWidthAreaShowing;
    TextView mLineWidthValue;

    RelativeLayout mColorArea;
    boolean mColorAreaShowing;

    AppCompatSeekBar mWidthSeekBar;
    ImageButton mDraw, mErase, mChooseColor, mUndo, mRedo;
    View mDrawSelected, mEraseSelected;

    public HomeFragment() {
        //empty constr per the docs
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mActionWrapper = (LinearLayout) view.findViewById(R.id.action_wrapper);
        mCanvas = (CanvasView) view.findViewById(R.id.canvas);
        mDraw = (ImageButton) view.findViewById(R.id.draw);
        mErase = (ImageButton) view.findViewById(R.id.erase);
        mChooseColor = (ImageButton) view.findViewById(R.id.color);
        mUndo = (ImageButton) view.findViewById(R.id.undo);
        mRedo = (ImageButton) view.findViewById(R.id.redo);

        mDrawSelected = view.findViewById(R.id.draw_selected);
        mEraseSelected = view.findViewById(R.id.erase_selected);

        mLineWidthArea = (RelativeLayout) view.findViewById(R.id.line_width_wrapper);
        mWidthSeekBar = (AppCompatSeekBar) view.findViewById(R.id.line_width_seekbar);
        mLineWidthValue = (TextView) view.findViewById(R.id.line_width_value);

        setCustomListeners();
        setUpPage();

    }

    /**
     * Small method to fade in one view while fading out another
     *
     * @param fadeIn
     * @param fadeOut
     */
    private void fadeInFadeOut(View fadeIn, View fadeOut) {
        if (fadeIn.getAlpha() <= 0f) {
            fadeIn.animate().alpha(1f).setListener(null);
        }

        if (fadeOut.getAlpha() >= 1f) {
            fadeOut.animate().alpha(0f).setListener(null);
        }
    }


    private void showOrHideLineWidth() {
        if (!mAnimationBusy) {
            if (mColorAreaShowing && !mWidthAreaShowing) {
                //TODO:Close this area first
                //Can happen concurrently
                mColorAreaShowing = false;
            }

            mAnimationBusy = true;

            log("Height of action_wrapper: " + String.valueOf(mActionWrapperHeight));

            mLineWidthArea.animate()
                    .translationYBy((mWidthAreaShowing ? 1 : -1) * mActionWrapperHeight + (mWidthAreaShowing ? -6 : 6))
                    .setInterpolator(new DecelerateInterpolator())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mAnimationBusy = false;
                            mWidthAreaShowing = !mWidthAreaShowing;
                        }
                    }).setDuration(Constants.SHORT_DURATION).start();


        }
    }

    private void setUpPage() {
        mLineWidthValue.setText(String.valueOf((int) mCanvas.getCurrentWidth()));
        mWidthSeekBar.setProgress((int) mCanvas.getCurrentWidth());
    }

    private void setCustomListeners() {
        mCanvas.setListener(new CanvasView.CanvasTouchListener() {
            @Override
            public void onTouch() {
                if (mWidthAreaShowing)
                    showOrHideLineWidth();

                if (mColorAreaShowing) {
                    //TODO:

                }
            }
        });

        mDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fadeInFadeOut(mDrawSelected, mEraseSelected);
                showOrHideLineWidth();
                mCanvas.setPenMode();
            }
        });

        mErase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fadeInFadeOut(mEraseSelected, mDrawSelected);
                showOrHideLineWidth();
                mCanvas.setEraserMode();
            }
        });

        mUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCanvas.goHistory(CanvasView.History.BACKWARD);
            }
        });

        mRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCanvas.goHistory(CanvasView.History.FORWARD);
            }
        });

        mActionWrapper.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //Best way to obtain height
                mActionWrapperHeight = mActionWrapper.getHeight();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                    mActionWrapper.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                else
                    mActionWrapper.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });

        mWidthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCanvas.setStrokeWidth((float) progress);
                mLineWidthValue.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public Bitmap getBitmap() {
        return mCanvas.getBitmap();
    }

    public void clearCanvas() {
        mCanvas.clearCanvas();
    }


}

