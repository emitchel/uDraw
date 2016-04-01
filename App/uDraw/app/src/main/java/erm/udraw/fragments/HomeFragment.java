package erm.udraw.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

import erm.udraw.R;
import erm.udraw.objects.Constants;
import erm.udraw.views.CanvasView;

/**
 * A placeholder fragment containing a simple view.
 */
public class HomeFragment extends BaseFragment {
    public static final int SPACE_TO_COVER_RADIUS = 6;
    CanvasView mCanvas;

    LinearLayout mActionWrapper;
    int mActionWrapperHeight = 0;

    boolean mColorAnimationBusy, mWidthAnimationBusy;

    RelativeLayout mLineWidthArea;
    boolean mWidthAreaShowing;
    TextView mLineWidthValue;

    RelativeLayout mColorArea;
    HorizontalScrollView mColorScroll;
    boolean mColorAreaShowing;
    LinearLayout mColorSelections;
    View mColorSelected;

    TextView mPlayback;

    AppCompatSeekBar mWidthSeekBar;
    ImageButton mDraw, mErase, mChooseColor, mUndo, mRedo;
    View mDrawSelected, mEraseSelected;

    SharedPreferences localPreferences;

    public HomeFragment() {
        //empty constr per the docs
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        localPreferences = getSharedPreferences();
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

        mColorArea = (RelativeLayout) view.findViewById(R.id.color_wrapper);
        mColorScroll = (HorizontalScrollView) view.findViewById(R.id.color_scroll);
        mColorSelections = (LinearLayout) view.findViewById(R.id.color_selections);
        mColorSelected = view.findViewById(R.id.color_choice);
        mPlayback = (TextView) view.findViewById(R.id.playback);

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
        if (mColorAreaShowing && !mWidthAreaShowing) {

            showOrHideColorSelector();
        }
        if (!mWidthAnimationBusy) {
            mWidthAnimationBusy = true;

            log("Height of action_wrapper: " + String.valueOf(mActionWrapperHeight));

            mLineWidthArea.animate()
                    .translationYBy((mWidthAreaShowing ? 1 : -1) * mActionWrapperHeight + (mWidthAreaShowing ? -SPACE_TO_COVER_RADIUS : SPACE_TO_COVER_RADIUS))
                    .setInterpolator(new DecelerateInterpolator())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mWidthAnimationBusy = false;
                            mWidthAreaShowing = !mWidthAreaShowing;
                        }
                    }).setDuration(Constants.SHORT_DURATION).start();


        }
    }

    private void showOrHideColorSelector() {
        if (mWidthAreaShowing && !mColorAreaShowing) {
            showOrHideLineWidth();
        }

        if (!mColorAnimationBusy) {
            mColorAnimationBusy = true;

            log("Height of action_wrapper: " + String.valueOf(mActionWrapperHeight));

            mColorArea.animate()
                    .translationYBy((mColorAreaShowing ? 1 : -1) * mActionWrapperHeight + (mColorAreaShowing ? -SPACE_TO_COVER_RADIUS : SPACE_TO_COVER_RADIUS))
                    .setInterpolator(new DecelerateInterpolator())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mColorAnimationBusy = false;
                            mColorAreaShowing = !mColorAreaShowing;
                        }
                    }).setDuration(Constants.SHORT_DURATION).start();


        }
    }

    private void setColor(CanvasView.Color color) {
        mCanvas.setColor(color);

        GradientDrawable background = (GradientDrawable) mColorSelected.getBackground();
        background.setColor(color.hex);

        if (mColorAreaShowing)
            showOrHideColorSelector();

        //Set to pen mode after selecting color
        mCanvas.setPenMode();
        fadeInFadeOut(mDrawSelected, mEraseSelected);

    }

    /**
     * Important step to generate the possible colors the user can choose
     */
    private void setUpPage() {
        mLineWidthValue.setText(String.valueOf((int) mCanvas.getCurrentWidth()));
        mWidthSeekBar.setProgress((int) mCanvas.getCurrentWidth());

        ArrayList<CanvasView.Color> colorsAvailable = mCanvas.getAvailableColors();
        //Adding available colors to horizontal scroll
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        for (final CanvasView.Color color : colorsAvailable) {


            View colorView = inflater.inflate(R.layout.color_view, null);
            GradientDrawable bg = (GradientDrawable) colorView.findViewById(R.id.circle).getBackground();
            bg.setColor(color.hex);
            colorView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setColor(color);
                }
            });
            mColorSelections.addView(colorView);
        }
        //add the custom color section
    }

    private void setCustomListeners() {
        mCanvas.setListener(new CanvasView.CanvasTouchListener() {
            @Override
            public void onTouch() {
                if (mWidthAreaShowing)
                    showOrHideLineWidth();

                if (mColorAreaShowing)
                    showOrHideColorSelector();

            }
        });

        mDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fadeInFadeOut(mDrawSelected, mEraseSelected);
                if (!mWidthAreaShowing)
                    showOrHideLineWidth();
                mCanvas.setPenMode();
            }
        });

        mErase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fadeInFadeOut(mEraseSelected, mDrawSelected);
                if (!mWidthAreaShowing)
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

        /**
         * Using this to obtain the actual height of the paint control
         */
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

        mChooseColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrHideColorSelector();
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


    public void setBitmapBackground(Bitmap bitmap) {
        if (bitmap != null) {
            mCanvas.setBitmapBackground(bitmap);
        }
    }

    public Bitmap getBitmap() {
        return mCanvas.getBitmap();
    }

    public void clearCanvas() {
        mCanvas.clearCanvas();
    }

    public void rotateBackground() {
        mCanvas.rotateClockwise();
    }

    public void playBack() {
        mPlayback.setVisibility(View.VISIBLE);
        mPlayback.animate().alpha(.5f).setDuration(Constants.SHORT_DURATION).setListener(null);

        mCanvas.playBack(new CanvasView.CanvasPlayBackListener() {
            @Override
            public void onPlayBackFinished() {
                mPlayback.animate().alpha(0).setDuration(Constants.SHORT_DURATION).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mPlayback.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    /**
     * Usability feature to close any open windows.
     * @return
     */
    public boolean closeWidthOrColorPicker() {
        if (mWidthAreaShowing) {
            showOrHideLineWidth();
            return true;
        }

        if (mColorAreaShowing) {
            showOrHideColorSelector();
            return true;
        }

        return false;
    }

}

