package erm.udraw.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import erm.udraw.objects.Constants;
import erm.udraw.objects.Utils;

/**
 * Created by ellio on 3/7/2016.
 */
public class CanvasView extends View implements View.OnTouchListener {
    private static float DEFAULT_STROKE_WIDTH = 12f;
    private static final float TOLERANCE = 5;

    final Handler handler = new Handler();

    private Bitmap mBitmap;
    Canvas mCanvas;
    Color mCurrentColor;
    float mCurrentSize;
    boolean mEraseMode;
    boolean mPlaybackMode;

    Stack<Stroke> mStrokes = new Stack<Stroke>();
    Stack<Stroke> mStrokesHolder = new Stack<Stroke>();
    Stack<Stroke> mUndoneStrokes = new Stack<Stroke>();
    Map mHistory = new HashMap();

    private float mX, mY;

    CanvasTouchListener mListener;

    CanvasPlayBackListener mPlayBackListener;

    public interface CanvasPlayBackListener {
        public void onPlayBackFinished();
    }

    public interface CanvasTouchListener {
        public void onTouch();
    }

    public void setListener(CanvasTouchListener listener) {
        this.mListener = listener;
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
        this.setOnTouchListener(this);
        mCurrentColor = Color.BLACK;
        mCurrentSize = DEFAULT_STROKE_WIDTH;

    }

    private Paint getNewPaint() {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);

        if(mEraseMode)
            paint.setColor(Color.WHITE.hex);
        else
            paint.setColor(mCurrentColor.hex);

        paint.setStrokeWidth(mCurrentSize);
        paint.setStrokeCap(Paint.Cap.ROUND);

        return paint;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

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
                if (clr != WHITE)
                    arrayList.add(clr);
            }
            return arrayList;
        }
    }

    public ArrayList<Color> getAvailableColors() {
        return Color.getArrayListOfAvailableColors();
    }

    public void goHistory(History direction) {
        if (!mPlaybackMode) {
            if (direction == History.FORWARD) {
                //Redo
                if (mUndoneStrokes.size() > 0) {
                    mStrokes.push(mUndoneStrokes.pop());
                    invalidate();
                }
            } else {
                //Undo
                if (mStrokes.size() > 0) {
                    mUndoneStrokes.push(mStrokes.pop());
                    invalidate();
                }

            }
        }
    }


    public void setColor(Color newColor) {
        if (!mEraseMode) {
            this.mCurrentColor = newColor;
        }
    }

    public void setStrokeWidth(float newStroke) {
        this.mCurrentSize = newStroke;
    }

    public float getCurrentWidth() {
        return this.mCurrentSize;
    }


    /**
     * Yes this is NOT a real eraser, a different approach would need to be implemented
     */
    public void setEraserMode() {

        mEraseMode = true;

    }

    public void setPenMode() {

        mEraseMode = false;
    }


    // override onSizeChanged
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    // override onDraw
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mStrokes != null) {
            for (Stroke stroke : mStrokes) {
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
        mStrokes.push(stroke);
        mX = x;
        mY = y;
    }

    private void pointMove(float x, float y) {
        //retrieve the stroke and add new point to its path
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOLERANCE || dy >= TOLERANCE) {
            Stroke stroke = mStrokes.peek();
            if (stroke != null) {
                stroke.getPath().quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);

                mX = x;
                mY = y;
            }
        }

    }

    public void clearCanvas() {
        mStrokes.clear();
        mUndoneStrokes.clear();
        invalidate();
    }


    /**
     * This playback isn't a true playback in that it only
     * shows the paths but not the points being drawn.
     * Could rewire this to output each point of the path.
     *
     * @param listener
     */
    public void playBack(CanvasPlayBackListener listener) {
        if (!mPlaybackMode) {
            mPlaybackMode = true;
            this.mPlayBackListener = listener;

            //Transfer strokes to temporary holder
            mStrokesHolder.addAll(mStrokes);
            mStrokes.clear();
            invalidate();

            drawOutStrokesOverTime();


        }

    }

    private void drawOutStrokesOverTime() {
        if (mStrokesHolder.size() > 0) {
            mStrokes.push(mStrokesHolder.get(0));
            mStrokesHolder.remove(0);
            invalidate();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawOutStrokesOverTime();
                }
            }, Constants.MEDIUM_DURATION);
        } else {
            this.mPlayBackListener.onPlayBackFinished();
            mPlaybackMode = false;
        }

    }

    //override the onTouchEvent
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if (!mPlaybackMode) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startTouch((int) x, (int) y);
                    if (mListener != null)
                        mListener.onTouch();
                    break;
                case MotionEvent.ACTION_MOVE:
                    pointMove(x, y);

                    break;
                case MotionEvent.ACTION_UP:

                    break;
            }
            invalidate();
            return true;
        } else {
            return false;
        }
    }

    public Bitmap getBitmap() {
        return Utils.getBitmapFromView(this);

    }


    // _Name meaning inner class attribute
    public class Stroke {
        private Path _path;
        private Paint _paint;

        public Stroke(Paint paint) {
            _paint = paint;
        }

        public Path getPath() {
            return _path;
        }

        public Paint getPaint() {
            return _paint;
        }

        public void addPoint(Point pt) {
            if (_path == null) {
                _path = new Path();
                _path.moveTo(pt.x, pt.y);
            } else {
                _path.lineTo(pt.x, pt.y);
            }
        }
    }


}
