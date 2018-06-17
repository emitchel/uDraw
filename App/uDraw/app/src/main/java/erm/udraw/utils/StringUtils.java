package erm.udraw.utils;

public class StringUtils {
  public static boolean isValidString(String candidate) {
    if (candidate == null) return false;

    candidate = candidate.trim().toLowerCase();
    return !candidate.equals("") && !candidate.equals("null") && candidate.length() != 0;
  }
}
