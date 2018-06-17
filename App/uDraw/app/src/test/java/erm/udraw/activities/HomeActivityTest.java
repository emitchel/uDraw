package erm.udraw.activities;

import erm.udraw.BuildConfig;
import erm.udraw.R;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.TestCase.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class HomeActivityTest {
  private HomeActivity activity;

  @Before
  public void setUp() throws Exception {
    activity = Robolectric.buildActivity(HomeActivity.class)
        .create()
        .resume()
        .get();
  }

  @Test
  public void activityShouldExist() throws Exception {
    assertNotNull(activity);
  }

  @Test
  public void canvasFragmentShouldExist() throws Exception {
    assertNotNull(activity.getSupportFragmentManager().findFragmentById(R.id.canvas_fragment));
  }
}
