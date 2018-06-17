package erm.udraw.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import erm.udraw.R;
import erm.udraw.utils.Constants;
import erm.udraw.views.CanvasView;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class HomeFragment extends BaseFragment {
  private CanvasView canvas;

  private LinearLayout actionWrapper;
  private int actionWrapperHeight = 0;

  private boolean colorAnimationBusy;
  private boolean widthAnimationBusy;

  private RelativeLayout lineWidthArea;
  private boolean widthAreaShowing;
  private TextView lineWidthValue;

  private RelativeLayout colorArea;
  private boolean colorAreaShowing;
  private LinearLayout colorSelections;
  private View colorSelected;

  private TextView playBackText;

  private AppCompatSeekBar widthSeekBar;
  private ImageButton mDraw, mErase, mChooseColor, mUndo, mRedo;
  private View mDrawSelected, mEraseSelected;

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
    actionWrapper = (LinearLayout) view.findViewById(R.id.action_wrapper);
    canvas = (CanvasView) view.findViewById(R.id.canvas);
    mDraw = (ImageButton) view.findViewById(R.id.draw);
    mErase = (ImageButton) view.findViewById(R.id.erase);
    mChooseColor = (ImageButton) view.findViewById(R.id.color);
    mUndo = (ImageButton) view.findViewById(R.id.undo);
    mRedo = (ImageButton) view.findViewById(R.id.redo);

    mDrawSelected = view.findViewById(R.id.draw_selected);
    mEraseSelected = view.findViewById(R.id.erase_selected);

    lineWidthArea = (RelativeLayout) view.findViewById(R.id.line_width_wrapper);
    widthSeekBar = (AppCompatSeekBar) view.findViewById(R.id.line_width_seekbar);
    lineWidthValue = (TextView) view.findViewById(R.id.line_width_value);

    colorArea = (RelativeLayout) view.findViewById(R.id.color_wrapper);
    colorSelections = (LinearLayout) view.findViewById(R.id.color_selections);
    colorSelected = view.findViewById(R.id.color_choice);
    playBackText = (TextView) view.findViewById(R.id.playback);

    setCustomListeners();
    setUpPage();
  }

  /**
   * Small method to fade in one view while fading out another
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
    if (colorAreaShowing && !widthAreaShowing) {

      showOrHideColorSelector();
    }
    if (!widthAnimationBusy) {
      widthAnimationBusy = true;

      log("Height of action_wrapper: " + String.valueOf(actionWrapperHeight));

      lineWidthArea.animate()
          .translationYBy((widthAreaShowing ? 1 : -1) * actionWrapperHeight + (widthAreaShowing
              ? -Constants.Size.SPACE_TO_COVER_RADIUS : Constants.Size.SPACE_TO_COVER_RADIUS))
          .setInterpolator(new DecelerateInterpolator())
          .setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
              widthAnimationBusy = false;
              widthAreaShowing = !widthAreaShowing;
            }
          }).setDuration(Constants.Durations.SHORT_DURATION).start();
    }
  }

  private void showOrHideColorSelector() {
    if (widthAreaShowing && !colorAreaShowing) {
      showOrHideLineWidth();
    }

    if (!colorAnimationBusy) {
      colorAnimationBusy = true;

      log("Height of action_wrapper: " + String.valueOf(actionWrapperHeight));

      colorArea.animate()
          .translationYBy((colorAreaShowing ? 1 : -1) * actionWrapperHeight + (colorAreaShowing
              ? -Constants.Size.SPACE_TO_COVER_RADIUS : Constants.Size.SPACE_TO_COVER_RADIUS))
          .setInterpolator(new DecelerateInterpolator())
          .setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
              colorAnimationBusy = false;
              colorAreaShowing = !colorAreaShowing;
            }
          }).setDuration(Constants.Durations.SHORT_DURATION).start();
    }
  }

  private void setColor(CanvasView.Color color) {
    canvas.setColor(color);

    GradientDrawable background = (GradientDrawable) colorSelected.getBackground();
    background.setColor(color.hex);

    if (colorAreaShowing) {
      showOrHideColorSelector();
    }

    //Set to pen mode after selecting color
    canvas.setPenMode();
    fadeInFadeOut(mDrawSelected, mEraseSelected);
  }

  /**
   * Important step to generate the possible colors the user can choose
   */
  private void setUpPage() {
    lineWidthValue.setText(String.valueOf((int) canvas.getCurrentWidth()));
    widthSeekBar.setProgress((int) canvas.getCurrentWidth());

    ArrayList<CanvasView.Color> colorsAvailable = canvas.getAvailableColors();
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
      colorSelections.addView(colorView);
    }
    //add the custom color section
  }

  private void setCustomListeners() {
    canvas.setListener(new CanvasView.CanvasTouchListener() {
      @Override
      public void onTouch() {
        if (widthAreaShowing) {
          showOrHideLineWidth();
        }

        if (colorAreaShowing) {
          showOrHideColorSelector();
        }
      }
    });

    mDraw.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        fadeInFadeOut(mDrawSelected, mEraseSelected);
        if (!widthAreaShowing) {
          showOrHideLineWidth();
        }
        canvas.setPenMode();
      }
    });

    mErase.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        fadeInFadeOut(mEraseSelected, mDrawSelected);
        if (!widthAreaShowing) {
          showOrHideLineWidth();
        }
        canvas.setEraserMode();
      }
    });

    mUndo.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        canvas.goHistory(CanvasView.History.BACKWARD);
      }
    });

    mRedo.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        canvas.goHistory(CanvasView.History.FORWARD);
      }
    });

    /**
     * Using this to obtain the actual height of the paint control
     */
    actionWrapper.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {
            //Best way to obtain height
            actionWrapperHeight = actionWrapper.getHeight();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
              actionWrapper.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            } else {
              actionWrapper.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
          }
        });

    mChooseColor.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showOrHideColorSelector();
      }
    });

    widthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        canvas.setStrokeWidth((float) progress);
        lineWidthValue.setText(String.valueOf(progress));
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
      canvas.setBitmapBackground(bitmap);
    }
  }

  public Bitmap getBitmap() {
    return canvas.getBitmap();
  }

  public void clearCanvas() {
    canvas.clearCanvas();
  }

  public void rotateBackground() {
    canvas.rotateClockwise();
  }

  public void playBack() {
    playBackText.setVisibility(View.VISIBLE);
    playBackText.animate()
        .alpha(.5f)
        .setDuration(Constants.Durations.SHORT_DURATION)
        .setListener(null);

    canvas.playBack(new CanvasView.CanvasPlaybackListener() {
      @Override
      public void onPlayBackFinished() {
        playBackText.animate()
            .alpha(0)
            .setDuration(Constants.Durations.SHORT_DURATION)
            .setListener(new AnimatorListenerAdapter() {
              @Override
              public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                playBackText.setVisibility(View.GONE);
              }
            });
      }
    });
  }

  /**
   * Usability feature to close any open windows.
   */
  public boolean closeWidthOrColorPicker() {
    if (widthAreaShowing) {
      showOrHideLineWidth();
      return true;
    }

    if (colorAreaShowing) {
      showOrHideColorSelector();
      return true;
    }

    return false;
  }
}

