package erm.udraw.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.view.View;

public class ImageUtils {

  public static Bitmap getBitmapFromView(View view) {
    //Define a bitmap with the same size as the view
    Bitmap returnedBitmap =
        Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
    //Bind a canvas to it
    Canvas canvas = new Canvas(returnedBitmap);
    //Get the view's background
    Drawable bgDrawable = view.getBackground();
    if (bgDrawable != null) {
      //has background drawable, then draw it on the canvas
      bgDrawable.draw(canvas);
    } else {
      //does not have background drawable, then draw white background on the canvas
      canvas.drawColor(android.graphics.Color.WHITE);
    }
    // draw the view on the canvas
    view.draw(canvas);
    //return the bitmap
    return returnedBitmap;
  }

  public static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
    int width = image.getWidth();
    int height = image.getHeight();
    float ratioBitmap = (float) width / (float) height;
    float ratioMax = (float) maxWidth / (float) maxHeight;

    int finalWidth = maxWidth;
    int finalHeight = maxHeight;
    if (ratioMax > 1) {
      finalWidth = (int) ((float) maxHeight * ratioBitmap);
    } else {
      finalHeight = (int) ((float) maxWidth / ratioBitmap);
    }
    image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
    return image;
  }

  public static Bitmap rotateImage(Bitmap source, float angle) {
    Bitmap retVal;

    Matrix matrix = new Matrix();
    matrix.postRotate(angle);
    retVal = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

    return retVal;
  }

}
