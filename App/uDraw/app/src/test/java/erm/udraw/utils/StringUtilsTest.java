package erm.udraw.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringUtilsTest {
  @Test
  public void isValidString_isCorrect() {
    assertEquals(true, StringUtils.isValidString("valid string"));
  }

  @Test
  public void isValidString_isIncorrect() {
    assertEquals(false, StringUtils.isValidString("null"));
    assertEquals(false, StringUtils.isValidString(""));
    assertEquals(false, StringUtils.isValidString(" "));
    assertEquals(false, StringUtils.isValidString(null));
  }
}
