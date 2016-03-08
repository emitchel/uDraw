package erm.udraw.objects;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ellio on 3/6/2016.
 *
 * Generic, miscellaneous file contains common methods
 */
public class Utils {

    public static boolean isValidString(String s){
        s=s.trim().toLowerCase();
        return s!=null && !s.equals("") && !s.equals("null") && s.length()!=0;
    }

    public static String getCurrentDateTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateandTime = sdf.format(new Date());
        return currentDateandTime;
    }




}
