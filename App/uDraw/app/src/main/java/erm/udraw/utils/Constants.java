package erm.udraw.utils;

/**
 * Created by ellio on 3/6/2016.
 */
public class Constants {
  public class Durations {
    public final static int SHORT_DURATION = 250;
    public final static int MEDIUM_DURATION = 650;
    public final static int LONG_DURATION = 1000;
    public final static int SUPER_LONG_DURATION = 2000;
  }

  public class IntentActions {
    public static final int TAKE_PHOTO = 0;
    public static final int CHOOSE_EXISTING = 1;
    public static final int NEW_PHOTO = 2;
    public static final int SHARE_PHOTO = 3;
    public static final int SAVE_PHOTO = 4;
  }

  public class Misc {
    public static final String PNG = ".png";
    public static final String JPG = ".jpg";
    public static final String U_DRAW = "uDraw";
    public static final String IMAGE_TYPE = "image/*";
    public static final String GALLERY_3D = "gallery3d";
    public static final float STROKE_TOLERANCE = 1f;
  }

  public class Size {
    public static final int SPACE_TO_COVER_RADIUS = 6;
    public static final float DEFAULT_STROKE_WIDTH = 12f;
  }
}
