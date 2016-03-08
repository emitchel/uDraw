package erm.udraw.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Created by ellio on 3/7/2016.
 */
public class CanvasView extends View implements View.OnTouchListener {
    private static int DEFAULT_STROKE_WIDTH = 6;

    private Bitmap mBitmap;
    Canvas mCanvas;
    Path mPath;
    Paint mPaint;
    Color mCurrentColor;

    Stack<Path> mPaths = new Stack<Path>();
    Stack<Path> mUndonePaths = new Stack<Path>();
    Map mHistory = new HashMap();

    private float mX, mY;
    private static final float TOLERANCE = 5;


    public CanvasView(Context context)
    {
        super(context);
        init();
    }
    public CanvasView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }
    public CanvasView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init(){
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setOnTouchListener(this);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        this.setDrawingCacheEnabled(true);
        mPaint.setColor(Color.BLACK.hex);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setXfermode(null);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(DEFAULT_STROKE_WIDTH);
        mCanvas = new Canvas();
        mPath = new Path();
        mPaths.add(mPath);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    /**
     * Actions describing the history of a path
     */
    public enum Action {
        ADD, REMOVE
    }

    /**
     * Colors supported
     */
    public enum Color {
        BLACK(android.graphics.Color.BLACK), DARK_GRAY(android.graphics.Color.DKGRAY),
        LIGHT_GRAY(android.graphics.Color.LTGRAY), BLUE(android.graphics.Color.BLUE),
        RED(android.graphics.Color.RED), GREEN(android.graphics.Color.GREEN),
        ORANGE(0xFFA500), YELLOW(android.graphics.Color.YELLOW);

        int hex;

        Color(int hexValue) {
            this.hex = hexValue;
        }

        public static ArrayList<Color> getArrayListOfAvailableColors() {
            ArrayList<Color> arrayList = new ArrayList<Color>();
            for (Color clr : Color.values()) {
                arrayList.add(clr);
            }
            return arrayList;
        }
    }

    public ArrayList<Color> getAvailableColors() {
        return Color.getArrayListOfAvailableColors();
    }

    public Color getCurrentColor() {
        return this.mCurrentColor;
    }

    public void setColor(Color newColor) {
        mPaint.setColor(newColor.hex);
        this.mCurrentColor = newColor;
    }

    public void setStrokeWidth(float newStroke) {
        mPaint.setStrokeWidth(newStroke);
    }

    private void addHistory(Path path, Action action) {
        mHistory.put(path, action);
    }


    private void setEraserMode() {
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }



    // override onSizeChanged
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // your Canvas will draw onto the defined Bitmap
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    // override onDraw
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // draw the mPath with the mPaint on the canvas when onDraw
        for (Path p : mPaths){
            canvas.drawPath(p, mPaint);
        }
        canvas.drawPath(mPath, mPaint);
    }

    // when ACTION_DOWN start touch according to the x,y values
    private void startTouch(float x, float y) {
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    // when ACTION_MOVE move touch according to the x,y values
    private void moveTouch(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOLERANCE || dy >= TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    public void clearCanvas() {
        mPaths.clear();
        mUndonePaths.clear();
        mPath.reset();
        invalidate();
    }

    // when ACTION_UP stop touch
    private void upTouch() {
        mPath.lineTo(mX, mY);
    }

    //override the onTouchEvent
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                moveTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                upTouch();
                invalidate();
                break;
        }
        return true;
    }

    public Bitmap getBitmap(){
        return this.getDrawingCache();
    }


}
