package erm.udraw.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import erm.udraw.utils.Constants;
import erm.udraw.utils.ImageUtils;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Custom class used to draw
 *
 * Created by ellio on 3/7/2016.
 */
public class CanvasView extends View implements View.OnTouchListener {

  final Handler handler = new Handler();

  private Color currentColor;
  private float currentSize;
  private boolean inEraseMode;
  private boolean inPlayBackMode;

  private Stack<Stroke> strokes = new Stack<>();
  private Stack<Stroke> strokesHolder = new Stack<>();
  private Stack<Stroke> undoneStrokes = new Stack<>();

  private float currentX, currentY;

  private CanvasTouchListener touchListener;

  private CanvasPlaybackListener playbackListener;

  private Bitmap bitmap;
  private Canvas canvas;

  public enum History {
    FORWARD, BACKWARD
  }

  /**
   * Colors supported
   */
  public enum Color {
    BLACK(android.graphics.Color.BLACK), DARK_GRAY(android.graphics.Color.DKGRAY),
    LIGHT_GRAY(android.graphics.Color.LTGRAY), BLUE(android.graphics.Color.BLUE),
    RED(android.graphics.Color.RED), GREEN(android.graphics.Color.GREEN),
    ORANGE(0xFFFFA500), YELLOW(android.graphics.Color.YELLOW), WHITE(android.graphics.Color.WHITE);

    public int hex;

    Color(int hexValue) {
      this.hex = hexValue;
    }

    public static ArrayList<Color> getArrayListOfAvailableColors() {
      ArrayList<Color> arrayList = new ArrayList<Color>();
      for (Color clr : Color.values()) {
        if (clr != WHITE) {
          arrayList.add(clr);
        }
      }
      return arrayList;
    }
  }

  /**
   * Interface to bubble up play back state
   */
  public interface CanvasPlaybackListener {
    public void onPlayBackFinished();
  }

  /**
   * Required to know when to hide UI controls
   */
  public interface CanvasTouchListener {
    public void onTouch();
  }

  public void setListener(CanvasTouchListener listener) {
    this.touchListener = listener;
  }

  public CanvasView(Context context) {
    super(context);
    init();
  }

  public CanvasView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public CanvasView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  private void init() {
    setBackgroundColor(android.graphics.Color.WHITE);
    setFocusable(true);
    setFocusableInTouchMode(true);
    setOnTouchListener(this);
    currentColor = Color.BLACK;
    currentSize = Constants.Size.DEFAULT_STROKE_WIDTH;
  }

  private void setBackgroundV16Plus(View view, Bitmap bitmap) {
    view.setBackground(new BitmapDrawable(getResources(), bitmap));
  }

  private void setBackgroundV16Minus(View view, Bitmap bitmap) {
    view.setBackgroundDrawable(new BitmapDrawable(bitmap));
  }

