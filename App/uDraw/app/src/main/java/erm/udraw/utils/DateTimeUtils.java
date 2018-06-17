package erm.udraw.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtils {

  public static String getCurrentDateTime() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
    String currentDateandTime = sdf.format(new Date());
    return currentDateandTime;
  }
}