  public void setBitmapBackground(Bitmap bitmap) {
    if (bitmap != null) {
      Matrix m = new Matrix();
      m.setRectToRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()),
          new RectF(0, 0, this.getWidth(), this.getHeight()), Matrix.ScaleToFit.CENTER);
      bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
      if (android.os.Build.VERSION.SDK_INT >= 16) {
        setBackgroundV16Plus(this, bitmap);
      } else {
        setBackgroundV16Minus(this, bitmap);
      }
    }
  }

  /**
   * Method for user to rotate the background image clockwise
   */
  public void rotateClockwise() {
    strokesHolder.clear();
    strokesHolder.addAll(strokes);
    strokes.clear();
    invalidate();
    Bitmap bitmap = ImageUtils.getBitmapFromView(this);
    bitmap = ImageUtils.rotateImage(bitmap, 90);
    setBitmapBackground(bitmap);
    strokes.addAll(strokesHolder);
    strokesHolder.clear();
    invalidate();
  }

  /**
   * Return a new Patin object based on state
   */
  private Paint getNewPaint() {
    Paint paint = new Paint();
    paint.setStyle(Paint.Style.STROKE);

    if (inEraseMode) {
      paint.setColor(Color.WHITE.hex);
    } else {
      paint.setColor(currentColor.hex);
    }

    paint.setStrokeWidth(currentSize);
    paint.setStrokeCap(Paint.Cap.ROUND);

    return paint;
  }

  @Override
  public boolean onTouch(View v, MotionEvent event) {
    return false;
  }

  public ArrayList<Color> getAvailableColors() {
    return Color.getArrayListOfAvailableColors();
  }

  /**
   * Handles Undo, Redo methods
   */
  public void goHistory(History direction) {
    if (!inPlayBackMode) {
      if (direction == History.FORWARD) {
        //Redo
        if (undoneStrokes.size() > 0) {
          strokes.push(undoneStrokes.pop());
          invalidate();
        }
      } else {
        //Undo
        if (strokes.size() > 0) {
          undoneStrokes.push(strokes.pop());
          invalidate();
        }
      }
    }
  }

  public void setColor(Color newColor) {
    this.currentColor = newColor;
  }

  public void setStrokeWidth(float newStroke) {
    this.currentSize = newStroke;
  }

  public float getCurrentWidth() {
    return this.currentSize;
  }

  /**
   * Yes this is NOT a real eraser, a different approach would need to be implemented
   */
  public void setEraserMode() {

    inEraseMode = true;
  }

  public void setPenMode() {

    inEraseMode = false;
  }

  // override onSizeChanged
  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    canvas = new Canvas(bitmap);
  }

  // override onDraw
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (strokes != null) {
      for (Stroke stroke : strokes) {
        if (stroke != null) {
          Path path = stroke.getPath();
          Paint painter = stroke.getPaint();
          if ((path != null) && (painter != null)) {
            canvas.drawPath(path, painter);
          }
        }
      }
    }
  }

  // when ACTION_DOWN start touch according to the x,y values
  private void startTouch(int x, int y) {
    //create a paint with random color
    Paint paint = getNewPaint();

    //create the Stroke
    Point pt = new Point(x, y);
    Stroke stroke = new Stroke(paint);
    stroke.addPoint(pt);
    strokes.push(stroke);
    currentX = x;
    currentY = y;
  }

  private void pointMove(float x, float y) {
    //clear the undone paths since we're starting a new sequence
    undoneStrokes.clear();
    //retrieve the stroke and add new point to its path
    float dx = Math.abs(x - currentX);
    float dy = Math.abs(y - currentY);
    if (dx >= Constants.Misc.STROKE_TOLERANCE || dy >= Constants.Misc.STROKE_TOLERANCE) {
      Stroke stroke = strokes.peek();
      if (stroke != null) {
        stroke.getPath().quadTo(currentX, currentY, (x + currentX) / 2, (y + currentY) / 2);

        currentX = x;
        currentY = y;
      }
    }
  }

  private void touchUp() {
    Stroke stroke = strokes.peek();
    if (stroke != null) {
      canvas.drawPath(stroke.getPath(), stroke.getPaint());
    }
  }

  public void clearCanvas() {
    strokes.clear();
    undoneStrokes.clear();
    setBackgroundColor(android.graphics.Color.WHITE);
    invalidate();
  }

  /**
   * This playback isn't a true playback in that it only
   * shows the paths but not the points being drawn.
   * Could rewire this to output each point of the path.
   */
  public void playBack(CanvasPlaybackListener listener) {
    if (!inPlayBackMode) {
      inPlayBackMode = true;
      this.playbackListener = listener;

      //Transfer strokes to temporary holder
      strokesHolder.addAll(strokes);
      strokes.clear();
      invalidate();

      drawOutStrokesOverTime();
    }
  }

  private void drawOutStrokesOverTime() {
    if (strokesHolder.size() > 0) {
      strokes.push(strokesHolder.get(0));
      strokesHolder.remove(0);
      invalidate();
      handler.postDelayed(new Runnable() {
        @Override
        public void run() {
          drawOutStrokesOverTime();
        }
      }, Constants.Durations.MEDIUM_DURATION);
    } else {
      this.playbackListener.onPlayBackFinished();
      inPlayBackMode = false;
    }
  }

  //override the onTouchEvent
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    float x = event.getX();
    float y = event.getY();

    if (!inPlayBackMode) {
      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          startTouch((int) x, (int) y);
          if (touchListener != null) {
            touchListener.onTouch();
          }
          break;
        case MotionEvent.ACTION_MOVE:
          pointMove(x, y);

          break;
        case MotionEvent.ACTION_UP:
          touchUp();
          break;
      }
      invalidate();
      return true;
    } else {
      return false;
    }
  }

  public Bitmap getBitmap() {
    return ImageUtils.getBitmapFromView(this);
  }

  /**
   * Simple container class to manage strokes over time
   */
  public class Stroke {
    private Path _path;
    private Paint _paint;

    Stroke(Paint paint) {
      _paint = paint;
    }

    Path getPath() {
      return _path;
    }

    Paint getPaint() {
      return _paint;
    }

    void addPoint(Point pt) {
      if (_path == null) {
        _path = new Path();
        _path.moveTo(pt.x, pt.y);
      } else {
        _path.lineTo(pt.x, pt.y);
      }
    }
  }
}
